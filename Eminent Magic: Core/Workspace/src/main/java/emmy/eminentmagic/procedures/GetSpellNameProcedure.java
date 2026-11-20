package emmy.eminentmagic.procedures;

public class GetSpellNameProcedure {
	public static String execute() {
		return net.minecraft.client.Minecraft.getInstance().player instanceof net.minecraft.world.entity.player.Player player
				? emmy.eminentmagic.staff.StaffService.getSelectedSpell(player).map(emmy.eminentmagic.spell.SpellDefinition::displayName).orElse("No Spell Selected")
				: "No Spell Selected";
	}
}