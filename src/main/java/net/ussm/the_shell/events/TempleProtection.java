package net.ussm.the_shell.events;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.NotchAltarBlock;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class TempleProtection {
    // /gamerule theShellTempleProtection false turns the protection off
    public static final GameRules.Key<GameRules.BooleanValue> PROTECTION =
            GameRules.register("theShellTempleProtection", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    // blocks that can always be broken inside a temple (traps, webs...)
    private static final TagKey<Block> BREAKABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "temple_breakable"));
    private static final List<ResourceKey<Structure>> TEMPLES = List.of(
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace("jungle_pyramid")),
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "jungle_temple")));

    // locked until every altar of the temple is solved; a temple without altars stays locked
    private static boolean isLocked(ServerLevel level, BlockPos pos) {
        if (!level.getGameRules().getBoolean(PROTECTION)) return false;
        for (ResourceKey<Structure> key : TEMPLES) {
            Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(key);
            if (structure == null) continue;
            StructureStart temple = level.structureManager().getStructureWithPieceAt(pos, structure);
            if (!temple.isValid()) continue;
            boolean altars = false;
            for (BlockPos p : BlockPos.betweenClosed(temple.getBoundingBox().minX(), temple.getBoundingBox().minY(), temple.getBoundingBox().minZ(),
                    temple.getBoundingBox().maxX(), temple.getBoundingBox().maxY(), temple.getBoundingBox().maxZ())) {
                BlockState s = level.getBlockState(p);
                if (!(s.getBlock() instanceof NotchAltarBlock)) continue;
                if (!s.getValue(NotchAltarBlock.SOLVED)) return true;
                altars = true;
            }
            return !altars;
        }
        return false;
    }

    private static boolean refuse(Player player, ServerLevel level, BlockPos pos) {
        if (player.isCreative() || !isLocked(level, pos)) return false;
        player.displayClientMessage(Component.translatable("message.the_shell.temple.locked"), true);
        return true;
    }

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(BREAKABLE)) return;
        if (event.getLevel() instanceof ServerLevel level && refuse(event.getPlayer(), level, event.getPos())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void blockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Player player && refuse(player, level, event.getPos())) event.setCanceled(true);
    }
}
