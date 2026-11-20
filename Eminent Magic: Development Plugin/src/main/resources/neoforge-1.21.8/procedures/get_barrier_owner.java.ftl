java.util.Optional.ofNullable(${input$BARRIER})
		.map(barrier -> barrier.owner().toString()).orElse("")
