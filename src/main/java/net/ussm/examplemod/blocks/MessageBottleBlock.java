package net.ussm.examplemod.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ussm.examplemod.ExampleMod;

// Bouteille échouée : se pose couchée (debout en étant accroupi) dans 16 directions comme un crâne, le bouchon vers où
// on regarde. Clic droit les mains vides pour sortir ce qu'il y a dedans, avec un objet pour le mettre dedans.
public class MessageBottleBlock extends Block implements EntityBlock {
    public static final MapCodec<MessageBottleBlock> CODEC = simpleCodec(MessageBottleBlock::new);
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final BooleanProperty STANDING = BooleanProperty.create("standing");
    public static final ResourceKey<LootTable> TREASURE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "gameplay/message_bottle"));
    private static final VoxelShape[] LYING_SHAPES = shapes(1.5, 14.5, 5.5, 10.5, 5);
    private static final VoxelShape[] STANDING_SHAPES = shapes(5.5, 10.5, 5.5, 10.5, 13);

    public MessageBottleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0).setValue(STANDING, false));
    }

    // Contour pour chacune des 16 directions : le modèle de base a le bouchon à l'est (lacet 270°)
    private static VoxelShape[] shapes(double x1, double x2, double z1, double z2, double height) {
        VoxelShape[] shapes = new VoxelShape[16];
        for (int r = 0; r < 16; r++) {
            double angle = Math.toRadians(r * 22.5 - 270);
            double cos = Math.cos(angle), sin = Math.sin(angle);
            double minX = 16, maxX = 0, minZ = 16, maxZ = 0;
            for (double x : new double[]{x1 - 8, x2 - 8}) {
                for (double z : new double[]{z1 - 8, z2 - 8}) {
                    minX = Math.min(minX, 8 + x * cos - z * sin);
                    maxX = Math.max(maxX, 8 + x * cos - z * sin);
                    minZ = Math.min(minZ, 8 + x * sin + z * cos);
                    maxZ = Math.max(maxZ, 8 + x * sin + z * cos);
                }
            }
            shapes[r] = Block.box(minX, 0, minZ, maxX, height, maxZ);
        }
        return shapes;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, STANDING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int rotation = Mth.floor(context.getRotation() * 16.0F / 360.0F + 0.5F) & 15;
        return this.defaultBlockState().setValue(ROTATION, rotation).setValue(STANDING, context.isSecondaryUseActive());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(STANDING) ? STANDING_SHAPES : LYING_SHAPES)[state.getValue(ROTATION)];
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof MessageBottleBlockEntity bottle)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(this.asItem()) || bottle.getItems().stream().noneMatch(ItemStack::isEmpty)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            bottle.insert(stack.consumeAndReturn(1, player));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6F, 1.4F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MessageBottleBlockEntity bottle) || bottle.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            bottle.takeAll().forEach(item -> Block.popResource(level, pos, item));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MessageBottleBlockEntity bottle) {
            bottle.unpackLoot();
            Containers.dropContents(level, pos, bottle.getItems());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MessageBottleBlockEntity(pos, state);
    }
}
