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
    // batterie : un tick de moteur = 1, pleine = 5 minutes a fond
    public static final int MAX_ENERGY = 6000;
    // niveau d'amelioration : 0 de base, 1 fer, 2 diamant, 3 netherite
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> TIER =
            net.minecraft.network.syncher.SynchedEntityData.defineId(Submarine.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> ENERGY =
            net.minecraft.network.syncher.SynchedEntityData.defineId(Submarine.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

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
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
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

    public static int kitTier(net.minecraft.world.item.ItemStack stack) {
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

    // ce que vaut chaque carburant : le charbon et la redstone depannent, le kyber des abysses recharge a fond
    public static int fuelValue(net.minecraft.world.item.ItemStack stack) {
        if (stack.is(net.minecraft.world.item.Items.COAL) || stack.is(net.minecraft.world.item.Items.CHARCOAL)) return 150;
        if (stack.is(net.minecraft.world.item.Items.REDSTONE)) return 200;
        if (stack.is(net.minecraft.world.item.Items.REDSTONE_BLOCK)) return 1800;
        if (stack.is(ModItems.KYBER_CRYSTAL.get())) return MAX_ENERGY;
        return 0;
    }

    public int getInteriorId() {
        return interiorId;
    }

    public void setInteriorId(int id) {
        this.interiorId = id;
    }

    // on ne le casse qu'accroupi : sinon un clic rate en minant a cote (ou en tapant un poisson) le fait disparaitre, d'un coup en creatif
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player player) || !player.isShiftKeyDown()) return false;
        // en creatif le jeu le supprime sans rien lacher : on veut quand meme l'objet, sinon on perd la cabine
        if (player.getAbilities().instabuild && !this.level().isClientSide && !this.isRemoved()) {
            destroy(source);
            return true;
        }
        return super.hurt(source, amount);
    }

    // casse : l'objet garde les ameliorations, la batterie et la cabine (on la retrouve en le reposant)
    @Override
    protected void destroy(net.minecraft.world.damagesource.DamageSource source) {
        this.kill();
        if (!this.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOENTITYDROPS)) return;
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(getDropItem());
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, this.getCustomName());
        CompoundTag tag = new CompoundTag();
        tag.putInt("Tier", getTier());
        tag.putInt("Energy", getEnergy());
        if (interiorId >= 0) {
            tag.putInt("Interior", interiorId);
            net.ussm.the_shell.events.SubmarineInterior.store(this);
        }
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
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
    // quand quelqu'un est dedans on ne peut plus viser le sous-marin : sinon le pilote tape dedans au lieu de miner et ca le fait sortir
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
        // assis dans la bulle (jambes cachees dans la coque, tete sous le verre), et ca tourne avec le sous-marin
        return new Vec3(0.0, 0.45, 0.31).yRot((float) Math.toRadians(-this.getYRot()));
    }

    // clic droit : aux commandes, le bouton du tableau de bord mene a la cabine
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        net.minecraft.world.item.ItemStack kit = player.getItemInHand(hand);
        int niveau = kitTier(kit);
        if (niveau > 0) {
            if (!this.level().isClientSide) {
                if (niveau == getTier() + 1) {
                    setTier(niveau);
                    kit.consume(1, player);
                    net.ussm.the_shell.events.SubmarineInterior.upgrade(this);
                    this.level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.SMITHING_TABLE_USE, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.upgraded", niveau), true);
                } else if (niveau > getTier()) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.upgrade_order"), true);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // avec du carburant en main on recharge la batterie au lieu d'entrer
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        int fuel = fuelValue(stack);
        if (fuel > 0) {
            if (!this.level().isClientSide && getEnergy() < MAX_ENERGY) {
                setEnergy(getEnergy() + fuel);
                stack.consume(1, player);
                this.level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.NEUTRAL, 0.6F, 1.4F);
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.energy", getEnergy() * 100 / MAX_ENERGY), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // clic droit : direct aux commandes (avec shift on s'assoit quand on le lache, sinon le jeu nous refait descendre).
        // si quelqu'un pilote deja on descend dans la cabine
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            if (this.isVehicle()) net.ussm.the_shell.events.SubmarineInterior.enter(serverPlayer, this);
            else if (player.isSecondaryUseActive()) net.ussm.the_shell.events.SubmarineInterior.seatLater(serverPlayer, this);
            else player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        net.ussm.the_shell.events.AbyssTravel.check(this);
        if (!this.level().isClientSide && interiorId >= 0 && this.tickCount % 40 == 0) {
            setEnergy(getEnergy() + net.ussm.the_shell.events.SubmarineInterior.takeFuel(this));
            net.ussm.the_shell.events.SubmarineInterior.remember(this);
        }
        if (this.isControlledByLocalInstance()) {
            Vec3 motion = this.getDeltaMovement();
            if (this.isInWater()) {
                motion = motion.scale(0.9);
                if (this.getControllingPassenger() instanceof Player driver && getEnergy() > 0) {
                    this.setYRot(driver.getYRot());
                    Vec3 look = driver.getLookAngle();
                    Vec3 left = new Vec3(look.z, 0.0, -look.x).normalize();
                    double boost = 1.0 + 0.15 * getTier();   // chaque amelioration le rend un peu plus rapide
                    motion = motion.add(look.scale(driver.zza * 0.035 * boost)).add(left.scale(driver.xxa * 0.02 * boost));
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
            // le moteur use la batterie, et a plat le sous-marin ne repart plus
            if (this.getControllingPassenger() instanceof Player driver && (driver.zza != 0 || driver.xxa != 0) && getEnergy() > 0) {
                setEnergy(getEnergy() - 1);
                if (getEnergy() == 0) driver.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.the_shell.submarine.empty"), true);
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
