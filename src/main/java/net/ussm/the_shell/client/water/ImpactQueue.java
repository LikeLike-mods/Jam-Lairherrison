package net.ussm.the_shell.client.water;

import net.ussm.the_shell.world.water.ImpactWave;
import java.util.ArrayList;
import java.util.List;

public final class ImpactQueue {
    private final List<ImpactWave> waves = new ArrayList<>(ImpactWave.MAX_ACTIVE);

    public boolean add(ImpactWave wave, long gameTick) {
        prune(gameTick);
        if (wave.expired(gameTick) || wave.startTick() - gameTick > 20) return false;
        if (waves.size() == ImpactWave.MAX_ACTIVE) waves.removeFirst();
        waves.add(wave);
        return true;
    }

    public void prune(long gameTick) { waves.removeIf(wave -> wave.expired(gameTick)); }
    public void clear() { waves.clear(); }
    public int size() { return waves.size(); }
    public ImpactWave get(int index) { return waves.get(index); }
}
