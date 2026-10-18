package emmy.eminentmagic.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.Minecraft;

import emmy.eminentmagic.network.EminentMagicModVariables;
import emmy.eminentmagic.init.EminentMagicModGameRules;
import emmy.eminentmagic.init.EminentMagicModAttributes;

public class ConsumeManaProcedure {
	public static boolean execute(LevelAccessor world, Entity entity, double mana) {
		if (entity == null)
			return false;
		double CasterControl = 0;
		double CasterCurrentMana = 0;
		double CasterReserves = 0;
		double RemainingMana = 0;
		double ManaCost = 0;
		double RandomMessageNumber = 0;
		double CasterSelfDamage = 0;
		double CasterStrength = 0;
		if (getEntityGameType(entity) == GameType.CREATIVE) {
			return true;
		}
		ManaCost = mana;
		assert Boolean.TRUE; //#dbg:ConsumeMana:AttributeFetching
		CasterReserves = entity instanceof LivingEntity _livingEntity1 && _livingEntity1.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_RESERVES) ? _livingEntity1.getAttribute(EminentMagicModAttributes.MANA_RESERVES).getValue() : 0;
		CasterCurrentMana = entity instanceof LivingEntity _livingEntity2 && _livingEntity2.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CURRENT) ? _livingEntity2.getAttribute(EminentMagicModAttributes.MANA_CURRENT).getValue() : 0;
		CasterStrength = Minecraft.getInstance().player.getData(EminentMagicModVariables.PLAYER_VARIABLES).spellStrength;
		CasterControl = Minecraft.getInstance().player.getData(EminentMagicModVariables.PLAYER_VARIABLES).spellControl;
		assert Boolean.TRUE; //#dbg:ConsumeMana:CastingValidation
		RemainingMana = ManaCost;
		if (ManaCost <= CasterReserves) {
			assert Boolean.TRUE; //#dbg:ConsumeMana:ConsumeMana
			if (CasterCurrentMana >= RemainingMana) {
				assert Boolean.TRUE; //#dbg:ConsumeMana:EnoughMana
				if (entity instanceof LivingEntity _livingEntity3 && _livingEntity3.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CURRENT))
					_livingEntity3.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue((CasterCurrentMana - RemainingMana));
				RemainingMana = 0;
			} else {
				assert Boolean.TRUE; //#dbg:ConsumeMana:NotEnoughMana
				RemainingMana = RemainingMana - CasterCurrentMana;
				if (entity instanceof LivingEntity _livingEntity4 && _livingEntity4.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_CURRENT))
					_livingEntity4.getAttribute(EminentMagicModAttributes.MANA_CURRENT).setBaseValue(0);
				RemainingMana = ManaCost;
			}
			if (RemainingMana > 0) {
				assert Boolean.TRUE; //#dbg:ConsumeMana:ConsumeHealth
				CasterSelfDamage = Math.round((RemainingMana / (0.2 * CasterReserves)) * 20);
				if ((entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) <= CasterSelfDamage) {
					assert Boolean.TRUE; //#dbg:ConsumeMana:NotEnoughHealth
					RemainingMana = RemainingMana * ((CasterSelfDamage - (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1)) / CasterSelfDamage);
					CasterSelfDamage = (entity instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1) - 1;
				} else {
					assert Boolean.TRUE; //#dbg:ConsumeMana:EnoughHealth
					RemainingMana = 0;
				}
				entity.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("eminent_magic:not_enough_mana")))), (float) CasterSelfDamage);
			}
			if (RemainingMana > 0) {
				assert Boolean.TRUE; //#dbg:ConsumeMana:ConsumeReserves
				if (world instanceof ServerLevel _serverLevelGR10 && _serverLevelGR10.getGameRules().getBoolean(EminentMagicModGameRules.SPELLS_CONSUME_RESERVE)) {
					if (CasterReserves >= RemainingMana) {
						assert Boolean.TRUE; //#dbg:ConsumeMana:EnoughManaReserve
						if (entity instanceof LivingEntity _livingEntity11 && _livingEntity11.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_RESERVES))
							_livingEntity11.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue((CasterReserves - RemainingMana));
						RemainingMana = 0;
					} else {
						assert Boolean.TRUE; //#dbg:ConsumeMana:NotEnoughManaReserve
						RemainingMana = RemainingMana - CasterReserves;
						if (entity instanceof LivingEntity _livingEntity12 && _livingEntity12.getAttributes().hasAttribute(EminentMagicModAttributes.MANA_RESERVES))
							_livingEntity12.getAttribute(EminentMagicModAttributes.MANA_RESERVES).setBaseValue(0);
						RemainingMana = ManaCost;
					}
				}
			}
			if (RemainingMana > 0) {
				assert Boolean.TRUE; //#dbg:ConsumeMana:CannotCastDueToMana
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal((Component.translatable("message.eminent_magic.out_of_mana").getString())), true);
				return false;
			}
		} else {
			assert Boolean.TRUE; //#dbg:ConsumeMana:DisplayFailureMessage
			RandomMessageNumber = Mth.nextInt(RandomSource.create(), 1, 3);
			if (RandomMessageNumber == 1) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal((Component.translatable("message.eminent_magic.spell_complexity_failure_1").getString())), true);
			} else if (RandomMessageNumber == 2) {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal((Component.translatable("message.eminent_magic.spell_complexity_failure_2").getString())), true);
			} else {
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal((Component.translatable("message.eminent_magic.spell_complexity_failure_3").getString())), true);
			}
			return false;
		}
		return true;
	}

	private static GameType getEntityGameType(Entity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			return serverPlayer.gameMode.getGameModeForPlayer();
		} else if (entity instanceof Player player && player.level().isClientSide()) {
			PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(player.getGameProfile().getId());
			if (playerInfo != null)
				return playerInfo.getGameMode();
		}
		return null;
	}
}