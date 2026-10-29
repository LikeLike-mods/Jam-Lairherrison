package net.ussm.the_shell.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.ussm.the_shell.TheShell;

import java.util.Optional;

public class PalmSaplingBlock extends SaplingBlock {
    public static final TreeGrower PALM_GROWER = new TreeGrower(TheShell.MOD_ID + ":palm", Optional.empty(),
            Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "palm"))),
            Optional.empty());
    public static final TreeGrower BANANA_GROWER = grower("banana");
    public static final TreeGrower DATE_PALM_GROWER = grower("date_palm");

    private static TreeGrower grower(String name) {
        return new TreeGrower(TheShell.MOD_ID + ":" + name, Optional.empty(),
                Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, name))), Optional.empty());
    }

    public static final MapCodec<PalmSaplingBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TreeGrower.CODEC.fieldOf("tree").forGetter(block -> block.treeGrower),
            propertiesCodec()
    ).apply(instance, PalmSaplingBlock::new));

    public PalmSaplingBlock(TreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    public MapCodec<? extends SaplingBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SAND) || super.mayPlaceOn(state, level, pos);
    }
}
