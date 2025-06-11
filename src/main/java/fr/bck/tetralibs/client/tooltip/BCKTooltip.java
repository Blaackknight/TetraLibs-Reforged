package fr.bck.tetralibs.client.tooltip;


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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe qui gère l'affichage d'informations supplémentaires dans les infobulles des objets.
 * Elle est associée à l'abonnement aux événements liés à l'affichage des infobulles des objets.
 * <p>
 * Utilise un événement Forge pour modifier les infobulles des objets en fonction de leur clé.
 */
public class BCKTooltip {

    public static final class Utils {
        // ta couleur
        public static final String color = "§e";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }
    // Liste qui contient les clés des objets pour lesquels des informations supplémentaires doivent être affichées.
    private static final List<String> itemsWithAdditionalInfo = new ArrayList<>();

    /**
     * Événement qui est déclenché lorsque l'infobulle d'un objet est affichée.
     * Ajoute des informations supplémentaires si l'objet est dans la liste `itemsWithAdditionalInfo` et si la touche Shift est enfoncée.
     *
     * @param event L'événement déclenché lors de l'affichage de l'infobulle d'un objet.
     */
    @SubscribeEvent
    public static void onTooltipRender(ItemTooltipEvent event) {
        Minecraft mc = Minecraft.getInstance();

        // Si le joueur n'est pas dans un monde (e.g., au menu), on ignore l'événement.
        if (Minecraft.getInstance().level == null) {
            return;
        }

        // Récupère l'objet de l'inventaire dont l'infobulle est affichée.
        ItemStack stack = event.getItemStack();

        // Si l'objet est vide ou que les infobulles avancées ne sont pas activées, on arrête le traitement.
        if (stack.isEmpty()) {
            return;
        }
        // Vérifie si la touche Shift est enfoncée et si l'objet a des informations supplémentaires.
        if (Screen.hasShiftDown() && itemsWithAdditionalInfo.contains(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString())) {
            // Si des informations supplémentaires existent pour cet objet, les ajoute à l'infobulle.
            Component additionalInfo = getAdditionalInfo(stack);
            List<Component> co = event.getToolTip();
            co.add(Component.empty());  // Ajoute une ligne vide.
            co.add(Component.translatable("tetra_libs.item_tooltip.shift_pressed")); // Indication de la touche Shift préssé.
            co.add(additionalInfo);     // Ajoute l'information supplémentaire.
            co.add(Component.empty());  // Ajoute une ligne vide.
        } else if (itemsWithAdditionalInfo.contains(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString())) {
            // Si Shift n'est pas enfoncé, mais que l'objet a des informations supplémentaires,
            // ajoute une ligne indiquant d'appuyer sur Shift pour voir les infos.
            List<Component> co = event.getToolTip();
            co.add(Component.empty());  // Ajoute une ligne vide.
            co.add(Component.translatable("tetra_libs.item_tooltip.press_shift")); // Indication de la touche Shift.
        }
    }

    /**
     * Récupère les informations supplémentaires d'un objet en utilisant sa clé.
     * La clé de l'objet est utilisée pour générer une clé de traduction pour l'infobulle.
     *
     * @param stack L'objet dont les informations supplémentaires sont récupérées.
     * @return Un composant texte contenant les informations supplémentaires, ou null si aucune information n'est disponible.
     */
    private static Component getAdditionalInfo(ItemStack stack) {
        // Récupère la clé de l'objet.
        String key = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString();

        // Divise la clé en mod et nom d'objet.
        String[] data = key.split(":");
        String modName = data[0];      // Le nom du mod.
        String itemName = data[1];     // Le nom de l'objet.

        // Crée la clé de traduction pour l'infobulle.
        String translationKey = modName + "." + itemName + ".tooltip";

        // Récupère le composant texte traduit correspondant à la clé.
        return Component.translatable(translationKey); // Retourne l'information supplémentaire.
    }

    /**
     * Ajoute un objet à la liste des objets ayant des informations supplémentaires dans l'infobulle.
     *
     * @param item La clé de l'objet à ajouter à la liste.
     */
    public static void addItemWithAdditionalInfo(String item) {
        // Si l'objet n'est pas déjà dans la liste, on l'ajoute.
        if (!itemsWithAdditionalInfo.contains(item)) {
            itemsWithAdditionalInfo.add(item);
        }
    }

    /**
     * Supprime un objet de la liste des objets ayant des informations supplémentaires dans l'infobulle.
     *
     * @param item La clé de l'objet à retirer de la liste.
     */
    public static void removeItemWithAdditionalInfo(String item) {
        // Si l'objet est présent dans la liste, on le retire.
        itemsWithAdditionalInfo.remove(item);
    }
}
