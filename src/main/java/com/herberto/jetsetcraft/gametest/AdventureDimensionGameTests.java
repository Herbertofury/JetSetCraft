package com.herberto.jetsetcraft.gametest;

import com.herberto.jetsetcraft.JetSetCraft;
import com.herberto.jetsetcraft.adventure.AdventureWorld;
import com.herberto.jetsetcraft.liberty.LibertyWorld;
import com.herberto.jetsetcraft.rush.RushWorld;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Minimal data-defined Adventure dimension identity test.
 * Does not assert that maps, boss AI, return logic, or Adventure Mode are playable.
 */
@GameTestHolder(JetSetCraft.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdventureDimensionGameTests {
    @GameTest(template = "hoverboard_empty", timeoutTicks = 200)
    public static void separateAdventureDimensionRegistered(GameTestHelper helper) {
        ServerLevel source = helper.getLevel();
        ServerLevel adventure = source.getServer().getLevel(AdventureWorld.DIMENSION);
        ServerLevel rush = source.getServer().getLevel(RushWorld.DIMENSION);
        ServerLevel liberty = source.getServer().getLevel(LibertyWorld.DIMENSION);
        if (adventure == null) throw new GameTestAssertException("Missing Adventure Mode dimension JSON");
        if (!AdventureWorld.isAdventure(adventure) || AdventureWorld.isAdventure(source)
                || adventure == source || adventure == rush || adventure == liberty) {
            throw new GameTestAssertException("Adventure dimension is not independent of the source/Rush/Liberty worlds");
        }
        helper.succeed();
    }

    private AdventureDimensionGameTests() {}
}
