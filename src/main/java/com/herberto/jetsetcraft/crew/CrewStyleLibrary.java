package com.herberto.jetsetcraft.crew;

import com.herberto.jetsetcraft.compat.RushSpellCompat;
import com.herberto.jetsetcraft.liberty.LibertyResearch;
import com.herberto.jetsetcraft.rush.RushRun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Permanent build choices unlocked by existing shared crew XP; never a second currency or a draft gate. */
public final class CrewStyleLibrary {
    public record Lesson(RushRun.Upgrade upgrade, int requiredXp) {}
    public static final String HOME = "HOME";
    public static final List<Lesson> LESSONS = List.of(
            new Lesson(RushRun.Upgrade.BATTERY, 55), new Lesson(RushRun.Upgrade.AIR_BREAK, 110),
            new Lesson(RushRun.Upgrade.CHAIN, 180), new Lesson(RushRun.Upgrade.WARD, 260),
            new Lesson(RushRun.Upgrade.RADIANCE, 350), new Lesson(RushRun.Upgrade.FIREWORK, 450),
            new Lesson(RushRun.Upgrade.VENOM, 560), new Lesson(RushRun.Upgrade.CHILL, 680),
            new Lesson(RushRun.Upgrade.MULTISHOT, 810), new Lesson(RushRun.Upgrade.WIND, 950),
            new Lesson(RushRun.Upgrade.SHOCKWAVE, 1100), new Lesson(RushRun.Upgrade.TEMPO, 1260),
            new Lesson(RushRun.Upgrade.SOUL, 1430), new Lesson(RushRun.Upgrade.SOUL_BURST, 1610),
            new Lesson(RushRun.Upgrade.EXPOSE, 1800), new Lesson(RushRun.Upgrade.SPELL_RELAY, 2000));

    public static boolean known(String id) {
        return HOME.equals(id) || LESSONS.stream().anyMatch(lesson -> lesson.upgrade().name().equals(id));
    }

    private static boolean providerAvailable(Lesson lesson) {
        return lesson.upgrade() != RushRun.Upgrade.SPELL_RELAY || RushSpellCompat.fireboltAvailable();
    }

    public static boolean canSelect(CrewHouseData data, UUID owner, String id) {
        if (data.blocked(owner)) return false;
        if (HOME.equals(id)) return true;
        return LESSONS.stream().anyMatch(lesson -> lesson.upgrade().name().equals(id)
                && data.styleXp(owner) >= lesson.requiredXp() && providerAvailable(lesson));
    }

    public static Optional<RushRun.Upgrade> preparation(CrewHouseData data, UUID owner) {
        String chosen = data.selectedStyle(owner);
        if (HOME.equals(chosen) || !canSelect(data, owner, chosen)) return Optional.empty();
        return LESSONS.stream().filter(lesson -> lesson.upgrade().name().equals(chosen)).map(Lesson::upgrade).findFirst();
    }

    /** A researched preparation replaces the learned style in the same single starting-rank slot. */
    public static Optional<RushRun.Upgrade> preparation(CrewHouseData data, ServerPlayer player) {
        if (player == null || data.blocked(player.getUUID())) return Optional.empty();
        Optional<RushRun.Upgrade> research = LibertyResearch.preparation(player);
        return research.isPresent() ? research : preparation(data, player.getUUID());
    }

    public static CompoundTag snapshot(CrewHouseData data, UUID owner, boolean canManage) {
        CompoundTag out = new CompoundTag();
        long xp = data.styleXp(owner);
        out.putLong("Xp", xp); out.putLong("Revision", data.styleRevision(owner));
        out.putString("Selected", data.selectedStyle(owner));
        out.putBoolean("CanManage", canManage && !data.blocked(owner));
        out.putInt("Total", LESSONS.size());
        out.putInt("Unlocked", (int) LESSONS.stream().filter(lesson -> xp >= lesson.requiredXp()).count());
        ListTag lessons = new ListTag();
        for (Lesson lesson : LESSONS) {
            CompoundTag row = new CompoundTag();
            row.putString("Id", lesson.upgrade().name()); row.putString("Name", lesson.upgrade().label());
            row.putString("Description", lesson.upgrade().description()); row.putInt("RequiredXp", lesson.requiredXp());
            row.putLong("RemainingXp", Math.max(0, lesson.requiredXp() - xp));
            row.putBoolean("Unlocked", xp >= lesson.requiredXp());
            row.putBoolean("Available", providerAvailable(lesson));
            row.putBoolean("Selected", lesson.upgrade().name().equals(data.selectedStyle(owner)));
            lessons.add(row);
        }
        out.put("Lessons", lessons);
        return out;
    }

    private CrewStyleLibrary() {}
}
