package emmy.eminentmagic.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import emmy.eminentmagic.EminentMagicMod;
import emmy.eminentmagic.spell.SpellCatalog;
import emmy.eminentmagic.spell.SpellDefinition;

@EventBusSubscriber
public record SpellCatalogSyncMessage(int schemaVersion, List<SpellDefinition> definitions) implements CustomPacketPayload {
	private static final int MAX_SPELLS = 4096;
	private static final int MAX_STRING_LENGTH = 256;

	public static final Type<SpellCatalogSyncMessage> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "spell_catalog_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SpellCatalogSyncMessage> STREAM_CODEC =
			StreamCodec.of(SpellCatalogSyncMessage::write, SpellCatalogSyncMessage::read);

	public SpellCatalogSyncMessage {
		definitions = List.copyOf(definitions);
		if (definitions.size() > MAX_SPELLS) {
			throw new IllegalArgumentException("Spell catalog exceeds the network limit");
		}
	}

	private static void write(FriendlyByteBuf buffer, SpellCatalogSyncMessage message) {
		buffer.writeVarInt(message.schemaVersion());
		buffer.writeVarInt(message.definitions().size());
		for (SpellDefinition definition : message.definitions()) {
			buffer.writeUtf(definition.id().toString(), MAX_STRING_LENGTH);
			buffer.writeUtf(definition.displayName(), MAX_STRING_LENGTH);
			buffer.writeDouble(definition.baseCost());
			buffer.writeDouble(definition.strengthScaling());
			buffer.writeDouble(definition.controlScaling());
			buffer.writeUtf(definition.element(), MAX_STRING_LENGTH);
			buffer.writeBoolean(definition.channelled());
			writeOptionalString(buffer, definition.onCast());
			writeOptionalString(buffer, definition.whileCharging());
			writeOptionalString(buffer, definition.whileChannelling());
		}
	}

	private static SpellCatalogSyncMessage read(FriendlyByteBuf buffer) {
		int schemaVersion = buffer.readVarInt();
		int count = buffer.readVarInt();
		if (count < 0 || count > MAX_SPELLS) {
			throw new IllegalArgumentException("Invalid spell count in catalog sync: " + count);
		}
		List<SpellDefinition> definitions = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			String rawId = buffer.readUtf(MAX_STRING_LENGTH);
			ResourceLocation id = ResourceLocation.tryParse(rawId);
			if (id == null) {
				throw new IllegalArgumentException("Invalid spell ID in catalog sync: " + rawId);
			}
			definitions.add(new SpellDefinition(id, buffer.readUtf(MAX_STRING_LENGTH), buffer.readDouble(),
					buffer.readDouble(), buffer.readDouble(), buffer.readUtf(MAX_STRING_LENGTH), buffer.readBoolean(),
					readOptionalString(buffer), readOptionalString(buffer), readOptionalString(buffer)));
		}
		return new SpellCatalogSyncMessage(schemaVersion, definitions);
	}

	private static void writeOptionalString(FriendlyByteBuf buffer, String value) {
		buffer.writeBoolean(value != null);
		if (value != null) {
			buffer.writeUtf(value, MAX_STRING_LENGTH);
		}
	}

	private static String readOptionalString(FriendlyByteBuf buffer) {
		return buffer.readBoolean() ? buffer.readUtf(MAX_STRING_LENGTH) : null;
	}

	@Override
	public Type<SpellCatalogSyncMessage> type() {
		return TYPE;
	}

	public static void handle(final SpellCatalogSyncMessage message, final IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND) {
			return;
		}
		context.enqueueWork(() -> SpellCatalog.applySyncedDefinitions(message.schemaVersion(), message.definitions()))
				.exceptionally(exception -> {
					EminentMagicMod.LOGGER.error("Could not apply server spell catalog", exception);
					context.connection().disconnect(Component.literal("Failed to load spell definitions from the server"));
					return null;
				});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		EminentMagicMod.addNetworkMessage(TYPE, STREAM_CODEC, SpellCatalogSyncMessage::handle);
	}
}
