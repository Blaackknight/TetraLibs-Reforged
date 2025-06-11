package fr.bck.tetralibs.permissions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.data.BCKUserdata;
import fr.bck.tetralibs.module.ModuleIds;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.stream.StreamSupport;

/**
 * Classe gérant les permissions des joueurs et du serveur.
 * Elle permet d'ajouter, de supprimer et de vérifier des permissions pour les joueurs et le serveur.
 */
public class BCKPermissions {
    public static final class Utils {
        // ta couleur
        public static final String color = "§2";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public final static String supreme_permission = "bck";  // Permission suprême qui donne tous les droits
    private static final Map<String, String> defaultPermissionsMap = new LinkedHashMap<>();
    private static final String CONFIG_KEY = "bck_permissions";

    private static boolean isEnabled() {
        return ModulesConfig.isEnabled(ModuleIds.BCK_PERMISSIONS);
    }

    static {
        addDefaultPermission("block.place", "bck");
        addDefaultPermission("block.break", "bck");
        addDefaultPermission("item.drop", "bck");
        addDefaultPermission("item.pickup", "bck");
        addDefaultPermission("player.send_message", "bck");
        addDefaultPermission("player.move", "bck");
        addDefaultPermission("player.jump", "bck");
        addDefaultPermission("player.interact", "bck");
        addDefaultPermission("player.attack", "bck");
        addDefaultPermission("server.command.help", "bck");
        addDefaultPermission("server.command.me", "bck");
        addDefaultPermission("server.command.msg", "bck");
        addDefaultPermission("server.command.tell", "bck");
        addDefaultPermission("server.command.w", "bck");
        addDefaultPermission("server.command.teammsg", "bck");
        addDefaultPermission("server.command.tm", "bck");
        addDefaultPermission("server.command.trigger", "bck");
    }

    // Permissions par défaut attribuées aux joueurs
    public static String[] getDefaultPermissions() {
        if (!isEnabled()) {
            return new String[0];
        }
        if (defaultPermissionsMap.isEmpty()) {
            // Si la map est vide, charger les permissions depuis Serverdata
            Object storedData = BCKServerdata.data("server.players.default_permissions");
            if (storedData instanceof Map<?, ?> map) {
                map.forEach((key, value) -> defaultPermissionsMap.put((String) key, (String) value));
            }
        }

        // Retourner uniquement les clés des permissions
        return defaultPermissionsMap.keySet().toArray(new String[0]);
    }

