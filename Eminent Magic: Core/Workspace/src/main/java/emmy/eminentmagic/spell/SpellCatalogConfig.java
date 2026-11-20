package emmy.eminentmagic.spell;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.fml.loading.FMLPaths;

import emmy.eminentmagic.EminentMagicMod;

final class SpellCatalogConfig {
	static final int SCHEMA_VERSION = 1;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private SpellCatalogConfig() {
	}

	static Map<ResourceLocation, SpellDefinition> load(Map<ResourceLocation, SpellDefinition> defaults) {
		Path configDirectory = FMLPaths.CONFIGDIR.get().resolve(EminentMagicMod.MODID);
		Path configFile = configDirectory.resolve("spells.json");
		try {
			Files.createDirectories(configDirectory);
			JsonObject config;
			if (Files.exists(configFile)) {
				try (Reader reader = Files.newBufferedReader(configFile)) {
					config = JsonParser.parseReader(reader).getAsJsonObject();
				}
			} else {
				config = readLegacyConfig(configDirectory, defaults);
			}

			int schemaVersion = GsonHelper.getAsInt(config, "schemaVersion", SCHEMA_VERSION);
			if (schemaVersion != SCHEMA_VERSION) {
				throw new IllegalArgumentException("Unsupported spell config schema version " + schemaVersion);
			}

			Map<ResourceLocation, SpellDefinition> resolved = resolve(defaults, config);
			write(configFile, resolved);
			return resolved;
		} catch (IOException | RuntimeException exception) {
			throw new IllegalStateException("Could not load spell config " + configFile, exception);
		}
	}

	static void reset(Map<ResourceLocation, SpellDefinition> defaults) throws IOException {
		if (defaults.isEmpty()) {
			throw new IllegalStateException("Spell defaults have not been loaded");
		}
		Path configFile = FMLPaths.CONFIGDIR.get().resolve(EminentMagicMod.MODID).resolve("spells.json");
		Files.createDirectories(configFile.getParent());
		write(configFile, defaults);
	}

	static Map<ResourceLocation, SpellDefinition> resolve(Map<ResourceLocation, SpellDefinition> defaults, JsonObject config) {
		if (GsonHelper.getAsInt(config, "schemaVersion", SCHEMA_VERSION) != SCHEMA_VERSION) {
			throw new IllegalArgumentException("Unsupported spell config schema version");
		}

		Map<ResourceLocation, SpellDefinition> resolved = new TreeMap<>(defaults);
		Map<ResourceLocation, Boolean> seen = new LinkedHashMap<>();
		JsonArray entries = GsonHelper.getAsJsonArray(config, "spells", new JsonArray());
		for (JsonElement entryElement : entries) {
			if (!entryElement.isJsonObject()) {
				EminentMagicMod.LOGGER.warn("Ignoring non-object entry in spell config");
				continue;
			}
			JsonObject entry = entryElement.getAsJsonObject();
			ResourceLocation id = configuredId(entry);
			if (id == null) {
				EminentMagicMod.LOGGER.warn("Ignoring spell config entry without a valid id");
				continue;
			}
			SpellDefinition base = defaults.get(id);
			if (base == null) {
				EminentMagicMod.LOGGER.warn("Ignoring spell config for unknown spell {}", id);
				continue;
			}
			if (seen.putIfAbsent(id, Boolean.TRUE) != null) {
				EminentMagicMod.LOGGER.warn("Ignoring duplicate spell config entry for {}", id);
				continue;
			}
			if (entry.has("channelled") || entry.has("hooks")) {
				EminentMagicMod.LOGGER.warn("Ignoring non-configurable channelled/hooks fields for spell {}", id);
			}
			try {
				resolved.put(id, applyOverrides(base, entry));
			} catch (RuntimeException exception) {
				EminentMagicMod.LOGGER.error("Ignoring invalid spell config for {}", id, exception);
			}
		}
		return Map.copyOf(resolved);
	}

	static JsonObject createConfig(Map<ResourceLocation, SpellDefinition> definitions) {
		JsonObject config = new JsonObject();
		config.addProperty("schemaVersion", SCHEMA_VERSION);
		JsonArray entries = new JsonArray();
		new TreeMap<>(definitions).values().forEach(definition -> entries.add(configEntry(definition)));
		config.add("spells", entries);
		return config;
	}

