package fr.bck.tetralibs.client.screens;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

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
 * Écran central de gestion TetraLibs (ouvert via la touche M ou un bouton).
 * <p>Les catégories sont fournies par {@link BCKCategoryRegistry} : aucun « + » n’est visible, l’utilisateur ne crée
 * plus de catégories manuellement. La liste de gauche affiche un bouton par catégorie enregistrée.
 * La zone de droite montre le panneau retourné par la catégorie sélectionnée.</p>
 */
public class BCKManagementScreen extends Screen {
    private static final int NAV_WIDTH = 120; // largeur colonne navigation
    private static final int PADDING = 20;

    private final List<BCKCategory> categories = BCKCategoryRegistry.getAll();

    /**
     * Panneau actif (détruit / reconstruit à chaque changement).
     */
    private ScrollPanel activePanel;

    public BCKManagementScreen() {
        super(Component.literal("TetraLibs Management"));
    }

    public void unlockCurrentGatePanel() {
        if (activePanel instanceof BCKGateCategory.GateKeyPanel gate) {
            gate.showSuccess();   // -> remplace EditBox par boutons super-admin
        }
    }

    public final ScrollPanel getActivePanel() {
        return activePanel;
    }

    // --------------------------------------------------------------------- init / layout
    @Override
    protected void init() {
        buildNavButtons();
        //BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKManagementScreen.class), "§6Buttons: §d" + categories.size());
        openCategory(0); // par défaut la première catégorie
        // bouton Done centré
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    private int selectedIdx = 0;          // mémorise la catégorie active

    /**
     * Construit la colonne gauche avec un bouton par catégorie.
     */
    private void buildNavButtons() {
        int y = PADDING;
        int i = 0;

        for (BCKCategory cat : categories) {
            final int idx = i;

            /* -- récupère les textures fournies par la catégorie -- */
            ResourceLocation icon = cat.getIcon();
            ResourceLocation norm = cat.getButtonSprite();
            ResourceLocation hover = cat.getButtonHover();
            ResourceLocation down = cat.getButtonPressed();

            if (norm != null) {   // ► bouton full-custom
                addRenderableWidget(new NavIconButton(PADDING, y, NAV_WIDTH - 2 * PADDING, 20, Component.literal(cat.getName()), icon, norm, hover, down, () -> {
                    selectedIdx = idx;
                    openCategory(idx);
                }));
            } else {              // ► fallback : bouton vanilla
                addRenderableWidget(Button.builder(Component.literal(cat.getName()), b -> {
                    selectedIdx = idx;
                    openCategory(idx);
                }).bounds(PADDING, y, NAV_WIDTH - 2 * PADDING, 20).build());
            }
            y += 24;
            i++;
        }
    }

    private void openCategory(int idx) {
        if (idx < 0 || idx >= categories.size()) return;
        // Détruire l’ancien panneau
        if (activePanel != null) removeWidget(activePanel);
        // Fabriquer le nouveau
        BCKCategory cat = categories.get(idx);
        // 2 bis) ***NOUVEAU*** – demande à la catégorie de se mettre à jour
        cat.refresh();                     // ← voilà l’appel propre
        int panelLeft = NAV_WIDTH + PADDING;
        int panelTop = PADDING;
        int panelW = this.width - panelLeft - PADDING;
        int panelH = this.height - panelTop - 60; // laisse place au bouton Done
        activePanel = cat.createPanel(Minecraft.getInstance(), panelLeft, panelTop, panelW, panelH);
        addRenderableWidget(activePanel);
    }

    // --------------------------------------------------------------------- resize support
    @Override
    public void resize(@NotNull Minecraft mc, int w, int h) {
        super.resize(mc, w, h);
        rebuild();
    }

    /**
     * Reconstruit entièrement l’écran après un redimensionnement.
     */
    private void rebuild() {
        this.clearWidgets();
        init();
    }

    // --------------------------------------------------------------------- fond semi‑transparent pour rappeler le menu principal
    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float delta) {
        /* --- fond global translucide (menu vanilla) --- */
        g.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        /* --- fond spécifique catégorie ---------------- */
        if (!categories.isEmpty()) {
            BCKCategory cat = categories.get(selectedIdx);

            int panelLeft = NAV_WIDTH + PADDING;
            int panelTop = PADDING;
            int panelW = this.width - panelLeft - PADDING;
            int panelH = this.height - panelTop - 60;

            assert Minecraft.getInstance().level != null;
            long ticks = Minecraft.getInstance().level.getGameTime();
            cat.renderBackground(g, panelLeft, panelTop, panelW, panelH, ticks);
        }

        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (activePanel instanceof ITickablePanel p) {
            p.tick();          // générique pour tous les panneaux qui l’implémentent
        }
    }
}