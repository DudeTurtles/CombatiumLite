package net.atlas.defaulted.component;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class PatchGenerators {
	public static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends PatchGenerator>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
}