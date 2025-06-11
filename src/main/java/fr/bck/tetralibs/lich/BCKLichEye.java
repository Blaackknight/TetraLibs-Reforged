package fr.bck.tetralibs.lich;

import com.google.gson.*;
import fr.bck.tetralibs.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class BCKLichEye {
    public static final class Utils {
        // ta couleur
        public static final String color = "§b";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    private static final String FILE_NAME = "necro_logs.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void deploy() {
        GameModeHandler.executeServerOnlyCode(() -> {
            File f = getFile();
            JsonObject object = new JsonObject();
            if (!f.exists()) {
                try {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                    {
                        Gson mainGSONBuilderVariable = new GsonBuilder().setPrettyPrinting().create();
                        try {
                            FileWriter fileWriter = new FileWriter(f);
                            fileWriter.write(mainGSONBuilderVariable.toJson(object));
                            fileWriter.close();
                        } catch (IOException exception) {
                            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "Error while writing file", exception);
                        }
                    }
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "File created at " + getFile().getPath());
                } catch (IOException exception) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "Error while creating file", exception);
                }
            }
            // Initialisation des données par défaut si non définies
            if (((DataWrapper) data("containers")).isNull()) data("containers", "set", new JsonObject());
            if (((DataWrapper) data("events")).isNull()) data("events", "set", new JsonObject());
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "The veil between worlds is lifted. LichEye observes all.");
        });
    }

    /**
     * Gère l'extraction ou la mise à jour des données dans un système de stockage global.
     * Cette méthode permet d'extraire des données ou de mettre à jour les données dans le fichier JSON du serveur.
     *
     * @param key   La clé de données cible (peut être une chaîne imbriquée, par ex. "server.stats.players_online").
     *              Cette clé représente l'emplacement des données dans le fichier de stockage, permettant d'extraire ou de modifier des valeurs spécifiques.
     * @param mode  Le mode d'opération :
     *              - "extract" : pour récupérer une valeur associée à la clé donnée.
     *              - "set" : pour définir ou modifier la valeur associée à la clé.
     * @param value La valeur à définir dans le cas du mode "set" (ignorée si le mode est "extract").
     *              Cette valeur sera enregistrée dans le fichier de données globales.
     * @return La valeur extraite si le mode est "extract", sinon la clé elle-même (utilisée lors de l'opération "set").
     * En mode "extract", la méthode renvoie la valeur associée à la clé dans le fichier de données. En mode "set", elle renvoie simplement la clé.
     */
    public static Object data(String key, String mode, Object value) {
        Object result = null;

        // Vérifie si le mode est "extract", pour récupérer une valeur
        if (mode.equals("extract")) {
            result = get(key); // Appelle la méthode 'get' pour extraire la valeur associée à la clé

            // Encapsule le résultat dans un DataWrapper si ce n'est pas déjà fait
            if (!(result instanceof DataWrapper)) {
                return new DataWrapper(result);
            }

            // Sinon, encapsule la valeur dans un DataWrapper
            return new DataWrapper(result);
        }
        // Vérifie si le mode est "set", pour définir une nouvelle valeur
        else if (mode.equals("set")) {
            try {
                set(getFile().getPath(), key, value); // Appelle la méthode 'set' pour modifier ou ajouter une valeur dans le fichier JSON
            } catch (Exception e) {
                // Gère les exceptions en cas d'échec de l'écriture dans le fichier
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "Error while setting key §8" + key, e);
            }
        }

        return result; // Retourne la valeur extraite ou la clé, selon le mode
    }

    /**
     * Gère l'extraction des données dans un système de stockage global.
     * Cette méthode simplifie l'appel à la méthode `data` en mode "extract" pour récupérer des données sans modifier quoi que ce soit.
     *
     * @param key La clé de données cible (peut être une chaîne imbriquée, par ex. "server.stats.players_online").
     * @return La valeur extraite du système de stockage pour la clé donnée.
     */
    public static Object data(String key) {
        return data(key, "extract", null);
    }

    /**
     * Définit une valeur dans un objet JSON imbriqué.
     *
     * @param jsonObject L'objet JSON dans lequel la valeur sera définie.
     * @param keyPath    Le chemin des clés imbriquées où définir la valeur.
     * @param value      La valeur à définir.
     */
    private static void setValue(JsonObject jsonObject, String keyPath, Object value) {
        String[] keys = keyPath.split("\\."); // Divise le chemin en un tableau de clés
        JsonObject currentObject = jsonObject;

        // Parcourt toutes les clés sauf la dernière
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            if (!currentObject.has(key) || !currentObject.get(key).isJsonObject()) {
                currentObject.add(key, new JsonObject());
            }
            currentObject = currentObject.getAsJsonObject(key);
        }

        // Définit la valeur à la dernière clé
        String lastKey = keys[keys.length - 1];
        if (value instanceof String) {
            currentObject.addProperty(lastKey, (String) value);
        } else if (value instanceof Double) {
            currentObject.addProperty(lastKey, (Double) value);
        } else if (value instanceof Number) {
            currentObject.addProperty(lastKey, (Number) value);
        } else if (value instanceof Boolean) {
            currentObject.addProperty(lastKey, (Boolean) value);
        } else if (value instanceof JsonObject) {
            currentObject.add(lastKey, (JsonObject) value);
        } else if (value instanceof JsonArray) {
            currentObject.add(lastKey, (JsonArray) value);
        } else if (value instanceof String[]) {
            JsonArray jsonArray = new JsonArray();
            for (String str : (String[]) value) {
                jsonArray.add(str);
            }
            currentObject.add(lastKey, jsonArray);
        } else {
            throw new IllegalArgumentException("Type de valeur non pris en charge : " + value.getClass().getSimpleName());
        }
    }

    /**
     * Définit une valeur dans un fichier JSON en suivant un chemin imbriqué.
     * Si un niveau intermédiaire est manquant, il est créé automatiquement.
     * Les modifications sont persistées dans le fichier JSON.
     *
     * @param filePath Le chemin du fichier JSON où les données seront modifiées.
     * @param keyPath  Le chemin des clés imbriquées dans le fichier JSON, séparées par des points (par exemple, "player.username").
     * @param value    La valeur à définir, qui peut être de type String, Number, Boolean, JsonObject ou JsonArray.
     * @throws IOException Si une erreur de lecture ou d'écriture du fichier se produit, cette exception est levée.
     */
    public static void set(String filePath, String keyPath, Object value) throws IOException {
        // Charger l'objet JSON existant ou en créer un nouveau s'il n'existe pas
        JsonObject jsonObject = BCKUtils.FileUtil.loadJson(filePath);

        // Définir la valeur dans l'objet JSON en suivant le chemin imbriqué
        setValue(jsonObject, keyPath, value);

        // Sauvegarder l'objet JSON modifié dans le fichier spécifié
        BCKUtils.FileUtil.saveJsonObjectToFile(jsonObject, filePath);
    }

    /**
     * Récupère une valeur imbriquée à partir d'un fichier JSON.
     *
     * @param key La clé (ou chemin imbriqué) des données à récupérer.
     * @return La valeur extraite du fichier JSON, ou null si la clé n'existe pas.
     */
    public static Object get(String key) {
        try {
            JsonObject o = BCKUtils.FileUtil.loadJson(getFile().getPath());
            return getFromJson(o, key);
        } catch (IOException e) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "Error while getting key §8" + key, e);
        }
        return null;
    }

    private static String getPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        String path = FMLPaths.GAMEDIR.get().toString() + "/" + getWorldName() + "/licheye/";
        if (server != null) {
            if (server.isSingleplayer()) {
                path = FMLPaths.GAMEDIR.get().toString() + "/saves/" + getWorldName() + "/licheye/";
            }
        }
        // Sinon, retourne le dossier global de l'instance
        return path;
    }

    /**
     * Récupère le nom du monde actuel dans le serveur Minecraft.
     *
     * @return Le nom du monde, ou "Unknown" si le nom du monde est introuvable.
     */
    private static String getWorldName() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            WorldData worldData = server.getWorldData();
            return worldData.getLevelName(); // Récupère le nom du monde du serveur
        }
        return "unknown_world"; // Retourne "Unknown" si le nom du monde est introuvable
    }

    /**
     * Récupère les clés imbriquées d'un fichier JSON associé à une entité spécifique.
     * <p>
     * Cette méthode extrait les clés depuis un fichier JSON situé dans un répertoire spécifique
     * aux données utilisateur, en se basant sur l'identifiant unique de l'entité et son niveau.
     * Si une erreur se produit lors de l'extraction, elle est consignée dans les journaux,
     * et un tableau vide est renvoyé.
     *
     * @return Un tableau de chaînes de caractères contenant toutes les clés imbriquées
     * et pleinement qualifiées (e.g., "block.id", "block.properties.hardness").
     * Si une erreur se produit ou si le fichier est introuvable, un tableau vide est retourné.
     */
    public static String[] getKeys() {
        try {
            // Récupère l'ensemble des clés à partir du fichier JSON associé à l'entité.
            // Le fichier est nommé en fonction de l'UUID de l'entité et est situé
            // dans un chemin défini par userdataPath.
            Set<String> keys = JsonKeyExtractor.extractFullyQualifiedKeys(
                    FILE_NAME, // Nom du fichier basé sur l'UUID
                    getPath() // Chemin spécifique au niveau de l'entité
            );

            // Convertit l'ensemble des clés en un tableau de chaînes de caractères et le retourne.
            return keys.toArray(new String[0]);
        } catch (IOException e) {
            // Journalise l'erreur en utilisant SuperLog pour faciliter le débogage.
            // Inclut des informations sur l'emplacement de l'erreur et le message associé.
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "Error extracting keys: " + e.getMessage());
        }

        // Retourne un tableau vide si une exception est levée ou en cas de problème.
        return new String[0];
    }

    /**
     * Récupère le fichier de données du serveur en fonction du contexte (serveur ou global).
     *
     * @return Le fichier `server_data.json`.
     */
    private static File getFile() {
        String path = getPath() + "/" + FILE_NAME;
        return new File(path);
    }

    public JsonObject getContainerData(String modId, String typeName, int x, int y, int z) {
        // Récupérer le JsonArray
        JsonArray array = ((DataWrapper) BCKLichEye.data("containers." + modId + "." + typeName)).getJsonArray();

        // Rechercher l'objet correspondant aux coordonnées
        return getJsonObjectByCoordinates(array, x, y, z);
    }

    // Méthode pour récupérer un JsonObject par ses coordonnées
    public static JsonObject getJsonObjectByCoordinates(JsonArray array, int x, int y, int z) {

        // Logique principale côté serveur
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                // Vérifie la présence des clés "x", "y", "z"
                if (obj.has("x") && obj.has("y") && obj.has("z")) {
                    int objX = obj.get("x").getAsInt();
                    int objY = obj.get("y").getAsInt();
                    int objZ = obj.get("z").getAsInt();

                    // Compare les coordonnées
                    if (objX == x && objY == y && objZ == z) {
                        //SuperLog.info("LichEye", "Objet trouvé aux coordonnées : " + obj);
                        return obj; // Retourne l'objet trouvé
                    }
                } else {
                    BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "Un objet du tableau ne contient pas les clés nécessaires (x, y, z) : " + obj);
                }
            } else {
                BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "Un élément dans le JsonArray n'est pas un JsonObject : " + element);
            }
        }

        //SuperLog.info("LichEye", "Aucun objet trouvé aux coordonnées (" + x + ", " + y + ", " + z + ").");
        return null; // Si aucun objet ne correspond
    }

    // Méthode pour modifier un JsonObject et l'appliquer dans le JsonArray
    public static JsonArray updateJsonObjectInArray(JsonArray array, LevelAccessor world, int x, int y, int z, String key, Object newValue) {
        JsonObject obj = getJsonObjectByCoordinates(array, x, y, z);
        String dimension = null;
        // Récupérer la dimension
        if (world instanceof ServerLevel serverLevel) {
            dimension = serverLevel.dimension().location().toString();
        }
        if (obj != null) {
            // Si la clé existe déjà et contient un objet, fusionner les nouvelles données
            if (obj.has(key) && obj.get(key).isJsonObject()) {
                JsonObject existingData = obj.getAsJsonObject(key);
                JsonObject newData = toJsonElement(newValue).getAsJsonObject();

                // Fusionner les deux objets JSON
                for (String newKey : newData.keySet()) {
                    existingData.add(newKey, newData.get(newKey));
                }
            } else {
                // Ajouter ou remplacer la clé avec la nouvelle valeur
                obj.add(key, toJsonElement(newValue));
            }

            //SuperLog.info("LichEye", "Mise à jour de l'objet existant aux coordonnées (" + x + ", " + y + ", " + z + ") avec la clé : " + key);
        } else {
            // Si aucun objet trouvé, en créer un nouveau
            JsonObject newObj = new JsonObject();
            newObj.addProperty("x", x);
            newObj.addProperty("y", y);
            newObj.addProperty("z", z);
            newObj.addProperty("dimension", dimension);

            // Ajouter la nouvelle clé/valeur
            JsonElement valueElement = toJsonElement(newValue);
            newObj.add(key, valueElement);

            // Ajouter le nouvel objet au tableau
            array.add(newObj);
            //SuperLog.info("LichEye", "Ajout d'un nouvel objet aux coordonnées (" + x + ", " + y + ", " + z + ") avec la clé : " + key);
        }
        return array;
    }

    // Méthode pour convertir un Object en JsonElement tout en préservant les types
    private static JsonElement toJsonElement(Object value) {
        if (value instanceof String) {
            return new JsonPrimitive((String) value);
        } else if (value instanceof Integer) {
            return new JsonPrimitive((Integer) value); // Conserve le type Integer
        } else if (value instanceof Double) {
            return new JsonPrimitive((Double) value); // Conserve le type Double
        } else if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        } else if (value instanceof Character) {
            return new JsonPrimitive((Character) value);
        } else if (value instanceof JsonElement) {
            return (JsonElement) value; // Déjà un JsonElement
        } else if (value instanceof JsonObject) {
            return (JsonObject) value; // Cast direct si c'est un JsonObject
        } else if (value instanceof JsonArray) {
            return (JsonArray) value; // Cast direct si c'est un JsonArray
        } else {
            throw new IllegalArgumentException("Type de valeur non supporté : " + value.getClass());
        }
    }

    /**
     * Récupère la valeur correspondant à un chemin imbriqué dans un objet JSON.
     * Le chemin est composé de clés séparées par des points (ex. "player.username").
     * Si une clé est introuvable, la méthode retourne `null`.
     * <p>
     * Cette méthode permet d'accéder de manière flexible aux données stockées sous forme d'objet JSON
     * et de récupérer des éléments à n'importe quel niveau de profondeur.
     *
     * @param jsonObject L'objet JSON à interroger.
     * @param keyPath    Le chemin des clés imbriquées, séparées par des points (ex. "player.username").
     * @return La valeur trouvée (String, Number, Boolean, JsonObject ou JsonArray) ou `null` si la clé n'existe pas.
     * Si le chemin est invalide, la méthode retourne `null`.
     */
    private static Object getFromJson(JsonObject jsonObject, String keyPath) {
        // Divise le chemin imbriqué en un tableau de clés
        String[] keys = keyPath.split("\\.");
        JsonElement currentElement = jsonObject;

        // Parcourt chaque clé pour descendre dans l'objet JSON
        for (String key : keys) {
            // Si l'élément courant est un objet JSON et qu'il contient la clé actuelle
            if (currentElement.isJsonObject() && currentElement.getAsJsonObject().has(key)) {
                // Passe à l'élément suivant dans la hiérarchie
                currentElement = currentElement.getAsJsonObject().get(key);
            } else {
                // Si une clé n'existe pas, retourne null
                return null;
            }
        }

        // Détermine le type de la valeur trouvée et retourne la valeur correspondante
        if (currentElement.isJsonPrimitive()) {
            JsonPrimitive primitive = currentElement.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString(); // Retourne une chaîne
            } else if (primitive.isNumber()) {
                // Vérifie si le nombre peut être un entier
                Number number = primitive.getAsNumber();
                if (number.doubleValue() % 1 == 0) {
                    return number.intValue(); // Retourne un entier
                } else {
                    return number.doubleValue(); // Retourne un double
                }
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean(); // Retourne un booléen
            }
        } else if (currentElement.isJsonObject()) {
            return currentElement.getAsJsonObject(); // Retourne un objet JSON
        } else if (currentElement.isJsonArray()) {
            // Convertir le JsonArray en Object[], en conservant le type réel de chaque élément
            JsonArray jsonArray = currentElement.getAsJsonArray();
            Object[] result = new Object[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement element = jsonArray.get(i);

                // Détermine le type réel de chaque élément
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if (primitive.isString()) {
                        result[i] = primitive.getAsString();
                    } else if (primitive.isNumber()) {
                        Number number = primitive.getAsNumber();
                        if (number.doubleValue() % 1 == 0) {
                            result[i] = number.intValue(); // Conserve un entier
                        } else {
                            result[i] = number.doubleValue(); // Conserve un double
                        }
                    } else if (primitive.isBoolean()) {
                        result[i] = primitive.getAsBoolean();
                    }
                } else if (element.isJsonObject()) {
                    result[i] = element.getAsJsonObject(); // Conserve un JsonObject
                } else if (element.isJsonArray()) {
                    result[i] = element.getAsJsonArray(); // Conserve un JsonArray
                }
            }
            return result; // Retourne le tableau d'objets
        } else {
            return null; // Si le type n'est pas pris en charge, retourne null
        }

        return null; // Retourne null si le type n'est pas pris en charge
    }

    public static void containersInit(String modId, String typeName) {
        if (((DataWrapper) BCKLichEye.data("containers." + modId)).isNull())
            BCKLichEye.data("containers." + modId, "set", new JsonObject());
        if (((DataWrapper) BCKLichEye.data("containers." + modId + "." + typeName)).isNull())
            BCKLichEye.data("containers." + modId + "." + typeName, "set", new JsonArray());
    }

    public static void blocksInit(String modId, String typeName) {
        if (((DataWrapper) BCKLichEye.data("events.blocks." + modId)).isNull())
            BCKLichEye.data("events.blocks." + modId, "set", new JsonObject());
        if (((DataWrapper) BCKLichEye.data("events.blocks." + modId + "." + typeName)).isNull())
            BCKLichEye.data("events.blocks." + modId + "." + typeName, "set", new JsonArray());
    }

    public static void portalsInit() {
        if (((DataWrapper) BCKLichEye.data("events.portals")).isNull())
            BCKLichEye.data("events.portals", "set", new JsonArray());
    }
}
