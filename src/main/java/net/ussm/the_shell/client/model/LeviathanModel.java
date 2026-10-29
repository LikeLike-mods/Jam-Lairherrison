package net.ussm.the_shell.client.model;

import net.ussm.the_shell.entites.animations.SwimmingTurnProfiles;
import net.ussm.the_shell.entites.LeviathanEntity;

public final class LeviathanModel extends TurningEntityGeoModel<LeviathanEntity> {
    public LeviathanModel() {
        super("leviathan", SwimmingTurnProfiles.LEVIATHAN);
    }
}
