package net.ussm.the_shell.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;

// Here we make stations old & rusty
public class StationProcessors {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, TheShell.MOD_ID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<Rust>> RUST = PROCESSORS.register("station_rust", () -> () -> Rust.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<Flood>> FLOOD = PROCESSORS.register("station_flood", () -> () -> Flood.CODEC);

    // This bit of code randomly changes the block states
    public static class Rust extends StructureProcessor {
        public static final MapCodec<Rust> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("from").forGetter(p -> p.from),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("to").forGetter(p -> p.to),
                Codec.floatRange(0, 1).fieldOf("chance").forGetter(p -> p.chance)
        ).apply(i, Rust::new));

        private final Block from;
        private final Block to;
        private final float chance;

        public Rust(Block from, Block to, float chance) {
            this.from = from;
            this.to = to;
            this.chance = chance;
        }

        @Override
        public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo original,
                                                                  StructureTemplate.StructureBlockInfo current, StructurePlaceSettings settings) {
            if (!current.state().is(from) || settings.getRandom(current.pos()).nextFloat() >= chance) return current;
            return new StructureTemplate.StructureBlockInfo(current.pos(), copy(current.state(), to.defaultBlockState()), current.nbt());
        }

        @Override
        protected StructureProcessorType<?> getType() {
            return RUST.get();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static BlockState copy(BlockState from, BlockState to) {
        for (Property p : from.getProperties()) {
            if (to.hasProperty(p)) to = to.setValue(p, from.getValue(p));
        }
        return to;
    }

    // This bit of code randomly floods parts of the station
    public static class Flood extends StructureProcessor {
        public static final MapCodec<Flood> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.floatRange(0, 1).fieldOf("chance").forGetter(p -> p.chance)
        ).apply(i, Flood::new));

        private final float chance;

        public Flood(float chance) {
            this.chance = chance;
        }

        @Override
        public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo original,
                                                                  StructureTemplate.StructureBlockInfo current, StructurePlaceSettings settings) {
            if (RandomSource.create(Mth.getSeed(offset)).nextFloat() >= chance) return current;
            BlockState state = current.state();
            if (state.isAir()) return new StructureTemplate.StructureBlockInfo(current.pos(), Blocks.WATER.defaultBlockState(), null);
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                return new StructureTemplate.StructureBlockInfo(current.pos(), state.setValue(BlockStateProperties.WATERLOGGED, true), current.nbt());
            }
            return current;
        }

        @Override
        protected StructureProcessorType<?> getType() {
            return FLOOD.get();
        }
    }
}
