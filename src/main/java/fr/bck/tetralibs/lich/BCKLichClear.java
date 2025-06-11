package fr.bck.tetralibs.lich;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.modules.lich.BCKLichClearEventHandler;
import net.minecraftforge.common.MinecraftForge;

public class BCKLichClear {
    public static final class Utils {
        // ta couleur
        public static final String color = "§b";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(BCKLichClearEventHandler.class);
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichClear.class), "§aHandlers registered");
    }
}
