package fr.bck.tetralibs.core;

import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.modules.core.BCKCoreEventHandler;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;



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

public class BCKCore {
    public static final class Utils {
        // ta couleur
        public static final String color = "§b";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static void init() {
        BCKLog.info(TITLES_COLORS.title(BCKCore.class), "Created by §4§lDragClover (Blackknight/BCK)");
        MinecraftForge.EVENT_BUS.register(new BCKCoreEventHandler());
        BCKLog.debug(TITLES_COLORS.title(BCKCore.class), "§aHandlers registered");
        if (BCKUtils.GenericUtil.getModsUsing(TetralibsMod.MODID).isEmpty()) {
            BCKLog.info(TITLES_COLORS.title(BCKCore.class) + "§6/§9§nAPI§r", "§7No Mods using §cTetraLibs API §7founded..");
        } else {
            // Affiche la liste des mods utilisant l'API TetraLibs
            BCKLog.info(TITLES_COLORS.title(BCKCore.class) + "§6/§9§nAPI§r", "§7Mods using §cTetraLibs API§7: §e" + BCKUtils.GenericUtil.getModsUsing(TetralibsMod.MODID));
        }
        //AutoModuleScanner.find().forEach(TetraRegistries::registerAuto);
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingBiConsumer<T, U> {
        void accept(T t, U u) throws Throwable;
    }

    /**
     * Exécute l’opération fournie et wrappe toute Throwable levée
     * dans un nouveau Throwable enrichi du préfixe et du message.
     *
     * @param cat le titre / catégorie
     * @param msg message additionnel
     * @param <T> type de retour de l’opération
     * @return le résultat de l’opération si elle réussit
     * @throws Throwable en cas d’échec, wrappe la cause
     */
    public static <T> T doRiskyOperation(String cat, Object msg, ThrowingSupplier<T> op) {
        try {
            return op.get();
        } catch (Throwable t) {
            String fullMessage = cat + " §7- §e§oinfo supplémentaire§r §7: " + msg;
            throw new RuntimeException(fullMessage, t);
        }
    }

    public static void doRiskyOperation(String cat, Object msg, ThrowingRunnable op) {
        try {
            op.run();
        } catch (Throwable t) {
            String fullMessage = cat + " §7- §e§oinfo supplémentaire§r §7: " + msg;
            throw new RuntimeException(fullMessage, t);
        }
    }

    public static abstract class TITLES_COLORS {
        /**
         * Récupère la valeur de <code>Utils.full</code> dans la classe passée en paramètre.
         *
         * @param c   la classe mère (qui contient une inner class statique Utils)
         * @param <T> type de la classe
         * @return le champ Utils.full (p.ex. "§cBCKCombat")
         */
        public static <T> String title(Class<T> c) {
            // on parcourt les classes imbriquées
            for (Class<?> inner : c.getDeclaredClasses()) {
                if ("Utils".equals(inner.getSimpleName())) {
                    try {
                        // on récupère le Field full
                        Field fullField = inner.getDeclaredField("full");
                        fullField.setAccessible(true);
                        // comme c'est static, on passe null
                        return (String) fullField.get(null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException("Impossible d'accéder au champ full dans " + inner.getName(), e);
                    }
                }
            }
            BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKCore.class), "La classe " + c.getName() + " ne contient pas d'inner class Utils");
            return "§f" + c.getSimpleName();
        }
    }

    private BCKCore() {
    }
}
