package net.ussm.the_shell.entites;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.blocks.abyss.AbyssLightBlock;
import net.ussm.the_shell.events.AbyssTravel;
import net.ussm.the_shell.events.SubmarineInterior;
import net.ussm.the_shell.init.ModEntities;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.init.ModTags;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

// Yellow submarine
public class Submarine extends VehicleEntity implements GeoEntity {
    private static final RawAnimation PROPELLER = RawAnimation.begin().thenLoop("move.propeller");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int interiorId = -1;
    public static final int MAX_ENERGY = 6000;
    public static boolean freeGaze, catchUp;
    private static final EntityDataAccessor<Integer> TIER =
            SynchedEntityData.defineId(Submarine.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ENERGY =
            SynchedEntityData.defineId(Submarine.class, EntityDataSerializers.INT);

    public Submarine(EntityType<? extends Submarine> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public Submarine(Level level, double x, double y, double z, float yaw) {
        this(ModEntities.SUBMARINE.get(), level);
        this.setPos(x, y, z);
        this.setYRot(yaw);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ENERGY, MAX_ENERGY);
        builder.define(TIER, 0);
    }

    public int getTier() {
        return this.entityData.get(TIER);
    }

    public void setTier(int tier) {
        this.entityData.set(TIER, tier);
    }

    public static int kitTier(ItemStack stack) {
        if (stack.is(ModItems.SUBMARINE_UPGRADE_IRON.get())) return 1;
        if (stack.is(ModItems.SUBMARINE_UPGRADE_DIAMOND.get())) return 2;
        if (stack.is(ModItems.SUBMARINE_UPGRADE_NETHERITE.get())) return 3;
        return 0;
    }

    public int getEnergy() {
        return this.entityData.get(ENERGY);
    }

    public void setEnergy(int energy) {
        this.entityData.set(ENERGY, Math.max(0, Math.min(MAX_ENERGY, energy)));
    }

    public static int fuelValue(ItemStack stack) {
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 150;
        if (stack.is(Items.REDSTONE)) return 200;
        if (stack.is(Items.REDSTONE_BLOCK)) return 1800;
        if (stack.is(ModTags.KYBER_CRYSTALS)) return MAX_ENERGY;
        return 0;
    }

    public int getInteriorId() {
        return interiorId;
    }

    public void setInteriorId(int id) {
        this.interiorId = id;
    }

    private double lerpX, lerpY, lerpZ;
    private float lerpYRot;
    private int lerpSteps;

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpSteps = Math.max(steps, 6);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (this.level() instanceof ServerLevel server) {
            server.getChunkSource().broadcast(this, new ClientboundTeleportEntityPacket(this));
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        if (reason.shouldDestroy()) SubmarineInterior.lost(this);
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(1.5, 0.8, 1.5);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 side = Vec3.directionFromRotation(0.0F, getYRot() + 90.0F).scale(2.2);
        for (Vec3 attempt : new Vec3[]{position().add(side), position().subtract(side), position().add(0.0, 2.0, 0.0)}) {
            if (level().noCollision(passenger, passenger.getDimensions(Pose.STANDING).makeBoundingBox(attempt))) return attempt;
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) return false;
        if (!(source.getEntity() instanceof Player player) || !player.isShiftKeyDown()) return false;
        if (player.getAbilities().instabuild && !this.level().isClientSide && !this.isRemoved()) {
            destroy(source);
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void destroy(DamageSource source) {
        this.kill();
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) return;
        ItemStack stack = new ItemStack(getDropItem());
        stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        CompoundTag tag = new CompoundTag();
        tag.putInt("Tier", getTier());
        tag.putInt("Energy", getEnergy());
        if (interiorId >= 0) {
            tag.putInt("Interior", interiorId);
            SubmarineInterior.store(this);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        this.spawnAtLocation(stack);
    }

    @Override
    protected Item getDropItem() {
        return ModItems.SUBMARINE.get();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Interior")) interiorId = tag.getInt("Interior");
        if (tag.contains("Energy")) setEnergy(tag.getInt("Energy"));
        setTier(tag.getInt("Tier"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Interior", interiorId);
        tag.putInt("Energy", getEnergy());
        tag.putInt("Tier", getTier());
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved() && !this.isVehicle();
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        return this.hasPassenger(attacker);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof Player player ? player : null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, 0.45, 0.31).yRot((float) Math.toRadians(-this.getYRot()));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int kit = kitTier(stack);
        if (kit > 0) {
            if (!this.level().isClientSide) {
                if (kit == getTier() + 1) {
                    setTier(kit);
                    stack.consume(1, player);
                    SubmarineInterior.upgrade(this);
                    this.level().playSound(null, this.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    player.displayClientMessage(Component.translatable("message.the_shell.submarine.upgraded", kit), true);
                } else if (kit > getTier()) {
                    player.displayClientMessage(Component.translatable("message.the_shell.submarine.upgrade_order"), true);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        int fuel = fuelValue(stack);
        if (fuel > 0) {
            if (!this.level().isClientSide && getEnergy() < MAX_ENERGY) {
                setEnergy(getEnergy() + fuel);
                stack.consume(1, player);
                this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.NEUTRAL, 0.6F, 1.4F);
                player.displayClientMessage(Component.translatable("message.the_shell.submarine.energy", getEnergy() * 100 / MAX_ENERGY), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (this.isVehicle()) SubmarineInterior.enter(serverPlayer, this);
            else if (player.isSecondaryUseActive()) SubmarineInterior.seatLater(serverPlayer, this);
            else player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        AbyssTravel.check(this);
        if (!this.level().isClientSide && interiorId >= 0 && this.tickCount % 40 == 0) {
            setEnergy(getEnergy() + SubmarineInterior.takeFuel(this));
            SubmarineInterior.remember(this);
        }
        if (this.isControlledByLocalInstance()) {
            Vec3 motion = this.getDeltaMovement();
            if (this.isInWater()) {
                motion = motion.scale(0.9);
                if (this.getControllingPassenger() instanceof Player driver && getEnergy() > 0) {
                    boolean free = this.level().isClientSide && freeGaze;
                    if (!free) {
                        float gap = Mth.wrapDegrees(driver.getYRot() - this.getYRot());
                        if (catchUp && this.level().isClientSide) {
                            this.setYRot(this.getYRot() + Mth.clamp(gap, -9.0F, 9.0F));
                            if (Math.abs(gap) < 9.0F) catchUp = false;
                        } else {
                            this.setYRot(driver.getYRot());
                        }
                    }
                    Vec3 look = free || catchUp ? Vec3.directionFromRotation(0.0F, this.getYRot()) : driver.getLookAngle();
                    Vec3 left = new Vec3(look.z, 0.0, -look.x).normalize();
                    double boost = 1.0 + 0.15 * getTier();
                    motion = motion.add(look.scale(driver.zza * 0.035 * boost)).add(left.scale(driver.xxa * 0.02 * boost));
                }
            } else {
                motion = new Vec3(motion.x * 0.8, motion.y - 0.04, motion.z * 0.8);
            }
            this.setDeltaMovement(motion);
            this.move(MoverType.SELF, motion);
            lerpSteps = 0;
        } else {
            this.setDeltaMovement(Vec3.ZERO);
            if (this.level().isClientSide && lerpSteps > 0) {
                this.setPos(getX() + (lerpX - getX()) / lerpSteps, getY() + (lerpY - getY()) / lerpSteps, getZ() + (lerpZ - getZ()) / lerpSteps);
                this.setYRot(this.getYRot() + Mth.wrapDegrees(lerpYRot - this.getYRot()) / lerpSteps);
                lerpSteps--;
            }
        }

        if (this.level().isClientSide) {
            if (this.isInWater() && this.getDeltaMovement().lengthSqr() > 0.001 && this.random.nextInt(2) == 0) {
                Vec3 back = this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(-1.4)).add(0.0, 0.6, 0.0);
                this.level().addParticle(ParticleTypes.BUBBLE, back.x, back.y, back.z, 0.0, 0.05, 0.0);
            }
        } else {
            for (Entity passenger : this.getPassengers()) {
                passenger.setAirSupply(passenger.getMaxAirSupply());
            }
            if (this.getControllingPassenger() instanceof Player driver && (driver.zza != 0 || driver.xxa != 0) && getEnergy() > 0) {
                setEnergy(getEnergy() - 1);
                if (getEnergy() == 0) driver.displayClientMessage(Component.translatable("message.the_shell.submarine.empty"), true);
            }
            if (this.isInWater() && this.tickCount % 4 == 0 && getEnergy() > 0) {
                Vec3 front = this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(1.8)).add(0.0, 0.6, 0.0);
                AbyssLightBlock.placeAt(this.level(), BlockPos.containing(front));
                LivingEntity driver = this.getControllingPassenger();
                AbyssLightBlock.lightAhead(this.level(), front, driver != null ? driver.getLookAngle() : Vec3.directionFromRotation(0.0F, this.getYRot()), 22);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "propeller", 3, state ->
                this.isVehicle() && this.isInWater() ? state.setAndContinue(PROPELLER) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
