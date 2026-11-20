package emmy.eminentmagic.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;

import emmy.eminentmagic.EminentMagicMod;

@EventBusSubscriber
public record ControlSpellControlMessage(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<ControlSpellControlMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "key_control_spell_control"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ControlSpellControlMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, ControlSpellControlMessage message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new ControlSpellControlMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public Type<ControlSpellControlMessage> type() {
		return TYPE;
	}

	public static void handleData(final ControlSpellControlMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> {
			}).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		EminentMagicMod.addNetworkMessage(ControlSpellControlMessage.TYPE, ControlSpellControlMessage.STREAM_CODEC, ControlSpellControlMessage::handleData);
	}
}