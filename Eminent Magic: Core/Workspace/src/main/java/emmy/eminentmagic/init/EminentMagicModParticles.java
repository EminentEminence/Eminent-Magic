/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package emmy.eminentmagic.init;

import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import emmy.eminentmagic.client.particle.CapeParticleParticle;

@EventBusSubscriber(Dist.CLIENT)
public class EminentMagicModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(EminentMagicModParticleTypes.MANA_PARTICLE.get(), CapeParticleParticle::provider);
	}
}