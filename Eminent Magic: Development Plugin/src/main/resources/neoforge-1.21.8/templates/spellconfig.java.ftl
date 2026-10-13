<#-- Spell loading and config management are owned by Eminent Magic: Core.
package ${package};

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = "${modid}")
public final class ${JavaModName}SpellConfig {
	private static final Logger LOGGER = LogManager.getLogger(${JavaModName}SpellConfig.class);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Set<String> ELEMENTS = Set.of("fire", "water", "earth", "wind");
	private static volatile Map<String, JsonObject> spells = Map.of();

	private ${JavaModName}SpellConfig() {
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		load(event.getServer());
	}

	public static List<Map.Entry<String, JsonObject>> getAll() {
		return spells.entrySet().stream()
				.map(entry -> Map.entry(entry.getKey(), entry.getValue().deepCopy()))
				.sorted(Map.Entry.comparingByKey())
				.toList();
	}

	private static synchronized void load(MinecraftServer server) {
		Map<String, JsonObject> defaults = loadPackagedDefaults(server.getResourceManager());
		Path configFile = FMLPaths.CONFIGDIR.get().resolve("${modid}/Spells.json");
		JsonObject configRoot = new JsonObject();
		JsonObject configuredSpells = new JsonObject();
		boolean writeConfig = false;

		if (Files.exists(configFile)) {
			try {
				JsonElement parsed = JsonParser.parseString(Files.readString(configFile));
				if (!parsed.isJsonObject() || !parsed.getAsJsonObject().has("spells") || !parsed.getAsJsonObject().get("spells").isJsonObject()) {
					throw new JsonSyntaxException("Expected an object containing a spells object");
				}
				configRoot = parsed.getAsJsonObject();
				configuredSpells = configRoot.getAsJsonObject("spells");
			} catch (IOException | RuntimeException exception) {
				LOGGER.error("Spell config {} is invalid; using packaged defaults without overwriting it.", configFile, exception);
				spells = immutableCopy(defaults);
				return;
			}
		} else {
			writeConfig = true;
		}

		JsonObject normalizedSpells = new JsonObject();
		Map<String, JsonObject> effectiveSpells = new TreeMap<>();
		for (Map.Entry<String, JsonObject> entry : defaults.entrySet()) {
			String spellId = entry.getKey();
			JsonElement configured = configuredSpells.get(spellId);
			JsonObject override = configured != null && configured.isJsonObject() ? configured.getAsJsonObject() : null;
			if (configured != null && override == null) {
				LOGGER.warn("Spell config entry {} is invalid; using its packaged defaults.", spellId);
			}
			JsonObject effective = mergeSpell(spellId, entry.getValue(), override);
			effectiveSpells.put(spellId, effective);
			normalizedSpells.add(spellId, effective.deepCopy());
			if (configured == null || !configured.isJsonObject() || !effective.equals(configured)) {
				writeConfig = true;
			}
		}

		for (Map.Entry<String, JsonElement> entry : configuredSpells.entrySet()) {
			if (!defaults.containsKey(entry.getKey())) {
				normalizedSpells.add(entry.getKey(), entry.getValue().deepCopy());
			}
		}

		spells = Map.copyOf(effectiveSpells);
		if (writeConfig) {
			configRoot.add("spells", normalizedSpells);
			try {
				Files.createDirectories(configFile.getParent());
				Files.writeString(configFile, GSON.toJson(configRoot));
			} catch (IOException exception) {
				LOGGER.error("Could not write spell config {}. Packaged defaults remain active.", configFile, exception);
			}
		}
		LOGGER.info("Loaded {} spell definitions from packaged defaults and config.", spells.size());
	}

	private static Map<String, JsonObject> loadPackagedDefaults(ResourceManager resourceManager) {
		Map<String, JsonObject> defaults = new TreeMap<>();
		Map<ResourceLocation, Resource> resources = resourceManager.listResources("spells", location ->
				location.getNamespace().equals("${modid}") && location.getPath().startsWith("spells/") && location.getPath().endsWith(".json"));
		for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).toList()) {
			ResourceLocation location = entry.getKey();
			String path = location.getPath();
			String spellId = location.getNamespace() + ":" + path.substring("spells/".length(), path.length() - ".json".length());
			try (Reader reader = entry.getValue().openAsReader()) {
				JsonObject definition = JsonParser.parseReader(reader).getAsJsonObject();
				if (!isValidDefinition(definition)) {
					LOGGER.error("Packaged spell definition {} is invalid and will be skipped.", spellId);
					continue;
				}
				defaults.put(spellId, definition);
			} catch (IOException | RuntimeException exception) {
				LOGGER.error("Could not read packaged spell definition {}.", spellId, exception);
			}
		}
		return defaults;
	}

	private static JsonObject mergeSpell(String spellId, JsonObject defaults, JsonObject override) {
		JsonObject effective = override == null ? new JsonObject() : override.deepCopy();
		for (String field : List.of("spellName", "manaCost", "controlScaling", "strengthScaling", "element")) {
			JsonElement candidate = override == null ? null : override.get(field);
			if (candidate != null && isValidField(field, candidate)) {
				effective.add(field, candidate.deepCopy());
			} else {
				if (candidate != null) {
					LOGGER.warn("Invalid value for {}.{}; using the packaged default.", spellId, field);
				}
				effective.add(field, defaults.get(field).deepCopy());
			}
		}
		return effective;
	}

	private static boolean isValidDefinition(JsonObject definition) {
		return List.of("spellName", "manaCost", "controlScaling", "strengthScaling", "element").stream()
				.allMatch(field -> definition.has(field) && isValidField(field, definition.get(field)));
	}

	private static boolean isValidField(String field, JsonElement value) {
		if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
			return false;
		}
		JsonPrimitive primitive = value.getAsJsonPrimitive();
		if (field.equals("spellName")) {
			return primitive.isString() && !primitive.getAsString().isBlank();
		}
		if (field.equals("element")) {
			return primitive.isString() && ELEMENTS.contains(primitive.getAsString());
		}
		if (!primitive.isNumber()) {
			return false;
		}
		double number = primitive.getAsDouble();
		return Double.isFinite(number) && number >= 0;
	}

	private static Map<String, JsonObject> immutableCopy(Map<String, JsonObject> source) {
		Map<String, JsonObject> copy = new TreeMap<>();
		source.forEach((id, definition) -> copy.put(id, definition.deepCopy()));
		return Map.copyOf(copy);
	}
}
-->