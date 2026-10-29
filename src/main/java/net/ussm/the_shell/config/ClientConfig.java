package net.ussm.the_shell.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue WAVES_ENABLED;
    public static final ModConfigSpec.IntValue SHORE_MAP_SIZE;
    public static final ModConfigSpec.IntValue SHORE_COLUMNS_PER_TICK;
    public static final ModConfigSpec.DoubleValue WAVE_AMPLITUDE;
    public static final ModConfigSpec.DoubleValue FOAM_STRENGTH;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Client visuals, exclusively in the_shell:test_dim.",
                "Water physics, boats and server terrain remain vanilla.").push("waves");
        WAVES_ENABLED = b.define("enabled", true);
        WAVE_AMPLITUDE = b.comment("Swell amplitude in blocks. Minecraft+ preset: 0.28.")
                .defineInRange("amplitude", 0.28, 0.0, 0.65);
        FOAM_STRENGTH = b.comment("White foam strength, including shore breakers and occasional whitecaps.")
                .defineInRange("foamStrength", 0.85, 0.0, 1.0);
        SHORE_MAP_SIZE = b.comment("Width of the local shoreline map in blocks. 256 covers roughly 8 chunks in each direction.",
                "Beyond this map the shader fades back to vanilla water. 512 costs more CPU and memory.")
                .defineInRange("shoreMapSize", 256, 128, 512);
        SHORE_COLUMNS_PER_TICK = b.comment("Bounded terrain sampling budget. 1024 builds the default map in about 3.2 seconds.")
                .defineInRange("columnsPerTick", 1024, 128, 4096);
        b.pop();
        SPEC = b.build();
    }

    private ClientConfig() {}
}
