package fr.bck.tetralibs.core;

import fr.bck.tetralibs.module.AutoTetraModule;
import fr.bck.tetralibs.module.TetraModule;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;

import java.util.HashSet;
import java.util.Set;

public final class AutoModuleScanner {

    public static final class Utils {
        // ta couleur
        public static final String color = "§8";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    /**
     * Couple (classe du module, booléen def)
     */
    public record Holder(Class<? extends TetraModule> clazz, boolean def) {
    }

    public static Set<Holder> find() {
        Set<Holder> out = new HashSet<>();

        for (ModFileScanData scan : ModList.get().getAllScanData()) {
            for (AnnotationData a : scan.getAnnotations()) {
                //BCKLog.error(BCKCore.TITLES_COLORS.title(AutoModuleScanner.class), AutoTetraModule.class.getSimpleName() + " -> SKIPPED -> " + a.annotationType().getClassName());
                if (!a.annotationType().getClassName().equals(AutoTetraModule.class.getName())) {
                    continue;
                }
                try {
                    Class<?> raw = Class.forName(a.clazz().getClassName().replace('/', '.'));
                    if (!TetraModule.class.isAssignableFrom(raw)) continue;

                    @SuppressWarnings("unchecked") Class<? extends TetraModule> modClass = (Class<? extends TetraModule>) raw;

                    boolean defVal = a.annotationData().containsKey("def") ? (Boolean) a.annotationData().get("def") : true;

                    out.add(new Holder(modClass, defVal));

                } catch (ClassNotFoundException e) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(AutoModuleScanner.class), "Impossible de charger " + a.clazz(), e);
                }
            }
        }
        return out;
    }

    private AutoModuleScanner() {
    }
}
