package fr.bck.tetralibs.core;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.bck.tetralibs.permissions.BCKPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;



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
 * Classe fournissant des méthodes pour générer des suggestions de commandes pour les permissions.
 * Ces suggestions sont utilisées dans un contexte de commande Minecraft via Brigadier.
 */
public class BCKSuggestionProvider {

    public static final class Utils {
        // ta couleur
        public static final String color = "§7";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    /**
     * Génère des suggestions de permissions basées sur le type et l'entité du joueur.
     *
     * @param context Le contexte de la commande.
     * @param builder Le générateur de suggestions.
     * @param type    Le type de suggestions (par exemple "player_permissions").
     * @param player  L'entité du joueur (peut être null).
     * @return Une instance de CompletableFuture contenant les suggestions générées.
     */
    private static CompletableFuture<Suggestions> suggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, String type, Entity player) {
        String[] datas = BCKPermissions.getServerPermissions();

        if (Objects.equals(type, "player_permissions") && player != null) {
            datas = BCKPermissions.getEntityPermissions(player);
        } else if (Objects.equals(type, "server_permissions_without_player_permissions") && player != null) {
            String[] playerPermissions = BCKPermissions.getEntityPermissions(player);
            List<String> playerPermissionsList = Arrays.asList(playerPermissions);

            datas = Arrays.stream(datas).filter(permission -> !playerPermissionsList.contains(permission)).toArray(String[]::new);
        } else if (Objects.equals(type, "player_permissions_without_player_permissions") && player != null) {
            String[] playerPermissions = BCKPermissions.getEntityPermissions(player);
            List<String> serverPermissionsList = Arrays.asList(datas);

            datas = Arrays.stream(playerPermissions).filter(permission -> !serverPermissionsList.contains(permission)).toArray(String[]::new);
        }

        // Ajoute chaque permission dans le générateur de suggestions
        for (String permission : datas) {
            builder.suggest(permission);
        }

        return builder.buildFuture();
    }

    /**
     * Génère des suggestions basées sur une liste donnée.
     *
     * @param context Le contexte de la commande.
     * @param builder Le générateur de suggestions.
     * @param list    La liste des suggestions à proposer.
     * @return Une instance de CompletableFuture contenant les suggestions générées.
     */
    private static CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, String[] list) {
        // Ajoute chaque élément de la liste comme suggestion
        for (String data : list) {
            builder.suggest(data);
        }

        return builder.buildFuture();
    }

    /**
     * Crée un fournisseur de suggestions basé sur un type de permissions et un joueur.
     *
     * @param type   Le type de suggestion à générer (par exemple "player_permissions").
     * @param player L'entité du joueur (peut être null).
     * @return Un fournisseur de suggestions.
     */
    public static SuggestionProvider<CommandSourceStack> createSuggestionProvider(String type, Entity player) {
        return (context, builder) -> suggestions(context, builder, type, player);
    }

    /**
     * Crée un fournisseur de suggestions basé sur une liste spécifique de données.
     *
     * @param list La liste des suggestions à fournir.
     * @return Un fournisseur de suggestions.
     */
    public static SuggestionProvider<CommandSourceStack> createSuggestionProviderFromList(String[] list) {
        return (context, builder) -> listSuggestions(context, builder, list);
    }

    /**
     * Crée un fournisseur de suggestions à partir d'un tableau de suggestions.
     *
     * @param suggestions Un tableau de chaînes de caractères contenant les suggestions à offrir.
     * @return Un fournisseur de suggestions.
     */
    public static SuggestionProvider<CommandSourceStack> suggest(String[] suggestions) {
        return (CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) -> {
            // Affiche les suggestions dans la console pour le débogage
            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKSuggestionProvider.class), "Generating patterned suggestions for: " + Arrays.toString(suggestions) + " / " + suggestions.length);
            for (String suggestion : suggestions) {
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKSuggestionProvider.class), "Suggesting: " + suggestion);
                builder.suggest(suggestion);
            }
            return builder.buildFuture();
        };
    }
}
