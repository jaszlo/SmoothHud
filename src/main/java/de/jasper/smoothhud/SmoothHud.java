package de.jasper.smoothhud;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmoothHud implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("smoothhud");

	@Override
	public void onInitialize() {
		SelectedSlotAnimation.register();
	}
}