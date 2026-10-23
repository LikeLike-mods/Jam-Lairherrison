package net.ussm.examplemod.entity;

import software.bernie.geckolib.animation.RawAnimation;

// Animations des mobs qui ne sont pas dans DefaultAnimations de GeckoLib (les noms sont ceux des fichiers animation.json).
public final class AtollAnimations {
    public static final RawAnimation HOP = RawAnimation.begin().thenLoop("move.jump");
    public static final RawAnimation HIDE = RawAnimation.begin().thenPlayAndHold("misc.hide");
    public static final RawAnimation ROLL = RawAnimation.begin().thenPlayAndHold("misc.roll");
    public static final RawAnimation DEFEND = RawAnimation.begin().thenPlay("misc.defend");

    private AtollAnimations() {
    }
}
