package net.ussm.the_shell.client.renderers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.station.SlidingDoorBlock;
import net.ussm.the_shell.blocks.station.SlidingDoorBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SlidingDoorRenderer extends GeoBlockRenderer<SlidingDoorBlockEntity> {
    private static final ResourceLocation BROKEN = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/block/broken_sliding_door.png");

    public SlidingDoorRenderer() {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "sliding_door")) {
            @Override
            public ResourceLocation getTextureResource(SlidingDoorBlockEntity door) {
                return door.getBlockState().getBlock() instanceof SlidingDoorBlock block && block.isBroken() ? BROKEN : super.getTextureResource(door);
            }
        });
    }

    @Override
    public AABB getRenderBoundingBox(SlidingDoorBlockEntity door) {
        return new AABB(door.getBlockPos()).expandTowards(0, 1, 0).inflate(1);
    }
}
