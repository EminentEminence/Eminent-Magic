package emmy.eminentmagic.spell;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import emmy.eminentmagic.EminentMagicMod;

public final class SpellCatalog extends SimplePreparableReloadListener<Map<ResourceLocation, SpellDefinition>> {
	private static final FileToIdConverter SPELL_FILES = FileToIdConverter.json("spells");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile Map<ResourceLocation, SpellDefinition> spells = Map.of();

	public static void addServerReloadListeners(AddServerReloadListenersEvent event) {
		event.addListener(ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "spell_definitions"), new SpellCatalog());
	}

	public static Optional<SpellDefinition> get(ResourceLocation id) {
		return Optional.ofNullable(spells.get(id));
	}

	public static Optional<SpellDefinition> getById(String rawId) {
		if (rawId == null || rawId.isBlank()) {
			return Optional.empty();
		}
		String normalizedId = rawId.indexOf(':') >= 0 ? rawId : EminentMagicMod.MODID + ":" + rawId;
		return Optional.ofNullable(ResourceLocation.tryParse(normalizedId)).flatMap(SpellCatalog::get);
	}

	public static java.util.List<SpellDefinition> all() {
		return spells.values().stream().sorted(Comparator.comparing(spell -> spell.id().toString())).toList();
	}

	@Override
	protected Map<ResourceLocation, SpellDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, SpellDefinition> loaded = new TreeMap<>();
		Path configDirectory = FMLPaths.CONFIGDIR.get().resolve(EminentMagicMod.MODID).resolve("spells");
		try {
			Files.createDirectories(configDirectory);
		} catch (IOException exception) {
			EminentMagicMod.LOGGER.error("Could not create spell config directory {}", configDirectory, exception);
		}
		SPELL_FILES.listMatchingResources(resourceManager).forEach((fileId, resource) -> {
			ResourceLocation resourceId = SPELL_FILES.fileToId(fileId);
			try (Reader reader = resource.openAsReader()) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				SpellDefinition definition = parseDefinition(json, resourceId);
				if (loaded.putIfAbsent(definition.id(), definition) != null) {
					throw new IllegalArgumentException("Duplicate spell ID " + definition.id());
				}
				writeDefaultConfig(configDirectory, resourceId, json);
			} catch (IOException | RuntimeException exception) {
				EminentMagicMod.LOGGER.error("Could not load spell definition {}", fileId, exception);
			}
		});
		try (Stream<Path> files = Files.walk(configDirectory)) {
			files.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json")).forEach(path -> {
				try (Reader reader = Files.newBufferedReader(path)) {
					Path relative = configDirectory.relativize(path);
					String namespace = relative.getName(0).toString();
					String spellPath = relative.subpath(1, relative.getNameCount()).toString().replace(path.getFileSystem().getSeparator(), "/");
					ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(namespace, spellPath.substring(0, spellPath.length() - 5));
					SpellDefinition definition = parseDefinition(JsonParser.parseReader(reader).getAsJsonObject(), fileId);
					loaded.put(definition.id(), definition);
				} catch (IOException | RuntimeException exception) {
					EminentMagicMod.LOGGER.error("Could not load spell config {}", path, exception);
				}
			});
		} catch (IOException exception) {
			EminentMagicMod.LOGGER.error("Could not read spell config directory {}", configDirectory, exception);
		}
		return Map.copyOf(loaded);
	}

	@Override
	protected void apply(Map<ResourceLocation, SpellDefinition> loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
		spells = loaded;
		EminentMagicMod.LOGGER.info("Loaded {} spell definitions", loaded.size());
	}

	private static String optionalString(JsonObject json, String key) {
		return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
	}

	private static SpellDefinition parseDefinition(JsonObject json, ResourceLocation fallbackId) {
		if (GsonHelper.getAsInt(json, "schemaVersion", 1) != 1) {
			throw new IllegalArgumentException("Unsupported spell schema version");
		}
		String rawId = GsonHelper.getAsString(json, "id");
		ResourceLocation id = rawId.indexOf(':') >= 0 ? ResourceLocation.parse(rawId)
				: ResourceLocation.fromNamespaceAndPath(fallbackId.getNamespace(), rawId);
		String displayName = GsonHelper.getAsString(json, "displayName");
		String element = GsonHelper.getAsString(json, "element");
		double baseCost = GsonHelper.getAsDouble(json, "baseCost");
		double strengthScaling = GsonHelper.getAsDouble(json, "strengthScaling");
		double controlScaling = GsonHelper.getAsDouble(json, "controlScaling");
		if (displayName.isBlank() || element.isBlank() || !Double.isFinite(baseCost) || baseCost < 0
				|| !Double.isFinite(strengthScaling) || !Double.isFinite(controlScaling)) {
			throw new IllegalArgumentException("Spell name, element, and finite cost values are required");
		}
		JsonObject hooks = GsonHelper.getAsJsonObject(json, "hooks", new JsonObject());
		return new SpellDefinition(id, displayName, baseCost, strengthScaling, controlScaling, element,
				GsonHelper.getAsBoolean(json, "channelled", false), optionalString(hooks, "onCast"),
				optionalString(hooks, "whileCharging"), optionalString(hooks, "whileChannelling"));
	}

	private static void writeDefaultConfig(Path configDirectory, ResourceLocation resourceId, JsonObject json) {
		Path configFile = configDirectory.resolve(resourceId.getNamespace()).resolve(resourceId.getPath() + ".json");
		if (Files.exists(configFile)) {
			return;
		}
		try {
			Files.createDirectories(configFile.getParent());
			Files.writeString(configFile, GSON.toJson(json));
		} catch (IOException exception) {
			EminentMagicMod.LOGGER.error("Could not create default spell config {}", configFile, exception);
		}
	}
}