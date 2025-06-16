package fr.bck.tetralibs.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.bck.tetralibs.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
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
 * Classe responsable de la gestion des données utilisateur (JSON) avec debounce
 * pour regrouper et différer les écritures sur disque, évitant les pertes
 * lors d'opérations rapides répétées.
 */
public class BCKUserdata {
    public static final class Utils {
        // ta couleur
        public static final String color = "§5";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    // ----------------- Debounce -------------------
    private static final ScheduledExecutorService SAVE_SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> PENDING_SAVES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, JsonObject> PENDING_JSON = new ConcurrentHashMap<>();
    private static final long SAVE_DEBOUNCE_MS = 500;

    /**
     * Planifie la persistance différée du JsonObject en mémoire vers le fichier.
     */
    private static void scheduleFlush(String filePath) {
        ScheduledFuture<?> prev = PENDING_SAVES.get(filePath);
        if (prev != null && !prev.isDone()) {
            prev.cancel(false);
        }
        ScheduledFuture<?> future = SAVE_SCHEDULER.schedule(() -> {
            JsonObject pending = PENDING_JSON.remove(filePath);
            if (pending != null) {
                try {
                    BCKUtils.FileUtil.saveJsonObjectToFile(pending, filePath);
                } catch (IOException e) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Flush", "Failed to flush §8" + filePath + "§r: ", e);
                }
            }
            PENDING_SAVES.remove(filePath);
        }, SAVE_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        PENDING_SAVES.put(filePath, future);
    }

    /**
     * Retourne le JsonObject de travail (en cache ou chargé depuis le fichier).
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
     * Définit une valeur dans l'objet Json en mémoire selon un chemin imbriqué.
     * <p>
     * Supporte les types String, Number (Double, Integer…), Boolean, JsonObject, JsonArray,
     * ainsi que les tableaux String[], Number[], Boolean[].
     */
    private static void setValue(JsonObject current, String keyPath, Object value) {
        String[] keys = keyPath.split("\\.");
        for (int i = 0; i < keys.length - 1; i++) {
            String k = keys[i];
            if (!current.has(k) || !current.get(k).isJsonObject()) {
                current.add(k, new JsonObject());
            }
            current = current.getAsJsonObject(k);
        }
        String lastKey = keys[keys.length - 1];
        try {
            if (value instanceof String) {
                current.addProperty(lastKey, (String) value);
            } else if (value instanceof Number) {
                current.addProperty(lastKey, (Number) value);
            } else if (value instanceof Boolean) {
                current.addProperty(lastKey, (Boolean) value);
            } else if (value instanceof JsonObject) {
                current.add(lastKey, (JsonObject) value);
            } else if (value instanceof JsonArray) {
                current.add(lastKey, (JsonArray) value);
            } else if (value instanceof String[]) {
                JsonArray arr = new JsonArray();
                for (String s : (String[]) value) arr.add(s);
                current.add(lastKey, arr);
            } else if (value instanceof Number[]) {
                JsonArray arr = new JsonArray();
                for (Number n : (Number[]) value) arr.add(n);
                current.add(lastKey, arr);
            } else if (value instanceof Boolean[]) {
                JsonArray arr = new JsonArray();
                for (Boolean b : (Boolean[]) value) arr.add(b);
                current.add(lastKey, arr);
            } else {
                throw new IllegalArgumentException("Unsupported type: " + (value != null ? value.getClass().getName() : "null"));
            }
        } catch (Throwable t) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/SetValue", "Erreur en définissant '" + keyPath + "' = " + value, t);
            throw t;
        }
    }

    /**
     * Supprime une clé imbriquée de l'objet Json en mémoire.
     */
    private static void deleteKey(JsonObject jsonObject, String keyPath) {
        String[] keys = keyPath.split("\\.");
        JsonObject current = jsonObject;
        for (int i = 0; i < keys.length - 1; i++) {
            String k = keys[i];
            if (!current.has(k) || !current.get(k).isJsonObject()) {
                return;
            }
            current = current.getAsJsonObject(k);
        }
        current.remove(keys[keys.length - 1]);
    }

