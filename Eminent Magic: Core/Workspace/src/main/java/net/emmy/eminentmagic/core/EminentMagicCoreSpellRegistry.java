package net.emmy.eminentmagic.core;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@EventBusSubscriber(modid = EminentMagicCoreMod.MODID)
public final class EminentMagicCoreSpellRegistry {
	private static final Logger LOGGER = LogManager.getLogger(EminentMagicCoreSpellRegistry.class);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Set<String> ELEMENTS = Set.of("pure", "fire", "water", "earth", "wind");
	private static final List<String> DEFINITION_FIELDS = List.of("spellName", "baseCost", "controlScaling", "strengthScaling", "element", "duration");
	private static volatile Map<String, Map<String, JsonObject>> spellsByNamespace = Map.of();
	private static volatile Map<ResourceLocation, SpellDefinition> spellDefinitions = Map.of();

	private EminentMagicCoreSpellRegistry() {
	}

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		reload(event.getServer());
	}

	public static Optional<SpellDefinition> findSpell(ResourceLocation id) {
		return Optional.ofNullable(id == null ? null : spellDefinitions.get(id));
	}

	public static Optional<SpellDefinition> findSpell(String id) {
		return id == null ? Optional.empty() : Optional.ofNullable(ResourceLocation.tryParse(id)).flatMap(EminentMagicCoreSpellRegistry::findSpell);
	}

	public static List<SpellDefinition> getAll(String namespace) {
		return spellDefinitions.values().stream()
				.filter(spell -> spell.id().getNamespace().equals(namespace))
				.sorted(Comparator.comparing(spell -> spell.id().toString()))
				.toList();
	}

	public static List<SpellDefinition> getAll() {
		return spellDefinitions.values().stream()
				.sorted(Comparator.comparing(spell -> spell.id().toString()))
				.toList();
	}

	public static synchronized void reload(MinecraftServer server) {
		Map<String, Map<String, JsonObject>> defaults = loadPackagedDefaults(server.getResourceManager());
		Map<String, Map<String, JsonObject>> loaded = new TreeMap<>();
		for (Map.Entry<String, Map<String, JsonObject>> namespaceEntry : defaults.entrySet()) {
			String namespace = namespaceEntry.getKey();
			Map<String, JsonObject> namespaceDefaults = namespaceEntry.getValue();
			Path configFile = FMLPaths.CONFIGDIR.get().resolve(namespace).resolve("Spells.json");
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
					loaded.put(namespace, immutableCopy(namespaceDefaults));
					continue;
				}
			} else {
				writeConfig = true;
				JsonObject imported = importLegacyConfig(namespace, namespaceDefaults, configFile);
				if (imported != null) {
					configRoot = imported;
					configuredSpells = imported.getAsJsonObject("spells");
				}
			}

			JsonObject normalizedSpells = new JsonObject();
			Map<String, JsonObject> effectiveSpells = new TreeMap<>();
			for (Map.Entry<String, JsonObject> entry : namespaceDefaults.entrySet()) {
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
				if (!namespaceDefaults.containsKey(entry.getKey())) {
					normalizedSpells.add(entry.getKey(), entry.getValue().deepCopy());
				}
			}

			loaded.put(namespace, Map.copyOf(effectiveSpells));
			if (writeConfig) {
				configRoot.add("spells", normalizedSpells);
				try {
					Files.createDirectories(configFile.getParent());
					Files.writeString(configFile, GSON.toJson(configRoot));
				} catch (IOException exception) {
					LOGGER.error("Could not write spell config {}. Packaged defaults remain active.", configFile, exception);
				}
			}
		}

		spellsByNamespace = Map.copyOf(loaded);
		Map<ResourceLocation, SpellDefinition> definitions = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
		loaded.values().forEach(namespaceSpells -> namespaceSpells.forEach((spellId, definition) -> {
			ResourceLocation id = ResourceLocation.tryParse(spellId);
			if (id == null) {
				LOGGER.error("Spell definition has an invalid namespaced ID and will be skipped: {}", spellId);
				return;
			}
			definitions.put(id, toSpellDefinition(id, definition));
		}));
		spellDefinitions = Map.copyOf(definitions);
		LOGGER.info("Loaded {} spell definitions across {} mod namespaces.", getAll().size(), loaded.size());
	}

	private static Map<String, Map<String, JsonObject>> loadPackagedDefaults(ResourceManager resourceManager) {
		Map<String, Map<String, JsonObject>> defaults = new TreeMap<>();
		Map<ResourceLocation, Resource> resources = resourceManager.listResources("spells", location ->
				location.getPath().startsWith("spells/") && location.getPath().endsWith(".json"));
		for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).toList()) {
			ResourceLocation location = entry.getKey();
			String path = location.getPath();
			String spellId = location.getNamespace() + ":" + path.substring("spells/".length(), path.length() - ".json".length());
			try (Reader reader = entry.getValue().openAsReader()) {
				JsonObject source = JsonParser.parseReader(reader).getAsJsonObject();
				JsonObject definition = normalizePackagedDefinition(spellId, source);
				if (definition == null) {
					LOGGER.error("Packaged spell definition {} is invalid and will be skipped.", spellId);
					continue;
				}
				defaults.computeIfAbsent(location.getNamespace(), ignored -> new TreeMap<>()).put(spellId, definition);
			} catch (IOException | RuntimeException exception) {
				LOGGER.error("Could not read packaged spell definition {}.", spellId, exception);
			}
		}
		return defaults;
	}

	private static JsonObject mergeSpell(String spellId, JsonObject defaults, JsonObject override) {
		JsonObject effective = override == null ? new JsonObject() : override.deepCopy();
		effective.remove("manaCost");
		for (String field : DEFINITION_FIELDS) {
			JsonElement candidate = override == null ? null : getField(override, field);
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

	private static JsonObject normalizePackagedDefinition(String spellId, JsonObject source) {
		JsonElement spellName = source.get("spellName");
		JsonElement baseCost = getField(source, "baseCost");
		if (!isValidField("spellName", spellName) || !isValidField("baseCost", baseCost)) {
			return null;
		}

		JsonObject normalized = new JsonObject();
		normalized.add("spellName", spellName.deepCopy());
		normalized.add("baseCost", baseCost.deepCopy());
		for (String field : List.of("controlScaling", "strengthScaling", "element", "duration")) {
			JsonElement value = getField(source, field);
			if (value == null) {
				normalized.add(field, defaultValue(field));
			} else if (isValidField(field, value)) {
				normalized.add(field, value.deepCopy());
			} else {
				LOGGER.warn("Invalid packaged value for {}.{}; using the canonical default.", spellId, field);
				normalized.add(field, defaultValue(field));
			}
		}
		return normalized;
	}

	private static JsonElement getField(JsonObject object, String field) {
		if (object == null) {
			return null;
		}
		if (field.equals("baseCost") && !object.has(field)) {
			return object.get("manaCost");
		}
		return object.get(field);
	}

	private static JsonPrimitive defaultValue(String field) {
		return switch (field) {
			case "spellName" -> new JsonPrimitive("");
			case "baseCost" -> new JsonPrimitive(0.0);
			case "controlScaling", "strengthScaling" -> new JsonPrimitive(1.0);
			case "element" -> new JsonPrimitive("pure");
			case "duration" -> new JsonPrimitive(0);
			default -> throw new IllegalArgumentException("No spell default for " + field);
		};
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
		return Double.isFinite(number) && number >= 0 && (!field.equals("duration") || number <= Integer.MAX_VALUE && number == Math.rint(number));
	}

	private static SpellDefinition toSpellDefinition(ResourceLocation id, JsonObject definition) {
		return new SpellDefinition(id, definition.get("spellName").getAsString(), definition.get("baseCost").getAsDouble(),
				definition.get("controlScaling").getAsDouble(), definition.get("strengthScaling").getAsDouble(),
				definition.get("element").getAsString(), definition.get("duration").getAsInt());
	}

	private static JsonObject importLegacyConfig(String namespace, Map<String, JsonObject> defaults, Path destination) {
		if (!namespace.equals("eminent_magic_world")) {
			return null;
		}
		Path legacyFile = FMLPaths.CONFIGDIR.get().resolve("eminentmagic/Spells.json");
		if (!Files.exists(legacyFile)) {
			return null;
		}

		Path backupFile = legacyFile.resolveSibling("Spells.json.legacy-backup");
		try {
			if (!Files.exists(backupFile)) {
				Files.copy(legacyFile, backupFile, StandardCopyOption.COPY_ATTRIBUTES);
			}
			JsonElement parsed = JsonParser.parseString(Files.readString(legacyFile));
			if (!parsed.isJsonObject() || !parsed.getAsJsonObject().has("spellInfo") || !parsed.getAsJsonObject().get("spellInfo").isJsonArray()) {
				throw new JsonSyntaxException("Expected a spellInfo array");
			}

			Map<String, Integer> legacyIds = Map.of("eminent_magic_world:zoltraak", 1, "eminent_magic_world:freeze_coffin", 2);
			JsonObject importedSpells = new JsonObject();
			for (JsonElement entry : parsed.getAsJsonObject().getAsJsonArray("spellInfo")) {
				if (!entry.isJsonObject()) {
					LOGGER.warn("Skipping a non-object legacy spell entry in {}.", legacyFile);
					continue;
				}
				JsonObject legacySpell = entry.getAsJsonObject();
				if (!legacySpell.has("spellID") || !legacySpell.get("spellID").isJsonPrimitive() || !legacySpell.getAsJsonPrimitive("spellID").isNumber()) {
					LOGGER.warn("Skipping a legacy spell entry without a numeric spellID in {}.", legacyFile);
					continue;
				}
				int legacyId = legacySpell.get("spellID").getAsInt();
				String spellId = legacyIds.entrySet().stream().filter(entryId -> entryId.getValue() == legacyId).map(Map.Entry::getKey).findFirst().orElse(null);
				if (spellId == null || !defaults.containsKey(spellId)) {
					if (legacyId != 0) {
						LOGGER.warn("Legacy spell ID {} was not imported; it is unknown, deferred, or has no packaged spell definition. Original data is preserved at {}.", legacyId, backupFile);
					}
					continue;
				}

				JsonObject converted = new JsonObject();
				copyLegacyField(legacySpell, converted, "baseCost", "baseCost");
				copyLegacyField(legacySpell, converted, "costControlScaling", "controlScaling");
				copyLegacyField(legacySpell, converted, "costStrengthScaling", "strengthScaling");
				if (legacySpell.has("sourceCost") && legacySpell.get("sourceCost").isJsonPrimitive() && legacySpell.getAsJsonPrimitive("sourceCost").isNumber()
						&& legacySpell.get("sourceCost").getAsDouble() != 0) {
					LOGGER.warn("Legacy spell {} has sourceCost {}; Source-specific data was not imported. Original data is preserved at {}.", legacyId, legacySpell.get("sourceCost"), backupFile);
				}
				importedSpells.add(spellId, converted);
			}

			JsonObject result = new JsonObject();
			result.add("spells", importedSpells);
			LOGGER.info("Imported {} legacy spell overrides into {}. Original data is preserved at {}.", importedSpells.size(), destination, backupFile);
			return result;
		} catch (IOException | RuntimeException exception) {
			LOGGER.error("Could not import legacy spell config {}. Packaged defaults remain active; inspect or repair the file before retrying. A backup is at {} when available.", legacyFile, backupFile, exception);
			return null;
		}
	}

	private static void copyLegacyField(JsonObject source, JsonObject destination, String sourceField, String targetField) {
		if (source.has(sourceField)) {
			destination.add(targetField, source.get(sourceField).deepCopy());
		}
	}

	private static Map<String, JsonObject> immutableCopy(Map<String, JsonObject> source) {
		Map<String, JsonObject> copy = new TreeMap<>();
		source.forEach((id, definition) -> copy.put(id, definition.deepCopy()));
		return Map.copyOf(copy);
	}

	public record SpellDefinition(ResourceLocation id, String spellName, double baseCost, double controlScaling,
			double strengthScaling, String element, int duration) {
	}
}