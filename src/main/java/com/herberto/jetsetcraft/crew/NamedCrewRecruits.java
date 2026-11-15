package com.herberto.jetsetcraft.crew;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Named mission-roster profiles only. No entity, model, image or music reuse is implied. */
public final class NamedCrewRecruits {
    public static final String LUCY = "jetsetcraft:lucy_lacemaker";
    public record Entry(String id, String characterId, String name, String affiliation) {}
    private static final List<Entry> ENTRIES = entries();
    private static List<Entry> entries() {
        ArrayList<Entry> out = new ArrayList<>();
        out.add(new Entry(LUCY, LUCY, "Lucy Lacemaker", "jetsetcraft:amygdala"));
        out.add(new Entry("jetsetcraft:pomni", "jetsetcraft:pomni", "Pomni", "jetsetcraft:honkistas"));
        for (String number : List.of("1471_a", "682", "049", "173", "939")) {
            String character = "jetsetcraft:scp_" + number;
            String title = number.equals("1471_a") ? "MalO (SCP-1471-A)" : "SCP-" + number;
            out.add(new Entry(character, character, title, "jetsetcraft:scp"));
            out.add(new Entry(character + "_female", character, title + " (female interpretation)", "jetsetcraft:scp"));
            out.add(new Entry(character + "_femboy", character, title + " (femboy interpretation)", "jetsetcraft:scp"));
        }
        return List.copyOf(out);
    }
    public static List<Entry> all() { return ENTRIES; }
    public static Optional<Entry> byId(String id) {
        return ENTRIES.stream().filter(entry -> entry.id().equals(id)).findFirst();
    }
    public static Optional<String> name(String id) { return byId(id).map(Entry::name); }
    public static Optional<String> affiliation(String id) { return byId(id).map(Entry::affiliation); }
    public static String characterId(String id) { return byId(id).map(Entry::characterId).orElse(id); }
    private NamedCrewRecruits() {}
}
