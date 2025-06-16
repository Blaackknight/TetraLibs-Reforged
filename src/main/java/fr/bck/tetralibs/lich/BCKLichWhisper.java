package fr.bck.tetralibs.lich;

import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKUserdata;
import fr.bck.tetralibs.module.ModuleIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.server.ServerLifecycleHooks;



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
 * Canal de log “LichWhisper”.
 * <p>
 * Règles :
 * <ul>
 *   <li>Le niveau d’un joueur est stocké dans Userdata → <b>lich_whisper.level</b> (double).</li>
 *   <li><b>messageLevel == 6.0</b> : envoi console <u>seulement</u>.</li>
 *   <li><b>messageLevel &gt; 6.0</b> (6.x) : console + joueurs dont le niveau ≥ 1.<br>
 *       &nbsp;• si <code>only</code> est vrai ⇒ console + joueurs avec niveau ≥ <code>messageLevel</code>.</li>
 *   <li><b>messageLevel &lt; 6</b> : joueurs dont le niveau ≥ <code>messageLevel</code>.<br>
 *       &nbsp;• si <code>only</code> est vrai ⇒ console aussi.</li>
 * </ul>
 * Tous les messages portent le préfixe : §6[§5LichWhisper§6/§d&lt;messageLevel&gt;§6]
 */
public final class BCKLichWhisper {
    public static final class Utils {
        // ta couleur
        public static final String color = "§b";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static String[] defaultLog = "0|1|2|3|4|5".split("\\|");

    /**
     * @param body         Contenu déjà stylé (Component).
     * @param messageLevel Importance du log (ex : 4 / 6 / 6.2 …).
     * @param only         true → filtrage strict + console ; false → comportement normal.
     */
    public static void send(Component body, double messageLevel, boolean only) {
        if (!ModulesConfig.isEnabled(ModuleIds.BCK_LICH_WHISPER)) return;
        // ---------- Préfixe ----------
        String prefix = "§6[" + BCKCore.TITLES_COLORS.title(BCKLichWhisper.class) + "§6/§d" + messageLevel + "§6] §r";
        Component fullMsg = Component.literal(prefix).append(body);

        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        boolean consoleSent = false;

        for (ServerPlayer p : srv.getPlayerList().getPlayers()) {
            double playerLvl = getLogLevel(p);

            /* ----- cas 6.0 : jamais client ----- */
            if (messageLevel == 6.0) continue;

            /* ----- cas 6.x → console + public / restreint ----- */
            if (messageLevel > 6.0) {
                if (!consoleSent) {
                    logConsole(fullMsg);
                    consoleSent = true;
                }

                if (only) {
                    if (playerLvl >= messageLevel) p.sendSystemMessage(fullMsg);
                } else {
                    if (playerLvl >= 1.0) p.sendSystemMessage(fullMsg);
                }
                continue;
            }

            /* ----- cas < 6 ----- */
            if (playerLvl >= messageLevel) p.sendSystemMessage(fullMsg);
        }

        /* Ajoute la console pour les messages « normaux » si only = true */
        if (only && !consoleSent && messageLevel < 6.0) {
            logConsole(fullMsg);
        }
        /* Envoi console forcé déjà fait pour messageLevel >= 6 ;  sinon rien. */
    }

    public static void send(Component body, double messageLevel) {
        send(body, messageLevel, false);
    }

    public static void set(Entity entity, double lvl, boolean callback) {
        BCKUserdata.data("lich_whisper.level", "set", lvl, entity.level(), entity);
        if (callback)
            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichWhisper.class), "§8" + entity.getName().getString() + " §7level has been set to §d" + lvl);
    }

    public static void set(Entity entity, double lvl) {
        set(entity, lvl, false);
    }

    /* ============ utilitaires internes ============ */

    /**
     * lit Userdata : double ; renvoie 0.0 s’il n’existe pas
     */
    public static double getLogLevel(ServerPlayer p) {
        return ((DataWrapper) BCKUserdata.data("lich_whisper.level", p.level(), p)).getDouble();
    }

    private static void logConsole(Component msg) {
        ServerLifecycleHooks.getCurrentServer().sendSystemMessage(Component.literal("§6[" + BCKCore.TITLES_COLORS.title(BCKLichWhisper.class) + "§6] §r" + msg.getString()));
    }

    private BCKLichWhisper() {
    }
}
