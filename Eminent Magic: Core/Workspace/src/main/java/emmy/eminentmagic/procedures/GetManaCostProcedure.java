package emmy.eminentmagic.procedures;

public class GetManaCostProcedure {
	public static String execute() {
		return net.minecraft.client.Minecraft.getInstance().player instanceof net.minecraft.world.entity.player.Player player
				? emmy.eminentmagic.staff.StaffService.getSelectedSpell(player)
						.map(spell -> new java.text.DecimalFormat("####").format(Math.max(0,
								emmy.eminentmagic.spell.SpellCastingService.calculateManaCost(spell, player.getData(emmy.eminentmagic.network.EminentMagicModVariables.PLAYER_VARIABLES).spellControl,
										player.getData(emmy.eminentmagic.network.EminentMagicModVariables.PLAYER_VARIABLES).spellStrength))))
						.orElse("")
				: "";
	}
}