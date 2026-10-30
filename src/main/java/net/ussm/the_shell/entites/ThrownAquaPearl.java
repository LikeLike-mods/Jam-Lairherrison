package net.ussm.the_shell.entites;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.init.ModEntities;
import net.ussm.the_shell.init.ModItems;

// la perle d'ender sous-marine : sous l'eau elle file comme dans l'air (la vanilla est freinee net), et tombe moins vite
public class ThrownAquaPearl extends ThrownEnderpearl {
    public ThrownAquaPearl(EntityType<? extends ThrownAquaPearl> type, Level level) {
        super(type, level);
    }

    public ThrownAquaPearl(Level level, LivingEntity lanceur) {
        super(ModEntities.AQUA_PEARL.get(), level);
        setOwner(lanceur);
        setPos(lanceur.getX(), lanceur.getEyeY() - 0.1, lanceur.getZ());
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.AQUA_PEARL.get();
    }

    @Override
    public void tick() {
        Vec3 avant = getDeltaMovement();
        boolean eau = isInWater();
        super.tick();
        // le jeu vient de la freiner comme dans l'eau : on remet le freinage de l'air, et la moitie de la gravite
        if (eau && !isRemoved()) setDeltaMovement(avant.scale(0.99).add(0.0, -getGravity() * 0.5, 0.0));
    }
}
