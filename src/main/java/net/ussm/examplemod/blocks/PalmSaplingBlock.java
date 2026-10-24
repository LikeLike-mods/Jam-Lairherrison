package net.ussm.examplemod.blocks;

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
import net.ussm.examplemod.ExampleMod;

import java.util.Optional;

// Pousse de palmier : fait pousser le même palmier que la génération du monde (feature examplemod:palm),
// et se plante aussi dans le sable, pas seulement dans la terre.
public class PalmSaplingBlock extends SaplingBlock {
    public static final TreeGrower PALM_GROWER = new TreeGrower(ExampleMod.MOD_ID + ":palm", Optional.empty(),
            Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "palm"))),
            Optional.empty());
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
