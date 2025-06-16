package fr.bck.tetralibs.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.bck.tetralibs.core.*;
import fr.bck.tetralibs.permissions.BCKPermissions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.*;



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
 * Classe responsable de la gestion des données du serveur (JSON) avec debounce
 * pour regrouper et différer les écritures sur disque, évitant les pertes
 * lors d'opérations rapides répétées.
 */
public class BCKServerdata {
    public static final class Utils {
        // ta couleur
        public static final String color = "§5";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    private static final String FILE_NAME = "server_data.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Debounce executor et structures de cache
    private static final ScheduledExecutorService SAVE_SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> PENDING_SAVES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, JsonObject> PENDING_JSON = new ConcurrentHashMap<>();
    private static final long SAVE_DEBOUNCE_MS = 500;

    /**
     * Planifie la persistance différée du JsonObject vers le fichier.
     */
    private static void scheduleFlush(String filePath) {
        ScheduledFuture<?> prev = PENDING_SAVES.get(filePath);
        if (prev != null && !prev.isDone()) prev.cancel(false);
        ScheduledFuture<?> future = SAVE_SCHEDULER.schedule(() -> {
            JsonObject pending = PENDING_JSON.remove(filePath);
            if (pending != null) {
                try {
                    BCKUtils.FileUtil.saveJsonObjectToFile(pending, filePath);
                } catch (IOException e) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Flush", "Failed to flush §8" + filePath + "§r: ", e);
                }
            }
            PENDING_SAVES.remove(filePath);
        }, SAVE_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        PENDING_SAVES.put(filePath, future);
    }

    /**
     * Retourne le JsonObject de travail (cache ou chargé depuis le fichier).
     */
    private static JsonObject getWorkingJson(String filePath) throws IOException {
        JsonObject obj = PENDING_JSON.get(filePath);
        if (obj == null) {
            obj = BCKUtils.FileUtil.loadJson(filePath);
            PENDING_JSON.put(filePath, obj);
        }
        return obj;
    }

    /**
     * Définit une valeur dans l'objet Json en mémoire sur un chemin imbriqué.
     * <p>
     * Supporte les types String, Number, Boolean, JsonObject, JsonArray,
     * ainsi que String[], Number[], Boolean[].
     */
    private static void setValue(JsonObject json, String keyPath, Object value) {
        String[] parts = keyPath.split("\\.");
        JsonObject cur = json;
        for (int i = 0; i < parts.length - 1; i++) {
            String k = parts[i];
            if (!cur.has(k) || !cur.get(k).isJsonObject()) {
                cur.add(k, new JsonObject());
            }
            cur = cur.getAsJsonObject(k);
        }
        String last = parts[parts.length - 1];
        try {
            if (value instanceof String) {
                cur.addProperty(last, (String) value);
            } else if (value instanceof Number) {
                cur.addProperty(last, (Number) value);
            } else if (value instanceof Boolean) {
                cur.addProperty(last, (Boolean) value);
            } else if (value instanceof JsonObject) {
                cur.add(last, (JsonObject) value);
            } else if (value instanceof JsonArray) {
                cur.add(last, (JsonArray) value);
            } else if (value instanceof String[]) {
                JsonArray arr = new JsonArray();
                for (String s : (String[]) value) arr.add(s);
                cur.add(last, arr);
            } else if (value instanceof Number[]) {
                JsonArray arr = new JsonArray();
                for (Number n : (Number[]) value) arr.add(n);
                cur.add(last, arr);
            } else if (value instanceof Boolean[]) {
                JsonArray arr = new JsonArray();
                for (Boolean b : (Boolean[]) value) arr.add(b);
                cur.add(last, arr);
            } else {
                throw new IllegalArgumentException("Unsupported type: " + (value != null ? value.getClass().getName() : "null"));
            }
        } catch (Throwable t) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/SetValue", "Erreur en définissant '" + keyPath + "' = " + value, t);
            throw t;
        }
    }

