java.util.Optional.ofNullable(${input$BARRIER})
		.map(barrier -> barrier.id().toString()).orElse("")
