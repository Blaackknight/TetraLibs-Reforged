package fr.bck.tetralibs.recipe;

/*≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
 ≡              Copyright BCK, Inc 2025. (DragClover / Blackknight)              ≡
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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stocke et gère un ensemble de BCKRecipe dans une Map.
 * - addRecipe(...) pour ajouter
 * - findMatchingRecipe(...) pour chercher une correspondance
 * - getAllRecipes() pour parcourir
 * etc.
 */
public class BCKRecipeManager {
    public static final class Utils {
        // ta couleur
        public static final String color = "§e";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    // Map = {ID de recette -> instance de BCKRecipe}
    private static final Map<ResourceLocation, BCKRecipe> RECIPE_MAP = new LinkedHashMap<>();

    /**
     * Ajoute (ou remplace) une recette dans la map.
     */
    public static void addRecipe(BCKRecipe recipe) {
        RECIPE_MAP.put(recipe.getId(), recipe);
    }

    /**
     * Récupère la recette par son ID (ou null si aucune).
     */
    public static BCKRecipe getRecipe(ResourceLocation id) {
        return RECIPE_MAP.get(id);
    }

    /**
     * Retourne TOUTES les recettes enregistrées.
     */
    public static Collection<BCKRecipe> getAllRecipes() {
        return RECIPE_MAP.values();
    }

    /**
     * Parcourt toutes les recettes et renvoie la 1ère
     * qui matche le tableau de slots.
     */
    public static BCKRecipe findMatchingRecipe(ItemStack[] slots) {
        for (BCKRecipe recipe : RECIPE_MAP.values()) {
            if (recipe.matches(slots)) {
                return recipe;
            }
        }
        return null; // aucune ne correspond
    }

    /**
     * Supprime une recette selon son ID.
     */
    public static void removeRecipe(ResourceLocation id) {
        RECIPE_MAP.remove(id);
    }

    /**
     * Vide la liste de recettes (si besoin).
     */
    public static void clearAll() {
        RECIPE_MAP.clear();
    }
}
