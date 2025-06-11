package fr.bck.tetralibs.page;


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


import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKUserdata;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe gère la pagination et l'affichage du texte à travers des pages pour le mod TetraLibs.
 * Elle permet d'ajouter des pages, de les naviguer et de personnaliser les actions sur chaque texte affiché.
 */
public class TetraPage {
    public static final class Utils {
        // ta couleur
        public static final String color = "§8";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    // Variables statiques pour gérer l'affichage des actions et des comptes de survol.
    private static String actionsList = "Actions List: [";
    private static String hoverText = "Hover Count: (";
    private static int actionsCount = 0;
    private static int hoverCount = 0;

    // Listes contenant les pages standards et personnalisées.
    private static final List<List<Component>> pages = new ArrayList<>();
    private static final List<List<Component>> customPages = new ArrayList<>();

    // Méthodes d'accès aux pages
    public static final List<List<Component>> getPages = pages;
    public static final List<List<Component>> getCustomPages = customPages;

    /**
     * Nombre total de pages dans la liste {@link pages}.
     * Cette variable statique est initialisée avec la taille actuelle de la collection {@link pages},
     * ce qui représente le nombre d'éléments (pages) stockés.
     * <p>
     * Elle est mise à jour dynamiquement chaque fois que la collection {@link pages} est modifiée,
     * assurant ainsi une valeur correcte du nombre total de pages.
     */
    public static int pagesCount = 0;

    /**
     * Ajoute une page à la liste des pages standards.
     *
     * @param content Le contenu à afficher sur la page, sous forme de liste de composants.
     */
    public static void addPage(List<Component> content) {
        if (!pages.contains(content)) {
            pages.add(content);
        }
    }

    /**
     * Ajoute une page à la liste des pages personnalisées.
     *
     * @param content Le contenu à afficher sur la page, sous forme de liste de composants.
     */
    public static void addCustomPage(List<Component> content) {
        customPages.add(content);
    }

    /**
     * Crée un composant représentant un menu de navigation avec les boutons pour la page précédente et la suivante.
     *
     * @param entity L'entité du joueur, nécessaire pour la navigation.
     * @return Le composant de navigation.
     */
    public static Component navigMenu(Entity entity) {
        Component first = TetraPage.createText("<<<<<", Component.translatable("tetra_help.previous_page").getString(), "RUN_COMMAND", "/tetrahelp remove", entity);
        String cc = "‎(⌐▀͡ ̯ʖ▀)";
        cc = cc.substring(1);
        Component separator = TetraPage.createText("--------------", cc, "COPY_TO_CLIPBOARD", cc, null);
        Component second = TetraPage.createText(">>>>>", Component.translatable("tetra_help.next_page").getString(), "RUN_COMMAND", "/tetrahelp add", entity);
        return first.copy().append(separator).append(second);
    }

    /**
     * Affiche la page actuelle pour un joueur.
     *
     * @param source La source du commandant qui envoie le message.
     * @param entity L'entité du joueur qui demande la page.
     */
    public static void display(CommandSourceStack source, Entity entity) {
        int playerPage = (int) getPlayerPage(entity);
        if (playerPage > 0 && playerPage <= pages.size()) {
            List<Component> content = pages.get(playerPage - 1);
            for (Component line : content) {
                source.sendSystemMessage(line);
            }
        } else {
            pagesList(source);
        }
    }

    /**
     * Affiche la liste des pages disponibles au joueur.
     *
     * @param source La source du commandant qui envoie le message.
     */
    public static void pagesList(CommandSourceStack source) {
        if (pages.isEmpty()) {
            source.sendSystemMessage(Component.translatable("tetra_help.no_pages_found"));
        } else {
            source.sendSystemMessage(Component.literal(Component.translatable("tetra_help.available_pages").getString().replace("<num>", ("" + pages.size()))));
        }
    }

