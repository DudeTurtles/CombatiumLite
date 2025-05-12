package net.atlas.defaulted.component;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record ItemPatches(HolderSet<Item> items, List<TagKey<Item>> tags, List<PatchGenerator> generators, DataComponentPatch dataComponentPatch, int priority) implements Comparable<ItemPatches> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPatches> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.ITEM), ItemPatches::items,
        ByteBufCodecs.collection(ArrayList::new, TagKey.streamCodec(Registries.ITEM)), ItemPatches::tags,
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodecWithRegistriesTrusted(PatchGenerator.CODEC)), ItemPatches::generators,
        DataComponentPatch.STREAM_CODEC, ItemPatches::dataComponentPatch,
        ByteBufCodecs.VAR_INT, ItemPatches::priority, ItemPatches::new);
    @Override
    public int compareTo(ItemPatches o) {
        return priority - o.priority;
    }
}
