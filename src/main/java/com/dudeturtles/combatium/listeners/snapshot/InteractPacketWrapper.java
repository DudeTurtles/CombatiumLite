package com.dudeturtles.combatium.listeners.snapshot;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.util.LpVector3d;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.Optional;

public class InteractPacketWrapper extends PacketWrapper<InteractPacketWrapper> {
    private int entityID;
    private InteractPacketWrapper.InteractAction interactAction;
    private Optional<Vector3d> target;
    private InteractionHand interactionHand;
    private Optional<Boolean> sneaking;

    public InteractPacketWrapper(PacketReceiveEvent event) {
        super(event);
    }

    public InteractPacketWrapper(int entityID, InteractPacketWrapper.InteractAction interactAction, InteractionHand interactionHand, Optional<Vector3d> target, Optional<Boolean> sneaking) {
        super(PacketType.Play.Client.INTERACT_ENTITY);
        this.entityID = entityID;
        this.interactAction = interactAction;
        this.interactionHand = interactionHand;
        this.target = target;
        this.sneaking = sneaking;
    }

    public void read() {
        if (this.clientVersion.isNewerThanOrEquals(ClientVersion.V_26_1)) {
            this.entityID = this.readVarInt();
            this.interactionHand = this.readEnum(InteractionHand.values());
            this.target = Optional.of(LpVector3d.read(this));
            this.sneaking = Optional.of(this.readBoolean());
            this.interactAction = InteractAction.INTERACT_AT; // compat
        } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8)) {
            this.entityID = this.readVarInt();
            this.interactAction = this.readEnum(InteractAction.values());
            if (this.interactAction == InteractAction.INTERACT_AT) {
                this.target = Optional.of(new Vector3d(Vector3f.read(this)));
            } else {
                this.target = Optional.empty();
            }

            if (!this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) || this.interactAction != InteractAction.INTERACT && this.interactAction != InteractAction.INTERACT_AT) {
                this.interactionHand = InteractionHand.MAIN_HAND;
            } else {
                this.interactionHand = this.readEnum(InteractionHand.values());
            }

            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
                this.sneaking = Optional.of(this.readBoolean());
            } else {
                this.sneaking = Optional.empty();
            }
        } else {
            this.entityID = this.readInt();
            this.interactAction = this.readEnum(InteractAction.values());
            this.target = Optional.empty();
            this.interactionHand = InteractionHand.MAIN_HAND;
            this.sneaking = Optional.empty();
        }

    }

    public void write() {
        if (this.serverVersion == ServerVersion.V_1_7_10) {
            this.writeInt(this.entityID);
            this.writeByte(this.interactAction.ordinal());
        } else {
            this.writeVarInt(this.entityID);
            if (this.serverVersion.isOlderThan(ServerVersion.V_26_1)) {
                this.writeVarInt(this.interactAction.ordinal());
                if (this.interactAction == InteractPacketWrapper.InteractAction.INTERACT_AT) {
                    Vector3d targetVec = this.target.orElse(new Vector3d(0.0F, 0.0F, 0.0F));
                    this.writeDouble(targetVec.x);
                    this.writeDouble(targetVec.y);
                    this.writeDouble(targetVec.z);
                }
            }

            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) && (this.interactAction == InteractPacketWrapper.InteractAction.INTERACT || this.interactAction == InteractPacketWrapper.InteractAction.INTERACT_AT)) {
                this.writeVarInt(this.interactionHand.getId());
            }

            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_26_1)) {
                LpVector3d.write(this, this.target.orElse(new Vector3d(0.0F, 0.0F, 0.0F)));
            }

            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
                this.writeBoolean((Boolean) this.sneaking.orElse(false));
            }
        }

    }

    private static double sanitize(double value) {
        return Double.isNaN(value) ? (double)0.0F : Math.clamp(value, -1.7179869183E10, 1.7179869183E10);
    }

    private static long pack(double value) {
        return Math.round((value * (double)0.5F + (double)0.5F) * (double)32766.0F);
    }

    private static double unpack(long value) {
        return Math.min((double)(value & 32767L), (double)32766.0F) * (double)2.0F / (double)32766.0F - (double)1.0F;
    }

    public void copy(InteractPacketWrapper wrapper) {
        this.entityID = wrapper.entityID;
        this.interactAction = wrapper.interactAction;
        this.target = wrapper.target;
        this.interactionHand = wrapper.interactionHand;
        this.sneaking = wrapper.sneaking;
    }

    public int getEntityId() {
        return this.entityID;
    }

    public void setEntityId(int entityID) {
        this.entityID = entityID;
    }

    public InteractPacketWrapper.InteractAction getAction() {
        return this.interactAction;
    }

    public void setAction(InteractPacketWrapper.InteractAction interactAction) {
        this.interactAction = interactAction;
    }

    public InteractionHand getHand() {
        return this.interactionHand;
    }

    public void setHand(InteractionHand interactionHand) {
        this.interactionHand = interactionHand;
    }

    public Optional<Vector3d> getTarget() {
        return this.target;
    }

    public void setTarget(Optional<Vector3d> target) {
        this.target = target;
    }

    public Optional<Boolean> isSneaking() {
        return this.sneaking;
    }

    public void setSneaking(Optional<Boolean> sneaking) {
        this.sneaking = sneaking;
    }

    public static enum InteractAction {
        INTERACT,
        ATTACK,
        INTERACT_AT,
        AIR_SWING;

        public static final InteractAction[] VALUES = values();

        private InteractAction() {
        }
    }
}
