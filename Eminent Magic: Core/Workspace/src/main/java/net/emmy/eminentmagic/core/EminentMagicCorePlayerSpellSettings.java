package net.emmy.eminentmagic.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.core.HolderLookup;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public final class EminentMagicCorePlayerSpellSettings {
	private static final String CONTROL_KEY = "spellControl";
	private static final String STRENGTH_KEY = "spellStrength";
	private static final double LEGACY_DEFAULT = 1.0;
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EminentMagicCoreMod.MODID);
	public static final Supplier<AttachmentType<SpellSettings>> PLAYER_SETTINGS = ATTACHMENT_TYPES.register("player_spell_settings", () -> AttachmentType.serializable(SpellSettings::new).build());

	private EminentMagicCorePlayerSpellSettings() {
	}

	public static void register(IEventBus modEventBus) {
		ATTACHMENT_TYPES.register(modEventBus);
		modEventBus.addListener(EminentMagicCorePlayerSpellSettings::registerMessages);
		NeoForge.EVENT_BUS.addListener(EminentMagicCorePlayerSpellSettings::onPlayerLoggedIn);
		NeoForge.EVENT_BUS.addListener(EminentMagicCorePlayerSpellSettings::onPlayerRespawned);
		NeoForge.EVENT_BUS.addListener(EminentMagicCorePlayerSpellSettings::onPlayerChangedDimension);
		NeoForge.EVENT_BUS.addListener(EminentMagicCorePlayerSpellSettings::onPlayerTick);
		NeoForge.EVENT_BUS.addListener(EminentMagicCorePlayerSpellSettings::clonePlayer);
	}

	public static double getControl(Player player, ResourceLocation spellId) {
		if (player == null || !EminentMagicCoreSpellRegistry.findSpell(spellId).isPresent()) {
			return 0;
		}
		return clamp(player.getData(PLAYER_SETTINGS).control.getOrDefault(spellId.toString(), LEGACY_DEFAULT), EminentMagicCoreManaAttributes.getManaControl(player));
	}

	public static double getStrength(Player player, ResourceLocation spellId) {
		if (player == null || !EminentMagicCoreSpellRegistry.findSpell(spellId).isPresent()) {
			return 0;
		}
		return clamp(player.getData(PLAYER_SETTINGS).strength.getOrDefault(spellId.toString(), LEGACY_DEFAULT), EminentMagicCoreManaAttributes.getManaStrength(player));
	}

	public static boolean setControl(Player player, ResourceLocation spellId, double value) {
		if (!canUpdate(player, spellId, value)) {
			return false;
		}
		SpellSettings settings = player.getData(PLAYER_SETTINGS);
		settings.control.put(spellId.toString(), clamp(value, EminentMagicCoreManaAttributes.getManaControl(player)));
		settings.dirty = true;
		return true;
	}

	public static boolean setStrength(Player player, ResourceLocation spellId, double value) {
		if (!canUpdate(player, spellId, value)) {
			return false;
		}
		SpellSettings settings = player.getData(PLAYER_SETTINGS);
		settings.strength.put(spellId.toString(), clamp(value, EminentMagicCoreManaAttributes.getManaStrength(player)));
		settings.dirty = true;
		return true;
	}

	private static boolean canUpdate(Player player, ResourceLocation spellId, double value) {
		return player != null && !player.level().isClientSide() && Double.isFinite(value) && value >= 0
				&& EminentMagicCoreSpellRegistry.findSpell(spellId).isPresent();
	}

	private static double clamp(double value, double maximum) {
		return Math.max(0, Math.min(value, maximum));
	}

	private static void registerMessages(FMLCommonSetupEvent event) {
		EminentMagicCoreMod.addNetworkMessage(SettingsSyncMessage.TYPE, SettingsSyncMessage.STREAM_CODEC, SettingsSyncMessage::handle);
	}

	private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		syncToClient(event.getEntity());
	}

	private static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
		syncToClient(event.getEntity());
	}

	private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		syncToClient(event.getEntity());
	}

	private static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player instanceof ServerPlayer serverPlayer) {
			SpellSettings settings = serverPlayer.getData(PLAYER_SETTINGS);
			if (settings.dirty) {
				PacketDistributor.sendToPlayer(serverPlayer, new SettingsSyncMessage(settings.encode()));
				settings.dirty = false;
			}
		}
	}

	private static void clonePlayer(PlayerEvent.Clone event) {
		event.getEntity().setData(PLAYER_SETTINGS, event.getOriginal().getData(PLAYER_SETTINGS).copy());
	}

	private static void syncToClient(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			PacketDistributor.sendToPlayer(serverPlayer, new SettingsSyncMessage(serverPlayer.getData(PLAYER_SETTINGS).encode()));
		}
	}

	private static Map<String, Double> readValues(JsonElement element) {
		Map<String, Double> values = new TreeMap<>();
		if (element == null || !element.isJsonObject()) {
			return values;
		}
		for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
			ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
			JsonElement value = entry.getValue();
			if (id != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
				double number = value.getAsDouble();
				if (Double.isFinite(number) && number >= 0) {
					values.put(id.toString(), number);
				}
			}
		}
		return values;
	}

	public static final class SpellSettings implements ValueIOSerializable {
		private final Map<String, Double> control = new TreeMap<>();
		private final Map<String, Double> strength = new TreeMap<>();
		private boolean dirty;

		@Override
		public void serialize(ValueOutput output) {
			output.putString(CONTROL_KEY, encodeValues(control));
			output.putString(STRENGTH_KEY, encodeValues(strength));
		}

		@Override
		public void deserialize(ValueInput input) {
			control.clear();
			strength.clear();
			control.putAll(readValues(JsonParser.parseString(input.getStringOr(CONTROL_KEY, "{}"))));
			strength.putAll(readValues(JsonParser.parseString(input.getStringOr(STRENGTH_KEY, "{}"))));
			dirty = false;
		}

		private SpellSettings copy() {
			SpellSettings result = new SpellSettings();
			result.control.putAll(control);
			result.strength.putAll(strength);
			return result;
		}

		private String encode() {
			JsonObject object = new JsonObject();
			object.add(CONTROL_KEY, valuesObject(control));
			object.add(STRENGTH_KEY, valuesObject(strength));
			return object.toString();
		}

		private static String encodeValues(Map<String, Double> values) {
			return valuesObject(values).toString();
		}

		private static JsonObject valuesObject(Map<String, Double> values) {
			JsonObject object = new JsonObject();
			values.forEach(object::addProperty);
			return object;
		}
	}

	private record SettingsSyncMessage(String encodedSettings) implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
		private static final Type<SettingsSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EminentMagicCoreMod.MODID, "player_spell_settings_sync"));
		private static final StreamCodec<RegistryFriendlyByteBuf, SettingsSyncMessage> STREAM_CODEC = StreamCodec.of(
				(buffer, message) -> buffer.writeUtf(message.encodedSettings),
				buffer -> new SettingsSyncMessage(buffer.readUtf()));

		@Override
		public Type<SettingsSyncMessage> type() {
			return TYPE;
		}

		private static void handle(SettingsSyncMessage message, IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND) {
				context.enqueueWork(() -> {
					try {
						JsonObject encoded = JsonParser.parseString(message.encodedSettings).getAsJsonObject();
						SpellSettings settings = new SpellSettings();
						settings.control.putAll(readValues(encoded.get(CONTROL_KEY)));
						settings.strength.putAll(readValues(encoded.get(STRENGTH_KEY)));
						context.player().setData(PLAYER_SETTINGS, settings);
					} catch (RuntimeException exception) {
						EminentMagicCoreMod.LOGGER.warn("Ignoring malformed player spell settings sync payload.", exception);
					}
				});
			}
		}
	}
}