    // --------------- Méthodes publiques --------------

    /**
     * Extraction ou mise à jour de données utilisateur.
     * Mode "extract": lecture synchrone depuis le fichier.
     * Mode "set": modifie l'objet Json en mémoire et planifie la sauvegarde.
     * Mode "delete": supprime la clé en mémoire et planifie la sauvegarde.
     */
    public static Object data(String key, String mode, Object value, LevelAccessor world, Entity player) {
        //BCKLog.debug("§8BCKUserdata§6/§dDebug", "§7key: §d" + key + " §e-> §b" + mode + " §e-> §9" + value);
        String filePath = getFile(world, player).getPath();
        switch (mode) {
            case "extract" -> {
                try {
                    JsonObject o = getWorkingJson(filePath);
                    Object res = BCKUtils.FileUtil.getFromJson(o, key);
                    return new DataWrapper(res);
                } catch (IOException e) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Extract", "Failed to load §8" + filePath + "§r: ", e);
                    return new DataWrapper(null);
                }
            }
            case "set" -> {
                try {
                    JsonObject working = getWorkingJson(filePath);
                    if (value instanceof ItemStack) {
                        value = BCKUtils.ItemUtil.toJson((ItemStack) value);
                    } else if (value instanceof BlockState) {
                        value = BCKUtils.BlockUtil.toJson((BlockState) value);
                    }
                    setValue(working, key, value);
                    scheduleFlush(filePath);
                } catch (IOException e) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Set", "Failed to schedule set §8" + key + "§r: ", e);
                }
                return key;
            }
            case "delete" -> {
                try {
                    JsonObject working = getWorkingJson(filePath);
                    deleteKey(working, key);
                    scheduleFlush(filePath);
                } catch (IOException e) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Delete", "Failed to schedule delete §8" + key + "§r: ", e);
                }
            }
        }
        return null;
    }

    /**
     * Lecture simplifiée en mode extract.
     */
    public static Object data(String key, LevelAccessor world, Entity player) {
        return data(key, "extract", null, world, player);
    }

    // ------- Fonctions existantes (inchangées) -------

    /**
     * Récupère les clés imbriquées d'un fichier JSON pour une entité.
     */
    public static String[] getKeys(Entity entity) {
        try {
            Set<String> keys = JsonKeyExtractor.extractFullyQualifiedKeys(entity.getStringUUID() + ".json", userdataPath(entity.level()));
            return keys.toArray(new String[0]);
        } catch (IOException e) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Keys", "Error extracting keys: ", e);
        }
        return new String[0];
    }

    /**
     * Liste tous les fichiers userdata dans un monde.
     */
    public static File[] getUserdatas(LevelAccessor world) {
        File dir = new File(userdataPath(world));
        return dir.isDirectory() ? dir.listFiles() : null;
    }

    /**
     * Charge les données d'un joueur (invoqué au besoin).
     */
    public static void load(LevelAccessor world, Entity player) {
        GameModeHandler.executeServerOnlyCode(() -> {
            getFile(world, player); // crée le fichier si absent via init
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Load", "(" + BCKUtils.ServerUtil.getWorldFolderName() + ") Userdata loaded for " + player.getStringUUID());
        });
    }

    /**
     * Chargement global des userdata au startup du monde.
     */
    public static void worldLoad(LevelAccessor world) {
        GameModeHandler.executeServerOnlyCode(() -> {
            File[] files = getUserdatas(world);
            if (files != null) {
                for (File f : files) {
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/WorldLoad", "Userdata loaded for " + f.getPath());
                }
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/WorldLoad", files.length + " userdata found");
            } else {
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/WorldLoad", "0 userdata found");
            }
        });
    }

    /**
     * Sauvegarde explicite des données d'un joueur.
     */
    public static void save(LevelAccessor world, Entity player) {
        GameModeHandler.executeServerOnlyCode(() -> {
            getFile(world, player); // assure l'existence
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Save", "(" + BCKUtils.ServerUtil.getWorldFolderName() + ") Userdata saved for " + player.getStringUUID());
        });
    }

    /**
     * Initialisation du fichier et des valeurs par défaut.
     */
    public static void init(LevelAccessor world, Entity player) {
        GameModeHandler.executeServerOnlyCode(() -> {
            File f = getFile(world, player);
            JsonObject object = new JsonObject();
            if (!f.exists()) {
                try {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    FileWriter writer = new FileWriter(f);
                    writer.write(gson.toJson(object));
                    writer.close();
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUserdata.class) + "§6/Init", "Userdata file created for " + player.getStringUUID());
                } catch (IOException e) {
                    BCKLog.fatal(BCKCore.TITLES_COLORS.title(BCKUserdata.class), "Error while creating file", e);
                }
            }
            // valeurs par défaut
            if (((DataWrapper) data("default_username", world, player)).isNull())
                data("default_username", "set", player.getDisplayName().getString(), world, player);
            if (((DataWrapper) data("uuid", world, player)).isNull())
                data("uuid", "set", player.getStringUUID(), world, player);
            if (((DataWrapper) data("economy.money", world, player)).isNull())
                data("economy.money", "set", 0.0, world, player);
            if (((DataWrapper) data("economy.bank", world, player)).isNull())
                data("economy.bank", "set", 0.0, world, player);
            if (((DataWrapper) data("lich_whisper.level", world, player)).isNull())
                data("lich_whisper.level", "set", 0.0, world, player);
            if (((DataWrapper) data("first_join", world, player)).isNull())
                data("first_join", "set", false, world, player);
            if (((DataWrapper) data("tetra_page.page", world, player)).isNull())
                data("tetra_page.page", "set", 1, world, player);
            if (((DataWrapper) data("permissions", world, player)).isNull())
                data("permissions", "set", new String[0], world, player);
        });
    }

    /**
     * Extraction utilitaire (synchrone) d'une valeur.
     */
    public static Object get(String key, LevelAccessor world, Entity player) {
        try {
            JsonObject o = BCKUtils.FileUtil.loadJson(getFile(world, player).getPath());
            return BCKUtils.FileUtil.getFromJson(o, key);
        } catch (IOException e) {
            BCKLog.fatal(BCKCore.TITLES_COLORS.title(BCKUserdata.class), "Error while reading file with key §8" + key, e);
            return null;
        }
    }

    /**
     * Écriture synchrone directe (mode set), peu utilisée désormais.
     */
    public static void set(String filePath, String keyPath, Object value, LevelAccessor world, Entity player) throws IOException {
        JsonObject jsonObject = BCKUtils.FileUtil.loadJson(filePath);
        setValue(jsonObject, keyPath, value);
        BCKUtils.FileUtil.saveJsonObjectToFile(jsonObject, filePath);
    }

    /**
     * Ajoute une propriété String à un JsonObject.
     */
    public static void addProperty(JsonObject object, String name, String content) {
        object.addProperty(name, content);
    }

    /**
     * Ajoute une propriété Double à un JsonObject.
     */
    public static void addProperty(JsonObject object, String name, Double content) {
        object.addProperty(name, content);
    }

    /**
     * Ajoute une propriété Boolean à un JsonObject.
     */
    public static void addProperty(JsonObject object, String name, Boolean content) {
        object.addProperty(name, content);
    }

    private static File[] getFiles(String path) {
        File dir = new File(path);
        return dir.isDirectory() ? dir.listFiles() : null;
    }

    private static File getFile(LevelAccessor world, Entity player) {
        String path = userdataPath(world) + player.getStringUUID() + ".json";
        return new File(path);
    }

    private static String userdataPath(LevelAccessor world) {
        String base = FMLPaths.GAMEDIR.get().toString();
        String worldName = BCKUtils.ServerUtil.getWorldFolderName();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.isSingleplayer()) {
            return base + "/saves/" + worldName + "/userdata/";
        } else {
            return base + "/" + worldName + "/userdata/";
        }
    }
}