    public static void addDefaultPermission(String permission, String id, boolean callback) {
        if (!isEnabled()) return;
        if (!defaultPermissionsMap.containsKey(permission)) {
            defaultPermissionsMap.put(permission, id);
            //Serverdata.data("server.players.default_permissions", "set", defaultPermissionsMap);
            if (callback)
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class) + "§6/Default", "§7Permission §8" + permission + " §7added with id §8" + id);
        } else {
            if (callback)
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class) + "§6/Default", "§7Permission §8" + permission + " §9§malready exists§r§7.");
        }
    }

    public static void addDefaultPermission(String permission, String id) {
        addDefaultPermission(permission, id, false);
    }

    /**
     * Ajoute les permissions par défaut à un joueur si elles ne sont pas déjà attribuées.
     *
     * @param entity   L'entité (joueur) à qui les permissions seront attribuées.
     * @param callback Si 1, les permissions seront ajoutées; sinon elles ne seront pas ajoutées.
     */
    public static void defaultPermissions(Entity entity, int callback) {
        if (!isEnabled()) return;
        JsonArray dperms = ((DataWrapper) BCKServerdata.data("server.players.default_permissions")).getJsonArray();
        List<String> defaultPermsList = StreamSupport.stream(dperms.spliterator(), false).map(JsonElement::getAsString).toList();
        for (String perm : defaultPermsList) {
            if (!hasPermission(entity, perm)) {
                if (callback == 1) {
                    addPermission(entity, perm, true);
                } else if (callback == 2) {
                    addPermission(entity, perm, false);
                } else {
                    addPermission(entity, perm, false);
                }
            }
        }
        if (dperms.isEmpty())
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§7No default permission found..");
        else
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§7DefaultPermissions §n§8(" + defaultPermsList.size() + ")§r §7have been added to §8" + entity.getName().getString());
    }

    /**
     * Variante de la méthode defaultPermissions sans argument callback.
     *
     * @param entity L'entité (joueur) à qui les permissions seront attribuées.
     */
    public static void defaultPermissions(Entity entity) {
        defaultPermissions(entity, 0);
    }

    /**
     * Formate les permissions du serveur selon un modèle spécifié.
     *
     * @param pattern     Modèle dans lequel les permissions doivent être insérées.
     * @param replaceable Élément à remplacer dans le modèle par les permissions.
     * @return Une chaîne de caractères contenant les permissions formatées.
     */
    public static String getFormattedServerPermissions(String pattern, String replaceable) {
        if (!isEnabled()) return "";
        StringBuilder txt = new StringBuilder();
        String[] permissions = getServerPermissions();
        for (String perm : permissions) {
            String formattedPerm = pattern.replace(replaceable, perm).replace("%nl%", "\\n");
            txt.append(formattedPerm);
        }
        return txt.substring(0, txt.length() - 2);
    }

    /**
     * Variante de la méthode getFormattedServerPermissions avec un modèle par défaut.
     *
     * @param pattern Modèle dans lequel les permissions doivent être insérées.
     * @return Une chaîne de caractères contenant les permissions formatées.
     */
    public static String getFormattedServerPermissions(String pattern) {
        return getFormattedServerPermissions(pattern, "%perm%");
    }

    /**
     * Formate les permissions d'un joueur selon un modèle spécifié.
     *
     * @param entity      L'entité (joueur) dont les permissions sont récupérées.
     * @param pattern     Modèle dans lequel les permissions doivent être insérées.
     * @param replaceable Élément à remplacer dans le modèle par les permissions.
     * @return Une chaîne de caractères contenant les permissions formatées.
     */
    public static String getFormattedPlayerPermissions(Entity entity, String pattern, String replaceable) {
        if (!isEnabled()) return "";
        StringBuilder txt = new StringBuilder();
        String[] permissions = getPermissionsArray(entity);
        for (String perm : permissions) {
            String formattedPerm = pattern.replace(replaceable, perm).replace("%nl%", "\\n");
            txt.append(formattedPerm);
        }
        return txt.substring(0, txt.length() - 2);
    }

    /**
     * Variante de la méthode getFormattedPlayerPermissions avec un modèle par défaut.
     *
     * @param entity  L'entité (joueur) dont les permissions sont récupérées.
     * @param pattern Modèle dans lequel les permissions doivent être insérées.
     * @return Une chaîne de caractères contenant les permissions formatées.
     */
    public static String getFormattedPlayerPermissions(Entity entity, String pattern) {
        return getFormattedPlayerPermissions(entity, pattern, "%perm%");
    }

    /**
     * Ajoute une permission au serveur si elle n'existe pas déjà.
     *
     * @param permission La permission à ajouter.
     * @param callback   Si true, un message de confirmation sera affiché.
     * @return True si la permission a été ajoutée, sinon false.
     */
    public static boolean addServerPermission(String permission, boolean callback) {
        if (!isEnabled()) return false;
        // 1) récupère le tableau actuel
        String[] current = getServerPermissions();
        List<String> list = new ArrayList<>(Arrays.asList(current));
        if (list.contains(permission)) {
            if (callback)
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class) + "§6/Server", "§8" + permission + " §9§malready exists§r §7in §oserver permissions");
            return false;
        }
        // 2) ajoute la nouvelle permission
        list.add(permission);
        // 3) persiste **une seule fois** le nouveau tableau
        BCKServerdata.data("server.permissions", "set", list.toArray(new String[0]));
        if (callback) {
            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class) + "§6/Server", "§8" + permission + " §7has been §aadded §7to §oserver permissions");
            //BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class) + "§6/Server", "current perms: " + Arrays.toString(list.toArray(new String[0])));
        }
        return true;
    }

    /**
     * Variante de la méthode addServerPermission sans argument callback.
     *
     * @param permission La permission à ajouter.
     * @return True si la permission a été ajoutée, sinon false.
     */
    public boolean addServerPermission(String permission) {
        return addServerPermission(permission, false);
    }

    /**
     * Supprime une permission du serveur si elle existe.
     *
     * @param permission La permission à supprimer.
     * @param callback   Si true, un message de confirmation sera affiché.
     * @return True si la permission a été supprimée, sinon false.
     */
    public static boolean removeServerPermission(String permission, boolean callback) {
        if (!isEnabled()) return false;
        String[] currentPermissions = getServerPermissions();
        if (Arrays.asList(currentPermissions).contains(permission)) {
            String[] newPermissions = Arrays.stream(currentPermissions).filter(perm -> !perm.equals(permission)).toArray(String[]::new);
            BCKServerdata.data("server.permissions", "set", newPermissions);
            if (callback) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§8" + permission + " §7has been §cremoved §7from §oserver permissions");
            }
            return true;
        } else {
            if (callback) {
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§8" + permission + " §7was not §dfound §7in §oserver permissions");
            }
        }
        return false;
    }

    /**
     * Variante de la méthode removeServerPermission sans argument callback.
     *
     * @param permission La permission à supprimer.
     * @return True si la permission a été supprimée, sinon false.
     */
    public static boolean removeServerPermission(String permission) {
        return removeServerPermission(permission, false);
    }

    /**
     * Récupère les permissions du serveur sous forme de tableau.
     *
     * @return Un tableau de permissions du serveur.
     */
    public static String[] getServerPermissions() {
        if (!isEnabled()) return new String[0];
        String[] server_permissions = ((DataWrapper) BCKServerdata.data("server.permissions")).getStringArray();
        //BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class) + "§6/Get", "perm: " + Arrays.toString(server_permissions));
        return server_permissions == null ? new String[0] : server_permissions;
    }

    /**
     * Ajoute une permission à un joueur.
     *
     * @param entity     L'entité (joueur) à qui la permission sera ajoutée.
     * @param permission La permission à ajouter.
     * @param callback   Si true, un message de confirmation sera affiché.
     * @return True si la permission a été ajoutée, sinon false.
     */
    public static boolean addPermission(Entity entity, String permission, boolean callback) {
        if (!isEnabled()) return false;
        String[] currentPermissions = getPermissionsArray(entity);
        if (!Arrays.asList(currentPermissions).contains(permission)) {
            String[] newPermissions = Arrays.copyOf(currentPermissions, currentPermissions.length + 1);
            newPermissions[newPermissions.length - 1] = permission;
            setPlayerPermissions(entity, newPermissions);
            if (callback) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§8" + permission + " §7has been §aadded §7to §8" + entity.getName().getString());
            }
            return true;
        }
        return false;
    }

    /**
     * Variante de la méthode addPermission sans argument callback.
     *
     * @param entity     L'entité (joueur) à qui la permission sera ajoutée.
     * @param permission La permission à ajouter.
     * @return True si la permission a été ajoutée, sinon false.
     */
    public static boolean addPermission(Entity entity, String permission) {
        return addPermission(entity, permission, false);
    }

    /**
     * Supprime une permission d'un joueur.
     *
     * @param entity     L'entité (joueur) de qui la permission sera supprimée.
     * @param permission La permission à supprimer.
     * @param callback   Si true, un message de confirmation sera affiché.
     * @return True si la permission a été supprimée, sinon false.
     */
    public static boolean removePermission(Entity entity, String permission, boolean callback) {
        if (!isEnabled()) return false;
        String[] currentPermissions = getPermissionsArray(entity);
        if (Arrays.asList(currentPermissions).contains(permission)) {
            String[] newPermissions = Arrays.stream(currentPermissions).filter(perm -> !perm.equals(permission)).toArray(String[]::new);
            setPlayerPermissions(entity, newPermissions);
            if (callback) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§8" + permission + " §7has been §cremoved §7from §8" + entity.getName().getString());
            }
            return true;
        }
        return false;
    }

    /**
     * Récupère les permissions d'un joueur sous forme de tableau.
     *
     * @param entity L'entité (joueur) dont les permissions seront récupérées.
     * @return Un tableau de permissions du joueur.
     */
    public static String[] getPermissionsArray(Entity entity) {
        if (!isEnabled()) return new String[0];
        String[] permissions = ((DataWrapper) BCKUserdata.data("permissions", entity.level(), entity)).getStringArray();
        return permissions == null ? new String[0] : permissions;
    }

    /**
     * Vérifie si un joueur possède une permission.
     *
     * @param entity     L'entité (joueur) dont les permissions seront vérifiées.
     * @param permission La permission à vérifier.
     * @return True si le joueur a la permission, sinon false.
     */
    public static boolean hasPermission(Entity entity, String permission) {
        if (!isEnabled()) return false;
        String[] permissions = getPermissionsArray(entity);
        //BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "perm: §d" + permission + " §e-> §b" + Arrays.toString(permissions));
        if (Arrays.asList(permissions).contains(supreme_permission)) {
            return true;
        } else if (Arrays.asList(permissions).contains("op") && !permission.equals(supreme_permission)) {
            return true;
        }
        return Arrays.asList(permissions).contains(permission);
    }

    /**
     * Récupère les permissions d'une entité.
     *
     * @param entity L'entité dont les permissions seront récupérées.
     * @return Un tableau de permissions de l'entité.
     */
    public static String[] getEntityPermissions(Entity entity) {
        return getPermissionsArray(entity);
    }

    /**
     * Définit les permissions d'un joueur dans les données utilisateurs.
     *
     * @param entity      L'entité (joueur) dont les permissions seront définies.
     * @param permissions Le tableau de permissions à attribuer.
     */
    public static void setPlayerPermissions(Entity entity, String[] permissions) {
        if (!isEnabled()) return;
        BCKUserdata.data("permissions", "set", permissions, entity.level(), entity);
    }
}