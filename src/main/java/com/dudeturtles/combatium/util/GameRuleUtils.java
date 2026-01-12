package com.dudeturtles.combatium.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;

import java.util.function.ToIntFunction;

public class GameRuleUtils {
    public static GameRule<Boolean> registerBoolean(String name, GameRuleCategory category, boolean defaultValue) {
        return register(name, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, FeatureFlagSet.of(), GameRuleTypeVisitor::visitBoolean, (_boolean) -> _boolean ? 1 : 0);
    }

    public static GameRule<Integer> registerInteger(String name, GameRuleCategory category, int defaultValue, int min) {
        return registerInteger(name, category, defaultValue, min, Integer.MAX_VALUE, FeatureFlagSet.of());
    }

    public static GameRule<Integer> registerInteger(String name, GameRuleCategory category, int defaultValue, int min, int max) {
        return registerInteger(name, category, defaultValue, min, max, FeatureFlagSet.of());
    }

    public static GameRule<Integer> registerInteger(String name, GameRuleCategory category, int defaultValue, int min, int max, FeatureFlagSet requiredFeatures) {
        return register(name, category, GameRuleType.INT, IntegerArgumentType.integer(min, max), Codec.intRange(min, max), defaultValue, requiredFeatures, GameRuleTypeVisitor::visitInteger, (integer) -> integer);
    }

    public static <T> GameRule<T> register(String name, GameRuleCategory category, GameRuleType type, ArgumentType<T> argument, Codec<T> valueCodec, T defaultValue, FeatureFlagSet requiredFeatures, GameRules.VisitorCaller<T> visitorCaller, ToIntFunction<T> commandResultFunction) {
        return Registry.register(BuiltInRegistries.GAME_RULE, name, new GameRule<>(category, type, argument, visitorCaller, valueCodec, commandResultFunction, defaultValue, requiredFeatures));
    }
}
