package net.atlas.defaulted.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;

public interface PatchGenerator {
	Codec<PatchGenerator> CODEC = PatchGenerators.ID_MAPPER.codec(ResourceLocation.CODEC)
        .dispatch("generator", PatchGenerator::codec, mapCodec -> mapCodec);
    MapCodec<? extends PatchGenerator> codec();
}
