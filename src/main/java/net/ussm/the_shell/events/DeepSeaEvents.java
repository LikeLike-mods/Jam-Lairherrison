package net.ussm.the_shell.events;

import net.minecraft.network.chat.Component;
import net.ussm.the_shell.init.ModBlocks;
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
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.AbyssLightBlock;
import net.ussm.the_shell.entites.Submarine;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class DeepSeaEvents {
    public static final ResourceKey<DamageType> PRESSURE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "pressure"));
    private static final ResourceLocation HEAVY = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "diving_boots_heavy");
    private static final ResourceLocation WALK = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "diving_boots_walk");
    private static final Map<UUID, Integer> LAST_LEVEL = new HashMap<>();

    @SubscribeEvent
    public static void jump(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.onGround() || !player.isInWater() || !isJumping(player)) return;
        if (!net.ussm.the_shell.items.DivingBootsItem.isWeighted(player.getItemBySlot(EquipmentSlot.FEET))) return;
        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x, 0.34, v.z);
        JUMP_TICK.put(player, player.tickCount);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        boolean inWater = player.isInWater();
        heavyBoots(player, inWater && net.ussm.the_shell.items.DivingBootsItem.isWeighted(player.getItemBySlot(EquipmentSlot.FEET)));
        if (inWater) ventLift(player);

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (player.isEyeInFluid(FluidTags.WATER)) breathe(player);
        refillOxygen(player, inWater && player.level().getBlockState(player.blockPosition().below()).is(ModBlocks.AIR_VENT.get()));

        if (player.tickCount % 20 == 0) pressure(serverPlayer, inWater);
    }

    private static final java.lang.reflect.Field JUMPING = jumpingField();
    private static final Map<Player, Integer> JUMP_TICK = new java.util.WeakHashMap<>();

    private static java.lang.reflect.Field jumpingField() {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("jumping");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static boolean isJumping(Player player) {
        try {
            return JUMPING != null && JUMPING.getBoolean(player);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static void heavyBoots(Player player, boolean on) {
        AttributeInstance walk = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (on) {
            if (walk != null) walk.addOrUpdateTransientModifier(new AttributeModifier(WALK, 0.6, AttributeModifier.Operation.ADD_VALUE));
            if (speed != null) speed.addOrUpdateTransientModifier(new AttributeModifier(HEAVY, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            // on coule, sauf juste apres un saut (sinon on ne decolle pas)
            boolean saut = player.tickCount - JUMP_TICK.getOrDefault(player, -100) < 3;
            if (!player.onGround() && !player.getAbilities().flying && !saut) {
                Vec3 v = player.getDeltaMovement();
                player.setDeltaMovement(v.x, v.y - 0.05, v.z);
            }
        } else {
            if (walk != null) walk.removeModifier(WALK);
            if (speed != null) speed.removeModifier(HEAVY);
        }
    }

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

    private static void breathe(Player player) {

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);

        if (head.is(ModItems.DIVING_HELMET.get()) || head.is(ModItems.DIVING_ABYSS_HELMET.get())) {
            // le casque respire dans la bouteille d'oxygene, quand elle est vide on se noie comme d'habitude
            int oxygen = player.getData(net.ussm.the_shell.init.ModAttachments.OXYGEN);
            if (oxygen > 0) {
                player.setAirSupply(player.getMaxAirSupply());
                if (!(player.getVehicle() instanceof Submarine)) setOxygen(player, oxygen - 1);
            }
        } else if (head.is(AbyssLightBlock.HEAD_LIGHTS) && player.tickCount % 2 == 0) {
            player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 1));
        }
    }

    // la bouteille se remplit des qu'on a la tete hors de l'eau (surface, station, cabine du sous-marin) ou dans les bulles d'une grille
    private static void refillOxygen(Player player, boolean bubbles) {
        int oxygen = player.getData(net.ussm.the_shell.init.ModAttachments.OXYGEN);
        if (oxygen >= net.ussm.the_shell.init.ModAttachments.MAX_OXYGEN) return;
        if (!player.isEyeInFluid(FluidTags.WATER) || bubbles || player.getVehicle() instanceof Submarine) setOxygen(player, oxygen + 24);
    }

    private static void setOxygen(Player player, int oxygen) {
        oxygen = Math.max(0, Math.min(net.ussm.the_shell.init.ModAttachments.MAX_OXYGEN, oxygen));
        int old = player.getData(net.ussm.the_shell.init.ModAttachments.OXYGEN);
        player.setData(net.ussm.the_shell.init.ModAttachments.OXYGEN, oxygen);
        if (old / 40 != oxygen / 40 || oxygen == 0) player.syncData(net.ussm.the_shell.init.ModAttachments.OXYGEN);
    }

    // dans le sous-marin les poissons ne peuvent pas nous toucher
    @SubscribeEvent
    public static void subShield(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (event.getEntity().getVehicle() instanceof Submarine && event.getSource().getEntity() != null) event.setCanceled(true);
    }

    public static boolean hasFullSuit(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        return (head.is(ModItems.DIVING_HELMET.get()) || head.is(ModItems.DIVING_ABYSS_HELMET.get()))
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.DIVING_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.DIVING_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.DIVING_BOOTS.get());
    }

    private static void pressure(ServerPlayer player, boolean inWater) {
        int level = 0;
        if (inWater && !player.isCreative() && !player.isSpectator() && !hasFullSuit(player) && !(player.getVehicle() instanceof Submarine)) {
            double y = player.getY();
            int top = TestChunkGenerator.ABYSS;
            if (player.level().dimension() == AtollTravel.ATOLL) level = y < top - 80 ? 3 : y < top - 40 ? 2 : y < top ? 1 : 0;
            else if (player.level().dimension() == AbyssTravel.ABYSS) level = y < net.ussm.the_shell.worldgen.AbyssTerrain.FLOOR - 16 ? 3 : 2;
        }

        int last = LAST_LEVEL.getOrDefault(player.getUUID(), 0);
        LAST_LEVEL.put(player.getUUID(), level);

        if (level == 3 && last < 3) {
            player.displayClientMessage(Component.translatable("pressure.the_shell.crushed"), true);
        }

        if (level >= 1) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, true, false));
        }
        if (level >= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, false));
        }
        if (level == 3) {
            player.hurt(new DamageSource(player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PRESSURE)), 2.0F);
        }
    }
}
