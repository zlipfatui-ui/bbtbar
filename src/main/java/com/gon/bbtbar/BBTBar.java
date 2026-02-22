package com.gon.bbtbar;

import com.gon.bbtbar.net.BBTBarNetwork;
import net.minecraftforge.fml.common.Mod;

@Mod(BBTBar.MODID)
public class BBTBar {
    public static final String MODID = "bbtbar";

    public BBTBar() {
        BBTBarNetwork.init();
    }
}
