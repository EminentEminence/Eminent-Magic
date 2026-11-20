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
public record CycleSpellMessage(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<CycleSpellMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "key_cycle_spell"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CycleSpellMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, CycleSpellMessage message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new CycleSpellMessage(buffer.readInt(), buffer.readInt()));

	@Override
	public Type<CycleSpellMessage> type() {
		return TYPE;
	}

	public static void handleData(final CycleSpellMessage message, final IPayloadContext context) {
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
		EminentMagicMod.addNetworkMessage(CycleSpellMessage.TYPE, CycleSpellMessage.STREAM_CODEC, CycleSpellMessage::handleData);
	}
}