    /**
     * Supprime une clé imbriquée de l'objet Json en mémoire.
     */
    private static void deleteKey(JsonObject json, String keyPath) {
        String[] parts = keyPath.split("\\.");
        JsonObject cur = json;
        for (int i = 0; i < parts.length - 1; i++) {
            String k = parts[i];
            if (!cur.has(k) || !cur.get(k).isJsonObject()) return;
            cur = cur.getAsJsonObject(k);
        }
        cur.remove(parts[parts.length - 1]);
    }

    // -------- Méthodes publiques --------

    /**
     * Extraction ou mise à jour de données globales serveur.
     * Mode "extract" : lecture synchrone.
     * Mode "set"     : modifie en mémoire et planifie sauvegarde.
     * Mode "delete"  : supprime en mémoire et planifie.
     */
    public static Object data(String key, String mode, Object value) {
        String filePath = getFile().getPath();
        if ("extract".equals(mode)) {
            try {
                JsonObject o = getWorkingJson(filePath);
                Object res = BCKUtils.FileUtil.getFromJson(o, key);
                return new DataWrapper(res);
            } catch (IOException e) {
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Extract", "Failed to load §8" + filePath + "§r: ", e);
                return new DataWrapper(null);
            }
        } else if ("set".equals(mode)) {
            try {
                JsonObject working = getWorkingJson(filePath);
                // Sérialisation spéciale si besoin
                if (value instanceof ItemStack) value = BCKUtils.ItemUtil.toJson((ItemStack) value);
                if (value instanceof BlockState) value = BCKUtils.BlockUtil.toJson((BlockState) value);
                setValue(working, key, value);
                scheduleFlush(filePath);
            } catch (IOException e) {
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Set", "Failed to schedule set §8" + key + "§r: ", e);
            }
            return key;
        } else if ("delete".equals(mode)) {
            try {
                JsonObject working = getWorkingJson(filePath);
                deleteKey(working, key);
                scheduleFlush(filePath);
            } catch (IOException e) {
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Delete", "Failed to schedule delete §8" + key + "§r: ", e);
            }
        }
        return null;
    }

    /**
     * Lecture simplifiée (mode extract).
     */
    public static Object data(String key) {
        return data(key, "extract", null);
    }

    // ------- Fonctions existantes (inchangées) -------

