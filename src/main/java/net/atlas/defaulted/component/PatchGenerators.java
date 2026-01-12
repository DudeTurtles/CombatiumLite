package net.atlas.defaulted.component;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class PatchGenerators {
	public static final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends PatchGenerator>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
}