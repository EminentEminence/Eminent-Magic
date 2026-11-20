package emmy.eminentmagic.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.Minecraft;

import emmy.eminentmagic.init.EminentMagicModKeyMappings;
import emmy.eminentmagic.network.ControlSpellControlMessage;
import emmy.eminentmagic.network.ControlSpellStrengthMessage;

@EventBusSubscriber(Dist.CLIENT)
public final class SpellControlInputHandler {
	private SpellControlInputHandler() {
	}

	@SubscribeEvent
	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.screen != null) {
			return;
		}

		int direction = Double.compare(event.getScrollDeltaY(), 0);
		if (direction == 0) {
			return;
		}

		boolean handled = false;
		if (EminentMagicModKeyMappings.CONTROL_SPELL_STRENGTH.isDown()) {
			ClientPacketDistributor.sendToServer(new ControlSpellStrengthMessage(direction, 0));
			handled = true;
		}
		if (EminentMagicModKeyMappings.CONTROL_SPELL_CONTROL.isDown()) {
			ClientPacketDistributor.sendToServer(new ControlSpellControlMessage(direction, 0));
			handled = true;
		}
		if (handled) {
			event.setCanceled(true);
		}
	}
}
