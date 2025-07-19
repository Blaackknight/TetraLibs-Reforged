package fr.bck.tetralibs.client.screens;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ScrollPanel;

import javax.annotation.Nullable;


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
 * Interface qu’une catégorie du gestionnaire TetraLibs doit implémenter.
 * Chaque catégorie fournit son nom (affiché sur le bouton de gauche)
 * et fabrique le panneau (ScrollPanel) qui s’affichera à droite.
 */
public interface BCKCategory {
    /**
     * Nom affiché dans la colonne de navigation.
     */
    String getName();

    /**
     * Construit le panneau associé.
     *
     * @param mc     instance client
     * @param left   x du coin supérieur gauche
     * @param top    y du coin supérieur gauche
     * @param width  largeur disponible
     * @param height hauteur disponible
     * @return le panneau prêt à être ajouté à l’écran
     */
    ScrollPanel createPanel(Minecraft mc, int left, int top, int width, int height);

    /**
     * Icône 16×16 placée à gauche du texte.
     */
    default @Nullable ResourceLocation getIcon() {
        return null;
    }

    /**
     * Sprite pour l’état NORMAL (obligatoire si on veut un fond custom).
     */
    default @Nullable ResourceLocation getButtonSprite() {
        return null;
    }

    /**
     * Sprite survol ; si null ⇒ on ré-utilise normal.
     */
    default @Nullable ResourceLocation getButtonHover() {
        return null;
    }

    /**
     * Sprite pressé ; si null ⇒ on ré-utilise hover.
     */
    default @Nullable ResourceLocation getButtonPressed() {
        return null;
    }

    /**
     * Dessine le fond derrière le panneau.
     *
     * @param g     GuiGraphics courant
     * @param x     x du coin haut-gauche de la zone panneau
     * @param y     y du coin haut-gauche de la zone panneau
     * @param w,h   dimensions de la zone panneau
     * @param ticks âge du client (→ permet des animations)
     */
    default void renderBackground(GuiGraphics g, int x, int y, int w, int h, long ticks) {
        // impl. par défaut : rien (fond transparent)
    }

    /**
     * Appelé juste avant qu’un panneau de la catégorie ne soit (re)créé.
     * Par défaut : ne fait rien.
     */
    default void refresh() {}
}

