package emmy.eminentmagic.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.server.level.ServerLevel;

import emmy.eminentmagic.procedures.ManaChannelActiveTickConditionProcedure;

public class ManaChannelMobEffect extends MobEffect {
	public ManaChannelMobEffect() {
		super(MobEffectCategory.NEUTRAL, -1);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		ManaChannelActiveTickConditionProcedure.execute(level, entity);
		return super.applyEffectTick(level, entity, amplifier);
	}
}