package com.eminentmagic.development;

import net.mcreator.element.ModElementTypeLoader;

public final class ModElementTypeLoaderRegistry {
	private ModElementTypeLoaderRegistry() {
	}

	public static void register(net.mcreator.element.ModElementType<?> type) {
		ModElementTypeLoader.register(type);
	}
}