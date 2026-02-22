package com.gon.bbtbar.net;

import com.gon.bbtbar.client.ClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CCleanlinessSync(float value) {

    public static void encode(S2CCleanlinessSync msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.value);
    }

    public static S2CCleanlinessSync decode(FriendlyByteBuf buf) {
        return new S2CCleanlinessSync(buf.readFloat());
    }

    public static void handle(S2CCleanlinessSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientState.cleanliness = msg.value);
        ctx.get().setPacketHandled(true);
    }
}
