package net.ussm.examplemod.item;

import net.ussm.examplemod.init.ModComponents;
import net.ussm.examplemod.init.ModSounds;
import net.ussm.examplemod.client.LightsaberRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lightsabers
 *  - RMD            : on/off
 *  - Shift + RMD    : switch
 *  - When Off       : 0 dmg bonus
 */

public class LightsaberItem extends SwordItem implements GeoItem {
    private static final RawAnimation BLADE_ON = RawAnimation.begin().thenPlayAndHold("on");
    private static final RawAnimation BLADE_OFF = RawAnimation.begin().thenPlayAndHold("off");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String modelName;
    private final String textureName;
    private final ItemAttributeModifiers activeAttributes;
    private final Supplier<? extends Item> nextForm;

    public LightsaberItem(String modelName, int attackDamage, float attackSpeed,
                           Supplier<? extends Item> nextForm, Item.Properties properties) {
        this(modelName, modelName, attackDamage, attackSpeed, nextForm, properties);
    }

    public LightsaberItem(String modelName, String textureName, int attackDamage, float attackSpeed,
                               Supplier<? extends Item> nextForm, Item.Properties properties) {
        super(Tiers.NETHERITE, properties);
        this.modelName = modelName;
        this.textureName = textureName;
        this.activeAttributes = SwordItem.createAttributes(Tiers.NETHERITE, attackDamage, attackSpeed);
        this.nextForm = nextForm;

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(ModComponents.ACTIVE, true);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide())
            return InteractionResultHolder.consume(stack);

        if (player.isShiftKeyDown()) {
            ItemStack next = stack.transmuteCopy(this.nextForm.get());
            next.set(ModComponents.ACTIVE, true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.LIGHTSABER_IGNITE.get(), SoundSource.PLAYERS, 0.6F, 1.0F);

            return InteractionResultHolder.consume(next);
        }

        boolean active = !isActive(stack);
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
