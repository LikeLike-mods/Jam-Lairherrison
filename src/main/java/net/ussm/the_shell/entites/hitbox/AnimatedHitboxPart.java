package net.ussm.the_shell.entites.hitbox;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.PartEntity;
import net.minecraft.world.entity.LivingEntity;

public final class AnimatedHitboxPart extends PartEntity<LivingEntity> {
    private final String name;
    private boolean positioned;

    public AnimatedHitboxPart(LivingEntity parent, String name) {
        super(parent);
        this.name = name;
        noPhysics = true;
    }

    public String partName() { return name; }

    public void beginTick() {
        xo = xOld = getX(); yo = yOld = getY(); zo = zOld = getZ();
    }

    public void updateBounds(AABB bounds) {
        setPos(bounds.getCenter().x, bounds.minY, bounds.getCenter().z);
        setBoundingBox(bounds);
        if (!positioned) {
            beginTick();
            positioned = true;
        }
    }

    @Override public boolean isPickable() { return getParent().isAlive(); }
    @Override public boolean is(Entity entity) { return this == entity || getParent() == entity; }
    @Override public boolean hurt(DamageSource source, float amount) {
        return !isInvulnerableTo(source) && getParent().isAlive() && getParent().hurt(source, amount);
    }
    @Override public ItemStack getPickResult() { return getParent().getPickResult(); }
    @Override public EntityDimensions getDimensions(Pose pose) { return EntityDimensions.fixed(1, 1); }
    @Override public boolean shouldBeSaved() { return false; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
}
