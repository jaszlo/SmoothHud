package de.jasper.smoothhud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SelectedSlotAnimation {

    public static boolean isAnimating = false;
    public static int animationStep = 0;

    public static final double MAX_ANIMATION_STEPS = 16;

    public static int lerp(int start, int end, int index) {
        double dIndex = Math.min(index, MAX_ANIMATION_STEPS);
        return (int) ((double) start + ((double) end - (double) start) * (dIndex / MAX_ANIMATION_STEPS));
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            // Check if animation finished
            if (animationStep >= MAX_ANIMATION_STEPS) {
                isAnimating = false;
                animationStep = 0;
            }
        });
    }

}
