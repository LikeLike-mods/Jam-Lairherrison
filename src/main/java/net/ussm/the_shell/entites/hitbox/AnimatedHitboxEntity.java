package net.ussm.the_shell.entites.hitbox;

import java.util.List;
import net.ussm.the_shell.entites.animations.TurnBendProfile;
import net.ussm.the_shell.entites.animations.TurnBendingEntity;

public interface AnimatedHitboxEntity extends TurnBendingEntity {
    String getModelName();
    float getModelScale();
    double getModelYOffset();
    TurnBendProfile getTurnProfile();
    String getHitboxAnimation();
    double getHitboxAnimationTick(float partialTick);
    List<AnimatedHitboxRig.Section> getHitboxSections();
    default boolean isPlayingSpecialPose() { return false; }
    default boolean rotatesModelWithPitch() { return false; }
}
