package net.ussm.the_shell.entites.animations;

public final class SwimmingTurnProfiles {
    public static final TurnBendProfile LEVIATHAN = TurnBendProfile.builder("body")
            .spine("neck", -6).spine("head", -5)
            .spine("tail1", 4).spine("tail2", 5).spine("tail3", 6).spine("tail4", 7).spine("tail_fin", 6)
            .fins("fin_right", "fin_left", 18).bank(12).pitch(0.8F, 35).build();

    public static final TurnBendProfile BLUE_WHALE = TurnBendProfile.builder("body")
            .spine("head", -2)
            .spine("tail1", 1.2F).spine("tail2", 1.5F).spine("tail3", 1.8F).spine("tail4", 2).spine("flukes", 1.5F)
            .fins("right_flipper", "left_flipper", 4).bank(3).build();

    public static final TurnBendProfile BASILOSAURUS = TurnBendProfile.builder("body")
            .spine("neck", -6).spine("head", -5)
            .spine("abdomen", 2).spine("pelvis", 3)
            .spine("tail1", 4).spine("tail2", 5).spine("tail3", 6).spine("flukes", 5)
            .fins("right_flipper", "left_flipper", 14).bank(10).build();

    private SwimmingTurnProfiles() {}
}
