package net.atlas.defaulted.component;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
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
    @SuppressWarnings("deprecation")
    public boolean matchItem(Item item) {
        if (items.size() != 0 || !tags.isEmpty()) {
            Holder<Item> itemHolder = item.builtInRegistryHolder();
            List<TagKey<Item>> matchedTags = tags.stream().filter(itemHolder::is).toList();
            return items.contains(itemHolder) || !matchedTags.isEmpty();
        }
        return true;
    }
}
