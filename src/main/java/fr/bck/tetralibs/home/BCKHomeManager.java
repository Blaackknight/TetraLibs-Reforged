package fr.bck.tetralibs.home;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.data.BCKUserdata;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;



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

public class BCKHomeManager {
    public static final class Utils {
        // ta couleur
        public static final String color = "§d";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    private static int maxHomes = ((DataWrapper) BCKServerdata.data("homes.max_count")).getInt();

    /**
     * Définit un home pour un joueur en ajoutant un nouvel objet JSON dans le JsonArray,
     * incluant les coordonnées, la dimension, le pitch et le yaw.
     *
     * @param player    Le joueur pour lequel définir le home.
     * @param name      Le nom du home.
     * @param dimension La dimension où se trouve le home.
     * @param x         La coordonnée X du home.
     * @param y         La coordonnée Y du home.
     * @param z         La coordonnée Z du home.
     * @param pitch     L'angle de vue vertical du joueur.
     * @param yaw       L'angle de vue horizontal du joueur.
     */
    public static void setHome(Player player, String name, String dimension, double x, double y, double z, float pitch, float yaw) {
        //SuperLog.info("TetraLibs/BCKHome/Sethome", "Triggered");
        // Vérifie que le joueur n'est pas null pour éviter des NullPointerException
        if (player == null) {
            throw new IllegalArgumentException("[BCKHome] The player cannot be null.");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("[BCKHome] The home name cannot be null or empty.");
        }
        if (dimension == null || dimension.isEmpty()) {
            throw new IllegalArgumentException("[BCKHome] The dimension cannot be null or empty.");
        }

        try {
            JsonArray array = homes(player);

            JsonObject newHome = new JsonObject();
            newHome.addProperty("name", name);
            newHome.addProperty("dimension", dimension);
            newHome.addProperty("x", x);
            newHome.addProperty("y", y);
            newHome.addProperty("z", z);
            newHome.addProperty("pitch", pitch);
            newHome.addProperty("yaw", yaw);

            array.add(newHome);
            BCKUserdata.data("homes", "set", array, player.level(), player);

            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), "Successfully added home: " + name + " for player: " + player.getName().getString());
        } catch (Exception e) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), "Error while adding home: ", e);
        }
    }

    /**
     * Supprime un home spécifique d'un joueur en fonction de son nom.
     *
     * @param player Le joueur dont le home sera supprimé.
     * @param name   Le nom du home à supprimer.
     */
    public static void delHome(Player player, String name) {
        // Vérifie que le joueur n'est pas null pour éviter des NullPointerException
        if (player == null) {
            throw new IllegalArgumentException("[Tetralibs/BCKHome] The player cannot be null.");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("[Tetralibs/BCKHome] The home name cannot be null or empty.");
        }

        // Récupération de la liste actuelle des homes pour le joueur
        JsonArray array = homes(player);

        // Création d'un nouveau tableau pour stocker les homes restants
        JsonArray updatedArray = new JsonArray();

        // Parcourt le tableau existant pour copier tous les homes sauf celui à supprimer
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).isJsonObject()) {
                JsonObject home = array.get(i).getAsJsonObject();
                // Vérifie si le home correspond au nom à supprimer
                if (home.has("name") && home.get("name").getAsString().equals(name)) {
                    continue; // Ignore cet objet (ne l'ajoute pas au nouveau tableau)
                }
                updatedArray.add(home); // Ajoute les autres homes
            }
        }

        BCKLog.info(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), name + " has been removed from " + player.getName().getString());

        // Mise à jour des données de l'utilisateur avec le tableau modifié
        BCKUserdata.data("homes", "set", updatedArray, player.level(), (Entity) player);
    }

    /**
     * Téléporte un joueur vers un home spécifique avec un délai de 5 secondes pendant lequel il ne doit pas bouger.
     *
     * @param player Le joueur à téléporter.
     * @param name   Le nom du home vers lequel téléporter le joueur.
     */
    public static void home(Entity player, String name) {
        if (player == null) {
            throw new IllegalArgumentException("[Tetralibs/BCKHome] The player cannot be null.");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("[Tetralibs/BCKHome] The home name cannot be null or empty.");
        }
        if (player instanceof ServerPlayer _player && !_player.level().isClientSide()) {

            // Récupère la liste des homes
            JsonArray array = homes(_player);

            // Recherche le home correspondant
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    JsonObject home = array.get(i).getAsJsonObject();

                    if (home.has("name") && home.get("name").getAsString().equals(name)) {
                        // Vérifie si toutes les clés nécessaires existent
                        if (!home.has("dimension") || !home.has("x") || !home.has("y") || !home.has("z") || !home.has("pitch") || !home.has("yaw")) {
                            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), "Incomplete data for home: " + name);
                            return;
                        }

                        // Récupération des données du home
                        String dimension = home.get("dimension").getAsString();
                        double x = home.get("x").getAsDouble();
                        double y = home.get("y").getAsDouble();
                        double z = home.get("z").getAsDouble();
                        float pitch = home.get("pitch").getAsFloat();
                        float yaw = home.get("yaw").getAsFloat();

                        // Sauvegarde de la position actuelle pour vérifier si le joueur bouge
                        Vec3 initialPosition = _player.position();

                        // Envoi d'un message au joueur
                        if (((DataWrapper) BCKServerdata.data("homes.teleport_cooldown")).getDouble() > 0)
                            _player.displayClientMessage(Component.literal(((Component.translatable("command.home.teleport_in").getString()).replace("<count>", "" + ((DataWrapper) BCKServerdata.data("homes.teleport_cooldown")).getDouble()))), false);

                        // Planifie la téléportation après un délai
                        MinecraftServer server = _player.getServer();
                        if (server != null) {
                            long delayInMs = (long) (((DataWrapper) BCKServerdata.data("homes.teleport_cooldown")).getDouble() * 1000);
                            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                                // Vérifie si le joueur est toujours à la position initiale
                                if (_player.position().equals(initialPosition)) {
                                    // Téléportation
                                    teleportPlayer(_player, dimension, x, y, z, pitch, yaw);
                                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), _player.getName().getString() + " has been teleported to home: " + name);
                                    BCKLichWhisper.send(Component.literal(((Component.translatable("lich_whisper.home").getString()).replace("<home>", name)).replace("<player>", _player.getName().getString())), 3, false);
                                    _player.displayClientMessage(Component.literal(((Component.translatable("command.home.teleport").getString()).replace("<name>", name))), false);
                                } else {
                                    // Annule la téléportation si le joueur a bougé
                                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), _player.getName().getString() + " moved. Teleportation canceled.");
                                    _player.displayClientMessage(Component.literal(Component.translatable("command.home.teleport_canceled").getString()), false);
                                }
                            }, delayInMs, java.util.concurrent.TimeUnit.MILLISECONDS); // Délai de 5 secondes
                        }
                        return; // Fin de la méthode
                    }
                }
            }

            // Si le home n'est pas trouvé
            BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), "Home '" + name + "' not found for " + player.getName().getString());
            _player.displayClientMessage(Component.literal(Component.translatable("command.home.not_found").getString().replace("<name>", name)), false);
        }
    }

    public static JsonArray homes(Player player) {
        JsonArray array = ((DataWrapper) BCKUserdata.data("homes", player.level(), (Entity) player)).getJsonArray();
        if (array == null) array = new JsonArray();
        return array;
    }

    public static String[] homesAsStringArray(Player player) {
        // Récupère la liste des homes
        JsonArray homesArray = homes(player);

        // Prépare une liste pour stocker les noms des homes
        List<String> homeNames = new ArrayList<>();
        // Parcourt chaque objet dans le tableau JSON
        for (int i = 0; i < homesArray.size(); i++) {
            JsonObject home = homesArray.get(i).getAsJsonObject();
            if (home.has("name")) {
                homeNames.add(home.get("name").getAsString());
            }
        }

        // Convertit la liste en tableau et retourne
        return homeNames.toArray(new String[0]);
    }

    public static int count(Entity entity) {
        if (entity instanceof Player _player && !_player.level().isClientSide()) {
            return homes(_player).size();
        }
        return 0;
    }

    public static boolean has(Entity entity, String name) {
        if (entity instanceof Player _player && !_player.level().isClientSide()) {
            // Récupération du JsonArray des homes
            JsonArray homes = homes(_player); // Remplacez par votre logique pour obtenir les données
            // Vérifie si un JsonObject dans le tableau contient le nom donné
            for (int i = 0; i < homes.size(); i++) {
                if (homes.get(i).isJsonObject()) {
                    // Récupération de l'objet JSON
                    JsonObject home = homes.get(i).getAsJsonObject();
                    // Vérifie si le champ "name" correspond au nom recherché
                    if (home.has("name") && home.get("name").getAsString().equals(name)) {
                        //SuperLog.info("TetraLibs/BCKHome", "found: " + home.get("name").getAsString() + " -> " + name);
                        return true;
                    }
                }
            }
        }
        return false; // Aucun home correspondant trouvé
    }

    /**
     * Téléporte un joueur aux coordonnées spécifiées dans une dimension donnée,
     * y compris inter-dimensionnellement.
     *
     * @param player    Le joueur à téléporter.
     * @param dimension La dimension cible.
     * @param x         La coordonnée X.
     * @param y         La coordonnée Y.
     * @param z         La coordonnée Z.
     * @param pitch     L'angle de vue vertical.
     * @param yaw       L'angle de vue horizontal.
     */
    private static void teleportPlayer(Player player, String dimension, double x, double y, double z, float pitch, float yaw) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.level().dimension().location().toString().equals(dimension)) {
                // Téléportation dans la même dimension
                serverPlayer.teleportTo(serverPlayer.serverLevel(), x, y, z, yaw, pitch);
            } else {
                // Téléportation inter-dimensionnelle
                ServerLevel targetDimension = Objects.requireNonNull(serverPlayer.getServer()).getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)));

                if (targetDimension != null) {
                    serverPlayer.teleportTo(targetDimension, x, y, z, yaw, pitch);
                } else {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), "Target dimension not found: " + dimension);
                }
            }
        }
    }

    public static int getMaxHomes() {
        return maxHomes;
    }

    public static void setMaxHomes(int maxHomes) {
        BCKHomeManager.maxHomes = maxHomes;
    }
}
