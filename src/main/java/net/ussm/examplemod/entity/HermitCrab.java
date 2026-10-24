package net.ussm.examplemod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;

// Bernard-l'ermite : rentre dans sa coquille quand un joueur s'approche sans être accroupi, ou quand on le tape.
// Dans sa coquille il ne bouge plus et encaisse beaucoup moins.
public class HermitCrab extends AtollCreature {
    private static final EntityDataAccessor<Boolean> HIDING = SynchedEntityData.defineId(HermitCrab.class, EntityDataSerializers.BOOLEAN);
    private int hideTicks;

    public HermitCrab(EntityType<? extends HermitCrab> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HIDING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public boolean isHiding() {
        return this.entityData.get(HIDING);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            Player player = this.level().getNearestPlayer(this, 3.0);
            if (player != null && !player.isShiftKeyDown() && !player.isSpectator()) {
                hideTicks = 60;
            } else if (hideTicks > 0) {
                hideTicks--;
            }
            this.entityData.set(HIDING, hideTicks > 0);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isHiding();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isHiding()) {
            amount *= 0.25F;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            hideTicks = 100;
        }
        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HideTicks", hideTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        hideTicks = tag.getInt("HideTicks");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 4, state -> {
            if (this.isHiding()) return state.setAndContinue(AtollAnimations.HIDE);
            return state.setAndContinue(state.isMoving() ? DefaultAnimations.WALK : DefaultAnimations.IDLE);
        }));
    }
}
