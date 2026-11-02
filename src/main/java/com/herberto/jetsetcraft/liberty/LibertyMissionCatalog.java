package com.herberto.jetsetcraft.liberty;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Immutable mission vocabulary. World construction and successful gameplay actions belong to the server. */
public final class LibertyMissionCatalog {
    public enum ObjectiveKind { TERRITORY, RELAY_HOLD, COURIER, DEMOLITION, SWEEP, SCAN, ESCORT, DEFEND }
    public enum BaseKind { FABRICATOR, BUG_HOLE, WARP_SHIP }
    public enum Faction {
        AUTOMATON("automaton", "Automaton Machine Legion", "jetsetcraft:bone_drones", BaseKind.FABRICATOR,
                List.of("minecraft:skeleton", "minecraft:pillager"),
                List.of("minecraft:iron_golem", "minecraft:ravager")),
        TERMINID("terminid", "Terminid Neon Swarm", "jetsetcraft:dead_beat", BaseKind.BUG_HOLE,
                List.of("minecraft:cave_spider", "minecraft:silverfish"),
                List.of("minecraft:spider")),
        ILLUMINATE("illuminate", "Illuminate Prism Choir", "jetsetcraft:hex_appeal", BaseKind.WARP_SHIP,
                List.of("minecraft:zombie", "minecraft:vindicator"),
                List.of("minecraft:witch", "minecraft:shulker"));
        private final String id, name, gang;
        private final BaseKind baseKind;
        private final List<String> infantry, elites;
        Faction(String id, String name, String gang, BaseKind baseKind, List<String> infantry, List<String> elites) {
            this.id = id; this.name = name; this.gang = gang; this.baseKind = baseKind;
            this.infantry = List.copyOf(infantry); this.elites = List.copyOf(elites);
        }
        public String id() { return id; }
        public String displayName() { return name; }
        public String gangId() { return gang; }
        public BaseKind baseKind() { return baseKind; }
        public List<String> infantry() { return infantry; }
        public List<String> elites() { return elites; }
    }
    public record GoalSpec(String id, ObjectiveKind kind, String name, String description, int sites, int target, int seconds) {
        public GoalSpec {
            if (id == null || !id.matches("[a-z0-9_]+") || kind == null || name == null || description == null
                    || sites < 1 || sites > 4 || target < 1 || seconds < 0) throw new IllegalArgumentException("Invalid mission goal");
        }
    }
    public record StageSpec(String name, List<GoalSpec> goals) {
        public StageSpec { goals = List.copyOf(goals); if (name == null || goals.isEmpty()) throw new IllegalArgumentException("Empty mission stage"); }
    }
    public record MissionTemplate(String id, String name, String description, Set<Faction> factions, List<StageSpec> stages) {
        public MissionTemplate {
            factions = Set.copyOf(factions); stages = List.copyOf(stages);
            if (id == null || !id.matches("[a-z0-9_]+") || name == null || description == null || factions.isEmpty() || stages.isEmpty())
                throw new IllegalArgumentException("Invalid mission template");
        }
        public boolean supports(Faction faction) { return factions.contains(faction); }
    }
    private static final Set<Faction> ALL = Set.of(Faction.values());
    private static GoalSpec goal(String id, ObjectiveKind kind, String name, String description, int sites, int target, int seconds) {
        return new GoalSpec(id, kind, name, description, sites, target, seconds);
    }
    private static StageSpec stage(String name, GoalSpec... goals) { return new StageSpec(name, List.of(goals)); }
    private static MissionTemplate mission(String id, String name, String description, Set<Faction> factions, StageSpec... stages) {
        return new MissionTemplate(id, name, description, factions, List.of(stages));
    }
    private static final List<MissionTemplate> TEMPLATES = List.of(
        mission("turf_liberation", "Turf Liberation", "Paint connected districts, seize their signal zones and defend the final street party.", ALL,
            stage("Claim the streets", goal("turf", ObjectiveKind.TERRITORY, "Paint the district", "Paint distinct marked turf cells; rivals can reclaim unfinished turf.", 2, 24, 0)),
            stage("Keep the airwaves", goal("zone", ObjectiveKind.RELAY_HOLD, "Hold the signal zone", "Keep skating inside the zone; nearby rivals contest the broadcast.", 2, 1, 90)),
            stage("Protect the mural", goal("stand", ObjectiveKind.DEFEND, "Defend the street party", "Hold the district against counterattacks until the defense clock completes.", 1, 1, 120))),
        mission("signal_blackout", "Signal Blackout", "Locate enemy uplinks, break their power cores and deliver the captured broadcast.", ALL,
            stage("Locate the network", goal("scan", ObjectiveKind.SCAN, "Scan the uplink", "Activate the marked terminal, then stay close while it scans.", 2, 1, 12)),
            stage("Cut the power", goal("power", ObjectiveKind.DEMOLITION, "Break the uplink cores", "Damage every marked physical core; merely standing nearby cannot sabotage it.", 1, 3, 0)),
            stage("Broadcast the truth", goal("data", ObjectiveKind.COURIER, "Run the data relay", "Skate through the lit checkpoints in order to deliver the captured data.", 1, 5, 0))),
        mission("chromatic_convoy", "Chromatic Convoy", "Paint a launch lane, escort the moving paint rig and secure its final transmission.", ALL,
            stage("Open a lane", goal("lane", ObjectiveKind.TERRITORY, "Paint the convoy lane", "Claim distinct turf cells around the launch yard.", 1, 18, 0)),
            stage("Keep the rig moving", goal("rig", ObjectiveKind.ESCORT, "Escort the paint rig", "Stay with the rig and clear nearby rivals so it advances through its route.", 1, 5, 0)),
            stage("Send the payload", goal("signal", ObjectiveKind.RELAY_HOLD, "Transmit the color burst", "Keep skating around the relay while the area is uncontested.", 1, 1, 100))),
        mission("sample_heist", "Sample Heist", "Survey research vaults, move their samples across a hostile route and defend the handoff.", ALL,
            stage("Find the research", goal("vault", ObjectiveKind.SCAN, "Scan the research vault", "Activate this terminal and hold close to recover its survey.", 3, 1, 15)),
            stage("Move the samples", goal("sample", ObjectiveKind.COURIER, "Run the sample relay", "Visit the ordered route while skating; teleports do not advance it.", 1, 6, 0)),
            stage("Secure the handoff", goal("handoff", ObjectiveKind.DEFEND, "Defend the sample handoff", "Hold the handoff site against contesting enemies.", 1, 1, 90))),
        mission("machine_shutdown", "Machine Shutdown", "Break fabrication lines, steal their production ledger and escape by rail.", Set.of(Faction.AUTOMATON),
            stage("Shut the factories", goal("fabricator", ObjectiveKind.DEMOLITION, "Destroy the fabricator cores", "Break the marked cores inside each Machine Legion factory.", 2, 3, 0)),
            stage("Steal the ledger", goal("ledger", ObjectiveKind.SCAN, "Copy the production ledger", "Activate the ledger terminal and protect the scan.", 1, 1, 18)),
            stage("Get the data out", goal("rail", ObjectiveKind.COURIER, "Skate the escape rail", "Deliver the ledger through the ordered checkpoints.", 1, 4, 0))),
        mission("factory_uprising", "Factory Uprising", "Capture the factory district's airwaves, demolish its megafactories and break the response force.", Set.of(Faction.AUTOMATON),
            stage("Take the signals", goal("radio", ObjectiveKind.RELAY_HOLD, "Seize the factory broadcast", "Keep moving inside an uncontested radio zone.", 2, 1, 90)),
            stage("Dismantle the district", goal("factory", ObjectiveKind.DEMOLITION, "Demolish the factory cores", "Destroy all marked cores in this fortified factory.", 2, 4, 0)),
            stage("Break the response", goal("response", ObjectiveKind.SWEEP, "Clear the response force", "Defeat mission-owned enemies inside this response sector.", 1, 35, 0))),
        mission("nest_diving", "Nest Diving", "Find the swarm's tunnel network, seal its nesting holes and race out with the survey.", Set.of(Faction.TERMINID),
            stage("Track the infestation", goal("spore", ObjectiveKind.SCAN, "Scan the spore trail", "Start and protect a short scan of this swarm site.", 2, 1, 10)),
            stage("Seal the nests", goal("nest", ObjectiveKind.DEMOLITION, "Seal the bug-hole cores", "Destroy every marked nest core to close its reinforcement route.", 3, 3, 0)),
            stage("Escape with the survey", goal("survey", ObjectiveKind.COURIER, "Run the survey route", "Skate through the ordered escape beacons.", 1, 5, 0))),
        mission("hive_breaker", "Hive Breaker", "Draw out the swarm, destroy its central brood engine and paint the liberated approaches.", Set.of(Faction.TERMINID),
            stage("Draw out the swarm", goal("swarm", ObjectiveKind.SWEEP, "Thin the defending swarm", "Defeat unique mission-owned enemies in this hive sector.", 2, 28, 0)),
            stage("Break the brood engine", goal("brood", ObjectiveKind.DEMOLITION, "Destroy the brood cores", "Break the giant nest's marked cores.", 1, 6, 0)),
            stage("Reclaim the hive", goal("mural", ObjectiveKind.TERRITORY, "Paint the hive approaches", "Claim distinct turf cells on the cleared hive approaches.", 2, 30, 0))),
        mission("warp_blackout", "Warp Blackout", "Claim shield-control plazas, disable landed warp ships and jam the surviving fleet.", Set.of(Faction.ILLUMINATE),
            stage("Overpaint the shields", goal("shield", ObjectiveKind.TERRITORY, "Paint the shield plaza", "Claim the marked cells to take control of the shield plaza.", 2, 24, 0)),
            stage("Ground the fleet", goal("ship", ObjectiveKind.DEMOLITION, "Disable the warp-ship cores", "Destroy the marked cores of each grounded ship.", 2, 3, 0)),
            stage("Jam the fleet", goal("jam", ObjectiveKind.RELAY_HOLD, "Hold the fleet jammer", "Skate inside the uncontested jammer zone until transmission completes.", 1, 1, 120))),
        mission("prism_recon", "Prism Recon", "Document strange structures, relay your evidence and collapse the portal station.", Set.of(Faction.ILLUMINATE),
            stage("Document the anomaly", goal("evidence", ObjectiveKind.SCAN, "Record the anomaly", "Activate and protect the marked evidence scanner.", 3, 1, 16)),
            stage("Relay the evidence", goal("proof", ObjectiveKind.COURIER, "Carry the evidence", "Cross the evidence beacons in their displayed order.", 1, 5, 0)),
            stage("Collapse the station", goal("portal", ObjectiveKind.DEMOLITION, "Destroy the portal cores", "Damage all physical cores at the portal station.", 1, 4, 0))),
        mission("evacuation_rush", "Evacuation Rush", "Secure a rescue channel, escort two moving evacuation markers and defend the departure plaza.", ALL,
            stage("Open the rescue channel", goal("rescue", ObjectiveKind.RELAY_HOLD, "Raise the rescue signal", "Skate around the rescue relay while keeping rivals out.", 1, 1, 70)),
            stage("Guide the evacuees", goal("evac", ObjectiveKind.ESCORT, "Escort the evacuation group", "Stay near this moving marker and keep its route clear.", 2, 4, 0)),
            stage("Hold the departure plaza", goal("depart", ObjectiveKind.DEFEND, "Defend the evacuation plaza", "Hold the plaza against the final response force.", 1, 1, 150))),
        mission("district_holdout", "District Holdout", "Paint a defensive network, hold its linked plazas and defeat the final strike force.", ALL,
            stage("Build the turf network", goal("network", ObjectiveKind.TERRITORY, "Claim the defense network", "Paint distinct cells at each defensive plaza.", 3, 24, 0)),
            stage("Hold the plazas", goal("plaza", ObjectiveKind.DEFEND, "Defend the plaza", "Remain in the plaza while rivals attempt to contest it.", 2, 1, 180)),
            stage("Break the last push", goal("push", ObjectiveKind.SWEEP, "Defeat the final strike force", "Defeat mission-owned enemies within the final sector.", 1, 30, 0))),
        mission("mural_rocket", "Mural Rocket", "Recover launch art, power the paint rocket and defend its final countdown.", ALL,
            stage("Recover the launch art", goal("codes", ObjectiveKind.SCAN, "Recover the launch codes", "Activate both control terminals and hold each scan.", 2, 1, 18)),
            stage("Power the launcher", goal("power", ObjectiveKind.RELAY_HOLD, "Charge the launch grid", "Skate around the launcher relay while clearing its response force.", 1, 1, 100)),
            stage("Launch the color burst", goal("launch", ObjectiveKind.DEFEND, "Protect the launch countdown", "Hold the launch plaza until its defended countdown completes.", 1, 1, 150))),
        mission("pigment_pipeline", "Pigment Pipeline", "Restore a stolen pump network and protect the district's pigment supply.", ALL,
            stage("Trace the pipeline", goal("valve", ObjectiveKind.SCAN, "Locate the pump controls", "Survey the marked pump terminals to restore the control network.", 2, 1, 20)),
            stage("Restart the pumps", goal("pump", ObjectiveKind.RELAY_HOLD, "Power the pigment pump", "Skate within each uncontested pump zone to keep it running.", 2, 1, 95)),
            stage("Fill the tanks", goal("tank", ObjectiveKind.DEFEND, "Protect the pigment transfer", "Keep the transfer yard clear while the tank-loading clock runs.", 1, 1, 150))),
        mission("swarm_purge", "Swarm Purge", "Break two swarm fronts, collapse their breeding route and survive the final surge.", Set.of(Faction.TERMINID),
            stage("Break the fronts", goal("front", ObjectiveKind.SWEEP, "Clear the swarm front", "Defeat distinct mission-owned swarm enemies inside this sector.", 2, 35, 0)),
            stage("Cut the breeding route", goal("breach", ObjectiveKind.DEMOLITION, "Collapse the swarm holes", "Destroy the marked nest cores feeding the surge.", 2, 3, 0)),
            stage("Survive the surge", goal("surge", ObjectiveKind.SWEEP, "Repel the swarm surge", "Defeat the final local wave quota; distant or repeated deaths do not count.", 1, 60, 0))),
        mission("machine_eradication", "Machine Eradication", "Break a Machine Legion formation, hold its response beacon and defeat the counterstrike.", Set.of(Faction.AUTOMATON),
            stage("Break the formation", goal("formation", ObjectiveKind.SWEEP, "Dismantle the machine formation", "Defeat the mission-owned machine force in this district.", 1, 50, 0)),
            stage("Draw the reinforcements", goal("beacon", ObjectiveKind.DEFEND, "Hold the counterstrike beacon", "Remain in the signal plaza and keep its held clock uncontested.", 1, 1, 150)),
            stage("Finish the counterstrike", goal("strike", ObjectiveKind.SWEEP, "Defeat the machine counterstrike", "Clear the final local response quota.", 1, 45, 0))),
        mission("fleet_eradication", "Fleet Eradication", "Trace a Prism Choir landing, defeat its invasion groups and hold the last clear plaza.", Set.of(Faction.ILLUMINATE),
            stage("Trace the landing", goal("landing", ObjectiveKind.SCAN, "Decode the landing signal", "Activate and protect each landing-frequency scanner.", 2, 1, 15)),
            stage("Break the invasion groups", goal("invader", ObjectiveKind.SWEEP, "Defeat the invasion group", "Defeat unique mission-owned enemies in each invasion district.", 2, 40, 0)),
            stage("Hold the last plaza", goal("clear", ObjectiveKind.DEFEND, "Defend the reclaimed plaza", "Keep the plaza uncontested until the defense timer completes.", 1, 1, 150))),
        mission("runway_revolt", "Runway Revolt", "Steal the air-base manifest, destroy its grounded launch cores and skate the escape route.", Set.of(Faction.AUTOMATON),
            stage("Find the launch schedule", goal("manifest", ObjectiveKind.SCAN, "Steal the runway manifest", "Activate the air-base terminal and protect the download.", 1, 1, 30)),
            stage("Ground the launches", goal("pad", ObjectiveKind.DEMOLITION, "Destroy the grounded launch cores", "Damage every marked core at each launch pad.", 3, 2, 0)),
            stage("Escape with the manifest", goal("escape", ObjectiveKind.COURIER, "Run the air-base escape", "Collect the manifest canister and skate its ordered checkpoints.", 1, 4, 0))),
        mission("blitz_blockbuster", "Blitz Blockbuster", "A compact search-and-destroy chain: crack outposts, smash the response and break the command base.", ALL,
            stage("Find and crack the outposts", goal("outpost", ObjectiveKind.DEMOLITION, "Crack the forward outpost", "Find each outpost and destroy its physical cores.", 2, 2, 0)),
            stage("Smash the response", goal("response", ObjectiveKind.SWEEP, "Break the rapid response", "Defeat the local response quota before moving to the command base.", 1, 35, 0)),
            stage("Break the command base", goal("command", ObjectiveKind.DEMOLITION, "Destroy the command-base cores", "Destroy the larger final base's marked cores.", 1, 5, 0))),
        mission("asset_guard", "Asset Guard", "Power two security zones, defend the valuable-artifact bays and dispatch their secure courier.", ALL,
            stage("Power the security zones", goal("security", ObjectiveKind.RELAY_HOLD, "Power the asset security relay", "Maintain an uncontested skating broadcast at each security relay.", 2, 1, 65)),
            stage("Defend the asset bays", goal("bay", ObjectiveKind.DEFEND, "Protect the valuable-artifact bay", "Hold each asset bay against the incoming response.", 2, 1, 130)),
            stage("Dispatch the secure courier", goal("dispatch", ObjectiveKind.COURIER, "Deliver the secured artifacts", "Pick up the asset canister and complete the ordered skating route.", 1, 4, 0))),
        mission("seismic_session", "Seismic Session", "Survey mineral sites, protect a deep scan and retrieve the resulting pigment research.", ALL,
            stage("Survey the mineral sites", goal("probe", ObjectiveKind.SCAN, "Survey the pigment seam", "Activate and hold each research probe's close-range scan.", 3, 1, 20)),
            stage("Protect the deep scan", goal("drill", ObjectiveKind.DEFEND, "Defend the deep-scan platform", "Keep the drilling plaza uncontested for the research cycle.", 1, 1, 150)),
            stage("Recover the research", goal("readout", ObjectiveKind.SCAN, "Read the geological results", "Activate and protect the final research readout.", 1, 1, 30))),
        mission("raise_the_colors", "Raise the Colors", "Paint the flag plazas, broadcast your crew colors and defend the final celebration.", ALL,
            stage("Prepare the flag plazas", goal("plaza", ObjectiveKind.TERRITORY, "Paint the crew-flag plaza", "Claim distinct turf cells around each flag site.", 2, 18, 0)),
            stage("Broadcast the crew colors", goal("flag", ObjectiveKind.RELAY_HOLD, "Raise the crew-color signal", "Keep skating around each uncontested flag relay.", 3, 1, 75)),
            stage("Defend the celebration", goal("party", ObjectiveKind.DEFEND, "Protect the crew celebration", "Hold the final celebration plaza against rival contest.", 1, 1, 100))),
        mission("commando_confidential", "Commando Confidential", "Recover black-box intelligence, skate it through enemy territory and upload the evidence.", Set.of(Faction.AUTOMATON),
            stage("Recover the black boxes", goal("blackbox", ObjectiveKind.SCAN, "Recover the black-box evidence", "Approach and activate each evidence terminal, then protect its scan.", 3, 1, 18)),
            stage("Run the intelligence route", goal("intel", ObjectiveKind.COURIER, "Carry the confidential evidence", "Collect the evidence canister and follow the ordered route.", 1, 6, 0)),
            stage("Upload the evidence", goal("upload", ObjectiveKind.SCAN, "Upload the recovered intelligence", "Activate the extraction uplink and stay close for the upload.", 1, 1, 35))),
        mission("hatchery_rampage", "Hatchery Rampage", "Destroy egg-cluster cores, clear the protecting swarm and paint the broken hatchery.", Set.of(Faction.TERMINID),
            stage("Break the egg clusters", goal("eggs", ObjectiveKind.DEMOLITION, "Destroy the egg-cluster cores", "Break every marked physical core in each hatchery cluster.", 3, 4, 0)),
            stage("Clear the brood response", goal("brood", ObjectiveKind.SWEEP, "Defeat the hatchery defenders", "Defeat unique mission-owned enemies in the brood sectors.", 2, 24, 0)),
            stage("Reclaim the hatchery", goal("reclaim", ObjectiveKind.TERRITORY, "Paint the emptied hatchery", "Claim distinct turf cells in the reclaimed hatchery district.", 1, 30, 0))),
        mission("dark_ink_protocol", "Dark Ink Protocol", "Historical-scenario remix: survey the dark-ink sites, protect their drill cycles and collapse a corrupted nest.", Set.of(Faction.TERMINID),
            stage("Survey the dark-ink sites", goal("fluid", ObjectiveKind.SCAN, "Survey the dark-ink reservoir", "Activate the reservoir terminal and hold its scan.", 2, 1, 18)),
            stage("Protect the drill cycles", goal("drill", ObjectiveKind.DEFEND, "Protect the dark-ink drill", "Hold each drilling site while keeping the surrounding swarm clear.", 2, 1, 135)),
            stage("Collapse the corrupted nest", goal("collapse", ObjectiveKind.DEMOLITION, "Break the corrupted-nest cores", "Destroy the final nest's physical cores.", 1, 4, 0))),
        mission("termicide_remix", "Termicide Remix", "Historical-scenario remix: bring a district treatment network online and get its research back out.", Set.of(Faction.TERMINID),
            stage("Activate the treatment grid", goal("treatment", ObjectiveKind.RELAY_HOLD, "Power the pigment-treatment tower", "Skate within each uncontested treatment relay.", 3, 1, 100)),
            stage("Hold the treatment cycle", goal("cycle", ObjectiveKind.DEFEND, "Defend the district treatment", "Keep the central treatment plaza uncontested through its full cycle.", 1, 1, 180)),
            stage("Recover the treatment research", goal("research", ObjectiveKind.COURIER, "Deliver the treatment research", "Collect the research canister and skate the marked escape beacons.", 1, 5, 0))),
        mission("lair_documentary", "Lair Documentary", "Break into a Prism Choir research site, document its experiments and deliver the evidence.", Set.of(Faction.ILLUMINATE),
            stage("Break into the lair", goal("entry", ObjectiveKind.DEMOLITION, "Destroy the lair-entry cores", "Destroy the marked entry cores before the evidence sites activate.", 1, 3, 0)),
            stage("Document the experiments", goal("record", ObjectiveKind.SCAN, "Record the illicit experiment", "Activate each evidence recorder and protect its close-range capture.", 3, 1, 20)),
            stage("Upload the documentary", goal("evidence", ObjectiveKind.COURIER, "Deliver the recorded evidence", "Collect the recording canister and skate its ordered upload route.", 1, 6, 0))),
        mission("headliner_hunt", "Headliner Hunt", "Track a priority gang enforcer, defeat that actual marked target and deliver proof of the takedown.", ALL,
            stage("Track the headliner", goal("track", ObjectiveKind.SCAN, "Trace the priority target", "Activate and protect the tracking terminals to reveal the headliner's sector.", 2, 1, 22)),
            stage("Defeat the headliner", goal("priority", ObjectiveKind.SWEEP, "Defeat the marked priority enforcer", "Only the designated native priority target counts; ordinary rival deaths do not.", 1, 1, 0)),
            stage("Deliver the proof", goal("proof", ObjectiveKind.COURIER, "Carry the headliner evidence", "Collect the evidence canister and complete its ordered route.", 1, 5, 0)))
    );
    private static final List<GoalSpec> OPTIONAL = List.of(
            goal("mural", ObjectiveKind.TERRITORY, "Optional: community mural", "Claim distinct turf cells for a crew research bonus.", 1, 18, 0),
            goal("pirate", ObjectiveKind.RELAY_HOLD, "Optional: pirate uplink", "Broadcast while skating in this uncontested zone.", 1, 1, 75),
            goal("cache", ObjectiveKind.SCAN, "Optional: survey cache", "Activate and protect the cache scanner.", 1, 1, 15),
            goal("delivery", ObjectiveKind.COURIER, "Optional: pigment delivery", "Follow this ordered skating route for extra research.", 1, 4, 0),
            goal("convoy", ObjectiveKind.ESCORT, "Optional: stranded paint rig", "Escort the moving rig past its checkpoints.", 1, 3, 0),
            goal("ambush", ObjectiveKind.SWEEP, "Optional: rival ambush", "Defeat unique enemies in this ambush sector.", 1, 24, 0));

    public static List<MissionTemplate> templates() { return TEMPLATES; }
    public static List<MissionTemplate> templates(Faction faction) { return TEMPLATES.stream().filter(t -> t.supports(faction)).toList(); }
    public static MissionTemplate template(String id) { return TEMPLATES.stream().filter(t -> t.id().equals(id)).findFirst().orElse(null); }
    public static List<GoalSpec> optionalGoals() { return OPTIONAL; }
    public static List<Faction> factions() { return List.of(Faction.values()); }
    public static Faction faction(String id) {
        if (id == null) return null;
        String normalized = id.toLowerCase(Locale.ROOT);
        return factions().stream().filter(f -> f.id().equals(normalized)).findFirst().orElse(null);
    }
    private LibertyMissionCatalog() {}
}
