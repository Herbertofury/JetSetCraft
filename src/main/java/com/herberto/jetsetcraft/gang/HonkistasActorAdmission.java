package com.herberto.jetsetcraft.gang;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Trusted server provider boundary, not an automatic event director or character renderer.
 * No production provider is installed until its real actors/assets are accepted.
 */
public final class HonkistasActorAdmission {
    public enum State { ADMITTED, NO_PROVIDER, STALE_SOURCE, INVALID_ACTORS, CHECKPOINT_FAILED, WORLD_REJECTED }
    public record Result(State state, UUID event, List<Mob> actors) {
        public Result { actors=List.copyOf(actors); }
    }

    /** Provider returns NEW, unregistered, correctly authored actors in the owner's loaded region.
     * Caller owns encounter scoring/lifecycle; no packet accepts factories or entity IDs.
     * Test fixtures may use ordinary native mobs but do not establish character acceptance.
     */
    public static Result rival(ServerPlayer owner, UUID source, Supplier<List<Mob>> provider) {
        if (owner==null || owner.getServer()==null || !owner.getServer().isSameThread() || provider==null)
            return result(State.NO_PROVIDER);
        HonkistasEncounterData data=HonkistasEncounterData.get(owner.getServer());
        var entry=data.entry(owner.getUUID());
        if(entry==null || entry.stage()!=HonkistasEncounterData.Stage.SOURCE_RECORDED || !entry.sourceEvent().equals(source))
            return result(State.STALE_SOURCE);
        List<Mob> actors;
        try { actors=provider.get(); } catch(RuntimeException failure) { return result(State.INVALID_ACTORS); }
        if(actors==null || actors.isEmpty() || actors.size()>16) return result(State.INVALID_ACTORS);
        HashSet<UUID> identities=new HashSet<>();
        for(Mob mob:actors) {
            if(mob==null || mob.isRemoved() || !mob.isAlive() || mob.level()!=owner.serverLevel()
                    || owner.serverLevel().getEntity(mob.getUUID())!=null || !identities.add(mob.getUUID())
                    || mob.distanceToSqr(owner)>64*64
                    || !owner.serverLevel().isPositionEntityTicking(mob.blockPosition())
                    || !owner.serverLevel().getWorldBorder().isWithinBounds(mob.getBoundingBox())
                    || !owner.serverLevel().noCollision(mob)) return result(State.INVALID_ACTORS);
        }
        for(int i=0;i<actors.size();i++) for(int j=i+1;j<actors.size();j++)
            if(actors.get(i).getBoundingBox().intersects(actors.get(j).getBoundingBox()))
                return result(State.INVALID_ACTORS);
        actors=List.copyOf(actors);
        UUID event=UUID.randomUUID();
        if(!data.rivalAdmitted(owner.getUUID(),source,event)) return result(State.STALE_SOURCE);
        // The reserved event is durable before insertion. Any failure is fail-closed, never a reroll.
        if(!HonkistasEncounterData.saveAcknowledged(owner.getServer())) return new Result(State.CHECKPOINT_FAILED,event,List.of());
        java.util.ArrayList<Mob> inserted=new java.util.ArrayList<>();
        try {
            for(Mob mob:actors) {
                if(!owner.serverLevel().addFreshEntity(mob) || owner.serverLevel().getEntity(mob.getUUID())!=mob)
                    throw new IllegalStateException("Native actor admission rejected");
                inserted.add(mob);
            }
            return new Result(State.ADMITTED,event,inserted);
        } catch(RuntimeException failure) {
            for(Mob mob:inserted) mob.discard();
            // Includes a just-added but not observable body, not any pre-existing world entity.
            for(Mob mob:actors) mob.discard();
            data.close(owner.getUUID(),source);HonkistasEncounterData.saveAcknowledged(owner.getServer());
            return new Result(State.WORLD_REJECTED,event,List.of());
        }
    }
    private static Result result(State state){return new Result(state,null,List.of());}
    private HonkistasActorAdmission(){}
}
