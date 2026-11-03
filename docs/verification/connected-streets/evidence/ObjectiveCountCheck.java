import com.herberto.jetsetcraft.liberty.*;
import java.util.*;
public class ObjectiveCountCheck {
 public static void main(String[] args) {
  var templates=LibertyMissionCatalog.templates();
  int optional=LibertyDifficulty.of(10).optionalCount()+LibertyDifficulty.of(10).outpostCount();
  for(int limit:new int[]{28,34}) {
   int max=0; String name="";
   for(var t:templates.subList(0,limit)) { int primary=t.stages().stream().flatMap(s->s.goals().stream()).mapToInt(g->g.sites()).sum(); if(primary>max){max=primary;name=t.id();} }
   System.out.println((limit==28?"BASELINE_PREFIX":"CURRENT")+" templates="+limit+" max_primary="+max+" max_optional="+optional+" max_total="+(max+optional)+" example="+name);
  }
  for(var t:templates.subList(28,34)) { int primary=t.stages().stream().flatMap(s->s.goals().stream()).mapToInt(g->g.sites()).sum(); System.out.println("NEW "+t.id()+" primary="+primary+" maximum_total="+(primary+optional)+" normalized_crew_completion="+Math.min(3,primary*3/Math.max(1,primary))); }
 }
}
