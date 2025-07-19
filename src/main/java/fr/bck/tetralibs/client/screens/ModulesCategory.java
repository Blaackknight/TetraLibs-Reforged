package fr.bck.tetralibs.client.screens;


import com.mojang.blaze3d.vertex.Tesselator;
import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.core.ModuleManager;
import fr.bck.tetralibs.network.C2SApplyModulesPacket;
import fr.bck.tetralibs.utils.TexturedButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.*;


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

public class ModulesCategory implements BCKCategory {
    private static ResourceLocation rl(String file) {
        return ResourceLocation.fromNamespaceAndPath(TetralibsMod.MODID, "textures/gui/" + file);
    }

    private static final ResourceLocation BG_ROUNDED = rl("rounded_panel_bis.png");
    private static final ResourceLocation BG_ROUNDED_HOVER = rl("rounded_panel_bis.png");
    private static final ResourceLocation BG_VEINS = rl("bg_veins_tile64.png");
    private static final ResourceLocation BORDER_FLESH = rl("border_flesh_veins_thick32.png");
    private static final ResourceLocation TEX_SCROLL_BG = rl("scroll_bar_bg.png");
    private static final ResourceLocation TEX_SCROLL_GRIP = rl("scroll_bar_grip.png");

    private static final ResourceLocation TEX_CB_BG = rl("checkbox_bg.png");
    private static final ResourceLocation TEX_CB_CHECK = rl("checkbox_check.png");
    private static final ResourceLocation TEX_CB_CROSS = rl("checkbox_cross.png");
    private static final int BORDER_COLOR_NORMAL = 0x88AA0000; // rouge sombre
    private static final int BORDER_COLOR_HOVER = 0xFFFF3030; // rouge vif
    private static final int ENTRY_INNER_HEIGHT = 20;
    private static final int ENTRY_PADDING = 4;
    private static final int BAR_W = 8;      // largeur voulue
    private static final int ENTRY_TOTAL_HEIGHT = ENTRY_INNER_HEIGHT + ENTRY_PADDING * 2;

    private Panel currentPanel;

    private Map<ResourceLocation, Boolean> selectionMap = new LinkedHashMap<>();
    private final Map<ResourceLocation, Boolean> originalSelection = new LinkedHashMap<>();

    public ModulesCategory() {
        refreshFromConfig();
    }

    /**
     * Relit la configuration serveur et met à jour les 2 maps.
     * À appeler juste :
     * • quand l’écran s’ouvre,
     * • quand tu reçois le S2C de confirmation « modules appliqués ».
     */
    private void refreshFromConfig() {

        // 1) nouvelle photo de la config
        List<ModuleManager.ModuleStatus> fresh = ModuleManager.listAllModuleStatuses();

        // 2) map courante ← photo
        selectionMap.clear();
        for (ModuleManager.ModuleStatus st : fresh) {
            selectionMap.put(st.module().id(), st.enabled());
        }

        // 3) snapshot de référence pour le bouton Apply
        originalSelection.clear();
        originalSelection.putAll(selectionMap);

        // 4) si le panel est déjà à l’écran → on régénère les cases
        if (currentPanel != null) {       // garde un champ Panel currentPanel
            currentPanel.buildEntries();  // (rends la méthode public/protected)
            currentPanel.layoutEntries();
        }
    }

    @Override
    public String getName() {
        return "Modules";
    }

    @Override
    public ResourceLocation getIcon() {
        return rl("modules_icon.png");
    }

    @Override
    public ResourceLocation getButtonSprite() {
        return rl("btn_modules_detailed.png");
    }

    @Override
    public ResourceLocation getButtonHover() {
        return rl("btn_modules_hover_detailed.png");
    }

    @Override
    public ResourceLocation getButtonPressed() {
        return rl("btn_modules_down_detailed.png");
    }

    @Override
    public void refresh() {
        refreshFromConfig();
    }

