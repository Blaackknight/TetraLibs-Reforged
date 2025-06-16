package fr.bck.tetralibs.core;

import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.init.TetraRegistries;
import fr.bck.tetralibs.module.ModuleIds;
import fr.bck.tetralibs.module.TetraModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;



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
 * Gère le cycle de vie des modules (common / client / serveur).
 * Cette classe est abonnée UNIQUEMENT au bus MOD.
 */
@Mod.EventBusSubscriber(modid = TetralibsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModuleManager {
    public static final class Utils {
        // ta couleur
        public static final String color = "§9";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    /*───────────────────────── Hooks bus MOD ─────────────────────────*/

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent evt) {
        processModules(Hook.COMMON);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent evt) {
        processModules(Hook.CLIENT);
    }

    /**
     * @SubscribeEvent public static void onDedicatedServerSetup(FMLDedicatedServerSetupEvent evt) {
     * BCKLog.info(BCKCore.TITLES_COLORS.title(ModuleManager.class), "Server Setup");
     * processModules(Hook.SERVER);
     * }
     */

    /*───────────────────────── Enum & logique ───────────────────────*/

    public enum Hook {COMMON, CLIENT, SERVER}

    /**
     * Tous les modules Tetra enregistrés ici.
     */
    public static void processModules(Hook hook) {
        IForgeRegistry<TetraModule> registry = TetraRegistries.MODULES_SUPPLIER.get();

        // 1) On ne fait rien si le module "bck_core" de base est désactivé
        if (hook == Hook.SERVER && !ModulesConfig.isEnabled(ModuleIds.BCK_CORE)) {
            BCKLog.warn(BCKCore.TITLES_COLORS.title(ModuleManager.class), BCKCore.TITLES_COLORS.title(BCKCore.class) + " §n§4DISABLED§r §e→ §7tous les modules ignorés");
            return;
        }

        // 2) Pour chacun des modules enregistrés…
        moduleLoop:
        for (TetraModule module : registry) {
            ResourceLocation mid = module.id();
            TetraModule.Type type = module.type();

            // 2.a) Si l’utilisateur a désactivé ce module dans le TOML, on skip.
            if (hook == Hook.SERVER && !ModulesConfig.isEnabled(mid)) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(ModuleManager.class), "§eSKIPPED §o§7(module off)§r §7§l§n" + mid);
                continue;
            }

            // 2.b) Vérification des mods obligatoires (requiredMods)
            for (ResourceLocation reqMod : module.requiredMods()) {
                String modid = reqMod.getNamespace();
                if (!ModList.get().isLoaded(modid)) {
                    BCKLog.warn(BCKCore.TITLES_COLORS.title(ModuleManager.class) + "§6/§fModDependency", "§5" + mid + " §4DISABLED §e→ §7mod requis absent : §l§b" + modid);
                    continue moduleLoop;
                }
            }

            // 2.c) Vérification des mods interdits (excludedMods) : NOUVEAU
            for (ResourceLocation exMod : module.excludedMods()) {
                String modid = exMod.getNamespace();
                if (ModList.get().isLoaded(modid)) {
                    // On loggue en jaune pour indiquer qu’on skip à cause d’une incompatibilité
                    BCKLog.warn(BCKCore.TITLES_COLORS.title(ModuleManager.class) + "§6/§fModExclusion", "§5" + mid + " §4DISABLED §e→ §7mod incompatible détecté : §l§b" + modid);
                    continue moduleLoop;
                }
            }

            // 2.d) Vérification des dépendances entre modules Tetra (requiredModules)
            if (hook == Hook.SERVER && !depsSatisfied(module, registry, new HashSet<>())) {
                // depsSatisfied loggue déjà un message si un module Tetra est manquant ou si cycle
                continue;
            }

            // 3) S’il reste, on peut exécuter le hook correspondant pour ce module
            switch (hook) {
                case COMMON -> module.onCommonSetup();
                case CLIENT -> module.onClientSetup();
                case SERVER -> module.onServerSetup();
            }

            // 4) Si on est sur COMMON, on affiche “ENABLED” en vert
            if (hook == Hook.SERVER) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(ModuleManager.class), "§aENABLED §d§l" + mid + "§r §7-> §etype §7-> §b" + type);
            }
        }
    }

    /**
     * Dispatch générique pour n'importe quel Event Forge.
     *
     * @param <E>     le type d'événement (ex. LevelEvent.Load, PlayerEvent.PlayerLoggedInEvent...)
     * @param event   l'événement reçu
     * @param handler un BiConsumer qui appelle le hook correspondant sur le module
     */
    public static <E extends Event> void processGenericEvent(E event, BiConsumer<TetraModule, E> handler) {
        IForgeRegistry<TetraModule> registry = TetraRegistries.MODULES_SUPPLIER.get();
        if (!ModulesConfig.isEnabled(ModuleIds.BCK_CORE)) return;

        moduleLoop:
        for (TetraModule module : registry) {
            ResourceLocation mid = module.id();

            // 1) Config TOML
            if (!ModulesConfig.isEnabled(mid)) continue;

            // 2) requiredMods
            for (ResourceLocation req : module.requiredMods()) {
                if (!ModList.get().isLoaded(req.getNamespace())) {
                    BCKLog.warn(BCKCore.TITLES_COLORS.title(ModuleManager.class) + "§6/§fModDependency", "§d" + mid + " §eSKIPPED §e→ §7mod requis absent : §b§l" + req);
                    continue moduleLoop;
                }
            }

            // 3) excludedMods
            for (ResourceLocation ex : module.excludedMods()) {
                if (ModList.get().isLoaded(ex.getNamespace())) {
                    BCKLog.warn(BCKCore.TITLES_COLORS.title(ModuleManager.class) + "§6/§fModExclusion", "§d" + mid + " §eSKIPPED §e→ §7mod incompatible : §b§l" + ex);
                    continue moduleLoop;
                }
            }

            // 4) requiredModules (dépendances entre modules Tetra)
            if (!depsSatisfied(module, registry, new HashSet<>())) continue;

            // 5) Dispatch de l'événement au module
            handler.accept(module, event);
        }
    }

    /**
     * Vérifie récursivement les dépendances entre modules Tetra.
     */
    private static boolean depsSatisfied(TetraModule mod, IForgeRegistry<TetraModule> registry, Set<ResourceLocation> stack) {
        for (ResourceLocation depId : mod.requiredModules()) {
            if (stack.contains(depId)) {
                BCKLog.fatal(BCKCore.TITLES_COLORS.title(ModuleManager.class) + "§6/§fDependency", "Cycle détecté : §2" + stack);
                return false;
            }
            if (!ModulesConfig.isEnabled(depId)) {
                BCKLog.warn(BCKCore.TITLES_COLORS.title(ModuleManager.class) + "§6/§fDependency", "§f§l" + mod.id() + " §cdésactivé §e→ §7dépendance §5Tetra §cdésactivée §7: §8" + depId);
                return false;
            }
            TetraModule dep = registry.getValue(depId);
            if (dep == null) {
                BCKLog.error(BCKCore.TITLES_COLORS.title(ModuleManager.class) + "§6/§fDependency", "§f§l" + mod.id() + " §cdésactivé §e→ §7module §5Tetra §c§nmanquant§r §7: §8" + depId);
                return false;
            }
            stack.add(depId);
            if (!depsSatisfied(dep, registry, stack)) return false;
            stack.remove(depId);
        }
        return true;
    }

    public static IForgeRegistry<TetraModule> MODULES = TetraRegistries.MODULES_SUPPLIER.get();


    /**public static List<ModuleStatus> listAllModuleStatuses() {
     IForgeRegistry<TetraModule> registry = TetraRegistries.MODULES_SUPPLIER.get();
     List<ModuleStatus> statuses = new ArrayList<>();

     for (TetraModule module : registry) {
     ResourceLocation mid = module.id();
     // état courant dans la config (true = activé)
     boolean enabled = ModulesConfig.isEnabled(mid);
     // valeur par défaut (tel que déclarée dans ton ModulesConfig au démarrage)
     boolean defaultEnabled = ModulesConfig.getDefaultState(mid);
     statuses.add(new ModuleStatus(module, enabled, defaultEnabled));
     }

     return statuses;
     }*/

    /**
     * @return la liste de tous les modules Tetra connus,
     * avec pour chacun son état actuel et son état par défaut.
     */
    public static List<ModuleStatus> listAllModuleStatuses() {
        IForgeRegistry<TetraModule> registry = TetraRegistries.MODULES_SUPPLIER.get();
        var stats = new java.util.ArrayList<ModuleStatus>();
        for (TetraModule m : registry) {
            var id = m.id();
            stats.add(new ModuleStatus(m, ModulesConfig.isEnabled(id), ModulesConfig.getDefaultState(id)));
        }
        return stats;
    }


    /**
     * Check si un module existe dans la registry
     */
    public static boolean modulesContains(ResourceLocation id) {
        return TetraRegistries.MODULES_SUPPLIER.get().containsKey(id);
    }

    public record ModuleStatus(TetraModule module, boolean enabled, boolean defaultEnabled) {
    }

    /**
     * Tente de récupérer le TetraModule enregistré sous cet ID.
     *
     * @param id namespace:path du module
     * @return Optional contenant le module s’il existe, vide sinon
     */
    public static Optional<TetraModule> getModule(ResourceLocation id) {
        IForgeRegistry<TetraModule> registry = TetraRegistries.MODULES_SUPPLIER.get();
        return Optional.ofNullable(registry.getValue(id));
    }

    /**
     * Même chose mais lève une exception si le module n’existe pas.
     */
    public static TetraModule requireModule(ResourceLocation id) {
        return getModule(id).orElseThrow(() -> new IllegalArgumentException("Module non trouvé pour l’ID " + id));
    }

    private ModuleManager() {
    } // classe utilitaire, pas d'instance
}