	private static JsonObject readLegacyConfig(Path configDirectory, Map<ResourceLocation, SpellDefinition> defaults) {
		Path legacyDirectory = configDirectory.resolve("spells");
		JsonObject config = new JsonObject();
		config.addProperty("schemaVersion", SCHEMA_VERSION);
		JsonArray entries = new JsonArray();
		if (!Files.isDirectory(legacyDirectory)) {
			config.add("spells", entries);
			return config;
		}
		try (Stream<Path> files = Files.walk(legacyDirectory)) {
			files.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json")).forEach(path -> {
				try (Reader reader = Files.newBufferedReader(path)) {
					JsonObject entry = JsonParser.parseReader(reader).getAsJsonObject();
					if (!entry.has("id")) {
						ResourceLocation fallbackId = idFromLegacyPath(legacyDirectory, path);
						if (defaults.containsKey(fallbackId)) {
							entry.addProperty("id", fallbackId.toString());
						}
					}
					entries.add(entry);
				} catch (IOException | RuntimeException exception) {
					EminentMagicMod.LOGGER.error("Could not migrate legacy spell config {}", path, exception);
				}
			});
		} catch (IOException exception) {
			EminentMagicMod.LOGGER.error("Could not read legacy spell configs under {}", legacyDirectory, exception);
		}
		config.add("spells", entries);
		return config;
	}

	private static ResourceLocation idFromLegacyPath(Path legacyDirectory, Path path) {
		Path relative = legacyDirectory.relativize(path);
		if (relative.getNameCount() < 2) {
			return ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "invalid_legacy_spell");
		}
		String namespace = relative.getName(0).toString();
		String spellPath = relative.subpath(1, relative.getNameCount()).toString()
				.replace(path.getFileSystem().getSeparator(), "/");
		spellPath = spellPath.substring(0, spellPath.length() - ".json".length());
		ResourceLocation pathId = ResourceLocation.tryParse(namespace + ":" + spellPath);
		return pathId == null ? ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "invalid_legacy_spell") : pathId;
	}

	private static ResourceLocation configuredId(JsonObject entry) {
		if (!entry.has("id") || !entry.get("id").isJsonPrimitive()) {
			return null;
		}
		String rawId = entry.get("id").getAsString();
		if (rawId.isBlank()) {
			return null;
		}
		return rawId.indexOf(':') >= 0
				? ResourceLocation.tryParse(rawId)
				: ResourceLocation.tryParse(EminentMagicMod.MODID + ":" + rawId);
	}

	private static SpellDefinition applyOverrides(SpellDefinition base, JsonObject config) {
		String displayName = stringValue(config, "displayName", base.displayName());
		double baseCost = doubleValue(config, "baseCost", base.baseCost());
		double strengthScaling = doubleValue(config, "strengthScaling", base.strengthScaling());
		double controlScaling = doubleValue(config, "controlScaling", base.controlScaling());
		String element = stringValue(config, "element", base.element());
		return new SpellDefinition(base.id(), displayName, baseCost, strengthScaling, controlScaling, element,
				base.channelled(), base.onCast(), base.whileCharging(), base.whileChannelling());
	}

	private static String stringValue(JsonObject json, String key, String fallback) {
		if (!json.has(key)) {
			return fallback;
		}
		String value = json.get(key).getAsString();
		if (value.isBlank()) {
			throw new IllegalArgumentException(key + " must not be blank");
		}
		return value;
	}

	private static double doubleValue(JsonObject json, String key, double fallback) {
		double value = json.has(key) ? json.get(key).getAsDouble() : fallback;
		if (!Double.isFinite(value) || key.equals("baseCost") && value < 0) {
			throw new IllegalArgumentException(key + " must be finite" + (key.equals("baseCost") ? " and non-negative" : ""));
		}
		return value;
	}

	private static JsonObject configEntry(SpellDefinition definition) {
		JsonObject entry = new JsonObject();
		entry.addProperty("id", definition.id().toString());
		entry.addProperty("displayName", definition.displayName());
		entry.addProperty("baseCost", definition.baseCost());
		entry.addProperty("strengthScaling", definition.strengthScaling());
		entry.addProperty("controlScaling", definition.controlScaling());
		entry.addProperty("element", definition.element());
		return entry;
	}

	private static void write(Path configFile, Map<ResourceLocation, SpellDefinition> definitions) throws IOException {
		Files.writeString(configFile, GSON.toJson(createConfig(definitions)));
	}
}
