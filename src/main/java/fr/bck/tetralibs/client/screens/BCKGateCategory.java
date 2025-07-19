package fr.bck.tetralibs.client.screens;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.network.C2SValidateGateKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/*≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
 ≡           Copyright BCK, Inc 2025. (DragClover / Blackknight)                 ≡
 ≡                                                                               ≡
 ≡ Permission is hereby granted, free of charge, to any person obtaining a copy  ≡
 ≡ of this software and associated documentation files (the “Software”), to deal ≡
 ≡ in the Software without restriction, including without limitation the rights  ≡
 ≡ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ≡
 ≡ copies of the Software, and to permit persons to whom the Software is         ≡
 ≡ furnished to do so, subject to the following conditions:                      ≡
 ≡                                                                               ≡
 ≡ The above copyright notice and this permission notice shall be included in    ≡
 ≡ all copies or substantial portions of the Software.                           ≡
 ≡                                                                               ≡
 ≡ THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ≡
 ≡ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ≡
 ≡ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ≡
 ≡ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ≡
 ≡ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ≡
 ≡ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE ≡
 ≡ SOFTWARE.                                                                     ≡
 ≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡*/

/**
 * Category implementation displayed in the left column of {@code BCKManagementScreen}.
 * <p>
 * Fixes versus previous draft:
 * <ul>
 *     <li>Removed calls to {@code EditBox#setBorderColor} – not present in 1.20.x.</li>
 *     <li>Re‑used {@code setTextColor} to give instant visual feedback instead.</li>
 *     <li>Replaced nonexistent {@code C2SValidateGateKey.of(..)} helper with direct <code>new</code>.</li>
 *     <li>Used {@link MutableComponent} so we can safely {@code append} extra text.</li>
 *     <li>Removed shaky animation that tried to modify the final field {@code left}.</li>
 * </ul>
 */
public class BCKGateCategory implements BCKCategory {
    private static ResourceLocation rl(String file) {
        return ResourceLocation.fromNamespaceAndPath(TetralibsMod.MODID, "textures/gui/" + file);
    }

    // ---------------------------------------------------------------------
    // BCKCategory
    // ---------------------------------------------------------------------
    @Override
    public String getName() {
        return "BCK Gate";
    }

    @Override
    public ResourceLocation getIcon() {
        return rl("bck_gate_icon.png");
    }

    @Override
    public ResourceLocation getButtonSprite() {
        return rl("btn_bck_gate.png");
    }

    @Override
    public ResourceLocation getButtonHover() {
        return rl("btn_bck_gate_hover.png");
    }

    @Override
    public ResourceLocation getButtonPressed() {
        return rl("btn_bck_gate_down.png");
    }

    @Override
    public ScrollPanel createPanel(Minecraft mc, int left, int top, int w, int h) {
        return new GateKeyPanel(mc, left, top, w, h);
    }

    /* ------------------------------------------------------------------ */
    /* Inner class : ScrollPanel de saisie de la Gate Key                 */
    /* ------------------------------------------------------------------ */

    public static final class GateKeyPanel extends ScrollPanel implements ITickablePanel {
        // Size & assets ----------------------------------------------------
        private static final int PANEL_W = 176;
        private static final int PANEL_H = 96;
        private static final int BG_CORNER = 12; // 12‑px nine‑slice corner
        private static final ResourceLocation PANEL_BG = rl("gate_panel.png");

        // MC instance & widgets -------------------------------------------
        private final Minecraft mc;
        private final List<AbstractWidget> widgets = new ArrayList<>();
        private EditBox keyField;
        private FadeLabel feedbackLabel;
        private int attemptsLeft = 5;
        private boolean unlocked = false;
        private float alphaLocked = 1.0f;
        private float alphaAdmin = 0.0f;

        GateKeyPanel(Minecraft mc, int left, int top, int w, int h) {
            super(mc, w, h, top, left);
            this.mc = mc;
            buildLockedUI();
        }

        /* -------------------------------------------------------------- */
        /* UI builders                                                    */
        /* -------------------------------------------------------------- */
        private void buildLockedUI() {
            widgets.clear();
            int px = this.left + (this.width - PANEL_W) / 2;
            int py = this.top + (this.height - PANEL_H) / 2;

            keyField = new EditBox(mc.font, px + 12, py + 28, 152, 20, Component.translatable("tetralibs.gui.bck_gate.prompt"));
            keyField.setHint(Component.literal("XXXX-XXXX-XXXX"));
            keyField.setMaxLength(128);
            keyField.setFocused(true);      // Focus direct à l'ouverture
            keyField.setEditable(true);

            ImageButton validate = new ImageButton(px + 50, py + 56, 76, 20, 0, 0, 20, rl("btn_bck_gate_states.png"), 76, 60, btn -> TetralibsMod.PACKET_HANDLER.sendToServer(new C2SValidateGateKey(keyField.getValue())));

            feedbackLabel = new FadeLabel(Component.empty(), 0);
            feedbackLabel.setPos(px + 12, py + 4);

            widgets.add(keyField);
            widgets.add(validate);
            widgets.add(feedbackLabel);
        }

