package net.ussm.the_shell.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MessageBottleBlockEntity extends BlockEntity {
    public static final int CAPACITY = 4;
    private final NonNullList<ItemStack> items = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    public MessageBottleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MESSAGE_BOTTLE.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    public void setLootTable(ResourceKey<LootTable> lootTable, long seed) {
        this.lootTable = lootTable;
        this.lootTableSeed = seed;
    }

    public boolean insert(ItemStack stack) {
        for (int i = 0; i < CAPACITY; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack);
                changed();
                return true;
            }
        }
        return false;
    }

    public NonNullList<ItemStack> takeAll() {
        NonNullList<ItemStack> taken = NonNullList.create();
        for (int i = 0; i < CAPACITY; i++) {
            if (!items.get(i).isEmpty()) taken.add(items.get(i));
            items.set(i, ItemStack.EMPTY);
        }
        changed();
        return taken;
    }

    public void unpackLoot() {
        if (lootTable == null || !(this.level instanceof ServerLevel serverLevel)) return;
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                .create(LootContextParamSets.CHEST);
        List<ItemStack> loot = serverLevel.getServer().reloadableRegistries().getLootTable(lootTable).getRandomItems(params, lootTableSeed);
        lootTable = null;
        loot.forEach(this::insert);
        changed();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        unpackLoot();
    }

    private void changed() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        if (tag.contains("LootTable")) {
            lootTable = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString("LootTable")));
            lootTableSeed = tag.getLong("LootTableSeed");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, true, registries);
        if (lootTable != null) {
            tag.putString("LootTable", lootTable.location().toString());
            tag.putLong("LootTableSeed", lootTableSeed);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items, true, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
