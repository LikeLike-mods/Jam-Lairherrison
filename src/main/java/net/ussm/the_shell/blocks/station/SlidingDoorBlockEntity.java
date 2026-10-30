package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.ussm.the_shell.init.ModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SlidingDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private static final RawAnimation CLOSED = RawAnimation.begin().thenLoop("door.closed");
    private static final RawAnimation OPENED = RawAnimation.begin().thenLoop("door.opened");
    private static final RawAnimation OPENING = RawAnimation.begin().thenPlayAndHold("door.open");
    private static final RawAnimation CLOSING = RawAnimation.begin().thenPlayAndHold("door.close");
    private static final RawAnimation STUCK = RawAnimation.begin().thenLoop("door.stuck");
    private static final RawAnimation JAMMED = RawAnimation.begin().thenPlay("door.jammed");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Boolean lastOpen;
    private boolean moved;
    long lastUse = Long.MIN_VALUE / 2;

    public SlidingDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIDING_DOOR.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "door", 0, this::animate).triggerableAnim("jammed", JAMMED));
    }

    private PlayState animate(AnimationState<SlidingDoorBlockEntity> state) {
        BlockState block = getBlockState();
        if (block.getBlock() instanceof SlidingDoorBlock door && door.isBroken()) {
            return state.setAndContinue(STUCK);
        }
        boolean open = block.getValue(SlidingDoorBlock.OPEN);
        if (lastOpen == null) {
            lastOpen = open;
        } else if (lastOpen != open) {
            lastOpen = open;
            moved = true;
        }
        return state.setAndContinue(moved ? (open ? OPENING : CLOSING) : (open ? OPENED : CLOSED));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
