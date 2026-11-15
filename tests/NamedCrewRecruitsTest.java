import com.herberto.jetsetcraft.crew.NamedCrewRecruits;
import java.util.HashSet;
public final class NamedCrewRecruitsTest {
    public static void main(String[] args) {
        var ids = new HashSet<String>();
        assert NamedCrewRecruits.all().size() == 17;
        for (var entry : NamedCrewRecruits.all()) {
            assert ids.add(entry.id());
            assert NamedCrewRecruits.characterId(entry.id()).equals(entry.characterId());
            assert NamedCrewRecruits.name(entry.id()).orElseThrow().equals(entry.name());
        }
        for (String n : new String[]{"1471_a", "682", "049", "173", "939"}) {
            String base = "jetsetcraft:scp_" + n;
            for (String variant : new String[]{"", "_female", "_femboy"}) {
                assert NamedCrewRecruits.characterId(base + variant).equals(base);
                assert NamedCrewRecruits.affiliation(base + variant).orElseThrow().equals("jetsetcraft:scp");
            }
        }
        assert NamedCrewRecruits.byId("jetsetcraft:scp_682_female_extra").isEmpty();
        assert NamedCrewRecruits.byId(null).isEmpty();
        assert NamedCrewRecruits.characterId("minecraft:zombie").equals("minecraft:zombie");
        assert NamedCrewRecruits.affiliation("jetsetcraft:pomni").orElseThrow().equals("jetsetcraft:honkistas");
        System.out.println("17 named roster profiles: IDs, variants, affiliations and unknowns passed");
    }
}
