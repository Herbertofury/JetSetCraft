import com.herberto.jetsetcraft.activity.ActivityMode;
import com.herberto.jetsetcraft.activity.ActivityRun;
/** Executes the unmodified production clock classes. No Minecraft/Forge classes are stubbed. */
public final class StreetRoundSmoke {
  static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
  public static void main(String[] args) {
    ActivityRun tag = new ActivityRun(ActivityMode.TAG);
    for(int i=0;i<5;i++) { tag.tick(); check(tag.tagged() && !tag.won(), "early Tag win"); }
    tag.tick(); check(tag.tagged() && tag.won() && tag.progress()==6, "six real Tag objectives");
    int endedTicks=tag.ticks(); tag.tick(); check(!tag.tagged() && tag.ticks()==endedTicks, "terminal Tag score/tick mutation");
    ActivityRun showdown=new ActivityRun(ActivityMode.SHOWDOWN);
    for(int i=0;i<3;i++) check(showdown.tagged() && !showdown.won(), "early Showdown win");
    check(showdown.tagged() && showdown.won() && showdown.progress()==4, "four Showdown objectives");
    for(boolean expanded:new boolean[]{false,true}) {
      ActivityRun hill=expanded?new ActivityRun(ActivityMode.KING_OF_HILL,5,120,3600):new ActivityRun(ActivityMode.KING_OF_HILL);
      int duration=expanded?120:160, captures=expanded?5:3;
      check(hill.remaining()==(expanded?3600:2400), "round time budget changed");
      for(int site=0;site<captures;site++) {
        for(int tick=0;tick<duration;tick++) {
          check(!hill.holdHill(false,false) && !hill.holdHill(true,true), "absence or contention awarded capture");
          hill.tick(); check(hill.holdHill(true,false), "accepted uncontested time lost");
          check(hill.objectives()==site+(tick==duration-1?1:0), "capture threshold incorrect");
        }
      }
      check(hill.won() && hill.progress()==captures*duration && hill.remaining()>0, "hill did not finish exact objective");
      check(!hill.holdHill(true,false), "finished hill still scored");
    }
    for(ActivityMode mode:new ActivityMode[]{ActivityMode.TAG,ActivityMode.KING_OF_HILL,ActivityMode.SHOWDOWN}) {
      ActivityRun timeout=new ActivityRun(mode);
      for(int i=0;i<mode.durationTicks;i++) timeout.tick();
      check(timeout.ended() && !timeout.won() && timeout.remaining()==0 && !timeout.tagged() && !timeout.holdHill(true,false), "late objective beat deadline");
      ActivityRun stopped=new ActivityRun(mode);
      if(mode==ActivityMode.KING_OF_HILL) stopped.holdHill(true,false); else stopped.tagged();
      int progress=stopped.progress(); stopped.cancel();
      check(!stopped.won() && !stopped.tagged() && !stopped.holdHill(true,false) && stopped.progress()==progress, "cancel awarded/replayed progress");
    }
    int rejected=0;
    for(int[] spec:new int[][]{{0,120,3600},{13,120,3600},{5,99,3600},{5,241,3600},{5,120,1199},{5,120,6001}}) {
      try { new ActivityRun(ActivityMode.KING_OF_HILL,spec[0],spec[1],spec[2]); } catch(IllegalArgumentException expected) { rejected++; }
    }
    try { new ActivityRun(ActivityMode.RIVAL_RUSH); } catch(IllegalArgumentException expected) { rejected++; }
    try { new ActivityRun(null); } catch(IllegalArgumentException expected) { rejected++; }
    check(rejected==8, "an unsupported mode or objective bound was admitted");
    System.out.println("STREET_ROUND_SOURCE_SMOKE_PASS: actual ActivityMode/ActivityRun compiled; quick Tag/Showdown wins and 3x160-tick hills; expanded 5x120-tick hills; absent/contested holds; all quick timeouts and cancellation; terminal-state immutability; eight invalid admission bounds. No Minecraft/Forge runtime or gameplay claim.");
  }
}
