package emmy.eminentmagic.procedures;

public class CalculateManaCostProcedure {
	public static double execute(double baseCost, double controlScaling, double spellControl, double spellStrength, double strengthScaling) {
		return baseCost * ((spellControl * controlScaling) / 10) * ((spellStrength * strengthScaling) / 10) + baseCost;
	}
}