package net.ussm.the_shell.client.compat;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;

public final class Shaderpacks {
    private static final boolean IRIS = ModList.get().isLoaded("iris");

    private Shaderpacks() {}

    public static boolean inUse() {
        return IRIS && IrisApi.getInstance().isShaderPackInUse();
    }
}
