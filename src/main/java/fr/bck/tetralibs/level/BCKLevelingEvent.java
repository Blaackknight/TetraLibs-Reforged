package fr.bck.tetralibs.level;


/*≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
 ≡              Copyright BCK, Inc 2025. (DragClover / Blackknight)              ≡
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

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class BCKLevelingEvent {
    /**
     * Événement déclenché lorsqu'une entité LevelingEntity gagne de l'XP.
     */
    @Cancelable  // Permet d'annuler l'événement si nécessaire
    public static class ExperienceGainEvent extends EntityEvent {

        private final String type;
        private final double xpGained;
        private final double currentXP;

        public ExperienceGainEvent(Entity entity, String type, double xpGained, double currentXP) {
            super(entity);
            this.type = type;
            this.xpGained = xpGained;
            this.currentXP = currentXP;
        }

        /**
         * Retourne le type d'expérience ayant déclenché le gain.
         */
        public String getType() {
            return type;
        }

        /**
         * Retourne la quantité d'XP gagnée.
         */
        public double getXpGained() {
            return xpGained;
        }

        /**
         * Retourne l'XP totale actuelle après l'ajout.
         */
        public double getCurrentXP() {
            return currentXP;
        }
    }

    /**
     * Événement déclenché lorsqu'une entité LevelingEntity monte de niveau.
     */
    @Cancelable  // Permet d'annuler l'événement si nécessaire
    public static class LevelUpEvent extends EntityEvent {

        private final String type;
        private final int oldLevel;
        private final int newLevel;

        public LevelUpEvent(Entity entity, String type, int oldLevel, int newLevel) {
            super(entity);
            this.type = type;
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
        }

        /**
         * Retourne le type d'expérience ayant déclenché le level-up.
         */
        public String getType() {
            return type;
        }

        /**
         * Retourne l'ancien niveau avant le level-up.
         */
        public int getOldLevel() {
            return oldLevel;
        }

        /**
         * Retourne le nouveau niveau après le level-up.
         */
        public int getNewLevel() {
            return newLevel;
        }
    }
}
