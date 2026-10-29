package net.ussm.the_shell.entity;

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

// sous-marin : on avance la ou on regarde (haut/bas compris), ZQSD comme a pied. dedans on respire et la pression ne fait rien
public class Submarine extends VehicleEntity implements GeoEntity {
    private static final RawAnimation PROPELLER = RawAnimation.begin().thenLoop("move.propeller");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
    protected Item getDropItem() {
        return ModItems.SUBMARINE.get();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
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

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() || this.isVehicle()) return InteractionResult.PASS;
        if (!this.level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();
        net.ussm.the_shell.event.AbyssTravel.check(this);
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
                // hors de l'eau il tombe et n'avance plus
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
            // le phare eclaire devant
            if (this.isInWater() && this.tickCount % 4 == 0) {
                Vec3 front = this.position().add(Vec3.directionFromRotation(0.0F, this.getYRot()).scale(1.8)).add(0.0, 0.6, 0.0);
                AbyssLightBlock.placeAt(this.level(), BlockPos.containing(front));
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
