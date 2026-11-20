/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package emmy.eminentmagic.init;

import org.lwjgl.glfw.GLFW;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import emmy.eminentmagic.network.ToggleActivePerceptionMessage;
import emmy.eminentmagic.network.OpenSkillsSummaryMessage;

@EventBusSubscriber(Dist.CLIENT)
public class EminentMagicModKeyMappings {
	public static final KeyMapping OPEN_SKILLS_SUMMARY = new KeyMapping("key.eminent_magic.open_skills_summary", GLFW.GLFW_KEY_BACKSLASH, "key.categories.eminentmagic") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPacketDistributor.sendToServer(new OpenSkillsSummaryMessage(0, 0));
				OpenSkillsSummaryMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping CONTROL_SPELL_STRENGTH = new KeyMapping("key.eminent_magic.control_spell_strength", GLFW.GLFW_KEY_Z, "key.categories.eminentmagic");
	public static final KeyMapping CONTROL_SPELL_CONTROL = new KeyMapping("key.eminent_magic.control_spell_control", GLFW.GLFW_KEY_X, "key.categories.eminentmagic");
	public static final KeyMapping TOGGLE_ACTIVE_PERCEPTION = new KeyMapping("key.eminent_magic.toggle_active_perception", GLFW.GLFW_KEY_C, "key.categories.eminentmagic") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ClientPacketDistributor.sendToServer(new ToggleActivePerceptionMessage(0, 0));
				ToggleActivePerceptionMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping CYCLE_SPELL = new KeyMapping("key.eminent_magic.cycle_spell", GLFW.GLFW_KEY_V, "key.categories.eminentmagic");

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(OPEN_SKILLS_SUMMARY);
		event.register(CONTROL_SPELL_STRENGTH);
		event.register(CONTROL_SPELL_CONTROL);
		event.register(TOGGLE_ACTIVE_PERCEPTION);
		event.register(CYCLE_SPELL);
	}

	@EventBusSubscriber(Dist.CLIENT)
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event) {
			if (Minecraft.getInstance().screen == null) {
				OPEN_SKILLS_SUMMARY.consumeClick();
				TOGGLE_ACTIVE_PERCEPTION.consumeClick();
			}
		}
	}
}