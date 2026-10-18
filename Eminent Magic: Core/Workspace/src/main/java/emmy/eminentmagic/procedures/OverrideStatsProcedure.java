package emmy.eminentmagic.procedures;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import emmy.eminentmagic.init.EminentMagicModMenus;
import emmy.eminentmagic.init.EminentMagicModAttributes;

public class OverrideStatsProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_RESERVES))
			_livingEntity1.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(new Object() {
				double convert(String s) {
					try {
						return Double.parseDouble(s.trim());
					} catch (Exception e) {
					}
					return 0;
				}
			}.convert((entity instanceof Player _entity0 && _entity0.containerMenu instanceof EminentMagicModMenus.MenuAccessor _menu0) ? _menu0.getMenuState(0, "ManaReserves", "") : ""));
		if (entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_STRENGTH))
			_livingEntity3.getAttribute(EminentMagicModAttributes.MANA_STRENGTH).setBaseValue(new Object() {
				double convert(String s) {
					try {
						return Double.parseDouble(s.trim());
					} catch (Exception e) {
					}
					return 0;
				}
			}.convert((entity instanceof Player _entity2 && _entity2.containerMenu instanceof EminentMagicModMenus.MenuAccessor _menu2) ? _menu2.getMenuState(0, "ManaStrength", "") : ""));
		if (entity instanceof LivingEntity _livingEntity5 && _livingEntity5.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CONTROL))
			_livingEntity5.getAttribute(EminentMagicModAttributes.MANA_CONTROL).setBaseValue(new Object() {
				double convert(String s) {
					try {
						return Double.parseDouble(s.trim());
					} catch (Exception e) {
					}
					return 0;
				}
			}.convert((entity instanceof Player _entity4 && _entity4.containerMenu instanceof EminentMagicModMenus.MenuAccessor _menu4) ? _menu4.getMenuState(0, "ManaControl", "") : ""));
		if (entity instanceof LivingEntity _livingEntity7 && _livingEntity7.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_PERCEPTION))
			_livingEntity7.getAttribute(EminentMagicModAttributes.MANA_PERCEPTION).setBaseValue(new Object() {
				double convert(String s) {
					try {
						return Double.parseDouble(s.trim());
					} catch (Exception e) {
					}
					return 0;
				}
			}.convert((entity instanceof Player _entity6 && _entity6.containerMenu instanceof EminentMagicModMenus.MenuAccessor _menu6) ? _menu6.getMenuState(0, "ManaPerception", "") : ""));
	}
}