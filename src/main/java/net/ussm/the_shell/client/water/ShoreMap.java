package net.ussm.the_shell.client.water;

import net.ussm.the_shell.config.ClientConfig;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class ShoreMap implements AutoCloseable {
    public final int size;
    private final DynamicTexture texture;
    private final ClientLevel level;
    private final int seaLevel;
    private Scan scan;
    private int originX;
    private int originZ;
    private int age = 100;
    private boolean ready;

    public ShoreMap(ClientLevel level, int size, int seaLevel) {
        this.level = level;
        this.size = size;
        this.seaLevel = seaLevel;
        texture = new DynamicTexture(size, size, false);
        NativeImage pixels = texture.getPixels();
        if (pixels == null) throw new IllegalStateException("Missing shoreline image");
        pixels.fillRect(0, 0, size, size, 0xFF000000);
        texture.upload();
        texture.setFilter(true, false);
    }

    public boolean matches(ClientLevel world, int width, int sea) {
        return level == world && size == width && seaLevel == sea;
    }

    public int textureId() { return texture.getId(); }
    public int originX() { return originX; }
    public int originZ() { return originZ; }
    public boolean ready() { return ready; }

    public void tick(Vec3 camera) {
        age++;
        int wantedX = Math.floorDiv(Mth.floor(camera.x), 16) * 16 - size / 2;
        int wantedZ = Math.floorDiv(Mth.floor(camera.z), 16) * 16 - size / 2;
        if (scan != null && (Math.abs(wantedX - scan.x) > size / 4 || Math.abs(wantedZ - scan.z) > size / 4)) {
            scan = null;
        }
        if (scan == null && (!ready || age >= 100 || wantedX != originX || wantedZ != originZ)) {
            scan = new Scan(wantedX, wantedZ, size);
        }
        if (scan == null) return;
        int end = Math.min(scan.cursor + ClientConfig.SHORE_COLUMNS_PER_TICK.get(), size * size);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (; scan.cursor < end; scan.cursor++) {
            int i = scan.cursor;
            int x = scan.x + i % size;
            int z = scan.z + i / size;
            LevelChunk chunk = level.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false);
            if (chunk == null) continue;
            pos.set(x, seaLevel - 1, z);
            BlockState state = chunk.getBlockState(pos);
            boolean openWater = state.is(Blocks.WATER) && state.getFluidState().isSource()
                    && chunk.getBlockState(pos.setY(seaLevel)).isAir()
                    && chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) < seaLevel;
            scan.cells[i] = openWater ? ShoreField.WATER : ShoreField.LAND;
            if (openWater) {
                int depth = 1;
                while (depth < ShoreField.MAX_DEPTH) {
                    pos.setY(seaLevel - 1 - depth);
                    if (!chunk.getFluidState(pos).is(FluidTags.WATER)) break;
                    depth++;
                }
                scan.depths[i] = (byte) depth;
            }
        }
        if (scan.cursor == size * size) publish();
    }

    private void publish() {
        float[] distances = ShoreField.distances(scan.cells, size);
        NativeImage pixels = texture.getPixels();
        if (pixels == null) return;
        for (int i = 0; i < distances.length; i++) {
            pixels.setPixelRGBA(i % size, i / size,
                    ShoreField.pixel(scan.cells[i], Byte.toUnsignedInt(scan.depths[i]), distances[i]));
        }
        texture.upload();
        texture.setFilter(true, false);
        originX = scan.x;
        originZ = scan.z;
        ready = true;
        age = 0;
        scan = null;
    }

    @Override
    public void close() { texture.close(); }

    private static final class Scan {
        final int x;
        final int z;
        final byte[] cells;
        final byte[] depths;
        int cursor;

        Scan(int x, int z, int size) {
            this.x = x;
            this.z = z;
            cells = new byte[size * size];
            depths = new byte[size * size];
        }
    }
}
