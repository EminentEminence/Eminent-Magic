package emmy.eminentmagic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;

import emmy.eminentmagic.EminentMagicMod;

@EventBusSubscriber(modid = EminentMagicMod.MODID, value = Dist.CLIENT)
public class ChannelingOutlineFrameGraphListener {
	@SubscribeEvent
	public static void enableOutlineProcessing(FrameGraphSetupEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level != null) {
			for (Player player : minecraft.level.players()) {
				if (ChannelingOutlineClientRendering.isOutlineActive(player)) {
					event.enableOutlineProcessing();
					return;
				}
			}
		}
	}
}
