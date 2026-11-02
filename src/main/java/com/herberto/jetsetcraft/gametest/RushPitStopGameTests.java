package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.rush.RushDebrief;
import com.herberto.jetsetcraft.rush.RushPitStop;
import com.herberto.jetsetcraft.rush.RushRun;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

/** Production model transactions and detached previews; session/action/settlement tests are separate. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushPitStopGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void realClearReceiptsGuardOneSharedBudgetAndInvalidateOldStops(GameTestHelper helper) {
        RushRun run = run(7801);
        grant(run, RushRun.Upgrade.FORCE, 1); grant(run, RushRun.Upgrade.BATTERY, 2);
        grant(run, RushRun.Upgrade.AIR_BREAK, 1); grant(run, RushRun.Upgrade.EXPOSE, 1);
        for (int sample = 0; sample < 6; sample++) run.chargeLine(4);
        check(!run.openPitStop(receipt(run, 1), true) && !run.pitStopAvailable() && run.contractClears() == 0,
                "A receipt without an actual recorded clear opened a service or counted a contract");
        run.recordArenaClear(); int unopened = run.offerVersion();
        check(!run.openPitStop(null, true) && !run.openPitStop(new UUID(0, 0), true)
                        && !run.openPitStop(UUID.randomUUID(), true) && !run.openPitStop(receipt(run, 0), true)
                        && !run.openPitStop(receipt(run, -1), true) && !run.openPitStop(receipt(run, 2), true)
                        && run.offerVersion() == unopened && run.contractClears() == 0,
                "A null, zero, negative, unrelated or future clear receipt created a budget, revision or contract count");
        UUID first = receipt(run, 1);
        check(run.openPitStop(first, false) && run.pitStopCredits() == 1 && run.pitStopEarned() == 1
                        && run.pitStopReceipt().equals(first) && run.contractClears() == 0,
                "An actual normal clear did not open exactly one service");
        int firstVersion = run.offerVersion();
        check(!run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.FORCE, firstVersion - 1).accepted()
                        && !run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.CHILL, firstVersion).accepted()
                        && !run.takePitStop(RushPitStop.Service.REWIND, null, firstVersion).accepted()
                        && !run.takePitStop(RushPitStop.Service.REMIX, null, firstVersion).accepted()
                        && run.offerVersion() == firstVersion && run.pitStopCredits() == 1 && run.pitStopServicesUsed() == 0,
                "A stale, unowned, capped or incomplete service consumed the clear's budget");
        check(run.remix(RushRun.Upgrade.BATTERY, RushRun.Upgrade.CHAIN, firstVersion)
                        && run.rank(RushRun.Upgrade.CHAIN) == 2 && run.rank(RushRun.Upgrade.BATTERY) == 0
                        && run.lineCharge() == 0 && run.artifactCount() == 3 && run.pitStopCredits() == 0
                        && run.pitStopServicesUsed() == 1 && !run.remixReady()
                        && !run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.FORCE, run.offerVersion()).accepted(),
                "Remix lost invested rank/charge teardown or retained an independent second service");
        CompoundTag accepted = run.lastPitStopResult().snapshot();
        check(accepted.getString("Service").equals("REMIX") && accepted.getInt("SourceRankBefore") == 2
                        && accepted.getInt("RankBefore") == 0 && accepted.getInt("RankAfter") == 2
                        && !run.openPitStop(first, true) && run.contractClears() == 0
                        && run.lastPitStopResult().snapshot().equals(accepted),
                "A duplicate clear refilled its spent stop, promoted a contract or replaced its accepted result");
        int spentVersion = run.offerVersion(); run.closePitStop(); int closedVersion = run.offerVersion();
        check(closedVersion != spentVersion && !run.pitStopAvailable() && !run.openPitStop(first, false),
                "Closing a fully spent stop failed to invalidate its lifecycle or allowed it to reopen");
        run.closePitStop(); check(run.offerVersion() == closedVersion, "Repeated close changed an already closed stop");

        run.recordArenaClear(); UUID second = receipt(run, 2);
        check(!run.openPitStop(first, true) && run.openPitStop(second, true) && run.pitStopCredits() == 2
                        && run.pitStopEarned() == 2 && run.contractClears() == 1 && !run.openPitStop(second, true),
                "The next actual contract clear lost its two choices or admitted duplicate/old receipts");
        int beforeTune = run.offerVersion();
        check(run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.FORCE, beforeTune).accepted()
                        && run.rank(RushRun.Upgrade.FORCE) == 2 && run.pitStopCredits() == 1
                        && !run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.CHAIN, run.offerVersion()).accepted()
                        && !run.remix(RushRun.Upgrade.CHAIN, RushRun.Upgrade.WIND, beforeTune)
                        && run.pitStopCredits() == 1 && run.pitStopServicesUsed() == 2,
                "A bonus stop allowed two Tunes or spent a queued request against its second credit");
        check(run.remix(RushRun.Upgrade.CHAIN, RushRun.Upgrade.WIND, run.offerVersion())
                        && run.rank(RushRun.Upgrade.WIND) == 2 && run.pitStopCredits() == 0
                        && run.pitStopServicesUsed() == 3 && run.pitStopUsed(RushPitStop.Service.TUNE)
                        && run.pitStopUsed(RushPitStop.Service.REMIX),
                "Two different services did not share the bonus stop's exact two-credit limit");
        run.recordArenaClear(); check(run.openPitStop(receipt(run, 3), true), "Could not open another actual clear");
        int abandonedVersion = run.offerVersion(); run.recordArenaClear();
        check(run.openPitStop(receipt(run, 4), false) && run.pitStopCredits() == 1 && run.pitStopEarned() == 1
                        && run.contractClears() == 2 && !run.pitStopUsed(RushPitStop.Service.TUNE)
                        && !run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.FORCE, abandonedVersion).accepted(),
                "Unused services stacked across clears or an earlier stop's intent spent the new budget");
        int activeVersion = run.offerVersion(); run.finish(); run.grantRemix();
        check(!run.pitStopAvailable() && !run.remixReady() && run.pitStopCredits() == 0
                        && run.offerVersion() != activeVersion && !run.openPitStop(receipt(run, 4), true)
                        && !run.takePitStop(RushPitStop.Service.PATCH, null, run.offerVersion()).accepted(),
                "A finished run retained service authority");
        RushRun streets = new RushRun(UUID.randomUUID(), UUID.randomUUID(), 91, false, "jetsetcraft:dead_beat");
        streets.recordArenaClear(); streets.grantRemix();
        check(!streets.openPitStop(receipt(streets, 1), true) && !streets.pitStopAvailable() && streets.contractClears() == 0,
                "Free streets acquired a circuit-only service budget");
        CompoundTag report = RushDebrief.capture(run, RushDebrief.Outcome.BANKED, 0, null);
        check(report.getInt("Schema") == 1 && report.getLong("ContractsBanked") == 2 && report.getLong("ServicesUsed") == 3,
                "The finished run report lost actual contract/service counts or changed its compatible schema");
        CompoundTag oldReport = report.copy(); oldReport.remove("ContractsBanked"); oldReport.remove("ServicesUsed");
        CompoundTag loaded = RushDebrief.sanitize(oldReport);
        check(!loaded.isEmpty() && loaded.getInt("Schema") == 1 && loaded.getLong("ContractsBanked") == 0
                        && loaded.getLong("ServicesUsed") == 0 && loaded.getUUID("Run").equals(run.id())
                        && !oldReport.contains("ContractsBanked") && !oldReport.contains("ServicesUsed"),
                "An older Schema1 report lost its identity, inherited invented counters or was mutated during display");
        pass(helper, "rush_pit_stop_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void finalRoomServicesApplyOnlyActualCappedRecoveryAndOwnedRanks(GameTestHelper helper) {
        RushRun run = run(7802);
        grant(run, RushRun.Upgrade.VITALITY, 1); grant(run, RushRun.Upgrade.FORCE, RushRun.Upgrade.FORCE.maxRank());
        grant(run, RushRun.Upgrade.WARD, 1); grant(run, RushRun.Upgrade.BATTERY, 1); grant(run, RushRun.Upgrade.RADIANCE, 1);
        check(!run.hurt(90) && near(run.vitality(), 34) && near(run.ward(), 0), "Could not prepare actual run damage");
        for (int clear = 0; clear < 6; clear++) run.recordArenaClear();
        run.pauseForEncore();
        check(run.openPitStop(receipt(run, 6), true) && run.awaitingEncore(), "The sixth clear could not open its earned stop");
        int version = run.offerVersion(); long score = run.score(), xp = run.xp(), clock = run.elapsedTicks();
        check(!run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.FORCE, version).accepted()
                        && !run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.VENOM, version).accepted()
                        && !run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.REPAIR, version).accepted()
                        && !run.takePitStop(RushPitStop.Service.PATCH, RushRun.Upgrade.WARD, version).accepted()
                        && run.pitStopCredits() == 2 && run.score() == score && run.xp() == xp,
                "A capped, unowned, repeatable or malformed service spent a credit or awarded fallback score/XP");
        String preview = option(RushPitStop.snapshot(run), "PATCH").getString("Preview");
        check(preview.contains("34 → 78.8 (+44.8)") && preview.contains("0 → 6 (+6)"),
                "Patch preview did not show the actual bounded vitality and ward recovery");
        double healed = run.vitalityRestored();
        RushPitStop.Result patched = run.takePitStop(RushPitStop.Service.PATCH, null, version);
        check(patched.accepted() && near(run.vitality(), 78.8) && near(run.ward(), 6)
                        && near(run.vitalityRestored() - healed, 44.8) && near(run.wardRestored(), 6)
                        && near(patched.snapshot().getDouble("VitalityRestored"), 44.8)
                        && near(patched.snapshot().getDouble("WardRestored"), 6) && run.pitStopCredits() == 1,
                "Final-room Patch failed, overfilled resources or counted attempted healing instead of its actual effect");
        RushPitStop.Result tuned = run.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.WARD, run.offerVersion());
        check(tuned.accepted() && run.rank(RushRun.Upgrade.WARD) == 2 && near(run.maxWard(), 16) && near(run.ward(), 10)
                        && near(run.wardRestored(), 6) && near(tuned.snapshot().getDouble("WardRestored"), 0)
                        && run.artifactCount() == 3 && run.pitStopCredits() == 0 && run.awaitingEncore()
                        && run.elapsedTicks() == clock && run.score() == score && run.xp() == xp,
                "Final-room Tune lost Encore, filled old shield damage, counted new capacity as recharge or altered the run economy");

        RushRun partial = run(7803); grant(partial, RushRun.Upgrade.WARD, 1); partial.hurt(13.5);
        // The production model's real quiet-period recharge supplies a partially full shield; no field injection.
        for (int tick = 0; tick < 160; tick++) partial.tickActive();
        check(near(partial.vitality(), 98.5) && near(partial.ward(), 9), "Quiet recharge fixture did not reach the expected actual resources");
        openNext(partial, false); double wardBefore = partial.wardRestored();
        preview = option(RushPitStop.snapshot(partial), "PATCH").getString("Preview");
        check(preview.contains("98.5 → 100 (+1.5)") && preview.contains("9 → 12 (+3)"),
                "Near-full Patch advertised uncapped recovery");
        check(partial.takePitStop(RushPitStop.Service.PATCH, null, partial.offerVersion()).accepted()
                        && near(partial.vitalityRestored(), 1.5) && near(partial.wardRestored() - wardBefore, 3)
                        && near(partial.vitality(), 100) && near(partial.ward(), 12),
                "Near-full Patch inflated actual recovery metrics or exceeded either cap");

        RushRun full = run(7804); openNext(full, false); version = full.offerVersion();
        check(!option(RushPitStop.snapshot(full), "PATCH").getBoolean("Enabled")
                        && !full.takePitStop(RushPitStop.Service.PATCH, null, version).accepted()
                        && full.offerVersion() == version && full.pitStopCredits() == 1 && full.pitStopServicesUsed() == 0
                        && full.lastPitStopResult().snapshot().isEmpty() && full.maxWard() == 0 && full.ward() == 0,
                "A no-op Patch spent a credit, invented a result or created unequipped ward");
        full.hurt(10);
        check(full.takePitStop(RushPitStop.Service.PATCH, null, version).accepted() && full.vitality() == 100
                        && full.ward() == 0 && near(full.vitalityRestored(), 10),
                "A vitality-only Patch required a shield or manufactured one");

        RushRun provider = run(7805); provider.enableSpellRelay(true); grant(provider, RushRun.Upgrade.SPELL_RELAY, 1);
        provider.enableSpellRelay(false); openNext(provider, false);
        check(!provider.takePitStop(RushPitStop.Service.TUNE, RushRun.Upgrade.SPELL_RELAY, provider.offerVersion()).accepted()
                        && provider.rank(RushRun.Upgrade.SPELL_RELAY) == 1 && provider.pitStopCredits() == 1
                        && RushPitStop.snapshot(provider).getList("TuneChoices", Tag.TAG_COMPOUND).isEmpty(),
                "A provider-unavailable family became a tune target or spent a service");
        pass(helper, "rush_pit_stop_effects");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void rewindsPreserveDraftExclusionsAndSnapshotsCannotMutateServices(GameTestHelper helper) {
        RushRun run = run(7806);
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) if (!upgrade.artifact() && !upgrade.repeatable()) grant(run, upgrade, 1);
        grant(run, RushRun.Upgrade.WARD, 1); grant(run, RushRun.Upgrade.VENOM, 1); grant(run, RushRun.Upgrade.CHILL, 1);
        run.gainXp(run.xpToNextLevel());
        check(run.reroll(run.offerVersion()), "Could not spend a genuine finite reroll");
        RushRun.Upgrade excluded = run.offers().get(0);
        check(run.canBanish(0) && run.banish(0, run.offerVersion()) && run.rank(excluded) == 1,
                "Could not banish an actually owned rank-up from the real draft");
        Set<RushRun.Upgrade> exclusions = run.banishedDraftUpgrades();
        openNext(run, true); int blockedVersion = run.offerVersion();
        CompoundTag initial = RushPitStop.snapshot(run); ListTag choices = initial.getList("TuneChoices", Tag.TAG_COMPOUND);
        check(initial.getBoolean("Available") && initial.getInt("Credits") == 2 && initial.getInt("Earned") == 2
                        && initial.getInt("Version") == blockedVersion && initial.getUUID("Receipt").equals(receipt(run, 1))
                        && initial.getList("Options", Tag.TAG_COMPOUND).size() == 4 && choices.size() == 14
                        && initial.getCompound("LastResult").isEmpty(),
                "The pending stop lost its bounded four services, owned tune choices, receipt or current revision");
        for (int i = 0; i < choices.size(); i++) {
            CompoundTag choice = choices.getCompound(i); RushRun.Upgrade upgrade = RushRun.Upgrade.valueOf(choice.getString("Id"));
            check(run.canTune(upgrade) && choice.getInt("Rank") == 1 && choice.getInt("NextRank") == 2
                            && !choice.getString("NextDescription").isBlank() && upgrade != RushRun.Upgrade.SPELL_RELAY,
                    "Tune choices invented an unowned, repeatable, unavailable or imprecise rank-up");
        }
        initial.putInt("Credits", 99); choices.getCompound(0).putInt("NextRank", 99);
        check(!run.takePitStop(RushPitStop.Service.REWIND, null, blockedVersion).accepted()
                        && run.pitStopCredits() == 2 && run.rerollsRemaining() == 2 && run.banishesRemaining() == 1
                        && run.offerVersion() == blockedVersion && run.lastPitStopResult().snapshot().isEmpty(),
                "An outstanding earned XP offer or mutated snapshot spent/changed the service budget");
        check(run.choose(0, run.offerVersion()) && !run.hasOffer(), "Could not finish the real pending draft");
        long score = run.score(), xp = run.xp(), clock = run.elapsedTicks(); int level = run.level();
        RushPitStop.Result rewound = run.takePitStop(RushPitStop.Service.REWIND, null, run.offerVersion());
        check(rewound.accepted() && run.rerollsRemaining() == 3 && run.banishesRemaining() == 2
                        && run.rerollsUsed() == 1 && run.banishesUsed() == 1 && run.banishedDraftUpgrades().equals(exclusions)
                        && run.score() == score && run.xp() == xp && run.elapsedTicks() == clock && run.level() == level
                        && run.pitStopServicesUsed() == 1 && run.pitStopCredits() == 1,
                "Rewind erased exclusions, forged past control usage, exceeded caps or added run progression");
        CompoundTag stored = rewound.snapshot(); rewound.snapshot().putInt("RerollsAfter", 99);
        CompoundTag detached = RushPitStop.snapshot(run); detached.getCompound("LastResult").putString("Summary", "changed");
        check(run.lastPitStopResult().snapshot().equals(stored) && !option(RushPitStop.snapshot(run), "REWIND").getBoolean("Enabled")
                        && option(RushPitStop.snapshot(run), "REWIND").getString("Reason").contains("already used")
                        && !run.takePitStop(RushPitStop.Service.REWIND, null, run.offerVersion()).accepted()
                        && run.pitStopCredits() == 1 && run.lastPitStopResult().snapshot().equals(stored),
                "A copied/rejected service replaced the authoritative result or allowed the same type twice");
        check(run.takePitStop(RushPitStop.Service.TUNE, excluded, run.offerVersion()).accepted()
                        && run.rank(excluded) == 2 && run.banishedDraftUpgrades().equals(exclusions)
                        && run.artifactCount() == 3 && run.pitStopCredits() == 0,
                "A draft-only banish blocked an already earned Tune or was silently removed by it");

        run.gainXp(run.xpToNextLevel());
        check(run.reroll(run.offerVersion()) && run.choose(0, run.offerVersion()), "Could not prepare a partial control refill");
        openNext(run, false);
        String preview = option(RushPitStop.snapshot(run), "REWIND").getString("Preview");
        check(preview.contains("Rerolls 2 → 3 (+1)") && preview.contains("banishes 2 → 2 (+0)"),
                "Rewind preview ignored a partially capped draft-control budget");
        check(run.takePitStop(RushPitStop.Service.REWIND, null, run.offerVersion()).accepted()
                        && run.rerollsRemaining() == 3 && run.banishesRemaining() == 2
                        && run.banishedDraftUpgrades().equals(exclusions),
                "A partial Rewind exceeded a cap or changed exclusions");
        pass(helper, "rush_pit_stop_snapshots");
    }

    private static RushRun run(long seed) {
        RushRun run = new RushRun(UUID.randomUUID(), UUID.randomUUID(), seed, false, "jetsetcraft:dead_beat");
        run.configureCircuit(); return run;
    }
    private static void grant(RushRun run, RushRun.Upgrade upgrade, int ranks) {
        for (int rank = 0; rank < ranks; rank++) check(run.grantReward(upgrade), "Could not earn rank for " + upgrade.name());
    }
    /** The model fixture explicitly records a clear; actual room objectives/settlement are tested through the manager. */
    private static void openNext(RushRun run, boolean bonus) {
        run.recordArenaClear(); check(run.openPitStop(receipt(run, run.arenaClears()), bonus), "A recorded clear did not open its stop");
    }
    private static UUID receipt(RushRun run, long clear) {
        return UUID.nameUUIDFromBytes((run.id() + ":arena:" + ((clear - 1) / 6 + 1) + ":" + ((clear - 1) % 6)).getBytes(StandardCharsets.UTF_8));
    }
    private static CompoundTag option(CompoundTag snapshot, String id) {
        ListTag options = snapshot.getList("Options", Tag.TAG_COMPOUND);
        for (int i = 0; i < options.size(); i++) if (options.getCompound(i).getString("Id").equals(id)) return options.getCompound(i);
        throw new GameTestAssertException("Missing service option " + id);
    }
    private static boolean near(double a, double b) { return Math.abs(a - b) < .00001; }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushPitStopGameTests() {}
}
