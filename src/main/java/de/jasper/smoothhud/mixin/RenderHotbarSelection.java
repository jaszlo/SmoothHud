package de.jasper.smoothhud.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import de.jasper.smoothhud.SelectedSlotAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class RenderHotbarSelection {

    @Shadow @Final
    private static final Identifier HOTBAR_TEXTURE = new Identifier("hud/hotbar");
    @Shadow @Final
    private static final Identifier HOTBAR_SELECTION_TEXTURE = new Identifier("hud/hotbar_selection");
    @Shadow @Final
    private static final Identifier HOTBAR_OFFHAND_LEFT_TEXTURE = new Identifier("hud/hotbar_offhand_left");
    @Shadow @Final
    private static final Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE = new Identifier("hud/hotbar_offhand_right");
    @Shadow @Final
    private static final Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE = new Identifier("hud/hotbar_attack_indicator_background");
    @Shadow @Final
    private static final Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE = new Identifier("hud/hotbar_attack_indicator_progress");
    @Shadow
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Shadow
    private PlayerEntity getCameraPlayer() { return null; }

    @Shadow
    private void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed) {}

    @Unique
    private int getHotbarSelectionX(int from, int to, DrawContext context) {
        assert this.client.player != null;
        int x = context.getScaledWindowWidth() / 2;
        int xOffset = 91;
        // Nothing needs to be animated
        if (from == to) {
            return x - xOffset - 1 + this.client.player.getInventory().selectedSlot * 20;
        }

        // Get X values lerped between fromSlot and toSlot positions calculated below
        int start = x - xOffset - 1 + from * 20;
        int end   = x - xOffset - 1 + to * 20;

        SelectedSlotAnimation.animationStep++;
        return SelectedSlotAnimation.lerp(start, end, SelectedSlotAnimation.animationStep++);
    }

    @Unique
    private int selectedSlotNew  = 0; // From Last Tick
    @Unique
    private int selectedSlotLast = 0; // From Current Tick

    @Inject(method="renderHotbar", at=@At("HEAD"), cancellable=true)
    private void renderHotbar(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (this.client == null || this.client.player == null || this.client.player.getInventory() == null) return;
        int o;
        int n;
        int m;
        PlayerEntity playerEntity = this.getCameraPlayer();
        if (playerEntity == null) {
            return;
        }
        ItemStack itemStack = playerEntity.getOffHandStack();
        Arm arm = playerEntity.getMainArm().getOpposite();
        int x = context.getScaledWindowWidth() / 2;
        int width = 182;
        int xOffset = 91;
        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, -90.0f);
        context.drawGuiTexture(HOTBAR_TEXTURE, x - xOffset, context.getScaledWindowHeight() - 22, width, 22);

        // Changes to make hotbar selection icon move smoothly
        {

            assert this.client.player != null;
            // Only updated these if not animating
            if (!SelectedSlotAnimation.isAnimating) {
                this.selectedSlotLast = this.selectedSlotNew;
                this.selectedSlotNew = this.client.player.getInventory().selectedSlot;
                SelectedSlotAnimation.isAnimating = this.selectedSlotNew != this.selectedSlotLast;
            }

            // Get new X-Pos of Selection Texture
            int selectionX = getHotbarSelectionX(this.selectedSlotLast, this.selectedSlotNew, context);

            // Draw Selection Texture
            context.drawGuiTexture(HOTBAR_SELECTION_TEXTURE, selectionX, context.getScaledWindowHeight() - 22 - 1, 24, 23);
        }


        if (!itemStack.isEmpty()) {
            if (arm == Arm.LEFT) {
                context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, x - xOffset - 29, context.getScaledWindowHeight() - 23, 29, 24);
            } else {
                context.drawGuiTexture(HOTBAR_OFFHAND_RIGHT_TEXTURE, x + xOffset, context.getScaledWindowHeight() - 23, 29, 24);
            }
        }
        context.getMatrices().pop();
        RenderSystem.disableBlend();
        int l = 1;
        for (m = 0; m < 9; ++m) {
            int newX = x - 90 + m * 20 + 2;
            int y = context.getScaledWindowHeight() - 16 - 3;
            this.renderHotbarItem(context, newX, y, tickDelta, playerEntity, playerEntity.getInventory().main.get(m), l++);
        }
        if (!itemStack.isEmpty()) {
            m = context.getScaledWindowHeight() - 16 - 3;
            if (arm == Arm.LEFT) {
                this.renderHotbarItem(context, x - xOffset - 26, m, tickDelta, playerEntity, itemStack, l);
            } else {
                this.renderHotbarItem(context, x + xOffset + 10, m, tickDelta, playerEntity, itemStack, l);
            }
        }
        if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            RenderSystem.enableBlend();
            assert this.client.player != null;
            float f = this.client.player.getAttackCooldownProgress(0.0f);
            if (f < 1.0f) {
                n = context.getScaledWindowHeight() - 20;
                o = x + xOffset + 6;
                if (arm == Arm.RIGHT) {
                    o = x - xOffset - 22;
                }
                int p = (int)(f * 19.0f);
                context.drawGuiTexture(HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, o, n, 18, 18);
                context.drawGuiTexture(HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - p, o, n + 18 - p, 18, p);
            }
            RenderSystem.disableBlend();
        }

        ci.cancel();
    }
}
