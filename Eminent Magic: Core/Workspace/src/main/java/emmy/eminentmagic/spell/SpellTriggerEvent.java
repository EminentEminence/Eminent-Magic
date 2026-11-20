package emmy.eminentmagic.spell;

import net.neoforged.bus.api.Event;

public final class SpellTriggerEvent extends Event {
	private final SpellDefinition spell;
	private final SpellTrigger trigger;
	private final SpellTriggerContext context;

	public SpellTriggerEvent(SpellDefinition spell, SpellTrigger trigger, SpellTriggerContext context) {
		this.spell = spell;
		this.trigger = trigger;
		this.context = context;
	}

	public SpellDefinition spell() {
		return spell;
	}

	public SpellTrigger trigger() {
		return trigger;
	}

	public SpellTriggerContext context() {
		return context;
	}
}