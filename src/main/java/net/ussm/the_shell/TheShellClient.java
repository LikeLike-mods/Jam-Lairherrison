package net.ussm.the_shell;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.ussm.the_shell.client.water.OceanRenderer;
import net.ussm.the_shell.init.ModNetworking;

@Mod(value = TheShell.MOD_ID, dist = Dist.CLIENT)
public class TheShellClient {
    public TheShellClient(ModContainer container) {
        ModNetworking.setWaveImpactHandler(OceanRenderer::receiveImpact);
    }
}
