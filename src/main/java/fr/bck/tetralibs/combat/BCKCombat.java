package fr.bck.tetralibs.combat;

import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.data.BCKUserdata;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;




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

public class BCKCombat {

    public static final class Utils {
        // ta couleur
        public static final String color = "§c";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static void kill(Entity entity) {
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("bck_combat.join_while_in_combat")), false);
        TetralibsMod.queueServerWork(30, entity::kill);
        exit(entity);
    }

    /**
     * Permet de faire entré un joueur en mode combat.
     *
     * @param entity Le joueur
     */
    public static void enter(Entity entity) {
        double duration = getCombatDuration();
        double cooldown = getPlayerCombatCooldown(entity);
        double num = cooldown < duration ? (duration - cooldown) : duration;
        setPlayerInCombat(entity, num);
    }

    /**
     * Permet de faire sortir un joueur du mode combat.
     *
     * @param entity Le joueur
     */
    public static void exit(Entity entity) {
        if (inCombat(entity)) {
            BCKUserdata.data("bck_combat.in_combat", "set", false, entity.level(), entity);
            setPlayerCombatCooldown(entity, 0);
        }
    }

    /**
     * Permet de mettre le joueur en combat.
     *
     * @param entity   Le joueur
     * @param cooldown Le cooldown
     */
    public static void setPlayerInCombat(Entity entity, double cooldown) {
        BCKUserdata.data("bck_combat.in_combat", "set", true, entity.level(), entity);
        setPlayerCombatCooldown(entity, cooldown);
    }

    /**
     * Permet d'obtenir le cooldown de combat d'un joueur.
     *
     * @param entity Le joueur
     * @return Cooldown de combat.
     */
    public static double getPlayerCombatCooldown(Entity entity) {
        return ((DataWrapper) BCKUserdata.data("bck_combat.cooldown", entity.level(), entity)).getDouble();
    }

    /**
     * Permet de définir le cooldown de combat d'un joueur.
     *
     * @param entity   Le joueur
     * @param cooldown Cooldown de c ombat.
     */
    public static void setPlayerCombatCooldown(Entity entity, double cooldown) {
        BCKUserdata.data("bck_combat.cooldown", "set", cooldown, entity.level(), entity);
    }

    /**
     * Vérifie si le joueur est actuellement en combat.
     *
     * @param entity Le joueur
     * @return Si le joueur est en combat actuellement.
     */
    public static boolean inCombat(Entity entity) {
        return ((DataWrapper) BCKUserdata.data("bck_combat.in_combat", entity.level(), entity)).getBoolean();
    }

    /**
     * Permet d'obtenir la durée d'un combat.
     *
     * @return Obtient la durée d'un combat.
     */
    public static double getCombatDuration() {
        return ((DataWrapper) BCKServerdata.data("server.bck_combat.duration")).getDouble();
    }

    /**
     * Permet de définir la durée d'un combat.
     *
     * @param duration Durée du combat.
     */
    public static void setCombatDuration(double duration) {
        BCKServerdata.data("server.bck_combat.duration", "set", duration);
    }

    private BCKCombat() {
    }
}
