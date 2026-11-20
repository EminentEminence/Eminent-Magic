package emmy.eminentmagic.spell;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

public final class SpellCatalogGameTests {
	private SpellCatalogGameTests() {
	}

	public static void configOverrides(GameTestHelper helper) {
		ResourceLocation configuredId = ResourceLocation.fromNamespaceAndPath("eminent_magic", "configured_test");
		ResourceLocation defaultId = ResourceLocation.fromNamespaceAndPath("eminent_magic", "default_test");
		SpellDefinition configuredDefault = new SpellDefinition(configuredId, "Packaged name", 10, 1, 2, "Fire",
				true, "packaged_cast", "packaged_charge", "packaged_channel");
		SpellDefinition untouchedDefault = new SpellDefinition(defaultId, "Default name", 3, 0, 0, "Water",
				false, null, null, null);
		Map<ResourceLocation, SpellDefinition> defaults = Map.of(configuredId, configuredDefault, defaultId, untouchedDefault);
		JsonObject config = JsonParser.parseString("""
				{
				  "schemaVersion": 1,
				  "spells": [
				    {
				      "id": "eminent_magic:configured_test",
				      "displayName": "Configured name",
				      "baseCost": 25,
				      "strengthScaling": 4,
				      "controlScaling": 5,
				      "element": "Ice",
				      "channelled": false,
				      "hooks": { "onCast": "client_override" }
				    },
				    { "id": "missing:spell", "displayName": "Unknown spell", "baseCost": 99 },
				    { "displayName": "Missing ID", "baseCost": 100 }
				  ]
				}
				""").getAsJsonObject();

		Map<ResourceLocation, SpellDefinition> resolved = SpellCatalogConfig.resolve(defaults, config);
		SpellDefinition configured = resolved.get(configuredId);
		check(helper, configured != null, "A configured packaged spell was not loaded");
		check(helper, configured != null && configured.displayName().equals("Configured name")
				&& configured.baseCost() == 25 && configured.strengthScaling() == 4
				&& configured.controlScaling() == 5 && configured.element().equals("Ice"),
				"User-configurable spell fields were not applied");
		check(helper, configured != null && configured.channelled() && configured.onCast().equals("packaged_cast")
				&& configured.whileCharging().equals("packaged_charge")
				&& configured.whileChannelling().equals("packaged_channel"),
				"Config changed packaged channeling or hook behavior");
		check(helper, resolved.get(defaultId) == untouchedDefault,
				"A spell omitted from config did not retain its packaged defaults");
		check(helper, resolved.size() == defaults.size() && !resolved.containsKey(
				ResourceLocation.fromNamespaceAndPath("missing", "spell")),
				"An unknown config ID was loaded as a spell");

		JsonObject generatedConfig = SpellCatalogConfig.createConfig(defaults);
		var generatedEntries = generatedConfig.getAsJsonArray("spells");
		check(helper, generatedEntries.size() == defaults.size(), "Default config generation omitted packaged spells");
		for (var element : generatedEntries) {
			JsonObject entry = element.getAsJsonObject();
			check(helper, !entry.has("channelled") && !entry.has("hooks"),
					"Generated user config includes code-owned spell fields");
			check(helper, entry.has("id"), "Generated default config omitted a spell ID");
		}
		helper.succeed();
	}

	private static void check(GameTestHelper helper, boolean condition, String message) {
		if (!condition) {
			throw helper.assertionException(message);
		}
	}
}
