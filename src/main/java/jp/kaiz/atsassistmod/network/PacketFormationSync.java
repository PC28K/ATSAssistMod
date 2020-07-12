package jp.kaiz.atsassistmod.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.kaiz.atsassistmod.ATSAssistCore;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class PacketFormationSync implements IMessage, IMessageHandler<PacketFormationSync, IMessage> {
    private boolean toClient;
    private int entityId;
    private NBTTagCompound nbtData = new NBTTagCompound();


    public PacketFormationSync() {
    }

    public PacketFormationSync(EntityTrainBase train, boolean toClient) {
        this.toClient = toClient;
        this.entityId = train.getEntityId();
        if (toClient) {
            train.writeToNBT(this.nbtData);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.toClient = buf.readBoolean();
        this.entityId = buf.readInt();
        if (this.toClient) {
            this.nbtData = ByteBufUtils.readTag(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.toClient);
        buf.writeInt(this.entityId);
        if (this.toClient) {
            ByteBufUtils.writeTag(buf, this.nbtData);
        }
    }

    @Override
    public IMessage onMessage(PacketFormationSync message, MessageContext ctx) {
        if (message.toClient) {
            Entity entity = ATSAssistCore.proxy.getWorld().getEntityByID(message.entityId);
            if (entity instanceof EntityTrainBase) {
                entity.readFromNBT(message.nbtData);
                System.out.println("[Client Thread] Read");
            }
        } else {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            Entity entity = player.worldObj.getEntityByID(message.entityId);
            if (entity instanceof EntityTrainBase) {
                System.out.println("[Server Thread] Return Packet");
                return new PacketFormationSync((EntityTrainBase) entity, true);
            }
        }
        return null;
    }
}
