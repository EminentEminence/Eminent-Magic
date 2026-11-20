package emmy.eminentmagic.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

import emmy.eminentmagic.EminentMagicMod;
import emmy.eminentmagic.init.EminentMagicModMobEffects;

@EventBusSubscriber(modid = EminentMagicMod.MODID, value = Dist.CLIENT)
public class ChannelingOutlineClientRendering {
	private static final String CHANNELING_OUTLINE_FLAG = "eminent_magic:channeling_outline";
	private static final ContextKey<Boolean> CHANNELING_OUTLINE = new ContextKey<>(
			ResourceLocation.fromNamespaceAndPath(EminentMagicMod.MODID, "channeling_outline"));

	@SubscribeEvent
	public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
		event.registerEntityModifier(PlayerRenderer.class, (entity, state) -> state.setRenderData(CHANNELING_OUTLINE, isOutlineActive(entity)));
	}

	public static boolean isOutlineActive(Player player) {
		return player.hasEffect(EminentMagicModMobEffects.MANA_CHANNEL)
				&& player.getPersistentData().getBooleanOr(CHANNELING_OUTLINE_FLAG, false);
	}

	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
		for (PlayerSkin.Model skinModel : event.getSkins()) {
			PlayerRenderer renderer = event.getSkin(skinModel);
			if (renderer != null)
				renderer.addLayer(new ChannelingOutlineLayer(renderer));
		}
	}

	private static final class ChannelingOutlineLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
		private ChannelingOutlineLayer(PlayerRenderer renderer) {
			super(renderer);
		}

		@Override
		public void render(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight, PlayerRenderState state, float limbSwing, float limbSwingAmount) {
			if (!state.getRenderDataOrDefault(CHANNELING_OUTLINE, false) || state.skin == null)
				return;

			OutlineBufferSource outlineBufferSource = Minecraft.getInstance().renderBuffers().outlineBufferSource();
			outlineBufferSource.setColor(0x6C, 0x30, 0x82, 0xFF);
			poseStack.pushPose();
			try {
				poseStack.scale(1.08F, 1.08F, 1.08F);
				getParentModel().renderToBuffer(poseStack, outlineBufferSource.getBuffer(RenderType.outline(state.skin.texture())), packedLight, OverlayTexture.NO_OVERLAY, -1);
			} finally {
				poseStack.popPose();
				outlineBufferSource.setColor(0xFF, 0xFF, 0xFF, 0xFF);
			}
		}
	}
}
