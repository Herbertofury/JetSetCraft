package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.liberty.LibertyCampaignData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Pure production campaign transactions; no fake operation score, terrain mutation or wall-clock catch-up. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LibertyCampaignGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void campaignResultsUnlockActualFrontiersAndRemainExactlyOnceAfterLoad(GameTestHelper helper) {
        LibertyCampaignData campaign = new LibertyCampaignData(); UUID owner = UUID.randomUUID();
        for (int difficulty = 1; difficulty <= 10; difficulty++)
            check(campaign.canDeploy("neon_docks", difficulty) && campaign.canDeploy("overpass_uprising", difficulty)
                    && campaign.canDeploy("blackout_borough", difficulty), "An original region gated a selectable difficulty");
        check(!campaign.canDeploy("neon_frontier", 1) && !campaign.canDeploy("neon_docks", 0)
                && !campaign.canDeploy("neon_docks", 11), "Frontier or difficulty boundaries were ignored");
        var first = result(owner, 1, true, null);
        check(campaign.settle(first) && campaign.liberation("neon_docks") == 500, "A genuine outcome did not add exact liberation");
        CompoundTag paid = campaign.save(new CompoundTag());
        check(campaign.settle(first) && paid.equals(campaign.save(new CompoundTag())), "A repeated result changed campaign progress or revision");
        var changed = new LibertyCampaignData.Result(first.runId(), owner, "neon_docks", "terminid", 2, 3, 3, 0, 0, true, null);
        check(!campaign.settle(changed) && paid.equals(campaign.save(new CompoundTag())), "An old nonce accepted a changed authoritative result");
        for (int i = 0; i < 3; i++) check(campaign.settle(result(owner, 1, true, null)), "A later distinct result was rejected");
        check(campaign.liberation("neon_docks") == 2_000 && campaign.canDeploy("neon_frontier", 10)
                && !campaign.canDeploy("iron_horizon", 1) && campaign.frontierUnlocked(), "Twenty-percent liberation did not unlock exactly its frontier route");
        CompoundTag saved = campaign.save(new CompoundTag()); LibertyCampaignData loaded = LibertyCampaignData.load(saved);
        check(!loaded.blocked() && saved.equals(loaded.save(new CompoundTag())) && loaded.settle(first)
                && saved.equals(loaded.save(new CompoundTag())), "Campaign state or replay identity changed after save/load");
        check(LibertyCampaignData.Result.load(first.save()).orElseThrow().equals(first), "Journal result payload did not round-trip");
        pass(helper, "liberty_campaign_progress");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void campaignDeadlinesIgnoreUnattendedTimeAndOfferOnlyExplicitHomeDefense(GameTestHelper helper) {
        UUID owner = UUID.randomUUID(); LibertyCampaignData campaign = threatened(owner);
        var regional = campaign.offers(owner).get(0); CompoundTag initial = campaign.save(new CompoundTag());
        for (long tick = 0; tick < LibertyCampaignData.DEFENSE_WINDOW + 100; tick++) campaign.tick(tick, false);
        check(campaign.activeTicks() == 0 && initial.equals(campaign.save(new CompoundTag()))
                && !campaign.offers(owner).get(0).homeWorld(), "Unattended server time consumed a defense deadline");
        campaign.tick(100_000, true); campaign.tick(100_000, true);
        check(campaign.activeTicks() == 1, "Repeated manager ticks advanced the campaign twice");
        campaign = LibertyCampaignData.load(campaign.save(new CompoundTag()));
        campaign.tick(9_000_000, true);
        check(campaign.activeTicks() == 2 && campaign.offers(owner).get(0).id().equals(regional.id()), "Reload performed offline wall-clock catch-up");
        for (long tick = 9_000_001; campaign.activeTicks() < LibertyCampaignData.DEFENSE_WINDOW; tick++) campaign.tick(tick, true);
        var home = campaign.offers(owner).get(0);
        check(home.homeWorld() && home.deadline() == 0 && home.claimedRun() == null
                && campaign.offers(UUID.randomUUID()).stream().noneMatch(offer -> offer.id().equals(home.id()))
                && campaign.liberation("neon_docks") == 0, "A failed defense did not become a private untimed deployment offer");
        long version = campaign.revision(); UUID run = UUID.randomUUID();
        check(!campaign.claimDefense(owner, home.id(), run, version - 1)
                && !campaign.claimDefense(UUID.randomUUID(), home.id(), run, version)
                && campaign.claimDefense(owner, home.id(), run, version)
                && campaign.claimDefense(owner, home.id(), run, version), "Home defense ignored ownership, revision or idempotent claims");
        var victory = new LibertyCampaignData.Result(run, owner, "neon_docks", "terminid", 10, 3, 3, 0, 0, true, home.id());
        check(campaign.settle(victory) && campaign.offers(owner).stream().noneMatch(offer -> offer.id().equals(home.id()))
                && campaign.settle(victory), "A real home-defense result failed to consume its offer exactly once");
        pass(helper, "liberty_campaign_defense_clock");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void claimedDefensesPauseAndFailedDeploymentsCanReleaseWithoutInventingVictory(GameTestHelper helper) {
        UUID owner = UUID.randomUUID(); LibertyCampaignData campaign = threatened(owner); var offer = campaign.offers(owner).get(0);
        UUID run = UUID.randomUUID();
        check(campaign.claimDefense(owner, offer.id(), run, campaign.revision()), "The real regional offer could not be claimed");
        for (long tick = 0; tick <= LibertyCampaignData.DEFENSE_WINDOW + 10; tick++) campaign.tick(tick, true);
        check(campaign.offer(owner, offer.id()).orElseThrow().claimedRun().equals(run)
                && campaign.offers(owner).stream().noneMatch(LibertyCampaignData.DefenseOffer::homeWorld),
                "A campaign deadline invalidated an already accepted mission");
        var unbound = new LibertyCampaignData.Result(run, owner, "neon_docks", "terminid", 10, 0, 3, 0, 0, false, null);
        check(!campaign.settle(unbound), "A defense claim settled as an unrelated mission and orphaned its claim");
        check(campaign.releaseDefense(owner, run) && campaign.releaseDefense(owner, run) && !campaign.settled(run),
                "Entry rollback invented a result or could not be retried");
        campaign.tick(LibertyCampaignData.DEFENSE_WINDOW + 20, true);
        var home = campaign.offers(owner).get(0); UUID homeRun = UUID.randomUUID();
        check(home.homeWorld() && campaign.claimDefense(owner, home.id(), homeRun, campaign.revision()), "An expired released defense did not offer safe redeployment");
        var loss = new LibertyCampaignData.Result(homeRun, owner, "neon_docks", "terminid", 10, 0, 3, 0, 0, false, home.id());
        check(campaign.settle(loss) && campaign.offers(owner).get(0).claimedRun() == null
                && campaign.offers(owner).get(0).id().equals(home.id()), "A failed optional home defense stopped being safely retryable");
        check(campaign.save(new CompoundTag()).equals(LibertyCampaignData.load(campaign.save(new CompoundTag())).save(new CompoundTag())),
                "A released defense or failed-home receipt corrupted reload state");
        pass(helper, "liberty_campaign_defense_claims");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void malformedFutureAndFullCampaignLedgersRemainIntact(GameTestHelper helper) {
        UUID owner = UUID.randomUUID(); LibertyCampaignData campaign = new LibertyCampaignData(); var paid = result(owner, 1, true, null);
        check(campaign.settle(paid), "Receipt boundary fixture could not settle"); CompoundTag valid = campaign.save(new CompoundTag());
        for (int variant = 0; variant < 5; variant++) {
            CompoundTag invalid = valid.copy();
            switch (variant) {
                case 0 -> invalid.putInt("Schema", 2);
                case 1 -> invalid.putLong("ActiveTicks", -1);
                case 2 -> invalid.getList("Regions", Tag.TAG_COMPOUND).getCompound(0).putInt("Threat", 101);
                case 3 -> invalid.putString("Receipts", "future-shaped-ledger");
                default -> invalid.putString("FutureField", "retain me");
            }
            LibertyCampaignData blocked = LibertyCampaignData.load(invalid);
            blocked.tick(100, true);
            check(blocked.blocked() && !blocked.settle(paid) && !blocked.canDeploy("neon_docks", 1)
                    && invalid.equals(blocked.save(new CompoundTag())), "An invalid or future campaign record was reset or partially mutated");
        }
        CompoundTag full = valid.copy(); ListTag ledger = full.getList("Receipts", Tag.TAG_COMPOUND);
        for (int i = ledger.size(); i < LibertyCampaignData.MAX_RECEIPTS; i++) {
            CompoundTag row = new CompoundTag(); row.putUUID("Run", new UUID(0, i)); row.putString("Hash", "0".repeat(64)); ledger.add(row);
        }
        full.putLong("Revision", LibertyCampaignData.MAX_RECEIPTS); LibertyCampaignData capped = LibertyCampaignData.load(full);
        check(!capped.blocked() && capped.receiptCount() == LibertyCampaignData.MAX_RECEIPTS && capped.settle(paid)
                && !capped.settle(result(owner, 1, true, null)) && !capped.canDeploy("neon_docks", 10)
                && full.equals(capped.save(new CompoundTag())), "A full receipt ledger evicted replay protection or rejected an exact retry");
        pass(helper, "liberty_campaign_schema_caps");
    }

    private static LibertyCampaignData threatened(UUID owner) {
        LibertyCampaignData out = new LibertyCampaignData();
        check(out.settle(result(owner, 10, false, null)) && out.settle(result(owner, 10, false, null))
                && out.threat("neon_docks") == 60 && out.offers(owner).size() == 1, "Real failed results did not create bounded regional pressure");
        return out;
    }
    private static LibertyCampaignData.Result result(UUID owner, int difficulty, boolean extracted, UUID defense) {
        return new LibertyCampaignData.Result(UUID.randomUUID(), owner, "neon_docks", "terminid", difficulty,
                extracted ? 3 : 0, 3, 0, 0, extracted, defense);
    }
    private static void check(boolean ok, String message) { if (!ok) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String name) { System.out.println("JETSETCRAFT_LIBERTY_CAMPAIGN_PASS " + name); helper.succeed(); }
    private LibertyCampaignGameTests() { }
}
