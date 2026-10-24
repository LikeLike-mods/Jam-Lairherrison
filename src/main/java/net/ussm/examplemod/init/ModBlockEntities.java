package net.ussm.examplemod.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.blocks.MessageBottleBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExampleMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MessageBottleBlockEntity>> MESSAGE_BOTTLE = BLOCK_ENTITIES.register("message_bottle",
            () -> BlockEntityType.Builder.of(MessageBottleBlockEntity::new, ModBlocks.MESSAGE_BOTTLE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
