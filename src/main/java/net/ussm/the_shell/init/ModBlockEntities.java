package net.ussm.the_shell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.MessageBottleBlockEntity;
import net.ussm.the_shell.blocks.SlidingDoorBlockEntity;
import net.ussm.the_shell.blocks.StorageBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheShell.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MessageBottleBlockEntity>> MESSAGE_BOTTLE = BLOCK_ENTITIES.register("message_bottle",
            () -> BlockEntityType.Builder.of(MessageBottleBlockEntity::new, ModBlocks.MESSAGE_BOTTLE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SlidingDoorBlockEntity>> SLIDING_DOOR = BLOCK_ENTITIES.register("sliding_door",
            () -> BlockEntityType.Builder.of(SlidingDoorBlockEntity::new, ModBlocks.SLIDING_DOOR.get(), ModBlocks.BROKEN_SLIDING_DOOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageBlockEntity>> STORAGE = BLOCK_ENTITIES.register("storage",
            () -> BlockEntityType.Builder.of(StorageBlockEntity::new, ModBlocks.METAL_CRATE.get(), ModBlocks.RUSTY_BARREL.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
