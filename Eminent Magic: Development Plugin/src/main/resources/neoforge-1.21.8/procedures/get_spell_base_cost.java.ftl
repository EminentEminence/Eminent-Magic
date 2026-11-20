java.util.Optional.ofNullable(${input$SPELL})
	.map(emmy.eminentmagic.spell.SpellDefinition::baseCost).orElse(0.0)