    /* Cadre et effet chair / veines */
    private static final int BORDER_THICK = 4;
    private static final int BORDER_PAD_TOP = 8;
    private static final int BORDER_PAD_SIDE = 8;
    private static final int BORDER_PAD_BOT = 8;
    private static final int TILE = 8;
    private static final int SEED = 98765;
    private static final int ENTRY_H_INNER = 20;
    private static final int ENTRY_PAD = 4;
    private static final int ENTRY_H_TOTAL = ENTRY_H_INNER + ENTRY_PAD * 2;

    @Override
    public void renderBackground(GuiGraphics g, int x, int y, int w, int h, long ticks) {

        /* 1) fond noir translucide */
        g.fill(x, y, x + w, y + h, 0x90000000);

        /* 2) cadre chair sombre */
        int flesh = 0xFF4A2222;

        // haut
        g.fill(x, y, x + w, y + BORDER_THICK, flesh);
        // bas
        g.fill(x, y + h - BORDER_THICK, x + w, y + h, flesh);
        // gauche
        g.fill(x, y + BORDER_THICK, x + BORDER_THICK, y + h - BORDER_THICK, flesh);
        // droite
        g.fill(x + w - BORDER_THICK, y + BORDER_THICK, x + w, y + h - BORDER_THICK, flesh);

        /* 3) veines régulières (respiration lente) */
        float puls = (float) (Math.sin(ticks * 2 * Math.PI / 160) * .5 + .5); // cycle 8 s

        int topY = y + BORDER_THICK / 2;
        int botY = y + h - BORDER_THICK / 2 - 1;
        int leftX = x + BORDER_THICK / 2;
        int rightX = x + w - BORDER_THICK / 2 - 1;

        java.util.function.BiConsumer<Integer, Integer> veinSeg = (vx, vy) -> {
            int key = ((vx + vy * 31) ^ SEED);
            boolean beat = (key & 7) == (ticks / 40 & 7);   // 1/8 segments
            float amp = beat ? (0.15f * puls) : 0;
            int r = 210 + (int) (210 * amp);
            int gCol = 40 + (int) (40 * amp);
            int col = 0xFF000000 | (r << 16) | (gCol << 8) | 40;

            if (vy == topY || vy == botY) g.fill(vx, vy, vx + 3, vy + 1, col);
            else g.fill(vx, vy, vx + 1, vy + 3, col);
        };

        for (int vx = x + BORDER_THICK; vx < x + w - BORDER_THICK; vx += TILE) {
            veinSeg.accept(vx, topY);
            veinSeg.accept(vx, botY);
        }
        for (int vy = y + BORDER_THICK; vy < y + h - BORDER_THICK; vy += TILE) {
            veinSeg.accept(leftX, vy);
            veinSeg.accept(rightX, vy);
        }

        /* 4) excroissances aléatoires (respirent moins souvent) */
        Random rng = new Random(SEED);
        int maxOut = 6, maxIn = Math.max(2, BORDER_PAD_TOP / 2);
        java.util.function.IntFunction<Integer> pulseVeinCol = baseKey -> {
            boolean beat = ((baseKey ^ ticks) & 7) == 0;
            int rV = beat ? 255 : 220;
            int gV = beat ? 80 : 40;
            return 0xFF000000 | (rV << 16) | (gV << 8) | 40;
        };

        // TOP
        for (int vx = x + BORDER_THICK; vx < x + w - BORDER_THICK; vx += TILE) {
            if (rng.nextFloat() < .25f) { // ext
                int len = rng.nextInt(maxOut - 1) + 2;
                g.fill(vx, y - len, vx + 3, y, flesh);
                g.fill(vx + 1, y - len, vx + 2, y, pulseVeinCol.apply(vx));
            }
            if (rng.nextFloat() < .25f) { // int
                int len = rng.nextInt(maxIn - 1) + 2;
                int sY = y + BORDER_THICK + 2;
                g.fill(vx, sY, vx + 3, sY + len, flesh);
                g.fill(vx + 1, sY, vx + 2, sY + len, pulseVeinCol.apply(vx ^ 13));
            }
        }
        // BOTTOM
        for (int vx = x + BORDER_THICK; vx < x + w - BORDER_THICK; vx += TILE) {
            if (rng.nextFloat() < .25f) { // ext
                int len = rng.nextInt(maxOut - 1) + 2;
                g.fill(vx, y + h, vx + 3, y + h + len, flesh);
                g.fill(vx + 1, y + h, vx + 2, y + h + len, pulseVeinCol.apply(vx ^ 7));
            }
            if (rng.nextFloat() < .25f) { // int
                int len = rng.nextInt(maxIn - 1) + 2;
                g.fill(vx, y + h - BORDER_THICK - 2 - len, vx + 3, y + h - BORDER_THICK - 2, flesh);
                g.fill(vx + 1, y + h - BORDER_THICK - 2 - len, vx + 2, y + h - BORDER_THICK - 2, pulseVeinCol.apply(vx ^ 5));
            }
        }
        // LEFT / RIGHT
        for (int vy = y + BORDER_THICK; vy < y + h - BORDER_THICK; vy += TILE) {
            if (rng.nextFloat() < .25f) {
                int len = rng.nextInt(maxOut - 1) + 2;
                g.fill(x - len, vy, x, vy + 3, flesh);
                g.fill(x - len, vy + 1, x, vy + 2, pulseVeinCol.apply(vy ^ 21));
            }
            if (rng.nextFloat() < .25f) {
                int len = rng.nextInt(maxIn - 1) + 2;
                g.fill(x + BORDER_THICK + 2, vy, x + BORDER_THICK + 2 + len, vy + 3, flesh);
                g.fill(x + BORDER_THICK + 2, vy + 1, x + BORDER_THICK + 2 + len, vy + 2, pulseVeinCol.apply(vy ^ 17));
            }
            if (rng.nextFloat() < .25f) {
                int len = rng.nextInt(maxOut - 1) + 2;
                g.fill(x + w, vy, x + w + len, vy + 3, flesh);
                g.fill(x + w, vy + 1, x + w + len, vy + 2, pulseVeinCol.apply(vy ^ 31));
            }
            if (rng.nextFloat() < .25f) {
                int len = rng.nextInt(maxIn - 1) + 2;
                g.fill(x + w - BORDER_THICK - 2 - len, vy, x + w - BORDER_THICK - 2, vy + 3, flesh);
                g.fill(x + w - BORDER_THICK - 2 - len, vy + 1, x + w - BORDER_THICK - 2, vy + 2, pulseVeinCol.apply(vy ^ 27));
            }
        }
    }

