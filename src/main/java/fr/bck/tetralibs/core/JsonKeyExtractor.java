package fr.bck.tetralibs.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.common.Mod;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



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

@Mod.EventBusSubscriber
public class JsonKeyExtractor {
	public static final class Utils {
        // ta couleur
        public static final String color = "§f";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    /**
     * Extracts all nested keys with full paths (e.g., block.id, block.properties.hardness)
     * from a JSON file in the mod's config directory, including empty objects.
     *
     * @param fileName The name of the JSON file in the config directory.
     * @return A set of all unique fully qualified keys in the JSON file.
     * @throws IOException If the file cannot be read or parsed.
     */
    public static Set<String> extractFullyQualifiedKeys(String fileName, String path) throws IOException {
        // Path to the config file
        Path configPath = Paths.get(path, fileName);

        if (!Files.exists(configPath)) {
            throw new IOException("JSON file not found: " + configPath.toAbsolutePath());
        }

        // Read and parse JSON file
        try (FileReader reader = new FileReader(configPath.toFile())) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            Set<String> keys = new HashSet<>();

			//SuperLog.info("TetraLibs/Json", "keys: "+ keys);
            // Recursive key extraction
            extractKeysRecursive(rootElement, "", keys);
            //SuperLog.info("TetraLibs/Json", "keys: "+ keys);

            return keys;
        }
    }
    /**
     * Recursively extracts only fully qualified keys from a JSON element,
     * ensuring empty objects are included.
     *
     * @param element The current JSON element.
     * @param parentPath The parent path for the current element.
     * @param keys The set of keys being collected.
     */
    private static void extractKeysRecursive(JsonElement element, String parentPath, Set<String> keys) {
	    // Validation des arguments
	    if (keys == null) {
	        throw new IllegalArgumentException("The 'keys' set must not be null");
	    }
	    parentPath = parentPath == null ? "" : parentPath;
	
	    if (element.isJsonObject()) {
	        JsonObject jsonObject = element.getAsJsonObject();
	        boolean isEmpty = jsonObject.entrySet().isEmpty(); // Vérifie si l'objet est vide
	
	        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
	            String currentPath = parentPath.isEmpty() ? entry.getKey() : parentPath + "." + entry.getKey();
	            //SuperLog.info("TetraLibs/Json", "path: " + currentPath);
	
	            // Recurse into child elements
	            if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
	                extractKeysRecursive(entry.getValue(), currentPath, keys);
	            } else {
	                keys.add(currentPath); // Ajouter la clé complète pour les valeurs primitives
	                //SuperLog.info("TetraLibs/Json", "Adding key to keys: " + currentPath);
	            }
	        }
	
	        // Si l'objet est vide, ajoutez la clé racine (exemple : "server")
	        if (isEmpty) {
	            keys.add(parentPath);
	            //SuperLog.info("TetraLibs/Json", "empty path added: " + parentPath);
	        }
	    } else if (element.isJsonArray()) {
	        boolean isEmpty = element.getAsJsonArray().isEmpty(); // Vérifie si le tableau est vide
	
	        for (JsonElement item : element.getAsJsonArray()) {
	            extractKeysRecursive(item, parentPath, keys);
	        }
	
	        // Si le tableau est vide, ajoutez la clé racine
	        if (isEmpty) {
	            keys.add(parentPath);
	            //SuperLog.info("TetraLibs/Json", "empty array path added: " + parentPath);
	        }
	    } else if (element.isJsonPrimitive()) {
	        // Ajouter les primitives directement avec le chemin complet
	        keys.add(parentPath);
	        //SuperLog.info("TetraLibs/Json", "Adding primitive key to keys: " + parentPath);
	    } else if (element.isJsonNull()) {
	        // Ajouter les valeurs nulles
	        keys.add(parentPath + ".null");
	        //SuperLog.info("TetraLibs/Json", "Adding null key to keys: " + parentPath + ".null");
	    }
	}
}
