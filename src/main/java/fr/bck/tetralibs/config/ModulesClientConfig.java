package fr.bck.tetralibs.config;


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

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.network.SyncModulesPacket;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Map;

public class ModulesClientConfig {
    public static final class Utils {
        // ta couleur
        public static final String color = "§1";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_TOOLTIP;
    private static CommentedFileConfig CLIENT_CONFIG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder().comment("Activation / désactivation des modules TetraLibs (client)").push("modules");

        ENABLE_BCK_TOOLTIP = builder.define("bck_tooltip", true);

        builder.pop();
        CLIENT_SPEC = builder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    /**
     * À appeler lors du FMLClientSetupEvent pour charger le fichier.
     */
    public static void init() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("tetralibs-client.toml");
        CLIENT_CONFIG = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        CLIENT_CONFIG.load();

    }

    /**
     * Applique les overrides reçus du serveur.
     *
     * @param overrides map<moduleIdPath, overrideFalse>
     *                  true signifie "forcer en false"
     */
    public static void applyServerOverrides(Map<String, Boolean> overrides) {
        if (CLIENT_CONFIG == null) {
            throw new IllegalStateException("ModulesClientConfig.init() n’a pas été appelé !");
        }

        // Récupère la table [modules]
        CommentedConfig modulesSection = CLIENT_CONFIG.get("modules");

        // Parcourt chaque override
        for (Map.Entry<String, Boolean> entry : overrides.entrySet()) {
            String key = entry.getKey();
            boolean forceDisable = entry.getValue();
            BCKLog.debug(BCKCore.TITLES_COLORS.title(ModulesClientConfig.class) + "§6/§5Detection", "§d" + key + " §7need to be " + (!forceDisable ? "§atrue" : "§4false"));
            if (forceDisable) {
                // On force la clé "bck_tooltip" dans la section "modules" à false
                modulesSection.set(key, false);
                BCKLog.debug(BCKCore.TITLES_COLORS.title(ModulesClientConfig.class) + "§6/§5Action", "§7Override: §d" + key + " §e-> §4false");

                // Mise à jour immédiate du BooleanValue (ex. tooltip)
                if ("bck_tooltip".equals(key)) {
                    ENABLE_BCK_TOOLTIP.set(false);
                }
            }
        }

        // Sauvegarde : conservera la structure TOML correcte
        CLIENT_CONFIG.save();
        BCKLog.info(BCKCore.TITLES_COLORS.title(SyncModulesPacket.class), "§9Server modules §e-> §asynchronized §e-> §dClient");
    }
}
