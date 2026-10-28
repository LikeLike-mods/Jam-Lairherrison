package net.ussm.examplemod.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.init.ModBlocks;

// lumiere invisible du crane, elle s'enleve toute seule quand y a plus personne
public class AbyssLightBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<AbyssLightBlock> CODEC = simpleCodec(AbyssLightBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final TagKey<Item> HEAD_LIGHTS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "head_lights"));
    public static final TagKey<EntityType<?>> LIGHT_EMITTERS = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "light_emitters"));

    public AbyssLightBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    public static boolean wearsLight(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(HEAD_LIGHTS);
    }

    // joueur avec un crane lumineux ou mob qui eclaire (loupiote)
    public static boolean emitsLight(Entity entity) {
        return entity.getType().is(LIGHT_EMITTERS) || entity instanceof Player player && wearsLight(player);
    }

    public static void placeAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState light = ModBlocks.ABYSS_LIGHT.get().defaultBlockState();
        if (state.isAir()) {
            level.setBlock(pos, light, Block.UPDATE_ALL);
        } else if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
            level.setBlock(pos, light.setValue(WATERLOGGED, true), Block.UPDATE_ALL);
        }
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 10);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getEntitiesOfClass(Entity.class, new AABB(pos).inflate(2.0), AbyssLightBlock::emitsLight).isEmpty()) {
            level.setBlock(pos, state.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.scheduleTick(pos, this, 10);
        }
    }
}
