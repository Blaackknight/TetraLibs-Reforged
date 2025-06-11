package fr.bck.tetralibs.core;

import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.modules.core.BCKCoreEventHandler;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;

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