    @Override
    public ScrollPanel createPanel(Minecraft mc, int left, int top, int width, int height) {
        int inLeft = left + BORDER_PAD_SIDE;
        int inTop = top + BORDER_PAD_TOP;
        int inW = width - BORDER_PAD_SIDE * 2;
        int inH = height - BORDER_PAD_TOP - BORDER_PAD_BOT;
        currentPanel = new Panel(this, mc, inW, inH, inTop, inLeft);
        return currentPanel;
    }

    /* ====================================================================================================== */
    /*  PANEL                                                                                                 */
    /* ====================================================================================================== */
    private class Panel extends ScrollPanel {

        private final ModulesCategory parent;         // accès au snapshot + textures
        private final List<ModuleCheckBox> cbs = new ArrayList<>();
        private final int panelLeft, panelTop, panelW;
        private final TexturedButton applyBtn;

        Panel(ModulesCategory parent, Minecraft mc, int w, int h, int top, int left) {
            super(mc, w, h, top, left);
            this.parent = parent;
            panelLeft = left;
            panelTop = top;
            panelW = w;
            buildEntries();
            layoutEntries();

            // bouton “Apply” (tout de suite APRÈS layoutEntries, pour connaitre Y max)
            int bw = 80, bh = 20;
            int btnX = left + w - bw;   // bord droit
            int btnY = top + Math.max(h, getContentHeight()) + 8;
            applyBtn = new TexturedButton(btnX, btnY, bw, bh, Component.literal("Apply"), rl("btn_apply_idle.png"), rl("btn_apply_hover.png"), rl("btn_apply_down.png"), this::applyChanges);
            this.applyBtn.active = false;
        }


