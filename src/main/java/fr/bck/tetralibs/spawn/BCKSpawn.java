package fr.bck.tetralibs.spawn;


import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
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

public class BCKSpawn {
    public static final class Utils {
        // ta couleur
        public static final String color = "§a";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static boolean setSpawn(String dimension, double x, double y, double z, float pitch, float yaw, boolean callback) {
        try {
            BCKServerdata.data("spawn.dimension", "set", dimension);
            BCKServerdata.data("spawn.x", "set", x);
            BCKServerdata.data("spawn.y", "set", y);
            BCKServerdata.data("spawn.z", "set", z);
            BCKServerdata.data("spawn.pitch", "set", pitch);
            BCKServerdata.data("spawn.yaw", "set", yaw);

            if (callback)
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Spawn has been set to " + x + " " + y + " " + z);
            return true;
        } catch (Exception e) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Error retrieving spawn data: ", e);
            return false;
        }
    }

    public static boolean setSpawn(String dimension, double x, double y, double z, float pitch, float yaw) {
        return setSpawn(dimension, x, y, z, pitch, yaw, false);
    }

    public static void spawn(Entity entity) {
        if (entity == null) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "The entity cannot be null.");
            return;
        }
        // Récupération des données nécessaires pour le spawn
        String dimension;
        double x, y, z, pitch, yaw;

        try {
            dimension = ((DataWrapper) BCKServerdata.data("spawn.dimension")).getString();
            x = ((DataWrapper) BCKServerdata.data("spawn.x")).getDouble();
            y = ((DataWrapper) BCKServerdata.data("spawn.y")).getDouble();
            z = ((DataWrapper) BCKServerdata.data("spawn.z")).getDouble();
            pitch = ((DataWrapper) BCKServerdata.data("spawn.pitch")).getDouble();
            yaw = ((DataWrapper) BCKServerdata.data("spawn.yaw")).getDouble();
        } catch (Exception e) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Error retrieving spawn data: ", e);
            return;
        }

        // Vérification des données
        if (dimension == null || dimension.isEmpty()) {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "The spawn dimension is not set or invalid.");
            return;
        }

        if (x == 0 && y == 0 && z == 0) {
            BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "The spawn coordinates are set to (0, 0, 0). Is this intentional?");
        }
        if (entity instanceof ServerPlayer _player) {
            if (((DataWrapper) BCKServerdata.data("spawn.teleport_cooldown")).getDouble() > 0)
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.spawn.teleport_in", Component.literal(String.valueOf(((DataWrapper) BCKServerdata.data("spawn.teleport_cooldown")).getDouble())))), false);

            // Sauvegarde de la position actuelle pour vérifier si le joueur bouge
            Vec3 initialPosition = _player.position();

            MinecraftServer server = _player.getServer();
            if (server != null) {
                long delayInMs = (long) (((DataWrapper) BCKServerdata.data("spawn.teleport_cooldown")).getDouble() * 1000);
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    // Vérifie si le joueur est toujours à la position initiale
                    if (_player.position().equals(initialPosition)) {
                        // Vérification si le joueur est dans la dimension cible
                        if (entity.level().dimension().location().toString().equals(dimension)) {
                            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "The " + entity.getName().getString() + " is already in the spawn dimension.");
                        } else {
                            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Teleporting " + entity.getName().getString() + " to spawn dimension: " + dimension);
                        }
                        // Téléportation
                        boolean success = teleportPlayer(_player, dimension, x, y, z, (float) pitch, (float) yaw);
                        if (success) {
                            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKSpawn.class), _player.getName().getString() + " has been teleported to spawn");
                            BCKLichWhisper.send(BCKUtils.TextUtil.toStyled(Component.translatable("lich_whisper.spawn", Component.literal(_player.getName().getString()))), 3, false);
                            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.spawn.teleport", Component.literal(_player.getName().getString()))), false);
                        } else {
                            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Failed to teleport " + entity.getName().getString() + " to spawn.");
                            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.spawn.teleport_error", Component.literal(_player.getName().getString()))), false);
                        }
                    } else {
                        // Annule la téléportation si le joueur a bougé
                        BCKLog.info(BCKCore.TITLES_COLORS.title(BCKSpawn.class), _player.getName().getString() + " moved. Teleportation canceled.");
                        _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.spawn.teleport_canceled")), false);
                    }
                }, delayInMs, java.util.concurrent.TimeUnit.MILLISECONDS); // Délai de 5 secondes
            }
            return; // Fin de la méthode
        }
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
     * @return true si la téléportation a réussi, false sinon.
     */
    private static boolean teleportPlayer(Player player, String dimension, double x, double y, double z, float pitch, float yaw) {
        if (player instanceof ServerPlayer serverPlayer) {
            try {
                if (serverPlayer.level().dimension().location().toString().equals(dimension)) {
                    // Téléportation dans la même dimension
                    serverPlayer.teleportTo(serverPlayer.serverLevel(), x, y, z, yaw, pitch);
                    return true; // Téléportation réussie
                } else {
                    // Téléportation inter-dimensionnelle
                    ServerLevel targetDimension = Objects.requireNonNull(serverPlayer.getServer()).getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)));

                    if (targetDimension != null) {
                        serverPlayer.teleportTo(targetDimension, x, y, z, yaw, pitch);
                        return true; // Téléportation réussie
                    } else {
                        BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Target dimension not found: " + dimension);
                    }
                }
            } catch (Exception e) {
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Error during teleportation: ", e);
            }
        } else {
            BCKLog.error(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Entity is not a ServerPlayer or is null.");
        }
        return false; // Téléportation échouée
    }
}
