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
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public final class AnimatedHitboxPart extends PartEntity<LivingEntity> {
    private final String name;
    private boolean positioned;

    public AnimatedHitboxPart(LivingEntity parent, String name) {
        super(parent);
        this.name = name;
        noPhysics = true;
    }

    public String partName() { return name; }

    public static void pushAway(LivingEntity owner, AnimatedHitboxPart[] parts) {
        if (parts == null || !owner.isAlive()) return;
        for (AnimatedHitboxPart part : parts) {
            AABB box = part.getBoundingBox();
            List<? extends Entity> targets = owner.level().isClientSide()
                    ? owner.level().getEntities(EntityTypeTest.forClass(Player.class), box, EntitySelector.pushableBy(owner))
                    : owner.level().getEntities(owner, box, EntitySelector.pushableBy(owner));
            Vec3 center = box.getCenter();
            for (Entity target : targets) {
                if (target == owner || target instanceof PartEntity<?> || target.isPassengerOfSameVehicle(owner)) continue;
                double dx = target.getX() - center.x;
                double dy = target.isInWater() ? target.getY(0.5) - center.y : 0.0;
                double dz = target.getZ() - center.z;
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (length < 1.0E-4) {
                    dx = owner.getRandom().nextDouble() - 0.5;
                    dz = owner.getRandom().nextDouble() - 0.5;
                    length = Math.sqrt(dx * dx + dz * dz);
                }
                double strength = 0.08 / length;
                target.push(dx * strength, dy * strength, dz * strength);
            }
        }
    }

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
