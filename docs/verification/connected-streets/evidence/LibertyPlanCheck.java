import com.herberto.jetsetcraft.liberty.LibertyDifficulty;
import com.herberto.jetsetcraft.liberty.LibertyMissionCatalog;
import com.herberto.jetsetcraft.liberty.LibertyMissionPlan;
import java.util.*;

/** Executes current dependency-free production planning classes; this is not a Forge runtime test. */
public final class LibertyPlanCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static int coveredCells(LibertyMissionPlan.Point paint, double radius, LibertyMissionPlan.Objective site) {
        int covered = 0, cellSize = 4, limit = (int)Math.ceil(site.radius() / 4.0);
        for (int x=-limit; x<=limit; x++) for (int z=-limit; z<=limit; z++) {
            double localX=x*cellSize+cellSize*.5, localZ=z*cellSize+cellSize*.5;
            double worldX=site.center().x()+localX-(paint.x()+.5), worldZ=site.center().z()+localZ-(paint.z()+.5);
            if (localX*localX+localZ*localZ<=site.radius()*site.radius() && worldX*worldX+worldZ*worldZ<=radius*radius) covered++;
        }
        return covered;
    }
    public static void main(String[] args) {
        long started=System.nanoTime();
        int plans=0, eligible=0, maxObjectives=0;
        Set<LibertyMissionPlan.Point> firstSites=new HashSet<>();
        for (var faction:LibertyMissionCatalog.factions()) for (var template:LibertyMissionCatalog.templates(faction)) {
            eligible++;
            for (int difficulty=1; difficulty<=10; difficulty++) for(int minutes:new int[]{20,25,30})
                for(long seed:new long[]{0,19,-991}) {
                    var plan=LibertyMissionPlan.generate(seed,"neon_frontier",faction,template.id(),difficulty,minutes);
                    String label=template.id()+"/"+faction.id()+"/"+difficulty+"/"+minutes+"/"+seed;
                    check(plan.equals(LibertyMissionPlan.generate(seed,"neon_frontier",faction,template.id(),difficulty,minutes)),"Nondeterministic "+label);
                    check(plan.stages().size()==3 && plan.totalTicks()==minutes*1200 && plan.arenaRadius()>=512 && plan.arenaRadius()<=768
                            && plan.difficulty().residentCap()<=96 && plan.difficulty().maxSpawnsPerTick()==8,"Unbounded plan "+label);
                    int previous=-1;
                    for(long time:new long[]{-100,0,1,1200,6000,minutes*1200L,Long.MAX_VALUE}) {
                        int allowance=plan.difficulty().allowanceAt(time,minutes);
                        check(allowance>=previous && allowance<=plan.difficulty().budgetForMinutes(minutes),"Bad reinforcement budget "+label);
                        previous=allowance;
                    }
                    var objectives=plan.allObjectives(); Set<String> ids=new HashSet<>(); maxObjectives=Math.max(maxObjectives,objectives.size());
                    for(int i=0;i<objectives.size();i++) {
                        var objective=objectives.get(i);
                        check(ids.add(objective.id()) && Math.abs(objective.center().x())+objective.radius()<plan.arenaRadius()
                                && Math.abs(objective.center().z())+objective.radius()<plan.arenaRadius(),"Bad objective bound/id "+label);
                        check(objective.center().distanceSquared(plan.drop())>=128*128 && objective.center().distanceSquared(plan.extraction())>=128*128,
                                "Landing blocked "+label);
                        for(int j=0;j<i;j++) check(objective.center().distanceSquared(objectives.get(j).center())>=96*96,"Site spacing failed "+label);
                        for(var point:objective.checkpoints()) check(Math.abs(point.x())<plan.arenaRadius() && Math.abs(point.z())<plan.arenaRadius(),"Route escaped arena "+label);
                    }
                    firstSites.add(plan.primaryObjectives().get(0).center()); plans++;
                }
        }
        check(LibertyMissionCatalog.templates().size()==34 && plans==eligible*90 && firstSites.size()>plans*.9,"Catalogue coverage/diversity failed");
        System.out.printf(Locale.ROOT,"PASS production plan matrix templates=%d faction_template_pairs=%d plans=%d repeated_determinism_checks=%d unique_first_sites=%d max_objectives=%d%n",
                LibertyMissionCatalog.templates().size(),eligible,plans,plans,firstSites.size(),maxObjectives);
        int unsafe=0;
        for(String id:List.of("neon_block_party","last_train_home","pirate_radio_takeover","supply_line_shuffle","swarm_street_cleanup","prism_breakout")) {
            var template=LibertyMissionCatalog.template(id);
            var faction=template.factions().stream().sorted().findFirst().orElseThrow();
            var plan=LibertyMissionPlan.generate(209,"neon_docks",faction,id,4,20);
            int fixtureOverlaps=0;
            for(var required:plan.primaryObjectives()) {
                var finalMarker=required.kind()==LibertyMissionCatalog.ObjectiveKind.ESCORT
                        ? required.checkpoints().get(required.checkpoints().size()-1) : required.center();
                for(var optional:plan.optionalObjectives()) if(optional.kind()==LibertyMissionCatalog.ObjectiveKind.TERRITORY) {
                    int cells=coveredCells(finalMarker,24,optional);
                    if(cells>0) { fixtureOverlaps++; System.out.printf("OVERLAP repeated-paint id=%s primary=%s optional=%s cells=%d%n",id,required.id(),optional.id(),cells); }
                    if(required.kind()==LibertyMissionCatalog.ObjectiveKind.TERRITORY) {
                        int original=coveredCells(required.center(),required.radius()+4,optional);
                        check(original==0,"Primary territory action reaches optional mural in fixture "+id);
                    }
                }
            }
            unsafe+=fixtureOverlaps;
            System.out.printf("PASS seed209 fixture geometry operation=%s faction=%s primary=%d optional=%d repeat_paint_optional_overlaps=%d%n",id,faction.id(),plan.primaryObjectives().size(),plan.optionalObjectives().size(),fixtureOverlaps);
        }
        check(unsafe==0,"One of the exact fixture repeated-paint assertions is unsafe");
        for(int bad:new int[]{0,11}) { boolean rejected=false; try{LibertyDifficulty.of(bad);}catch(IllegalArgumentException expected){rejected=true;} check(rejected,"Invalid difficulty accepted"); }
        System.out.printf(Locale.ROOT,"PASS current-source dependency-free plan checks elapsed_ms=%.1f; Forge runtime and native mission actions were not executed%n",(System.nanoTime()-started)/1e6);
    }
}