        private void buildAdminUI() {
            widgets.clear();
            int panelLeft = this.left + (this.width - PANEL_W) / 2;
            int panelTop = this.top + (this.height - PANEL_H) / 2;

            int y = panelTop + 20;
            widgets.add(Button.builder(Component.literal("Give 64 Diamonds"), b -> mc.getConnection().sendCommand("/give @s minecraft:diamond 64")).pos(panelLeft + 18, y).size(140, 20).build());

            widgets.add(Button.builder(Component.literal("Reload Config"), b -> mc.getConnection().sendCommand("/reload")).pos(panelLeft + 18, y + 24).size(140, 20).build());
        }


        /* -------------------------------------------------------------- */
        /* External hooks (call depuis paquet S2C)                         */
        /* -------------------------------------------------------------- */
        public void showSuccess() {
            unlocked = true;
            buildAdminUI();
            feedbackLabel.reset(Component.translatable("tetralibs.gui.bck_gate.unlocked").copy().withStyle(s -> s.withColor(0x00A000)), 60);
        }

        public void showError(String reason) {
            attemptsLeft = Math.max(0, attemptsLeft - 1);
            keyField.setTextColor(0xFFAA0000); // rouge – feedback visuel rapide

            MutableComponent msg = Component.translatable(reason);
            if (attemptsLeft > 0) {
                msg.append(" ").append(Component.literal("(" + attemptsLeft + ")"));
            }
            feedbackLabel.reset(msg, 60);
        }

        /* -------------------------------------------------------------- */
        /* ScrollPanel overrides                                          */
        /* -------------------------------------------------------------- */
        @Override
        protected int getContentHeight() {
            return PANEL_H;
        }

        /* --------------------- ITickablePanel ------------------------ */
        @Override
        public void tick() {
            if (keyField != null) keyField.tick();
            feedbackLabel.tick();
        }

        @Override
        protected void drawPanel(GuiGraphics gg, int mouseX, int mouseY, Tesselator tess, int relX, int relY) {
            PoseStack ps = gg.pose();
            ps.pushPose();

            int px = this.left + (this.width - PANEL_W) / 2;
            int py = this.top + (this.height - PANEL_H) / 2;

            mc.getTextureManager().getTexture(PANEL_BG, null);
            gg.blitNineSliced(PANEL_BG, px, py, PANEL_W, PANEL_H, BG_CORNER, BG_CORNER, BG_CORNER, BG_CORNER, BG_CORNER);

            for (AbstractWidget w : widgets) {
                w.render(gg, mouseX, mouseY, mc.getDeltaFrameTime());
            }
            ps.popPose();
        }


        @Override
        public boolean mouseClicked(double x, double y, int btn) {
            for (AbstractWidget w : widgets) if (w.mouseClicked(x, y, btn)) return true;
            return super.mouseClicked(x, y, btn);
        }

        @Override
        public boolean keyPressed(int key, int scan, int mods) {
            for (AbstractWidget w : widgets) if (w.keyPressed(key, scan, mods)) return true;
            return super.keyPressed(key, scan, mods);
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput out) {
        }
    }

    // ---------------------------------------------------------------------
    // Simple fading label utility ----------------------------------------
    // ---------------------------------------------------------------------
    private static class FadeLabel extends AbstractWidget implements ITickablePanel {
        private int life;

        FadeLabel(Component msg, int ticks) {
            super(0, 0, 0, 0, msg);
            life = ticks;
        }

        void setPos(int x, int y) {
            setX(x);
            setY(y);
        }

        void reset(Component msg, int ticks) {
            setMessage(msg);
            life = ticks;
        }

        /* ITickablePanel */
        @Override
        public void tick() {
            if (life > 0) life--;        // décompte pour le fade‑out
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics gg, int mouseX, int mouseY, float p) {
            if (life <= 0) return;
            gg.drawString(Minecraft.getInstance().font, getMessage(), getX(), getY(), 0xFFFFFF);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput o) {
        }
    }
}
