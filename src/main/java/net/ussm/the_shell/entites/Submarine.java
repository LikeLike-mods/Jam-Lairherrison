package net.ussm.the_shell.entites;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.blocks.AbyssLightBlock;
import net.ussm.the_shell.init.ModEntities;
import net.ussm.the_shell.init.ModItems;
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

    public int getInteriorId() {
        return interiorId;
    }

    public void setInteriorId(int id) {
        this.interiorId = id;
    }

    @Override
    protected Item getDropItem() {
        return ModItems.SUBMARINE.get();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Interior")) interiorId = tag.getInt("Interior");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Interior", interiorId);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
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
        return new Vec3(0.0, 0.3, 0.0);
    }

    // clic droit : on entre dans la cabine, et l'echelle du fond mene aux commandes
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.ussm.the_shell.events.SubmarineInterior.enter(serverPlayer, this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        net.ussm.the_shell.events.AbyssTravel.check(this);
        if (!this.level().isClientSide && interiorId >= 0 && this.tickCount % 40 == 0) net.ussm.the_shell.events.SubmarineInterior.remember(this);
        if (this.isControlledByLocalInstance()) {
            Vec3 motion = this.getDeltaMovement();
            if (this.isInWater()) {
                motion = motion.scale(0.9);
                if (this.getControllingPassenger() instanceof Player driver) {
                    this.setYRot(driver.getYRot());
                    Vec3 look = driver.getLookAngle();
                    Vec3 left = new Vec3(look.z, 0.0, -look.x).normalize();
                    motion = motion.add(look.scale(driver.zza * 0.035)).add(left.scale(driver.xxa * 0.02));
                }
            } else {
                motion = new Vec3(motion.x * 0.8, motion.y - 0.04, motion.z * 0.8);
            }
            this.setDeltaMovement(motion);
            this.move(MoverType.SELF, motion);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
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
            if (this.isInWater() && this.tickCount % 4 == 0) {
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
