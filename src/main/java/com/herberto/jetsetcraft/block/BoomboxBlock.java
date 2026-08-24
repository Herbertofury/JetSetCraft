package com.herberto.jetsetcraft.block;

import com.herberto.jetsetcraft.blockentity.BoomboxBlockEntity;
import com.herberto.jetsetcraft.gang.HeadGangTargetResolver;
import com.herberto.jetsetcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation") // Minecraft 1.20.1 requires these deprecated BlockBehaviour override hooks.
public final class BoomboxBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Shapes.or(
            box(1.0D, 0.0D, 2.0D, 15.0D, 9.0D, 14.0D),
            box(3.0D, 9.0D, 4.0D, 13.0D, 12.0D, 12.0D),
            box(5.0D, 12.0D, 6.0D, 11.0D, 14.0D, 10.0D));

    public BoomboxBlock() {
        super(BlockBehaviour.Properties.of().strength(3.5F, 7.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        BlockEntity raw = level.getBlockEntity(pos);
        if (!(raw instanceof BoomboxBlockEntity boombox)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && held.isEmpty()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            ItemStack removed = boombox.removeTarget();
            if (removed.isEmpty()) {
                if (boombox.isChallengeActive()) {
                    boombox.cancelChallenge(true);
                    player.displayClientMessage(Component.translatable("message.jetsetcraft.boombox_cancelled"), true);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            }
            if (!player.addItem(removed)) player.drop(removed, false);
            player.displayClientMessage(Component.translatable("message.jetsetcraft.boombox_target_removed"), true);
            return InteractionResult.CONSUME;
        }

        if (!held.isEmpty() && HeadGangTargetResolver.resolve(held).isPresent()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!boombox.targetStack().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.jetsetcraft.boombox_target_occupied"), true);
                return InteractionResult.CONSUME;
            }
            ItemStack inserted = held.copy();
            inserted.setCount(1);
            if (!boombox.setTarget(inserted)) return InteractionResult.PASS;
            if (!player.getAbilities().instabuild) held.shrink(1);
            boombox.resolvedTarget().ifPresent(target -> player.displayClientMessage(
                    Component.translatable("message.jetsetcraft.boombox_tuned",
                            boombox.displayGangName(target.gangId())), true));
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            boombox.toggleChallenge(serverPlayer);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity raw = level.getBlockEntity(pos);
            if (raw instanceof BoomboxBlockEntity boombox && !level.isClientSide) {
                boombox.cancelChallengeForRemoval();
                ItemStack target = boombox.removeTargetForRemoval();
                if (!target.isEmpty()) net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), target);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity raw = level.getBlockEntity(pos);
        if (!(raw instanceof BoomboxBlockEntity boombox)) return 0;
        if (boombox.isChallengeActive()) return 15;
        return boombox.targetStack().isEmpty() ? 0 : 7;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoomboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.BOOMBOX.get(), BoomboxBlockEntity::serverTick);
    }
}
