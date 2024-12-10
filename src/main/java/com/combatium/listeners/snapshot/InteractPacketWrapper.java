package com.combatium.listeners.snapshot;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.Optional;

public class InteractPacketWrapper extends PacketWrapper<InteractPacketWrapper> {
    private int entityID;
    private InteractPacketWrapper.InteractAction interactAction;
    private Optional<Vector3f> target;
    private InteractionHand interactionHand;
    private Optional<Boolean> sneaking;

    public InteractPacketWrapper(PacketReceiveEvent event) {
        super(event);
    }

    public InteractPacketWrapper(int entityID, InteractPacketWrapper.InteractAction interactAction, InteractionHand interactionHand, Optional<Vector3f> target, Optional<Boolean> sneaking) {
        super(PacketType.Play.Client.INTERACT_ENTITY);
        this.entityID = entityID;
        this.interactAction = interactAction;
        this.interactionHand = interactionHand;
        this.target = target;
        this.sneaking = sneaking;
    }

    public void read() {
        if (this.serverVersion == ServerVersion.V_1_7_10) {
            this.entityID = this.readInt();
            byte typeIndex = this.readByte();
            this.interactAction = InteractPacketWrapper.InteractAction.VALUES[typeIndex];
            this.target = Optional.empty();
            this.interactionHand = InteractionHand.MAIN_HAND;
            this.sneaking = Optional.empty();
        } else {
            this.entityID = this.readVarInt();
            int typeIndex = this.readVarInt();
            this.interactAction = InteractPacketWrapper.InteractAction.VALUES[typeIndex];
            if (this.interactAction == InteractPacketWrapper.InteractAction.INTERACT_AT) {
                float x = this.readFloat();
                float y = this.readFloat();
                float z = this.readFloat();
                this.target = Optional.of(new Vector3f(x, y, z));
            } else {
                this.target = Optional.empty();
            }

            if (!this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) || this.interactAction != InteractPacketWrapper.InteractAction.INTERACT && this.interactAction != InteractPacketWrapper.InteractAction.INTERACT_AT) {
                this.interactionHand = InteractionHand.MAIN_HAND;
            } else {
                int handID = this.readVarInt();
                this.interactionHand = InteractionHand.getById(handID);
            }

            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
                this.sneaking = Optional.of(this.readBoolean());
            } else {
                this.sneaking = Optional.empty();
            }
        }

    }

    public void write() {
        if (this.serverVersion == ServerVersion.V_1_7_10) {
            this.writeInt(this.entityID);
            this.writeByte(this.interactAction.ordinal());
        } else {
            this.writeVarInt(this.entityID);
            this.writeVarInt(this.interactAction.ordinal());
            if (this.interactAction == InteractPacketWrapper.InteractAction.INTERACT_AT) {
                Vector3f targetVec = (Vector3f) this.target.orElse(new Vector3f(0.0F, 0.0F, 0.0F));
                this.writeFloat(targetVec.x);
                this.writeFloat(targetVec.y);
                this.writeFloat(targetVec.z);
            }

            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) && (this.interactAction == InteractPacketWrapper.InteractAction.INTERACT || this.interactAction == InteractPacketWrapper.InteractAction.INTERACT_AT)) {
                this.writeVarInt(this.interactionHand.getId());
            }

            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
                this.writeBoolean((Boolean) this.sneaking.orElse(false));
            }
        }

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

    public Optional<Vector3f> getTarget() {
        return this.target;
    }

    public void setTarget(Optional<Vector3f> target) {
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
