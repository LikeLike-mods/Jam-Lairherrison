package net.ussm.theshell.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.ussm.theshell.TheShell;
import net.ussm.theshell.blocks.SlidingDoorBlock;
import net.ussm.theshell.blocks.SlidingDoorBlockEntity;
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

    // la porte fait 2 blocs de haut, sinon le haut disparait quand le bas sort de l'ecran
    @Override
    public AABB getRenderBoundingBox(SlidingDoorBlockEntity door) {
        return new AABB(door.getBlockPos()).expandTowards(0, 1, 0).inflate(1);
    }
}
