package fr.bck.tetralibs.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Objects;



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

public class DataWrapper {
    public static final class Utils {
        // ta couleur
        public static final String color = "§7";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }
    private final Object data;

    public DataWrapper(Object data) {
        this.data = data;
    }

    public boolean getBoolean() {
        if (data instanceof Boolean) {
            return (Boolean) data;
        }
        return false; // Retourne une valeur par défaut
    }

    @Override
    public String toString() {
        if (data == null) {
            return "null"; // Si la donnée est nulle, retourne "null"
        } else if (data instanceof JsonObject || data instanceof JsonArray) {
            return data.toString(); // Retourne la représentation JSON brute
        } else if (data instanceof String) {
            return (String) data; // Retourne la chaîne telle quelle
        } else if (data instanceof Number || data instanceof Boolean) {
            return String.valueOf(data); // Retourne la valeur sous forme de chaîne
        } else if (data instanceof String[]) {
            return String.join(", ", (String[]) data); // Concatène les éléments du tableau en une chaîne
        } else {
            return data.toString(); // Dernier recours, utilise la méthode toString() par défaut
        }
    }

    public int getInt() {
        if (data instanceof Integer) {
            return (Integer) data;
        } else if (data instanceof Double) {
            return ((Double) data).intValue(); // Convertit Double en int si nécessaire
        }
        return 0; // Retourne une valeur par défaut
    }

    public double getDouble() {
        if (data instanceof Double) {
            return (Double) data;
        } else if (data instanceof Integer) {
            return ((Integer) data).doubleValue(); // Convertit Integer en double si nécessaire
        }
        return 0.0; // Retourne une valeur par défaut
    }

    public String getString() {
        if (data instanceof String) {
            return (String) data;
        }
        return ""; // Retourne une valeur par défaut
    }

    public boolean isNull() {
        return data == null;
    }

    public JsonObject getJsonObject() {
        if (data instanceof JsonObject) {
            return (JsonObject) data;
        }
        return null; // Retourne null en cas d'erreur
    }

    public String[] getStringArray() {
        if (data instanceof JsonArray jsonArray) {
            String[] result = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement element = jsonArray.get(i);
                // Vérifie si l'élément est une chaîne
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    result[i] = element.getAsString();
                } else {
                    result[i] = ""; // Ajoute une chaîne vide par défaut
                }
            }
            return result;
        } else if (data instanceof String[]) {
            return (String[]) data;
        }
        return new String[0]; // Retourne un tableau vide par défaut
    }

    public JsonArray getJsonArray() {
        // Si c'est déjà un JsonArray, on le retourne
        if (data instanceof JsonArray) {
            return (JsonArray) data;
        }
        // Si c'est un tableau Java, on le convertit en JsonArray
        else if (data instanceof Object[]) {
            JsonArray jsonArray = new JsonArray();
            for (Object obj : (Object[]) data) {
                try {
                    if (obj instanceof JsonElement) {
                        jsonArray.add((JsonElement) obj);
                    } else if (obj instanceof String) {
                        jsonArray.add((String) obj);
                    } else if (obj instanceof Number) {
                        jsonArray.add((Number) obj);
                    } else if (obj instanceof Boolean) {
                        jsonArray.add((Boolean) obj);
                    } else {
                        BCKLog.warn(BCKCore.TITLES_COLORS.title(DataWrapper.class), "Type d'objet inattendu dans le tableau : " + (obj != null ? obj.getClass().getName() : "null"));
                    }
                } catch (Throwable t) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(DataWrapper.class), "Erreur lors de la conversion de l'élément du tableau : " + obj, t);
                }
            }
            return jsonArray;
        }
        // Sinon on loggue et on renvoie un tableau vide
        BCKLog.warn(BCKCore.TITLES_COLORS.title(DataWrapper.class), "Type inattendu pour data : " + (data != null ? data.getClass().getName() : "null"));
        return new JsonArray();
    }

    /**
     * Récupère un ItemStack à partir des données.
     * La donnée doit être un JsonObject contenant les informations de l'item.
     *
     * @return Un ItemStack si les données sont valides, sinon un ItemStack vide.
     */
    public ItemStack getItemStack() {
        if (data instanceof JsonObject jsonObject) {
            return BCKUtils.ItemUtil.fromJson(jsonObject);
        }
        return ItemStack.EMPTY; // Retourne un ItemStack vide si la donnée est invalide
    }

    /**
     * Récupère un ItemStack à partir des données.
     * La donnée doit être un JsonObject contenant les informations de l'item.
     *
     * @return Un ItemStack si les données sont valides, sinon un ItemStack vide.
     */
    public Item getItem() {
        return getItemStack().getItem(); // Retourne un ItemStack vide si la donnée est invalide
    }

    /**
     * Récupère un Block à partir des données.
     * La donnée doit être un JsonObject contenant les informations du bloc.
     *
     * @return Un Block si les données sont valides, sinon null.
     */
    public Block getBlock() {
        if (data instanceof JsonObject jsonObject) {
            return Objects.requireNonNull(BCKUtils.BlockUtil.fromJson(jsonObject)).getBlock();
        }
        return null; // Retourne null si la donnée est invalide
    }
}
