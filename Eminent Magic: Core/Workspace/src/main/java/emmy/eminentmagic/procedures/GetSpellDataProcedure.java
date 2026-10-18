package emmy.eminentmagic.procedures;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;

public class GetSpellDataProcedure {
	public static com.google.gson.JsonObject execute(double spellID) {
		File SpellInfoFile = new File("");
		com.google.gson.JsonArray SpellInfoArray = new com.google.gson.JsonArray();
		double counter = 0;
		com.google.gson.JsonObject SpellInfoArrayObject = new com.google.gson.JsonObject();
		com.google.gson.JsonObject SpellInfoObject = new com.google.gson.JsonObject();
		com.google.gson.JsonObject emptySpellObject = new com.google.gson.JsonObject();
		boolean found = false;
		SpellInfoFile = new File((FMLPaths.GAMEDIR.get().toString() + "/config/eminentmagic/"), File.separator + "Spells.json");
		{
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(SpellInfoFile));
				StringBuilder jsonstringbuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					jsonstringbuilder.append(line);
				}
				bufferedReader.close();
				SpellInfoArrayObject = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
				SpellInfoArray = SpellInfoArrayObject.get("spellInfo").getAsJsonArray();
				for (int index0 = 0; index0 < (int) SpellInfoArray.size(); index0++) {
					SpellInfoObject = SpellInfoArray.get((int) counter).getAsJsonObject();
					if (SpellInfoObject.get("spellID").getAsDouble() == spellID) {
						found = true;
						break;
					}
					counter = counter + 1;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (found) {
			return SpellInfoObject;
		}
		return emptySpellObject;
	}
}