package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.crew.CrewRewardReceipt;
import com.herberto.jetsetcraft.rush.RushBuildPaths;
import com.herberto.jetsetcraft.rush.RushDebrief;
import com.herberto.jetsetcraft.rush.RushRun;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** Production run transactions and persisted display schema; runtime combat is exercised separately. */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RushBuildPathGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void draftControlsKeepEarnedChoicesBoundedAndRejectStaleSpending(GameTestHelper helper) {
        RushRun run = run(88231);
        check(run.grantStartingPerk(RushRun.Upgrade.CHAIN), "Could not equip a genuine starting artifact");
        run.gainXp(run.xpToNextLevel());
        List<RushRun.Upgrade> original = run.offers(); int initialVersion = run.offerVersion();
        int level = run.level(); long xp = run.xp(), ticks = run.elapsedTicks();
        check(original.size() == 3 && original.contains(RushRun.Upgrade.CHAIN),
                "A normal draft omitted every available rank-up for its equipped artifact");
        check(!run.reroll(initialVersion - 1) && run.rerollsRemaining() == 3,
                "A stale reroll spent a control");
        check(run.reroll(initialVersion) && run.rerollsRemaining() == 2 && run.rerollsUsed() == 1
                        && run.offers().stream().noneMatch(original::contains)
                        && run.level() == level && run.xp() == xp && run.elapsedTicks() == ticks,
                "A reroll repeated avoidable choices, consumed earned progression or advanced the clock");
        check(!run.choose(0, initialVersion) && !run.reroll(initialVersion), "An old offer could be claimed or rerolled after replacement");
        List<RushRun.Upgrade> beforeBanish = run.offers(); RushRun.Upgrade excluded = beforeBanish.get(1);
        int banishVersion = run.offerVersion(), rank = run.rank(excluded);
        check(run.banish(1, banishVersion) && run.banishesRemaining() == 1 && run.banishesUsed() == 1
                        && run.offers().get(0) == beforeBanish.get(0) && run.offers().get(2) == beforeBanish.get(2)
                        && !run.offers().contains(excluded) && run.rank(excluded) == rank
                        && run.xp() == xp && run.level() == level && !run.banish(1, banishVersion),
                "Banish changed kept rows, removed an earned rank, spent XP or admitted a replay");
        check(run.grantReward(excluded) && run.rank(excluded) == rank + 1 && !run.offers().contains(excluded),
                "A draft-only banish blocked a physical rank reward or leaked back into the refreshed draft");
        int nextBanish = firstBanishable(run);
        check(nextBanish >= 0 && run.banish(nextBanish, run.offerVersion()) && run.banishesRemaining() == 0,
                "The second permitted banish failed");
        check(!run.banish(0, run.offerVersion()) && run.banishesUsed() == 2, "A third banish bypassed the finite budget");
        for (int attempt = 0; attempt < 2; attempt++) check(run.reroll(run.offerVersion()), "A remaining earned reroll was unusable");
        check(run.rerollsRemaining() == 0 && !run.reroll(run.offerVersion()), "A fourth initial reroll bypassed the finite budget");
        for (int draft = 0; draft < 8; draft++) {
            check(run.offers().size() == 3 && run.offers().stream().distinct().count() == 3
                            && run.offers().stream().noneMatch(run.banishedDraftUpgrades()::contains)
                            && run.offers().stream().noneMatch(upgrade -> upgrade == RushRun.Upgrade.SPELL_RELAY)
                            && run.offers().stream().allMatch(upgrade -> upgrade.repeatable() || run.canAdvance(upgrade)),
                    "A later draft contained duplicate, excluded, provider-absent or unusable choices");
            check(run.choose(0, run.offerVersion()), "An actual valid draft could not be selected");
            run.gainXp(run.xpToNextLevel());
        }
        CompoundTag detached = RushBuildPaths.draftSnapshot(run); detached.putInt("Rerolls", 99);
        check(run.rerollsRemaining() == 0 && run.banishedDraftUpgrades().size() == 2,
                "A returned draft snapshot changed server-owned controls");

        RushRun exhausted = run(77);
        for (RushRun.Upgrade artifact : List.of(RushRun.Upgrade.CHAIN, RushRun.Upgrade.VENOM, RushRun.Upgrade.CHILL))
            for (int i = 0; i < artifact.maxRank(); i++) exhausted.grantReward(artifact);
        for (RushRun.Upgrade upgrade : RushRun.Upgrade.values()) if (!upgrade.artifact() && !upgrade.repeatable())
            for (int i = 0; i < upgrade.maxRank(); i++) exhausted.grantReward(upgrade);
        exhausted.gainXp(exhausted.xpToNextLevel());
        check(exhausted.offers().equals(List.of(RushRun.Upgrade.REPAIR, RushRun.Upgrade.CACHE, RushRun.Upgrade.SURGE))
                        && !exhausted.canReroll() && !exhausted.reroll(exhausted.offerVersion())
                        && exhausted.rerollsRemaining() == 3 && !exhausted.banish(0, exhausted.offerVersion()),
                "An exhausted draft lost its useful fallback or charged for a no-change reroll");
        long score = exhausted.score(); int version = exhausted.offerVersion();
        check(exhausted.choose(1, version) && exhausted.score() > score && !exhausted.choose(1, version),
                "A real fallback reward failed or could be claimed twice");
        pass(helper, "rush_draft_controls");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void earnedEvolutionsPreserveThreeSlotsAndAccountForActualRecovery(GameTestHelper helper) {
        RushRun run = run(934); run.configureCircuit();
        grant(run, RushRun.Upgrade.WARD, 3); grant(run, RushRun.Upgrade.RADIANCE, 2);
        grant(run, RushRun.Upgrade.FIREWORK, 1);
        run.grantHeadlinerReward(); int firstReward = run.offerVersion();
        check(run.hasBuildOpportunity() && run.headliners() == 1 && run.rerollsRemaining() == 3
                        && !run.evolve(RushBuildPaths.Evolution.RADIANT_BARRIER, firstReward) && run.hasBuildOpportunity(),
                "An unready recipe consumed or silently lost its earned opportunity");
        grant(run, RushRun.Upgrade.RADIANCE, 1);
        check(run.evolve(RushBuildPaths.Evolution.RADIANT_BARRIER, run.offerVersion())
                        && run.artifactCount() == 3 && run.evolutionActive(RushBuildPaths.Evolution.RADIANT_BARRIER)
                        && !run.hasBuildOpportunity() && !run.evolve(RushBuildPaths.Evolution.RADIANT_BARRIER, firstReward),
                "Evolution bypassed slot/rank requirements or spent the same opportunity twice");
        check(!run.hurt(30) && near(run.wardAbsorbed(), 20) && near(run.damageTaken(), 10),
                "Damage accounting included overkill or missed actual ward absorption");
        RushRun.Recovery first = run.applyRadiance(), second = run.applyRadiance(), third = run.applyRadiance();
        check(near(first.vitalityRestored(), 8) && near(first.wardRestored(), 0)
                        && near(second.vitalityRestored(), 2) && near(second.wardRestored(), 6)
                        && near(third.vitalityRestored(), 0) && near(third.wardRestored(), 6)
                        && near(run.vitalityRestored(), 10) && near(run.wardRestored(), 12)
                        && near(run.ward(), 12) && run.evolutionProcs(RushBuildPaths.Evolution.RADIANT_BARRIER) == 2,
                "Radiant barrier invented healing, banked excess overheal or exceeded its six-point conversion");
        run.heal(999); run.heal(Double.NaN);
        check(near(run.vitalityRestored(), 10), "Full-vitality or invalid healing inflated recovery statistics");
        run.pauseForEncore(); long paused = run.elapsedTicks(); double shield = run.ward();
        for (int tick = 0; tick < 120; tick++) run.tickActive();
        check(run.elapsedTicks() == paused && near(run.applyRadiance().wardRestored(), 0) && near(run.ward(), shield),
                "Encore pause generated an evolution proc or shield recharge");
        check(run.continueEndless(), "Could not resume the paused evolution fixture");
        run.gainXp(run.xpToNextLevel());
        check(near(run.applyRadiance().wardRestored(), 0) && run.reroll(run.offerVersion()) && run.rerollsRemaining() == 2,
                "An open draft healed the build or lost its reroll");
        run.grantHeadlinerReward();
        check(run.headliners() == 2 && run.rerollsRemaining() == 3 && run.hasBuildOpportunity(),
                "An owned headliner reward failed to replenish exactly one bounded reroll");
        check(run.choose(0, run.offerVersion()), "Could not finish the evolution fixture's real XP draft");
        run.grantRemix(); check(run.remix(RushRun.Upgrade.WARD, RushRun.Upgrade.VENOM, run.offerVersion()), "Could not remix the shield family");
        check(run.selectedEvolution() == RushBuildPaths.Evolution.RADIANT_BARRIER
                        && !run.evolutionActive(RushBuildPaths.Evolution.RADIANT_BARRIER) && run.ward() == 0,
                "Removing a recipe component retained active shield power");
        run.grantRemix(); check(run.remix(RushRun.Upgrade.VENOM, RushRun.Upgrade.WARD, run.offerVersion()), "Could not restore the invested shield rank");
        check(run.evolutionActive(RushBuildPaths.Evolution.RADIANT_BARRIER), "Restoring a selected recipe's ranks did not reactivate it");
        run.grantRemix(); check(run.remix(RushRun.Upgrade.WARD, RushRun.Upgrade.VENOM, run.offerVersion()), "Could not prepare the replacement recipe");
        run.grantRemix(); check(run.remix(RushRun.Upgrade.RADIANCE, RushRun.Upgrade.CHILL, run.offerVersion()), "Could not preserve the other invested rank");
        int replacementVersion = run.offerVersion();
        check(!run.evolve(RushBuildPaths.Evolution.CRYO_SHATTER, replacementVersion - 1)
                        && run.evolve(RushBuildPaths.Evolution.CRYO_SHATTER, replacementVersion)
                        && run.artifactCount() == 3 && !run.evolutionActive(RushBuildPaths.Evolution.RADIANT_BARRIER)
                        && !run.hasBuildOpportunity(), "A replacement evolution ignored its newly earned opportunity/version");
        check(run.tryEvolutionProc(RushBuildPaths.Evolution.CRYO_SHATTER)
                        && !run.tryEvolutionProc(RushBuildPaths.Evolution.CRYO_SHATTER), "Cryo shatter bypassed its active-time cooldown");
        run.pauseForEncore();
        for (int tick = 0; tick < 100; tick++) run.tickActive();
        check(!run.tryEvolutionProc(RushBuildPaths.Evolution.CRYO_SHATTER) && run.continueEndless(), "A paused proc became usable");
        for (int tick = 0; tick < 59; tick++) run.tickActive();
        check(!run.tryEvolutionProc(RushBuildPaths.Evolution.CRYO_SHATTER), "Cryo shatter rearmed before 60 active ticks");
        run.tickActive(); check(run.tryEvolutionProc(RushBuildPaths.Evolution.CRYO_SHATTER), "Cryo shatter failed to rearm after 60 active ticks");
        long headliners = run.headliners(); run.finish(); run.grantHeadlinerReward();
        check(run.headliners() == headliners && !run.tryEvolutionProc(RushBuildPaths.Evolution.CRYO_SHATTER)
                        && !run.evolve(RushBuildPaths.Evolution.RAILSTORM, run.offerVersion()) && !run.hasBuildOpportunity(),
                "A finished run retained reward/evolution authority");
        pass(helper, "rush_evolution_rules");
    }

    @GameTest(template = "hoverboard_empty", timeoutTicks = 100)
    public static void debriefIsBoundedDetachedAndCannotPromoteAnUnrelatedReward(GameTestHelper helper) {
        RushRun run = run(2341); run.configureCircuit();
        grant(run, RushRun.Upgrade.RADIANCE, 3); grant(run, RushRun.Upgrade.WARD, 3);
        run.grantHeadlinerReward(); check(run.evolve(RushBuildPaths.Evolution.RADIANT_BARRIER, run.offerVersion()), "Debrief recipe could not be earned");
        run.recordVirtualDamage(7.5, true); run.recordVirtualDamage(5.25, false);
        run.recordVirtualDamage(Double.NaN, true); run.recordVirtualDamage(-5, false);
        run.hurt(24); run.applyRadiance(); run.recordArenaClear(); run.addScore(640);
        run.tickActive(); run.finish();
        CrewRewardReceipt accepted = receipt(run.id(), run.gangId());
        CompoundTag captured = RushDebrief.capture(run, RushDebrief.Outcome.BANKED, 3, accepted);
        check(captured.getInt("Schema") == 1 && captured.getUUID("Run").equals(run.id())
                        && captured.getUUID("Owner").equals(run.owner()) && captured.getLong("Score") == 640
                        && captured.getLong("ArenaClears") == 1 && captured.getLong("Headliners") == 1
                        && near(captured.getDouble("PrimaryDamage"), 7.5) && near(captured.getDouble("SecondaryDamage"), 5.25)
                        && captured.getString("ReturnRewardStatus").equals("accepted")
                        && captured.getCompound("ReturnReward").getInt("CrewXp") == accepted.crewXp()
                        && captured.getCompound("Evolution").getBoolean("Active"),
                "The finished run's debrief lost real metrics, final build or accepted return receipt");
        CompoundTag saved = captured.copy(), reloaded = RushDebrief.sanitize(saved);
        check(reloaded.equals(captured), "A valid debrief did not round-trip its bounded display schema");
        reloaded.getCompound("ReturnReward").putInt("CrewXp", 999);
        reloaded.getList("Artifacts", Tag.TAG_COMPOUND).getCompound(0).putInt("Rank", 99);
        check(saved.equals(captured) && accepted.crewXp() == 18 && run.rank(RushRun.Upgrade.RADIANCE) == 3,
                "Changing a returned debrief mutated saved state, a receipt or the original run");
        check(RushDebrief.capture(run, RushDebrief.Outcome.BANKED, 3, receipt(UUID.randomUUID(), run.gangId()))
                        .getString("ReturnRewardStatus").equals("not_recorded"), "Another run's receipt became this run's banked reward");
        CompoundTag future = captured.copy(); future.putInt("Schema", 9); CompoundTag futureBefore = future.copy();
        check(RushDebrief.sanitize(future).isEmpty() && future.equals(futureBefore), "An unknown future result was invented or rewritten");
        CompoundTag oversized = captured.copy(); ListTag tooMany = oversized.getList("Artifacts", Tag.TAG_COMPOUND);
        while (tooMany.size() < 4) tooMany.add(tooMany.getCompound(0).copy());
        check(RushDebrief.sanitize(oversized).isEmpty(), "An oversized artifact history escaped the three-family bound");
        CompoundTag damaged = captured.copy(); damaged.putDouble("PrimaryDamage", Double.NaN); damaged.putLong("Cards", -5);
        damaged.putString("Arbitrary", "not part of the schema"); damaged.getCompound("ReturnReward").putString("Source", "tag");
        CompoundTag safe = RushDebrief.sanitize(damaged);
        check(near(safe.getDouble("PrimaryDamage"), 0) && safe.getLong("Cards") == 0 && !safe.contains("Arbitrary")
                        && safe.getString("ReturnRewardStatus").equals("not_recorded") && !safe.contains("ReturnReward"),
                "Malformed metrics or an unrelated settlement became authoritative debrief content");
        pass(helper, "rush_debrief_rules");
    }

    private static RushRun run(long seed) { return new RushRun(UUID.randomUUID(), UUID.randomUUID(), seed, false, "jetsetcraft:dead_beat"); }
    private static void grant(RushRun run, RushRun.Upgrade upgrade, int ranks) {
        for (int i = 0; i < ranks; i++) check(run.grantReward(upgrade), "Could not earn rank for " + upgrade.name());
    }
    private static int firstBanishable(RushRun run) {
        for (int i = 0; i < run.offers().size(); i++) if (run.canBanish(i)) return i;
        return -1;
    }
    private static CrewRewardReceipt receipt(UUID id, String gang) {
        return new CrewRewardReceipt(id, "rush_return", gang, 10, 4, 18, 0, 2, null, "", 0, 0, 1, 1,
                CrewRewardReceipt.Training.NO_FOCUS, false);
    }
    private static boolean near(double a, double b) { return Math.abs(a - b) < .00001; }
    private static void check(boolean condition, String message) { if (!condition) throw new GameTestAssertException(message); }
    private static void pass(GameTestHelper helper, String marker) { System.out.println("JETSETCRAFT_GAMETEST_PASS " + marker); helper.succeed(); }
    private RushBuildPathGameTests() {}
}
