package net.ussm.the_shell.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModAttachments;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

import java.util.Optional;

public class AtollTravel {
    public static final ResourceKey<Level> ATOLL = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "test_dim"));

    public static void blowConch(ServerPlayer player) {
        ServerLevel atoll = player.server.getLevel(ATOLL);
        if (atoll == null) return;
        if (player.level().dimension() == ATOLL) {
            setAtollPos(player, player.blockPosition());
            GlobalPos back = getReturn(player).orElse(null);
            ServerLevel level = back == null ? null : player.server.getLevel(back.dimension());
            if (level == null) {
                level = player.server.overworld();
                back = GlobalPos.of(Level.OVERWORLD, level.getSharedSpawnPos());
            }
            teleport(player, level, back.pos());
        } else {
            setReturn(player, GlobalPos.of(player.level().dimension(), player.blockPosition()));
            BlockPos last = getAtollPos(player).orElse(null);
            teleport(player, atoll, last != null ? last : surface(atoll, findBeach(player.getBlockX(), player.getBlockZ())));
        }
    }

    private static BlockPos findBeach(int x0, int z0) {
        for (int r = 0; r <= 4096; r += 16) {
            for (int dx = -r; dx <= r; dx += 16) {
                for (int dz = -r; dz <= r; dz += 16) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    float h = TestChunkGenerator.getHeight(x0 + dx, z0 + dz);
                    if (h >= TestChunkGenerator.SEA + 1 && h <= TestChunkGenerator.SEA + 3) {
                        return new BlockPos(x0 + dx, 0, z0 + dz);
                    }
                }
            }
        }
        return new BlockPos(x0, 0, z0);
    }

    private static BlockPos surface(ServerLevel level, BlockPos pos) {
        level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()), pos.getZ());
    }

    private static void teleport(ServerPlayer player, ServerLevel level, BlockPos pos) {
        level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.resetFallDistance();
    }

    private static Optional<GlobalPos> getReturn(ServerPlayer player) {
        return GlobalPos.CODEC.parse(NbtOps.INSTANCE, player.getData(ModAttachments.ATOLL_TRAVEL).get("return")).result();
    }

    private static void setReturn(ServerPlayer player, GlobalPos pos) {
        CompoundTag tag = player.getData(ModAttachments.ATOLL_TRAVEL).copy();
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).result().ifPresent(t -> tag.put("return", t));
        player.setData(ModAttachments.ATOLL_TRAVEL, tag);
    }

    private static Optional<BlockPos> getAtollPos(ServerPlayer player) {
        CompoundTag tag = player.getData(ModAttachments.ATOLL_TRAVEL);
        return tag.contains("atoll") ? Optional.of(BlockPos.of(tag.getLong("atoll"))) : Optional.empty();
    }

    private static void setAtollPos(ServerPlayer player, BlockPos pos) {
        CompoundTag tag = player.getData(ModAttachments.ATOLL_TRAVEL).copy();
        tag.putLong("atoll", pos.asLong());
        player.setData(ModAttachments.ATOLL_TRAVEL, tag);
    }
}
