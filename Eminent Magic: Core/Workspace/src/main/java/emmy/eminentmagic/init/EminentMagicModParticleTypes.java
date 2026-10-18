/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package emmy.eminentmagic.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

import emmy.eminentmagic.EminentMagicMod;

public class EminentMagicModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, EminentMagicMod.MODID);
	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MANA_PARTICLE = REGISTRY.register("mana_particle", () -> new SimpleParticleType(true));
}