package net.ussm.the_shell.entites.animations;

public interface TurnBendingEntity {
    float getTurnBend(float partialTick);

    default boolean isTurnBendingAllowed() {
        return true;
    }
}
