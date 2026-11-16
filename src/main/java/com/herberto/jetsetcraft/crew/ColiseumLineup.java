package com.herberto.jetsetcraft.crew;

import com.herberto.jetsetcraft.rush.RushAlbum;
import net.minecraft.server.level.ServerPlayer;
import java.util.*;

/** Read-only preview admission, never a battle permit, actor factory or reward source. */
public final class ColiseumLineup {
    public enum Reason { PREVIEW_ELIGIBLE, NO_CREW, RECOVERY_REQUIRED, WRONG_OWNER, STALE,
        INVALID_TEAM_SIZE, DUPLICATE_MEMBER, UNKNOWN_MEMBER, BUSY_MEMBER, MIXED_GANG,
        SAME_GANG, DEFEAT_NOT_RECORDED }
    public record Fighter(UUID memberId, String name, String species, String gang, int level) {}
    public record Preview(Reason reason, UUID crewId, long revision, List<Fighter> left, List<Fighter> right) {
        public Preview { left=List.copyOf(left); right=List.copyOf(right); }
        public boolean eligibleForPreview(){return reason==Reason.PREVIEW_ELIGIBLE;}
    }
    /** Production entry: all roster and defeat inputs come from the requesting server player. */
    public static Preview preview(ServerPlayer player,long expectedRevision,List<UUID> left,List<UUID> right){
        if(player==null)return rejected(Reason.NO_CREW);
        CrewHouseData data=CrewHouseData.get(player.getServer());
        if(data.blocked(player.getUUID()))return rejected(Reason.RECOVERY_REQUIRED);
        // Roster is bounded at12. Read only the relevant actor/gang defeat keys.
        CrewProfile crew=data.profile(player.getUUID());
        List<RushAlbum.Entry> earned=new ArrayList<>();
        if(crew!=null)for(CrewProfile.Member member:crew.members.values())
            if(RushAlbum.hasDefeated(player,member.species,member.gang))
                earned.add(new RushAlbum.Entry(member.species,member.gang,1,1,0));
        return evaluate(crew,player.getUUID(),expectedRevision,left,right,earned);
    }
    /** Pure policy seam for deterministic tests. Result is a preview, not authority to spawn. */
    public static Preview evaluate(CrewProfile crew,UUID owner,long revision,List<UUID> left,List<UUID> right,
                                   List<RushAlbum.Entry> defeats){
        if(crew==null)return rejected(Reason.NO_CREW);
        if(owner==null||!owner.equals(crew.owner))return rejected(Reason.WRONG_OWNER);
        if(revision<0||revision!=crew.revision)return rejected(Reason.STALE);
        if(left==null||right==null||left.isEmpty()||right.isEmpty()||left.size()>6||right.size()>6)
            return rejected(Reason.INVALID_TEAM_SIZE);
        Set<UUID> ids=new HashSet<>();
        List<Fighter> a=new ArrayList<>(),b=new ArrayList<>();
        Set<String> earned=new HashSet<>();
        if(defeats!=null)for(RushAlbum.Entry d:defeats)if(d!=null&&d.count()>0)earned.add(d.entityId()+"|"+d.gangId());
        for(int side=0;side<2;side++){
            List<Fighter> out=side==0?a:b;String gang=null;
            for(UUID id:side==0?left:right){
                if(id==null||!ids.add(id))return rejected(Reason.DUPLICATE_MEMBER);
                CrewProfile.Member m=crew.members.get(id);
                if(m==null)return rejected(Reason.UNKNOWN_MEMBER);
                if(crew.busy(id))return rejected(Reason.BUSY_MEMBER);
                if(gang!=null&&!gang.equals(m.gang))return rejected(Reason.MIXED_GANG);
                gang=m.gang;
                if(!earned.contains(m.species+"|"+m.gang))return rejected(Reason.DEFEAT_NOT_RECORDED);
                out.add(new Fighter(m.id,m.name,m.species,m.gang,m.level()));
            }
        }
        if(a.get(0).gang.equals(b.get(0).gang))return rejected(Reason.SAME_GANG);
        return new Preview(Reason.PREVIEW_ELIGIBLE,crew.id,crew.revision,a,b);
    }
    private static Preview rejected(Reason reason){return new Preview(reason,null,-1,List.of(),List.of());}
    private ColiseumLineup(){}
}
