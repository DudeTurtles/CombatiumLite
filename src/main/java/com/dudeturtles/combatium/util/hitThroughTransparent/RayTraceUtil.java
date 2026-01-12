package com.dudeturtles.combatium.util.hitThroughTransparent;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.bukkit.Location;

import java.util.Collection;
import java.util.function.Predicate;

import static net.minecraft.world.entity.projectile.ProjectileUtil.getManyEntityHitResult;

public class RayTraceUtil {
    public static HitResult raycastHitResult(Player player, Entity entity, Location eyeLocation) {
        Vec3 eyePosition = new Vec3(eyeLocation.x(), eyeLocation.y(), eyeLocation.z());
        Vec3 viewVector = new Vec3(eyeLocation.getDirection().getX(), eyeLocation.getDirection().getY(), eyeLocation.getDirection().getZ());
        ItemStack itemStack = player.getActiveItem();
        AttackRange attackRange = itemStack.get(DataComponents.ATTACK_RANGE);
        double blockInteractionRange = player.blockInteractionRange();
        HitResult hitResult = null;
        if (attackRange != null) {
            hitResult = getClosestHit(entity, eyePosition, viewVector, attackRange, EntitySelector.CAN_BE_PICKED);
            if (hitResult instanceof BlockHitResult) {
                hitResult = filterHitResult(hitResult, eyePosition, blockInteractionRange);
            }
        }

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            double entityInteractionRange = player.entityInteractionRange();
            hitResult = pick(entity, eyePosition, viewVector, blockInteractionRange, entityInteractionRange);
        }

        return hitResult;
    }

    private static HitResult pick(Entity entity, Vec3 eyePosition, Vec3 viewVector, double blockInteractionRange, double entityInteractionRange) {
        double maxReach = Math.max(blockInteractionRange, entityInteractionRange);
        double maxReachSqr = Mth.square(maxReach);
        HitResult hitResult = pick(entity, eyePosition, viewVector, maxReach);
        double blockHitDistance = hitResult.getLocation().distanceToSqr(eyePosition);
        if (hitResult.getType() != HitResult.Type.MISS) {
            maxReachSqr = blockHitDistance;
            maxReach = Math.sqrt(blockHitDistance);
        }

        Vec3 to = eyePosition.add(viewVector.x * maxReach, viewVector.y * maxReach, viewVector.z * maxReach);
        AABB aABB = entity.getBoundingBox().expandTowards(viewVector.scale(maxReach)).inflate(1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, eyePosition, to, aABB, EntitySelector.CAN_BE_PICKED, maxReachSqr);
        return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(eyePosition) < blockHitDistance
                ? filterHitResult(entityHitResult, eyePosition, entityInteractionRange)
                : filterHitResult(hitResult, eyePosition, blockInteractionRange);
    }

    public static HitResult pick(Entity entity, Vec3 eyePosition, Vec3 viewVector, double hitDistance) {
        Vec3 destPos = eyePosition.add(viewVector.x * hitDistance, viewVector.y * hitDistance, viewVector.z * hitDistance);
        return entity.level().clip(new ClipContext(eyePosition, destPos, ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, entity));
    }

    private static HitResult filterHitResult(HitResult hitResult, Vec3 eyePosition, double range) {
        Vec3 location = hitResult.getLocation();
        if (!location.closerThan(eyePosition, range)) {
            Vec3 hitLocation = hitResult.getLocation();
            Direction direction = Direction.getApproximateNearest(hitLocation.x - eyePosition.x, hitLocation.y - eyePosition.y, hitLocation.z - eyePosition.z);
            return BlockHitResult.miss(hitLocation, direction, BlockPos.containing(hitLocation));
        } else {
            return hitResult;
        }
    }

    private static HitResult getClosestHit(Entity attacker, Vec3 eyePosition, Vec3 viewVector, AttackRange attackRange, Predicate<Entity> filter) {
        Either<BlockHitResult, Collection<EntityHitResult>> hitEntitiesAlong = getHitEntitiesAlong(attacker, eyePosition, viewVector, attackRange, filter, ClipContext.Block.OUTLINE);
        if (hitEntitiesAlong.left().isPresent()) {
            return hitEntitiesAlong.left().get();
        } else {
            Collection<EntityHitResult> collection = hitEntitiesAlong.right().get();
            EntityHitResult closestHitResult = null;
            double closestHit = Double.MAX_VALUE;

            for(EntityHitResult entityHitResult1 : collection) {
                double currentHit = eyePosition.distanceToSqr(entityHitResult1.getLocation());
                if (currentHit < closestHit) {
                    closestHit = currentHit;
                    closestHitResult = entityHitResult1;
                }
            }

            if (closestHitResult != null) {
                return closestHitResult;
            } else {
                Vec3 headLookAngle = attacker.getHeadLookAngle();
                Vec3 inAngle = eyePosition.add(headLookAngle);
                return BlockHitResult.miss(inAngle, Direction.getApproximateNearest(headLookAngle), BlockPos.containing(inAngle));
            }
        }
    }
    public static Either<BlockHitResult, Collection<EntityHitResult>> getHitEntitiesAlong(Entity attacker, Vec3 eyePosition, Vec3 viewVector, AttackRange attackRange, Predicate<Entity> filter, ClipContext.Block clipContext) {
        Vec3 minRange = eyePosition.add(viewVector.scale(attackRange.effectiveMinRange(attacker)));
        double dotted = attacker.getKnownMovement().dot(viewVector);
        Vec3 maxRange = eyePosition.add(viewVector.scale((double)attackRange.effectiveMaxRange(attacker) + Math.max(0, dotted)));
        return getHitEntitiesAlong(attacker, eyePosition, minRange, filter, maxRange, attackRange.hitboxMargin(), clipContext);
    }
    private static Either<BlockHitResult, Collection<EntityHitResult>> getHitEntitiesAlong(Entity source, Vec3 startVec, Vec3 minReachVec, Predicate<Entity> filter, Vec3 maxReachVec, float margin, ClipContext.Block clipContext) {
        Level level = source.level();
        BlockHitResult blockHitResult = level.clipIncludingBorder(new ClipContext(startVec, maxReachVec, clipContext, ClipContext.Fluid.NONE, source));
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            maxReachVec = blockHitResult.getLocation();
            if (startVec.distanceToSqr(maxReachVec) < startVec.distanceToSqr(minReachVec)) {
                return Either.left(blockHitResult);
            }
        }

        AABB aabb = AABB.ofSize(minReachVec, margin, margin, margin).expandTowards(maxReachVec.subtract(minReachVec)).inflate(1.0F);
        Collection<EntityHitResult> manyEntityHitResult = getManyEntityHitResult(level, source, minReachVec, maxReachVec, aabb, filter, margin, clipContext, true);
        return !manyEntityHitResult.isEmpty() ? Either.right(manyEntityHitResult) : Either.left(blockHitResult);
    }
}