    /**
     * Crée un composant de texte avec des événements de survol et de clic configurés.
     *
     * @param text        Le texte du composant.
     * @param hoverText   Le texte affiché au survol.
     * @param clickAction L'action exécutée au clic (ex : RUN_COMMAND, OPEN_URL).
     * @param clickValue  La valeur associée à l'action (par exemple, la commande ou l'URL).
     * @param entity      L'entité du joueur (si nécessaire pour des actions comme changer de page).
     * @return Le composant avec le texte et les actions de survol/clic.
     */
    public static Component createText(String text, String hoverText, String clickAction, String clickValue, Entity entity) {
        Style style = Style.EMPTY;
        if (hoverText != null && !hoverText.isEmpty()) {
            hoverCount++;
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverText)));
        }
        if (clickAction != null) {
            actionsList = actionsList + clickAction + "(" + clickValue + "),";
            actionsCount++;
            if (clickAction.equalsIgnoreCase("NEXT_PAGE") && entity != null) {
                if (pagesCount > (int) getPlayerPage(entity)) {
                    setPlayerPage(entity, (int) getPlayerPage(entity) + 1);
                }
            } else if (clickAction.equalsIgnoreCase("PREVIOUS_PAGE") && entity != null) {
                if (pagesCount > 0) {
                    setPlayerPage(entity, (int) getPlayerPage(entity) - 1);
                }
            } else {
                ClickEvent.Action action = getClickAction(clickAction);
                if (action != null) {
                    if (clickValue != null && !clickValue.isEmpty()) {
                        style = style.withClickEvent(new ClickEvent(action, clickValue));
                    }
                }
            }
        }
        return Component.literal(text).setStyle(style);
    }

    /**
     * Surcharge de la méthode {@link createText} pour les cas où une entité n'est pas nécessaire.
     * Cette version de la méthode permet de créer un composant de texte interactif avec des actions de survol et de clic,
     * mais sans nécessiter d'entité associée.
     *
     * @param text        Le texte affiché dans le composant.
     * @param hoverText   Le texte à afficher lors du survol du composant.
     * @param clickAction L'action à effectuer lors d'un clic (par exemple, "open_url" ou "run_command").
     * @param clickValue  La valeur associée à l'action de clic (par exemple, une URL ou une commande).
     * @return Un composant de texte avec les actions définies.
     */
    public static Component createText(String text, String hoverText, String clickAction, String clickValue) {
        return createText(text, hoverText, clickAction, clickValue, null);
    }

    /**
     * Modifie un composant de texte existant en lui ajoutant des actions de survol et de clic.
     *
     * @param component   Le composant original à modifier.
     * @param text        Le texte à ajouter.
     * @param hoverText   Le texte de survol.
     * @param clickAction L'action au clic.
     * @param clickValue  La valeur de l'action.
     * @param entity      L'entité du joueur.
     * @return Le composant modifié avec les nouvelles actions.
     */
    public static Component modifyText(Component component, String text, String hoverText, String clickAction, String clickValue, Entity entity) {
        Style style = Style.EMPTY;
        if (hoverText != null && !hoverText.isEmpty()) {
            hoverCount++;
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverText)));
        }
        if (clickAction != null) {
            actionsList = actionsList + clickAction + "(" + clickValue + "),";
            actionsCount++;
            if (clickAction.equalsIgnoreCase("NEXT_PAGE") && entity != null) {
                if (pagesCount > (int) getPlayerPage(entity)) {
                    setPlayerPage(entity, (int) getPlayerPage(entity) + 1);
                }
            } else if (clickAction.equalsIgnoreCase("PREVIOUS_PAGE") && entity != null) {
                if (pagesCount > 0) {
                    setPlayerPage(entity, (int) getPlayerPage(entity) - 1);
                }
            } else {
                ClickEvent.Action action = getClickAction(clickAction);
                if (action != null) {
                    if (clickValue != null && !clickValue.isEmpty()) {
                        style = style.withClickEvent(new ClickEvent(action, clickValue));
                    }
                }
            }
        }
        Component newText = Component.literal(text).setStyle(style);
        return component.copy().append(newText);
    }

    /**
     * Enregistre les actions et les événements de survol pour des logs.
     */
    public static void logAllList() {
        if (actionsCount > 0) {
            actionsList = actionsList.substring(0, actionsList.length() - 1);
        }
        actionsList = actionsList + "] (" + actionsCount + ")";
        hoverText = hoverText + hoverCount + ")";
    }

    /**
     * Enregistre le nombre de pages et la distinction entre pages standards et personnalisées.
     *
     * @param edited Indique si les pages ont été modifiées.
     */
    public static void logPagesCount(boolean edited) {
        String txt = "page";
        String _txt = "page";
        if ((pages.size() - customPages.size()) > 1) {
            txt += "s";
        }
        if (customPages.size() > 1) {
            _txt += "s";
        }
        if (edited) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(TetraPage.class) + "§6/§fEdited", ("default: " + (pages.size() - customPages.size()) + " " + txt + " & custom: " + customPages.size() + " " + _txt + " (" + pages.size() + ") has been recalculated!"));
        } else {
            BCKLog.info(BCKCore.TITLES_COLORS.title(TetraPage.class), ("default: " + (pages.size() - customPages.size()) + " " + txt + " & custom: " + customPages.size() + " " + _txt + " (" + pages.size() + ") has been found!"));
        }
    }

    /**
     * Enregistre le nombre de pages sans modification.
     */
    public static void logPagesCount() {
        logPagesCount(false);
    }

    /**
     * Retourne l'action de clic associée à un nom.
     *
     * @param action Le nom de l'action.
     * @return L'action correspondante.
     */
    private static ClickEvent.Action getClickAction(String action) {
        return switch (action.toUpperCase()) {
            case "RUN_COMMAND" -> ClickEvent.Action.RUN_COMMAND;
            case "OPEN_URL" -> ClickEvent.Action.OPEN_URL;
            case "SUGGEST_COMMAND" -> ClickEvent.Action.SUGGEST_COMMAND;
            case "COPY_TO_CLIPBOARD" -> ClickEvent.Action.COPY_TO_CLIPBOARD;
            default -> null;
        };
    }

    /**
     * Met à jour la page du joueur.
     *
     * @param entity L'entité du joueur.
     * @param num    Le numéro de la page à définir.
     */
    public static void setPlayerPage(Entity entity, double num) {
        BCKUserdata.data("tetra_page.page", "set", (int) num, entity.level(), entity);
    }

    /**
     * Récupère la page actuelle du joueur.
     *
     * @param entity L'entité du joueur.
     * @return Le numéro de la page actuelle du joueur.
     */
    public static double getPlayerPage(Entity entity) {
        return ((DataWrapper) BCKUserdata.data("tetra_page.page", entity.level(), entity)).getDouble();
    }
}
