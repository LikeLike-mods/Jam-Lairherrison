package net.ussm.examplemod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.init.ModAttachments;
import net.ussm.examplemod.worldgen.TestChunkGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// on se noie dans l'ocean -> on s'echoue sur l'atoll. pour repartir faut la conque
@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public class AtollTravel {
    public static final ResourceKey<Level> ATOLL = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "test_dim"));
    private static final int DRIFT_TICKS = 50;
    private static final Map<UUID, Integer> DRIFTING = new HashMap<>();

    @SubscribeEvent
    public static void onDrowning(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getSource().is(DamageTypes.DROWN)) return;
        if (DRIFTING.containsKey(player.getUUID())) {
            event.setCanceled(true);
            return;
        }
        if (player.level().dimension() != Level.OVERWORLD || !player.level().getBiome(player.blockPosition()).is(BiomeTags.IS_OCEAN)) return;
        if (event.getAmount() < player.getHealth() + player.getAbsorptionAmount()) return;
        event.setCanceled(true);
        // a la surface sinon on se renoie direct
        BlockPos surface = player.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
        setReturn(player, GlobalPos.of(Level.OVERWORLD, surface));
        DRIFTING.put(player.getUUID(), DRIFT_TICKS);
        player.setAirSupply(player.getMaxAirSupply());
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DRIFT_TICKS + 30, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, DRIFT_TICKS + 60, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DRIFT_TICKS + 60, 3, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, DRIFT_TICKS + 20, 0, false, false));
        title(player, "atoll.examplemod.drift.title", "atoll.examplemod.drift.subtitle");
    }

    @SubscribeEvent
    public static void drift(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Integer left = DRIFTING.get(player.getUUID());
        if (left == null) return;
        player.setAirSupply(player.getMaxAirSupply());
        if (left > 0) {
            DRIFTING.put(player.getUUID(), left - 1);
            return;
        }
        DRIFTING.remove(player.getUUID());
        ServerLevel atoll = player.server.getLevel(ATOLL);
        if (atoll == null) return;
        teleport(player, atoll, surface(atoll, findBeach(player.getBlockX(), player.getBlockZ())));
        player.setHealth(Math.max(player.getHealth(), 6.0F));
        atoll.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.PLAYERS, 1.0F, 0.8F);
        atoll.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.6F, 0.7F);
        title(player, "atoll.examplemod.shipwreck.title", "atoll.examplemod.shipwreck.subtitle");
    }

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

    // plage la plus proche (hauteur entre 65 et 67)
    private static BlockPos findBeach(int x0, int z0) {
        for (int r = 0; r <= 4096; r += 16) {
            for (int dx = -r; dx <= r; dx += 16) {
                for (int dz = -r; dz <= r; dz += 16) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    float h = TestChunkGenerator.getHeight(x0 + dx, z0 + dz, 0f);
                    if (h >= 65f && h <= 67f) {
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

    private static void title(ServerPlayer player, String title, String subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(subtitle)));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(title)));
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
