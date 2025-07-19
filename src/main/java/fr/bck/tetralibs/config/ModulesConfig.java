package fr.bck.tetralibs.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.core.AutoModuleScanner;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.module.TetraModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;




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
 * Configuration TOML côté serveur :
 * <code>config/tetralibs-server.toml</code>
 * • Clés officielles pour modules (ForgeConfigSpec, Type.SERVER)
 * • Modules tiers ajoutés dynamiquement
 */
public final class ModulesConfig {

    public static final class Utils {
        // ta couleur
        public static final String color = "§f";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    /*──────── ForgeConfigSpec SERVER ────────*/
    public static final ForgeConfigSpec SERVER_SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_CORE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_BACKPACK;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_HOME;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_LEVELING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_PERMISSIONS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_RECIPE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_LICH_GUARD;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_SKILL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_SPAWN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_LICH_EYE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_LICH_CLEAR;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_LICH_WHISPER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_SERVERDATA;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_USERDATA;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_ECONOMY_MANAGER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_WARPS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_COMBAT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BCK_VANISH;

    // Valeurs par défaut (avant modification utilisateur)
    private static final Map<String, Boolean> DEFAULTS = new HashMap<>();
    private static Path configFilePath;

    // Pour forcer la relecture de la section TOML
    public static void reload() {
        // Réinitialise les caches pour forcer un re-chargement
        tomlSection = null;
        cache.clear();
        // Relire la config du disque
        loadSectionIfNeeded();
    }

    /**
     * Appelée depuis ton @Mod pour passer le Path exact
     */
    public static void setConfigFile(Path path) {
        configFilePath = path;
    }

    static {
        BUILDER.comment("Activation / désactivation des modules TetraLibs (serveur)");
        BUILDER.push("modules");

        // Pour chaque module, on stocke la valeur par défaut dans une variable
        boolean defCore = true;
        boolean defBackpack = false;
        boolean defHome = false;
        boolean defLeveling = false;
        boolean defPermissions = true;
        boolean defRecipe = true;
        boolean defLichGuard = false;
        boolean defSkill = false;
        boolean defSpawn = false;
        boolean defLichEye = false;
        boolean defLichClear = false;
        boolean defLichWhisper = true;
        boolean defServerData = true;
        boolean defUserData = true;
        boolean defEconomyManager = true;
        boolean defWarps = false;
        boolean defCombat = false;
        boolean defVanish = false;

        // On définit les BooleanValue avec ces valeurs et on alimente DEFAULTS
        ENABLE_BCK_CORE = BUILDER.define("bck_core", defCore);
        DEFAULTS.put("bck_core", defCore);

        ENABLE_BCK_BACKPACK = BUILDER.define("bck_backpack", defBackpack);
        DEFAULTS.put("bck_backpack", defBackpack);

        ENABLE_BCK_HOME = BUILDER.define("bck_home", defHome);
        DEFAULTS.put("bck_home", defHome);

        ENABLE_BCK_LEVELING = BUILDER.define("bck_leveling", defLeveling);
        DEFAULTS.put("bck_leveling", defLeveling);

        ENABLE_BCK_PERMISSIONS = BUILDER.define("bck_permissions", defPermissions);
        DEFAULTS.put("bck_permissions", defPermissions);

        ENABLE_BCK_RECIPE = BUILDER.define("bck_recipe", defRecipe);
        DEFAULTS.put("bck_recipe", defRecipe);

        ENABLE_BCK_LICH_GUARD = BUILDER.define("bck_lich_guard", defLichGuard);
        DEFAULTS.put("bck_lich_guard", defLichGuard);

        ENABLE_BCK_SKILL = BUILDER.define("bck_skill", defSkill);
        DEFAULTS.put("bck_skill", defSkill);

        ENABLE_BCK_SPAWN = BUILDER.define("bck_spawn", defSpawn);
        DEFAULTS.put("bck_spawn", defSpawn);

        ENABLE_BCK_LICH_EYE = BUILDER.define("bck_lich_eye", defLichEye);
        DEFAULTS.put("bck_lich_eye", defLichEye);

        ENABLE_BCK_LICH_CLEAR = BUILDER.define("bck_lich_clear", defLichClear);
        DEFAULTS.put("bck_lich_clear", defLichClear);

        ENABLE_BCK_LICH_WHISPER = BUILDER.define("bck_lich_whisper", defLichWhisper);
        DEFAULTS.put("bck_lich_whisper", defLichWhisper);

        ENABLE_BCK_SERVERDATA = BUILDER.define("bck_serverdata", defServerData);
        DEFAULTS.put("bck_serverdata", defServerData);

        ENABLE_BCK_USERDATA = BUILDER.define("bck_userdata", defUserData);
        DEFAULTS.put("bck_userdata", defUserData);

        ENABLE_BCK_ECONOMY_MANAGER = BUILDER.define("bck_economy_manager", defEconomyManager);
        DEFAULTS.put("bck_economy_manager", defEconomyManager);

        ENABLE_BCK_WARPS = BUILDER.define("bck_warps", defWarps);
        DEFAULTS.put("bck_warps", defWarps);

        ENABLE_BCK_COMBAT = BUILDER.define("bck_combat", defCombat);
        DEFAULTS.put("bck_combat", defCombat);

        ENABLE_BCK_VANISH = BUILDER.define("bck_vanish", defVanish);
        DEFAULTS.put("bck_vanish", defVanish);

        for (AutoModuleScanner.Holder holder : AutoModuleScanner.find()) {
            try {
                // on récupère l'ID directement depuis l'instance
                TetraModule inst = holder.clazz().getDeclaredConstructor().newInstance();
                String key = inst.id().getPath();
                boolean def = holder.def();
                BUILDER.define(key, def);
                DEFAULTS.put(key, def);
            } catch (Exception e) {
                // en dev, log d'erreur si instanciation échoue
                BCKLog.error(BCKCore.TITLES_COLORS.title(ModulesConfig.class), "Impossible d'ajouter dynamique " + holder.clazz(), e);
            }
        }

        BUILDER.pop();
        SERVER_SPEC = BUILDER.build();
    }

