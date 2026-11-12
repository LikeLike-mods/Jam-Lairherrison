package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.blocks.DecorBlock;
import net.ussm.the_shell.entites.Submarine;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.items.ChargingPlugItem;

// like an electric car charger: grab the plug, walk the cable to a submarine, plug it in
public class SubmarineChargerBlock extends DecorBlock {
    public static final BooleanProperty IN_USE = BooleanProperty.create("in_use");   // plug out of its holder

    public SubmarineChargerBlock(Properties properties) {
        super(properties, Block.box(3, 0, 3, 13, 16, 13));
        this.registerDefaultState(this.defaultBlockState().setValue(IN_USE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IN_USE);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) level.scheduleTick(pos, this, 20);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) level.scheduleTick(pos, this, 20);
        if (state.getValue(IN_USE)) {
            unplug(level, pos);
            player.displayClientMessage(Component.translatable("message.the_shell.charger.unplugged"), true);
        } else if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ChargingPlugItem.forCharger(pos));
            level.setBlock(pos, state.setValue(IN_USE, true), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.BLOCKS, 0.8F, 1.3F);
            int power = Energy.power(level, pos);
            player.displayClientMessage(power > 0 ? Component.translatable("message.the_shell.energy.power", power)
                    : Component.translatable("message.the_shell.energy.none"), true);
        }
        return InteractionResult.CONSUME;
    }

    // everything back in the holder: plugged submarines and plugs still in hand
    public static void unplug(Level level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(ChargingPlugItem.REACH + 4.0);
        for (Submarine sub : level.getEntitiesOfClass(Submarine.class, area, s -> pos.equals(s.getCharger()))) sub.setCharger(null);
        for (Player p : level.getEntitiesOfClass(Player.class, area)) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack held = p.getItemInHand(hand);
                if (held.getItem() instanceof ChargingPlugItem && pos.equals(held.get(ModComponents.CHARGER_POS.get()))) held.setCount(0);
            }
        }
        putBack(level, pos);
    }

    public static void putBack(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SubmarineChargerBlock) || !state.getValue(IN_USE)) return;
        level.setBlock(pos, state.setValue(IN_USE, false), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.BLOCKS, 0.8F, 0.9F);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, 20);
        if (!state.getValue(IN_USE)) return;
        Vec3 center = Vec3.atCenterOf(pos);
        int power = -1;
        for (Submarine sub : level.getEntitiesOfClass(Submarine.class, new AABB(pos).inflate(ChargingPlugItem.REACH + 4.0), s -> pos.equals(s.getCharger()))) {
            if (sub.distanceToSqr(center) > ChargingPlugItem.REACH * ChargingPlugItem.REACH) {   // drove off with the cable
                sub.setCharger(null);
                putBack(level, pos);
                continue;
            }
            if (power < 0) power = Energy.power(level, pos);
            if (power == 0 || sub.getEnergy() >= Submarine.MAX_ENERGY) continue;
            sub.setEnergy(Math.min(Submarine.MAX_ENERGY, sub.getEnergy() + power * 10));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, sub.getX(), sub.getY() + sub.getBbHeight() * 0.5, sub.getZ(), 3, 0.3, 0.3, 0.3, 0.05);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            AABB area = new AABB(pos).inflate(ChargingPlugItem.REACH + 4.0);
            for (Submarine sub : level.getEntitiesOfClass(Submarine.class, area, s -> pos.equals(s.getCharger()))) sub.setCharger(null);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
