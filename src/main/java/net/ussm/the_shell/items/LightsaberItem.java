package net.ussm.the_shell.items;

import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.init.ModSounds;
import net.ussm.the_shell.client.renderers.LightsaberRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.ussm.the_shell.init.ModEnchantments;
import net.ussm.the_shell.init.ModTags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lightsabers
 *  - RMD            : on/off
 *  - Shift + RMD    : switch
 *  - When Off       : 0 dmg bonus
 */

public class LightsaberItem extends Item implements GeoItem {
    private static final RawAnimation BLADE_ON = RawAnimation.begin().thenPlayAndHold("on");
    private static final RawAnimation BLADE_OFF = RawAnimation.begin().thenPlayAndHold("off");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String modelName;
    private final String textureName;
    private final ItemAttributeModifiers activeAttributes;
    private final Supplier<? extends Item> nextForm;

    public LightsaberItem(String modelName, int attackDamageBonus, float attackSpeed,
                           Supplier<? extends Item> nextForm, Item.Properties properties) {
        this(modelName, modelName, attackDamageBonus, attackSpeed, nextForm, properties);
    }

    public LightsaberItem(String modelName, String textureName, int attackDamageBonus, float attackSpeed,
                               Supplier<? extends Item> nextForm, Item.Properties properties) {
        super(properties.component(DataComponents.TOOL, SwordItem.createToolProperties()));
        this.modelName = modelName;
        this.textureName = textureName;
        this.activeAttributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamageBonus, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
        this.nextForm = nextForm;

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.the_shell.lightsaber.change_mode").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.the_shell.lightsaber.recharge").withStyle(ChatFormatting.DARK_AQUA));
    }

    public static final int MAX_USES = 4000;
    public static final int RECHARGE = 750;

    // at 0 the blade goes out by itself (the item keeps 1 hidden point so it never breaks)
    public static boolean isExhausted(ItemStack stack) {
        return stack.getDamageValue() >= MAX_USES;
    }

    public static boolean isActive(ItemStack stack) {
        return !isExhausted(stack) && stack.getOrDefault(ModComponents.ACTIVE, true);
    }

    private static void warnIfExhausted(ItemStack stack, LivingEntity user) {
        if (isExhausted(stack) && user instanceof Player player)
            player.displayClientMessage(Component.translatable("message.the_shell.kyber.exhausted"), true);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return Math.min(amount, Math.max(0, MAX_USES - stack.getDamageValue()));
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    // right-click in the inventory with a kyber crystal on the cursor
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !other.is(ModTags.KYBER_CRYSTALS) || stack.getDamageValue() == 0) return false;
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - RECHARGE - 250 * ModEnchantments.level(player.level(), stack, ModEnchantments.RESONANCE)));
        other.shrink(1);
        player.playSound(ModSounds.LIGHTSABER_IGNITE.get(), 0.6F, 1.2F);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide && isActive(stack) && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, miner, EquipmentSlot.MAINHAND);
            warnIfExhausted(stack, miner);
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide())
            return InteractionResultHolder.consume(stack);

        if (player.isShiftKeyDown()) {
            ItemStack next = stack.transmuteCopy(this.nextForm.get());
            next.set(ModComponents.ACTIVE, true);
            // no spamming: both forms wait a second before the next use
            player.getCooldowns().addCooldown(this, 20);
            player.getCooldowns().addCooldown(next.getItem(), 20);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.LIGHTSABER_IGNITE.get(), SoundSource.PLAYERS, 0.6F, 1.0F);

            return InteractionResultHolder.consume(next);
        }

        boolean active = !isActive(stack);
        if (active && isExhausted(stack)) {
            player.displayClientMessage(Component.translatable("message.the_shell.kyber.exhausted"), true);
            player.getCooldowns().addCooldown(this, 20);
            return InteractionResultHolder.consume(stack);
        }
        player.getCooldowns().addCooldown(this, 15);
        stack.set(ModComponents.ACTIVE, active);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                active ? ModSounds.LIGHTSABER_IGNITE.get() : ModSounds.LIGHTSABER_EXTINGUISH.get(),
                SoundSource.PLAYERS, 0.6F, 1.0F);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !(newStack.getItem() instanceof LightsaberItem);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return isActive(stack) ? this.activeAttributes : ItemAttributeModifiers.EMPTY;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }

    // mending would bypass the rule that kyber tools only recharge with a crystal
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.MENDING) && super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (isActive(stack)) {
            stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
            warnIfExhausted(stack, attacker);
        }
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(ability);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (level instanceof ServerLevel serverLevel)
            GeoItem.getOrAssignId(stack, serverLevel);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "blade", 0, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            boolean active = stack == null || isActive(stack);

            return state.setAndContinue(active ? BLADE_ON : BLADE_OFF);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private LightsaberRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new LightsaberRenderer(
                            LightsaberItem.this.modelName, LightsaberItem.this.textureName);

                return this.renderer;
            }
        });
    }
}
