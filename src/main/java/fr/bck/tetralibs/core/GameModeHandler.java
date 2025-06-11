package fr.bck.tetralibs.core;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Classe GameModeHandler
 * <p>
 * Fournit des méthodes utilitaires pour vérifier le mode de jeu (solo ou multijoueur)
 * et exécuter du code spécifiquement côté client ou serveur.
 */
public class GameModeHandler {
    public static final class Utils {
        // ta couleur
        public static final String color = "§7";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    /**
     * Vérifie si le joueur est en mode solo (jeu en solo).
     *
     * @return true si le joueur est en mode solo, false sinon.
     */
    public static boolean isSinglePlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.hasSingleplayerServer();
    }

    /**
     * Vérifie si le joueur est sur un serveur multijoueur.
     *
     * @return true si le joueur est sur un serveur multijoueur, false sinon.
     */
    public static boolean isServer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && !isSinglePlayer();
    }

    /**
     * Vérifie si le jeu tourne sur un serveur dédié.
     *
     * @return true si c'est un serveur dédié, false sinon.
     */
    public static boolean isDedicatedServer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null && server.isDedicatedServer();
    }

    /**
     * Exécute du code côté client uniquement.
     *
     * @param action L'action à exécuter côté client.
     */
    public static void executeClientOnlyCode(Runnable action) {
        if (action != null) {
            action.run();
        }
    }

    /**
     * Exécute du code côté serveur uniquement, avec accès au serveur.
     *
     * @param action L'action à exécuter côté serveur, avec un accès au serveur.
     *               Si null, rien ne se passe.
     */
    public static void executeServerOnlyCode(ServerAction action) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && action != null) {
            action.run(server);
        }
    }

    /**
     * Exécute du code côté serveur uniquement, sans avoir besoin du serveur.
     *
     * @param action Une action à exécuter (Runnable). Si null, rien ne se passe.
     */
    public static void executeServerOnlyCode(Runnable action) {
        if (ServerLifecycleHooks.getCurrentServer() != null && action != null) {
            action.run();
        }
    }

    /**
     * Interface fonctionnelle pour une action serveur.
     */
    @FunctionalInterface
    public interface ServerAction {
        void run(MinecraftServer server);
    }
}