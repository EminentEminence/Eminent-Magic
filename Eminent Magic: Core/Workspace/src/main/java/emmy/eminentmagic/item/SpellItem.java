package emmy.eminentmagic.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;

import emmy.eminentmagic.init.EminentMagicModSpellItems;
import emmy.eminentmagic.spell.SpellCastingService;
import emmy.eminentmagic.spell.SpellCatalog;
import emmy.eminentmagic.spell.SpellDefinition;
import emmy.eminentmagic.spell.SpellTrigger;
import emmy.eminentmagic.spell.SpellTriggerDispatcher;

public final class SpellItem extends Item {
	public static final String SPELL_ID_TAG = "spellId";

	public SpellItem(Item.Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		SpellDefinition spell = getSpellId(stack).flatMap(SpellCatalog::get).orElse(null);
		if (spell == null) {
			return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		}
		if (spell.channelled()) {
			player.startUsingItem(hand);
			return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		return SpellCastingService.cast(player, stack) ? InteractionResult.CONSUME : InteractionResult.FAIL;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 99999;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
		if (!level.isClientSide() && entity instanceof Player) {
			SpellTriggerDispatcher.fire(entity, stack, SpellTrigger.WHILE_CHANNELLING);
		}
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
		if (!level.isClientSide() && entity instanceof Player player) {
			return SpellCastingService.cast(player, stack);
		}
		return false;
	}

	public static ItemStack createStack(SpellDefinition spell) {
		ItemStack stack = new ItemStack(EminentMagicModSpellItems.SPELL.get());
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(SPELL_ID_TAG, spell.id().toString()));
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(spell.displayName()));
		stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Spell: " + spell.displayName()))));
		return stack;
	}

	public static Optional<ResourceLocation> getSpellId(ItemStack stack) {
		String spellId = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(SPELL_ID_TAG).orElse("");
		if (spellId.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(ResourceLocation.parse(spellId));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}
}