        private void renderCustomScrollbar(GuiGraphics g) {

            /* ---- mêmes calculs que ScrollPanel.render() ---- */
            int extra = getContentHeight() + border - height;
            if (extra <= 0) return;                 // pas de barre

            int barH = height * height / getContentHeight();
            barH = net.minecraft.util.Mth.clamp(barH, 32, height - border * 2);

            int barTop = (int) scrollDistance * (height - barH) / extra + top;
            barTop = Math.max(top, barTop);

            int barX = left + width - BAR_W;

            /* ---- rail (texture répétée verticalement) ---- */
            for (int y = 0; y < height; y += 16) {
                int hHere = Math.min(16, height - y);
                g.blit(TEX_SCROLL_BG, barX, top + y, 0, 0, BAR_W, hHere, 8, 16);
            }

            /* ---- poignée (texture répétée) ---- */
            for (int y = 0; y < barH; y += 16) {
                int hHere = Math.min(16, barH - y);
                g.blit(TEX_SCROLL_GRIP, barX, barTop + y, 0, 0, BAR_W, hHere, 8, 16);
            }
        }

        /* ----------------------------------------------------------------
         * 2)  override render : on laisse ScrollPanel tout faire, puis *
         *     on redessine NOTRE barre par-dessus.                         */
        /* ----------------------------------------------------------------*/
        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            super.render(gui, mouseX, mouseY, partialTick);  // dessin vanilla
            renderCustomScrollbar(gui);                      // ← ICI !
            applyBtn.render(gui, mouseX, mouseY, partialTick);
        }

        /* ------------ construction / layout des checkboxes ------------ */
        private void buildEntries() {

            // a) on repart de zéro
            cbs.clear();
            parent.selectionMap.clear();

            // b) on tire un *snapshot* frais de la config serveur
            List<ModuleManager.ModuleStatus> fresh = ModuleManager.listAllModuleStatuses();

            for (ModuleManager.ModuleStatus st : fresh) {
                ResourceLocation id = st.module().id();
                boolean enabled = st.enabled();

                // log pour vérification
                //BCKLog.debug("§cModulesCategory", "§b" + id + " §e-> " + (enabled ? "§atrue" : "§4false"));

                // case à cocher
                cbs.add(new ModuleCheckBox(0, 0, 150, ENTRY_H_INNER, Component.literal(id.toString()), enabled, id));

                // maps tenues à jour
                parent.selectionMap.put(id, enabled);
            }

            parent.originalSelection.clear();
            parent.originalSelection.putAll(parent.selectionMap);
        }

        private void layoutEntries() {
            int cols = Math.max(1, (panelW - 16) / 170);
            int colW = (panelW - 16) / cols;
            int baseX = panelLeft + 4;
            for (int i = 0; i < cbs.size(); i++) {
                ModuleCheckBox cb = cbs.get(i);
                int row = i / cols, col = i % cols;
                cb.setPosition(baseX + col * colW + ENTRY_PAD, panelTop + row * ENTRY_H_TOTAL + ENTRY_PAD);
                cb.setWidth(colW - ENTRY_PAD * 2);
            }
        }

        private void applyChanges() {
            // TODO : envoyer le packet au serveur
            TetralibsMod.PACKET_HANDLER.sendToServer(new C2SApplyModulesPacket(new LinkedHashMap<>(parent.selectionMap)));

            originalSelection.clear();
            originalSelection.putAll(selectionMap);
            applyBtn.active = false;          // re-devient grisé
        }

        @Override
        public int getContentHeight() {
            int cols = Math.max(1, (panelW - 16) / 170);
            return (int) Math.ceil((double) cbs.size() / cols) * ENTRY_H_TOTAL;
        }

