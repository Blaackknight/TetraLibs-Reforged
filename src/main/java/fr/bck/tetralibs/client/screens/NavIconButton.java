package fr.bck.tetralibs.client.screens;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

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

public class NavIconButton extends AbstractWidget {
    /* ------------------------------------------------ champs */
    private final ResourceLocation icon;
    private final ResourceLocation texNormal, texHover, texDown;
    private final Runnable onClick;

    private boolean isDown = false;               // ← NOUVEAU

    public NavIconButton(int x, int y, int w, int h, Component label, @Nullable ResourceLocation icon, ResourceLocation normal, @Nullable ResourceLocation hover, @Nullable ResourceLocation down, Runnable onClick) {
        super(x, y, w, h, label);
        this.icon = icon;
        this.texNormal = normal;
        this.texHover = hover != null ? hover : normal;
        this.texDown = down != null ? down : this.texHover;
        this.onClick = onClick;
    }

    /* ------------------------------------------------ RENDER */
    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {

        boolean hovered = isHovered();
        ResourceLocation tex = isDown ? texDown : (hovered ? texHover : texNormal);

        /* ------------------------------------------------ pose stack */
        g.pose().pushPose();

        if (isDown) {
            /* 1) se placer au centre du bouton … */
            float cx = getX() + this.width / 2f;
            float cy = getY() + this.height / 2f;
            g.pose().translate(cx, cy, 0);

            /* 2) … appliquer le scale (1.05 = +5 %) … */
            g.pose().scale(1.05f, 1.05f, 1f);

            /* 3) … puis revenir à l’origine avant de dessiner */
            g.pose().translate(-cx, -cy, 0);
        }

        /* ------------------------------------------------ fond */
        g.blit(tex, getX(), getY(), 0, 0, this.width, this.height, this.width, this.height);

        /* ------------------------------------------------ icône */
        if (icon != null) {
            int ix = getX() + 4;
            int iy = getY() + (this.height - 16) / 2;
            g.blit(icon, ix, iy, 0, 0, 16, 16, 16, 16);
        }

        /* ------------------------------------------------ texte */
        int tx = getX() + 24;
        int ty = getY() + (this.height - 8) / 2;
        int color = hovered ? 0xFFEBC86B : 0xFFFFFFFF;
        g.drawString(Minecraft.getInstance().font, getMessage(), tx, ty, color, false);

        g.pose().popPose();  // ← IMPORTANT : ré-initialise pour les widgets suivants
    }

    /* ------------------------------------------------ EVENTS */
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && isMouseOver(mx, my)) {
            isDown = true;                                   // bouton enfoncé
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0 && isDown) {
            isDown = false;
            if (isMouseOver(mx, my)) onClick.run();          // déclenche action
            return true;
        }
        return false;
    }

    /* narration (inchangé) */
    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput out) {
    }
}
