package com.gon.bbtbar.net;

import com.gon.bbtbar.BBTBar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BBTBarNetwork {
    private BBTBarNetwork() {}

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BBTBar.MODID, "net"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
    );

    private static int id = 0;

    public static void init() {
        CHANNEL.messageBuilder(S2CCleanlinessSync.class, id++)
                .encoder(S2CCleanlinessSync::encode)
                .decoder(S2CCleanlinessSync::decode)
                .consumerMainThread(S2CCleanlinessSync::handle)
                .add();
    }

    public static void sendCleanlinessTo(ServerPlayer player, float value) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2CCleanlinessSync(value));
    }
}
