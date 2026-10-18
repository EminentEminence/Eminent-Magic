/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package emmy.eminentmagic.init;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.GameRules;

@EventBusSubscriber
public class EminentMagicModGameRules {
	public static GameRules.Key<GameRules.BooleanValue> SPELLS_CONSUME_RESERVE;

	@SubscribeEvent
	public static void registerGameRules(FMLCommonSetupEvent event) {
		SPELLS_CONSUME_RESERVE = GameRules.register("spellsConsumeReserve", GameRules.Category.MISC, GameRules.BooleanValue.create(false));
	}
}