    // Gestion dynamique pour modules tiers
    private static final String SECTION = "modules";
    private static CommentedConfig tomlSection;
    private static CommentedFileConfig ROOT_CFG;
    private static final Map<String, Boolean> cache = new HashMap<>();

    /**
     * Indique si le module est activé (serveur).
     */
    public static boolean isEnabled(ResourceLocation id) {
        if (!ENABLE_BCK_CORE.get()) return false;
        String key = id.getPath();
        return switch (key) {
            case "bck_core" -> true;
            case "bck_backpack" -> ENABLE_BCK_BACKPACK.get();
            case "bck_home" -> ENABLE_BCK_HOME.get();
            case "bck_leveling" -> ENABLE_BCK_LEVELING.get();
            case "bck_permissions" -> ENABLE_BCK_PERMISSIONS.get();
            case "bck_recipe" -> ENABLE_BCK_RECIPE.get();
            case "bck_lich_guard" -> ENABLE_BCK_LICH_GUARD.get();
            case "bck_skill" -> ENABLE_BCK_SKILL.get();
            case "bck_spawn" -> ENABLE_BCK_SPAWN.get();
            case "bck_lich_eye" -> ENABLE_BCK_LICH_EYE.get();
            case "bck_lich_clear" -> ENABLE_BCK_LICH_CLEAR.get();
            case "bck_lich_whisper" -> ENABLE_BCK_LICH_WHISPER.get();
            case "bck_serverdata" -> ENABLE_BCK_SERVERDATA.get();
            case "bck_userdata" -> ENABLE_BCK_USERDATA.get();
            case "bck_economy_manager" -> ENABLE_BCK_ECONOMY_MANAGER.get();
            case "bck_warps" -> ENABLE_BCK_WARPS.get();
            case "bck_combat" -> ENABLE_BCK_COMBAT.get();
            case "bck_vanish" -> ENABLE_BCK_VANISH.get();
            default -> readOrCreateDynamic(key, false);
        };
    }

    /**
     * @return l’ensemble des ResourceLocation
     * de **tous** les modules présents dans le toml (clés de la section modules),
     * même si la valeur est false.
     */
    public static Set<ResourceLocation> getConfiguredModules() {
        // Force le chargement de tomlSection
        loadSectionIfNeeded();

        return tomlSection.valueMap().keySet().stream().map(key -> ResourceLocation.fromNamespaceAndPath(TetralibsMod.MODID, key)).collect(Collectors.toSet());
    }

    /**
     * Retourne l'état par défaut du module.
     */
    public static boolean getDefaultState(ResourceLocation id) {
        return DEFAULTS.getOrDefault(id.getPath(), false);
    }

    /**
     * Force la création d'une clé dynamique de module avec une valeur par défaut.
     */
    public static void ensureKey(String key, boolean def) {
        readOrCreateDynamic(key, def);
    }

    public static boolean defaultModulesContains(String id) {
        return DEFAULTS.containsKey(id);
    }

    private static boolean readOrCreateDynamic(String key, boolean def) {
        loadSectionIfNeeded();
        if (!cache.containsKey(key)) {
            cache.put(key, def);
            DEFAULTS.put(key, def);
            tomlSection.setComment(key, "Auto-add by TetraLibs");
            tomlSection.set(key, def);
            BCKLog.info(BCKCore.TITLES_COLORS.title(ModulesConfig.class), "§aAdded §7dynamic module key '§9" + key + "§7' §e= §b" + def);
        }
        return cache.get(key);
    }

    private static CommentedFileConfig ROOT_FILE;   // ↩ à mémoriser !

    private static void loadSectionIfNeeded() {
        if (tomlSection != null) return;
        Path path = configFilePath != null ? configFilePath : FMLPaths.CONFIGDIR.get().resolve("tetralibs-server.toml");
        ROOT_FILE = CommentedFileConfig.builder(path).autosave().sync().preserveInsertionOrder().build();
        ROOT_FILE.load();
        if (!ROOT_FILE.contains(SECTION)) ROOT_FILE.add(SECTION, ROOT_FILE.configFormat().createConfig());
        tomlSection = ROOT_FILE.get(SECTION);
        tomlSection.valueMap().forEach((k, v) -> cache.put(String.valueOf(k), Boolean.parseBoolean(String.valueOf(v))));
    }

    /*──────────────────── setters + persistance ───────────────────*/

    /**
     * Change l’état d’un module côté serveur.
     *
     * @param id    namespace:path du module
     * @param value true = activé, false = désactivé
     */
    public static void setEnabled(ResourceLocation id, boolean value) {
        String key = id.getPath();
        loadSectionIfNeeded();          // tu es sûr que tomlSection ≠ null
        cache.put(key, value);
        tomlSection.set(key, value);    // autosave() suffit si tu as gardé le fichier
    }

    /**
     * Force l’écriture du fichier tetralibs-server.toml.
     */
    public static void save() {
        if (ROOT_FILE != null) ROOT_FILE.save();   // autosave règle 95 % des cas ; ceci force 100 %
    }

    private ModulesConfig() {
    }
}