package net.ussm.the_shell.entites.hitbox;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class AnimatedHitbox<T extends LivingEntity & AnimatedHitboxEntity> {
    private final T entity;
    private final AnimatedHitboxRig rig;
    private final List<AnimatedHitboxRig.Section> sections;
    private final AnimatedHitboxPart[] parts;

    public AnimatedHitbox(T entity) {
        this.entity = entity;
        rig = AnimatedHitboxRig.load(entity.getModelName());
        sections = List.copyOf(entity.getHitboxSections());
        rig.validateSections(sections);
        parts = sections.stream().map(section -> new AnimatedHitboxPart(entity, section.name()))
                .toArray(AnimatedHitboxPart[]::new);
    }

    public AnimatedHitboxPart[] parts() { return parts; }

    public void setId(int parentId) {
        for (int i = 0; i < parts.length; i++) parts[i].setId(parentId + i + 1);
    }

    public void beginTick() {
        for (AnimatedHitboxPart part : parts) part.beginTick();
    }

    public void update(float partialTick) { rig.update(entity, sections, parts, partialTick); }

    public AABB cullingBounds(AABB bodyBounds) {
        for (AnimatedHitboxPart part : parts) bodyBounds = bodyBounds.minmax(part.getBoundingBox());
        return bodyBounds.inflate(0.5);
    }
}