        /* ------------ rendu panneau ------------ */
        @Override
        protected void drawPanel(GuiGraphics g, int right, int scrollY, Tesselator t, int mx, int my) {
            int viewH = this.bottom - this.top;
            int maxScroll = getContentHeight() - viewH;
            int off = maxScroll > 0 ? (int) this.scrollDistance : 0;

            g.pose().pushPose();
            g.pose().translate(0, -off, 0);
            for (ModuleCheckBox cb : cbs)
                cb.renderWidget(g, mx, my + off, Minecraft.getInstance().getDeltaFrameTime());
            g.pose().popPose();
        }

        /* click / release */
        @Override
        public boolean mouseClicked(double x, double y, int btn) {
            int viewH = this.bottom - this.top;
            int maxScrl = getContentHeight() - viewH;
            double adjY = y + (maxScrl > 0 ? this.scrollDistance : 0);

            /* --- cases --- */
            for (ModuleCheckBox cb : cbs)
                if (cb.mouseClicked(x, adjY, btn))   // AbstractWidget gère le son + onClick
                    return true;

            /* --- apply --- */
            if (applyBtn.mouseClicked(x, y, btn)) return true;

            return super.mouseClicked(x, y, btn);
        }

        @Override
        public boolean mouseReleased(double x, double y, int btn) {
            int viewH = this.bottom - this.top;
            int maxScrl = getContentHeight() - viewH;
            double adjY = y + (maxScrl > 0 ? this.scrollDistance : 0);

            for (ModuleCheckBox cb : cbs)
                if (cb.mouseReleased(x, adjY, btn)) return true;

            if (applyBtn.mouseReleased(x, y, btn)) return true;

            return super.mouseReleased(x, y, btn);
        }

        @Override
        public NarratableEntry.@NotNull NarrationPriority narrationPriority() {
            return NarratableEntry.NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput out) {
        }

        /* =================================================================== */
        /*   ModuleCheckBox : case + label                                     */
        /* =================================================================== */
        private class ModuleCheckBox extends AbstractWidget {

            private final ResourceLocation moduleId;
            private boolean checked;

            ModuleCheckBox(int x, int y, int w, int h, Component txt, boolean val, ResourceLocation id) {
                super(x, y, w, h, txt);
                this.checked = val;
                this.moduleId = id;
            }

            @Override
            public boolean isMouseOver(double mx, double my) {
                return mx >= getX() && mx <= getX() + width && my >= getY() && my <= getY() + height;
            }

            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float delta) {
                boolean hov = isMouseOver(mx, my);

                /* fond + bordure */
                g.blitNineSliced(BG_ROUNDED, getX() - ENTRY_PAD, getY() - ENTRY_PAD, width + ENTRY_PAD * 2, height + ENTRY_PAD * 2, 8, 3, 3, 3, 3);

                int col = hov ? 0xFFFF4A1A : 0xAA330000;
                g.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), col);
                g.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, col);
                g.fill(getX() - 1, getY(), getX(), getY() + height, col);
                g.fill(getX() + width, getY(), getX() + width + 1, getY() + height, col);

                /* case 14×14 */
                int bx = getX() + 3, by = getY() + 3;
                g.blit(TEX_CB_BG, bx, by, 0, 0, 14, 14, 14, 14);
                g.blit(checked ? TEX_CB_CHECK : TEX_CB_CROSS, bx, by, 0, 0, 14, 14, 14, 14);

                /* texte */
                int tx = getX() + 3 + 14 + 4, ty = getY() + (height - 8) / 2;
                g.drawString(Minecraft.getInstance().font, getMessage(), tx, ty, hov ? 0xFFF0C070 : 0xFFFFFFFF, false);
            }

            @Override
            public void onClick(double mx, double my) {
                checked = !checked;
                parent.selectionMap.put(moduleId, checked);
                applyBtn.active = !parent.selectionMap.equals(parent.originalSelection);
            }

            @Override
            protected void updateWidgetNarration(@NotNull NarrationElementOutput out) {
            }
        }
    }
}