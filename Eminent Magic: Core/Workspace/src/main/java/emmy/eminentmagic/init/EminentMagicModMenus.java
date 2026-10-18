/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package emmy.eminentmagic.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;

import java.util.Map;

import emmy.eminentmagic.world.inventory.SeventyfiveByFifteenMenu;
import emmy.eminentmagic.world.inventory.ManaSkillsSummaryGUIMenu;
import emmy.eminentmagic.world.inventory.HundredByTwentyfiveMenu;
import emmy.eminentmagic.network.MenuStateUpdateMessage;
import emmy.eminentmagic.EminentMagicMod;

public class EminentMagicModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, EminentMagicMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<ManaSkillsSummaryGUIMenu>> MANA_SKILLS_SUMMARY_GUI = REGISTRY.register("mana_skills_summary_gui", () -> IMenuTypeExtension.create(ManaSkillsSummaryGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<HundredByTwentyfiveMenu>> HUNDRED_BY_TWENTYFIVE = REGISTRY.register("hundred_by_twentyfive", () -> IMenuTypeExtension.create(HundredByTwentyfiveMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<SeventyfiveByFifteenMenu>> SEVENTYFIVE_BY_FIFTEEN = REGISTRY.register("seventyfive_by_fifteen", () -> IMenuTypeExtension.create(SeventyfiveByFifteenMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide) {
				if (Minecraft.getInstance().screen instanceof EminentMagicModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				ClientPacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}