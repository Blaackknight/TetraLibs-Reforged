package fr.bck.tetralibs.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

import java.util.Collections;
import java.util.Set;

/**
 * Contrat commun à tous les sous-modules de TetraLibs.
 *
 * <ul>
 *   <li><b>id()</b> doit renvoyer l’identifiant exact enregistré dans
 *       {@code TetraRegistries} (ex. {@code new ResourceLocation("tetralibs","bck_core")}).</li>
 *   <li><b>requiredModules()</b> déclare les dépendances entre modules Tetra ; si
 *       l’un manque ou est désactivé, le ModuleManager ignore ce module.</li>
 *   <li><b>requiredMods()</b> déclare les mods Forge **requises** (ex. “jei”, “tconstruct”) ;
 *       si l’un manque, le ModuleManager ignore ce module.</li>
 *   <li><b>excludedMods()</b> déclare les mods Forge **incompatibles** (ex. “create”, “immersiveengineering”) ;
 *       si l’un est présent, le ModuleManager ignore ce module.</li>
 *   <li>Les hooks de cycle de vie (onCommonSetup, onClientSetup…) sont optionnels ;
 *       surchargez-les uniquement si nécessaire.</li>
 * </ul>
 */
public interface TetraModule {
    /**
     * Les types possibles de module.
     */
    public static enum Type {
        CLIENT, SERVER, COMMON;

        /**
         * @return le nom standard (MAJUSCULES), ex. "CLIENT"
         */
        @Override
        public String toString() {
            return name();
        }

        /**
         * @return le nom en minuscules, ex. "client"
         */
        public String toLowerCase() {
            return name().toLowerCase();
        }
    }

    /*────────── Identifiant unique ──────────*/
    ResourceLocation id();

    /*────────── Type de module unique ──────────*/
    Type type();

    /*────────── Hooks cycle de vie (optionnels) ──────────*/
    default void onCommonSetup() {
    }

    /**
     * ───────────── <b>Hook client</b> ─────────────
     * Quand le client charge le mod.
     */
    default void onClientSetup() {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     * Serveur setup
     */
    default void onServerSetup() {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand le serveur démarre.
     */
    default void onServerStarting(ServerStartingEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand le serveur s'éteint.
     */
    default void onServerStopping(ServerStoppingEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un monde de charge.
     */
    default void onWorldLoad(LevelEvent.Load event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un monde se décharge.
     */
    default void onWorldUnload(LevelEvent.Unload event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un monde se sauvegarde.
     */
    default void onWorldSave(LevelEvent.Save event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un joueur se connecte.
     */
    default void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un joueur se déconnecte.
     */
    default void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un bloc est placé.
     */
    default void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un bloc est cassé.
     */
    default void onBlockBreak(BlockEvent.BreakEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un item est drop au sol.
     */
    default void onItemDropped(ItemTossEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un joueur récupère une item au sol.
     */
    default void onItemPickup(PlayerEvent.ItemPickupEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un message est envoyé.
     */
    default void onChat(ServerChatEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand une entité saute.
     */
    default void onEntityJump(LivingEvent.LivingJumpEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un joueur fait clic droit sur un block.
     */
    default void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand une entité est attaqué.
     */
    default void onEntityAttacked(LivingAttackEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand une commande est exécuté.
     */
    default void onCommand(CommandEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Chaque tick par mondes.
     */
    default void onWorldTick(TickEvent.LevelTickEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Chaque tick pour le serveur.
     */
    default void onServerTick(TickEvent.ServerTickEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Chaque tick par joueurs.
     */
    default void onPlayerTick(TickEvent.PlayerTickEvent event) {
    }

    /**
     * ──────── <b>Hook Client</b> ────────
     *
     * @param event Chaque tick par joueurs.
     */
    default void onClientTick(TickEvent.ClientTickEvent event) {
    }

    /**
     * ──────── <b>Hook Serveur</b> ────────
     *
     * @param event Quand un portail est créé.
     */
    default void onPortalCreate(BlockEvent.PortalSpawnEvent event) {
    }

    /**
     * ──────── <b>Hook Client</b> ────────
     *
     * @param event Rendu des items tooltips.
     */
    default void onTooltipRender(ItemTooltipEvent event) {
    }

    /*────────── Dépendances entre modules Tetra ──────────*/
    default Set<ResourceLocation> requiredModules() {
        return Collections.emptySet();
    }

    /*────────── Dépendances “positives” sur mods Forge ──────────*/
    default Set<ResourceLocation> requiredMods() {
        return Collections.emptySet();
    }

    /*────────── Dépendances “négatives” sur mods Forge ──────────*/
    default Set<ResourceLocation> excludedMods() {
        return Collections.emptySet();
    }

    /*────────── Activation par défaut (TOML) ──────────*/
    default boolean enabledByDefault() {
        return true;
    }
}