    public static String[] getKeys() {
        try {
            Set<String> keys = JsonKeyExtractor.extractFullyQualifiedKeys(FILE_NAME, getPath());
            return keys.toArray(new String[0]);
        } catch (IOException e) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Keys", "Error extracting keys: ", e);
        }
        return new String[0];
    }

    public static void load() {
        GameModeHandler.executeServerOnlyCode(() -> {
            init();
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Load", "(" + BCKUtils.ServerUtil.getWorldFolderName() + ") Serverdata loaded at " + getFile().getPath());
        });
    }

    public static void save() {
        // flush pending immediately
        String path = getFile().getPath();
        ScheduledFuture<?> pending = PENDING_SAVES.get(path);
        if (pending != null && !pending.isDone()) pending.cancel(false);
        JsonObject pendingJson = PENDING_JSON.remove(path);
        if (pendingJson != null) {
            try {
                BCKUtils.FileUtil.saveJsonObjectToFile(pendingJson, path);
            } catch (IOException e) {
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Save", "Error while saving", e);
            }
        }
        BCKLog.info(BCKCore.TITLES_COLORS.title(BCKServerdata.class), "Server data saved.");
    }

    public static void init() {
        GameModeHandler.executeServerOnlyCode(() -> {
            File f = getFile();
            JsonObject object = new JsonObject();
            if (!f.exists()) {
                try {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    try (FileWriter writer = new FileWriter(f)) {
                        writer.write(GSON.toJson(object));
                    }
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKServerdata.class) + "§6/Init", "(" + BCKUtils.ServerUtil.getWorldFolderName() + ") Serverdata file created at " + f.getPath());
                } catch (IOException e) {
                    BCKLog.fatal(BCKCore.TITLES_COLORS.title(BCKServerdata.class), "Error while creating file", e);
                }
            }
            // Initialisation des valeurs par défaut
            if (((DataWrapper) data("server.players.default_permissions")).isNull())
                data("server.players.default_permissions", "set", BCKPermissions.getDefaultPermissions());
            if (((DataWrapper) data("server.allow_fake_players")).isNull())
                data("server.allow_fake_players", "set", true);
            if (((DataWrapper) data("server.vanish.souls")).isNull()) data("server.vanish.souls", "set", new String[0]);
            if (((DataWrapper) data("homes.max_count")).isNull()) data("homes.max_count", "set", 1);
            if (((DataWrapper) data("homes.teleport_cooldown")).isNull()) data("homes.teleport_cooldown", "set", 5);
            if (((DataWrapper) data("spawn.teleport_cooldown")).isNull()) data("spawn.teleport_cooldown", "set", 0);
            if (((DataWrapper) data("spawn.dimension")).isNull()) data("spawn.dimension", "set", "minecraft:overworld");
            if (((DataWrapper) data("spawn.x")).isNull()) data("spawn.x", "set", 0);
            if (((DataWrapper) data("spawn.y")).isNull()) data("spawn.y", "set", 0);
            if (((DataWrapper) data("spawn.z")).isNull()) data("spawn.z", "set", 0);
            if (((DataWrapper) data("spawn.pitch")).isNull()) data("spawn.pitch", "set", 0);
            if (((DataWrapper) data("spawn.yaw")).isNull()) data("spawn.yaw", "set", 90);
            if (((DataWrapper) data("warp.teleport_delay")).isNull()) data("warp.teleport_delay", "set", 5);
            if (((DataWrapper) data("server.vanish.action_bar_message")).isNull())
                data("server.vanish.action_bar_message", "set", true);
            if (((DataWrapper) data("server.lich_clear.cooldown")).isNull())
                data("server.lich_clear.cooldown", "set", 300);
            if (((DataWrapper) data("server.lich_clear.max_duration")).isNull())
                data("server.lich_clear.max_duration", "set", 60);
            if (((DataWrapper) data("server.lich_clear.all_worlds")).isNull())
                data("server.lich_clear.all_worlds", "set", true);
            if (((DataWrapper) data("server.lich_clear.worlds_list")).isNull())
                data("server.lich_clear.worlds_list", "set", new String[0]);
            if (((DataWrapper) data("lich_eye.containers_logger")).isNull())
                data("lich_eye.containers_logger", "set", true);
            if (((DataWrapper) data("lich_eye.blocks_logger")).isNull())
                data("lich_eye.blocks_logger", "set", new String[0]);
            if (((DataWrapper) data("server.permissions")).isNull()) data("server.permissions", "set", new String[0]);
        });
    }

    /**
     * Extraction utilitaire (synchrone).
     **/
    public static Object get(String key) {
        try {
            return BCKUtils.FileUtil.getFromJson(BCKUtils.FileUtil.loadJson(getFile().getPath()), key);
        } catch (IOException e) {
            BCKLog.fatal(BCKCore.TITLES_COLORS.title(BCKServerdata.class), "Error while reading file for §8" + key, e);
            return null;
        }
    }

    private static File getFile() {
        String path = getPath() + "/" + FILE_NAME;
        return new File(path);
    }

    /**
     * Détermine dynamiquement le chemin pour stocker les données selon le contexte.
     * Si le serveur Minecraft est disponible et en solo, on utilise le dossier "saves" ;
     * sinon on utilise un chemin générique.
     */
    private static String getPath() {
        String base = FMLPaths.GAMEDIR.get().toString();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        String world = BCKUtils.ServerUtil.getWorldFolderName();
        // Si le serveur n'est pas encore initialisé ou en solo
        if (server == null || server.isSingleplayer()) {
            return base + "/saves/" + world + "/serverdata/";
        } else {
            return base + "/" + world + "/serverdata/";
        }
    }
}