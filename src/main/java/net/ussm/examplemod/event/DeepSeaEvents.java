package net.ussm.examplemod.event;

import net.minecraft.network.chat.Component;
import net.ussm.examplemod.init.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.blocks.AbyssLightBlock;
import net.ussm.examplemod.entity.Submarine;
import net.ussm.examplemod.init.ModItems;
import net.ussm.examplemod.worldgen.TestChunkGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// tout ce qui se passe sous l'eau en profondeur : pression, scaphandre (respire + lourd), crane qui aide a respirer
@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public class DeepSeaEvents {
    public static final ResourceKey<DamageType> PRESSURE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "pressure"));
    private static final ResourceLocation HEAVY = ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "diving_boots_heavy");
    private static final ResourceLocation WALK = ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "diving_boots_walk");
    private static final Map<UUID, Integer> LAST_LEVEL = new HashMap<>();

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        boolean inWater = player.isInWater();
        heavyBoots(player, inWater && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.DIVING_BOOTS.get()));
        if (inWater) ventLift(player);

        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (player.isEyeInFluid(FluidTags.WATER)) breathe(player);
        if (player.tickCount % 20 == 0) pressure(serverPlayer, inWater);
    }

    // bottes lestees : on coule et on marche au fond, un peu plus lentement
    private static void heavyBoots(Player player, boolean on) {
        AttributeInstance walk = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (on) {
            if (walk != null) walk.addOrUpdateTransientModifier(new AttributeModifier(WALK, 0.6, AttributeModifier.Operation.ADD_VALUE));
            if (speed != null) speed.addOrUpdateTransientModifier(new AttributeModifier(HEAVY, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if (!player.onGround() && !player.getAbilities().flying) {
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, v.y - 0.035, v.z);
            }
        } else {
            if (walk != null) walk.removeModifier(WALK);
            if (speed != null) speed.removeModifier(HEAVY);
        }
    }

    // au-dessus d'une grille d'aeration sous l'eau, les bulles font remonter
    private static void ventLift(Player player) {
        BlockPos pos = player.blockPosition();
        for (int i = 0; i <= 6; i++) {
            BlockState state = player.level().getBlockState(pos.below(i));
            if (state.is(ModBlocks.AIR_VENT.get())) {
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, Math.min(0.5, v.y + 0.06), v.z);
                player.resetFallDistance();
                return;
            }
            if (!state.getFluidState().is(FluidTags.WATER)) return;
        }
    }

    // casque de scaphandre = air infini, crane du poisson-loupiote = on perd l'air 2x moins vite
    private static void breathe(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.is(ModItems.DIVING_HELMET.get()) || head.is(ModItems.DIVING_ABYSS_HELMET.get())) {
            player.setAirSupply(player.getMaxAirSupply());
        } else if (head.is(AbyssLightBlock.HEAD_LIGHTS) && player.tickCount % 2 == 0) {
            player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 1));
        }
    }

    public static boolean hasFullSuit(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return (head.is(ModItems.DIVING_HELMET.get()) || head.is(ModItems.DIVING_ABYSS_HELMET.get()))
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.DIVING_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.DIVING_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.DIVING_BOOTS.get());
    }

    // pression dans les abysses de l'atoll : lenteur de minage, puis nausee + lenteur 40 blocs plus bas, puis degats encore 40 plus bas
    private static void pressure(ServerPlayer player, boolean inWater) {
        int level = 0;
        if (inWater && player.level().dimension() == AtollTravel.ATOLL && !player.isCreative() && !player.isSpectator()
                && !hasFullSuit(player) && !(player.getVehicle() instanceof Submarine)) {
            double y = player.getY();
            int top = TestChunkGenerator.ABYSS;
            level = y < top - 80 ? 3 : y < top - 40 ? 2 : y < top ? 1 : 0;
        }
        int last = LAST_LEVEL.getOrDefault(player.getUUID(), 0);
        if (level == 3 && last < 3) player.displayClientMessage(Component.translatable("pressure.examplemod.crushed"), true);
        LAST_LEVEL.put(player.getUUID(), level);
        if (level >= 1) player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, true, false));
        if (level >= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, false));
        }
        if (level >= 3) {
            player.hurt(new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PRESSURE)), 2.0F);
        }
    }
}
