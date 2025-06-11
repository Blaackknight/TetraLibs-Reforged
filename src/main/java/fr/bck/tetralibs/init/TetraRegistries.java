package fr.bck.tetralibs.init;

import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.AutoModuleScanner;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.module.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Gère tous les registries custom de TetraLibs.
 * Registry principal : {@code tetralibs:module} (type {@link TetraModule}).
 */
public final class TetraRegistries {

    public static final class Utils {
        // ta couleur
        public static final String color = "§3";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static final String MODID = TetralibsMod.MODID;

    /* ─────────────────────────── KEY & DEFERRED REGISTER ─────────────────────────── */

    /**
     * Clé identifiant le registry tetralibs:module
     */
    public static final ResourceKey<Registry<TetraModule>> MODULE_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "modules"));

    /**
     * DeferredRegister : construit l’IForgeRegistry pendant NewRegistryEvent
     */
    private static final DeferredRegister<TetraModule> MODULES = DeferredRegister.create(MODULE_REGISTRY_KEY, MODID);

    /**
     * Accès paresseux au registry une fois construit
     */
    public static final Supplier<IForgeRegistry<TetraModule>> MODULES_SUPPLIER = MODULES.makeRegistry(RegistryBuilder::new);

    /* ─────────────────────────── MÉTHODE UTILITAIRE ─────────────────────────── */

    private static RegistryObject<TetraModule> reg(String id, Supplier<? extends TetraModule> sup) {
        return MODULES.register(id, sup);
    }

    /* ─────────────────────────── MODULES : CORE ─────────────────────────── */

    /**
     * Cœur réseau + services partagés
     */
    public static final RegistryObject<TetraModule> BCK_CORE = reg("bck_core", BCKCoreModule::new);

    /* ─────────────────────────── MODULES : GAMEPLAY ─────────────────────────── */

    /**
     * Système de niveaux / XP
     */
    public static final RegistryObject<TetraModule> BCK_LEVELING = reg("bck_leveling", BCKLevelingModule::new);
    /**
     * Compétences de joueur
     */
    public static final RegistryObject<TetraModule> BCK_SKILL = reg("bck_skill", BCKSkillModule::new);
    /**
     * Régions protégées
     */
    public static final RegistryObject<TetraModule> BCK_REGION = reg("bck_region", BCKRegionModule::new);
    /**
     * Recettes spéciales
     */
    public static final RegistryObject<TetraModule> BCK_RECIPE = reg("bck_recipe", BCKRecipeModule::new);

    public static final RegistryObject<TetraModule> BCK_ECONOMY_MANAGER = reg("bck_economy_manager", BCKEconomyManagerModule::new);

    /* ─────────────────────────── MODULES : QOL / UTILITAIRES ─────────────────────────── */

    /**
     * Sac à dos persistant
     */
    public static final RegistryObject<TetraModule> BCK_BACKPACK = reg("bck_backpack", BCKBackpackModule::new);
    /**
     * Points de maison / téléportation
     */
    public static final RegistryObject<TetraModule> BCK_HOME = reg("bck_home", BCKHomeModule::new);
    /**
     * Système de permissions
     */
    public static final RegistryObject<TetraModule> BCK_PERMISSIONS = reg("bck_permissions", BCKPermissionsModule::new);
    /**
     * Gestion du point de spawn
     */
    public static final RegistryObject<TetraModule> BCK_SPAWN = reg("bck_spawn", BCKSpawnModule::new);
    /**
     * Tooltips d’items personnalisés
     */
    public static final RegistryObject<TetraModule> BCK_TOOLTIP = reg("bck_tooltip", BCKTooltipModule::new);

    /* ─────────────────────────── MODULES : DATA / SERVEUR ─────────────────────────── */

    /**
     * Logger d’évènements joueurs : ouvre-coffre, clic bloc, etc. (LichEye)
     */
    public static final RegistryObject<TetraModule> BCK_LICH_EYE = reg("bck_lich_eye", BCKLichEyeModule::new);
    /**
     * Clear les items
     */
    public static final RegistryObject<TetraModule> BCK_LICH_CLEAR = reg("bck_lich_clear", BCKLichClearModule::new);
    /**
     * Logs & debug serveur
     */
    public static final RegistryObject<TetraModule> BCK_LICH_WHISPER = reg("bck_lich_whisper", BCKLichWhisperModule::new);
    /**
     * Données serveur globales
     */
    public static final RegistryObject<TetraModule> BCK_SERVERDATA = reg("bck_serverdata", BCKServerdataModule::new);
    /**
     * Données utilisateur (NBT/JSON)
     */
    public static final RegistryObject<TetraModule> BCK_USERDATA = reg("bck_userdata", BCKUserdataModule::new);

    /**
     * Données utilisateur (NBT/JSON)
     */
    public static final RegistryObject<TetraModule> BCK_WARPS = reg("bck_warps", BCKWarpsModule::new);

    /**
     * Données utilisateur (NBT/JSON)
     */
    public static final RegistryObject<TetraModule> BCK_COMBAT = reg("bck_combat", BCKCombatModule::new);

    /* ─────────────────────────── ENREGISTREMENT SUR LE BUS ─────────────────────────── */

    /**
     * À appeler dans le constructeur de la classe @Mod
     */
    public static void register() {
        MODULES.register(FMLJavaModLoadingContext.get().getModEventBus());

        // modules tiers détectés automatiquement
        AutoModuleScanner.find().forEach(TetraRegistries::registerAuto);
    }

    public static void registerAuto(AutoModuleScanner.Holder holder) {
        try {
            TetraModule instance = holder.clazz().getDeclaredConstructor().newInstance();

            // on crée la clé dynamique dans la config TOML si elle n’existe pas
            ModulesConfig.ensureKey(instance.id().getPath(), holder.def());

            // on enregistre le module
            MODULES.register(instance.id().getPath(), () -> instance);

            BCKLog.info(BCKCore.TITLES_COLORS.title(TetraRegistries.class) + "§6/" + BCKCore.TITLES_COLORS.title(Module.class), "§7Auto-détecté §e" + instance.id() + " §7(def=§8" + holder.def() + "§7) -> §b" + instance.type());
        } catch (Exception e) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(TetraRegistries.class) + "§6/" + BCKCore.TITLES_COLORS.title(AutoModuleScanner.class), "§cÉchec d’instanciation : §l" + holder.clazz().getName(), e);
        }
    }

    private TetraRegistries() { /* Utility class : pas d’instanciation */ }
}