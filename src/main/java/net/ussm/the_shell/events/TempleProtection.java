package net.ussm.the_shell.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.ussm.the_shell.init.ModItems;
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

    // temples (by start chunk) whose protection was lifted because the conch was taken
    private static class Unlocked extends SavedData {
        final Set<Long> temples = new HashSet<>();

        static Unlocked load(CompoundTag tag, HolderLookup.Provider registries) {
            Unlocked data = new Unlocked();
            for (long l : tag.getLongArray("Temples")) data.temples.add(l);
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.put("Temples", new LongArrayTag(temples.stream().mapToLong(Long::longValue).toArray()));
            return tag;
        }
    }

    private static Unlocked unlocked(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(Unlocked::new, Unlocked::load, null), "the_shell_unlocked_temples");
    }

    private static StructureStart templeAt(ServerLevel level, BlockPos pos) {
        for (ResourceKey<Structure> key : TEMPLES) {
            Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(key);
            if (structure == null) continue;
            StructureStart temple = level.structureManager().getStructureWithPieceAt(pos, structure);
            if (temple.isValid()) return temple;
        }
        return null;
    }

    // conches each player had at the last check, so only a conch picked up inside the temple counts
    private static final Map<UUID, Integer> CONCHES = new HashMap<>();

    private static int conches(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) if (stack.is(ModItems.CONCH.get())) count += stack.getCount();
        for (ItemStack stack : player.getInventory().offhand) if (stack.is(ModItems.CONCH.get())) count += stack.getCount();
        return count;
    }

    // getting a conch (from a chest, a pedestal, the ground...) inside a temple lifts its protection;
    // walking in with one already on you does nothing
    @SubscribeEvent
    public static void conchTaken(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        int now = conches(player);
        Integer before = CONCHES.put(player.getUUID(), now);
        if (before == null || now <= before) return;
        ServerLevel level = player.serverLevel();
        StructureStart temple = templeAt(level, player.blockPosition());
        if (temple == null || !unlocked(level).temples.add(temple.getChunkPos().toLong())) return;
        unlocked(level).setDirty();
        player.displayClientMessage(Component.translatable("message.the_shell.temple.unlocked"), true);
    }

    // locked until every altar of the temple is solved; a temple without altars stays locked
    private static boolean isLocked(ServerLevel level, BlockPos pos) {
        if (!level.getGameRules().getBoolean(PROTECTION)) return false;
        StructureStart temple = templeAt(level, pos);
        if (temple == null || unlocked(level).temples.contains(temple.getChunkPos().toLong())) return false;
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

    private static boolean refuse(Player player, ServerLevel level, BlockPos pos) {
        if (player.isCreative() || !isLocked(level, pos)) return false;
        player.displayClientMessage(Component.translatable("message.the_shell.temple.locked"), true);
        return true;
    }

    // TNT and end crystals leave a protected temple alone
    @SubscribeEvent
    public static void noExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Map<Long, Boolean> locked = new HashMap<>();
        event.getAffectedBlocks().removeIf(p -> {
            StructureStart temple = templeAt(level, p);
            return temple != null && locked.computeIfAbsent(temple.getChunkPos().toLong(), k -> isLocked(level, p));
        });
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
