package fr.bck.tetralibs.warps;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.module.ModuleIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
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

public class BCKWarpManager {
    public static final class Utils {
        // ta couleur
        public static final String color = "§1";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    private static final String TITLE = BCKCore.TITLES_COLORS.title(BCKWarpManager.class);
    private static final String CONFIG_KEY = "bck_warps";
    private static final String FILE_NAME = "warps.json";
    private static final String DELAY_KEY = "warp.teleport_delay";
    private static final Map<String, Warp> warps = new LinkedHashMap<>();
    private static File warpFile;

    private static class Warp {
        double x, y, z;
        float yaw, pitch;
        String dimension;

        Warp(double x, double y, double z, float yaw, float pitch, String dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.dimension = dimension;
        }

        JsonObject toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("x", x);
            o.addProperty("y", y);
            o.addProperty("z", z);
            o.addProperty("yaw", yaw);
            o.addProperty("pitch", pitch);
            o.addProperty("dimension", dimension);
            return o;
        }
    }

    public static String delay_key = DELAY_KEY;

    public static double teleportDelay() {
        double delaySec = 0;
        try {
            DataWrapper dw = (DataWrapper) BCKServerdata.data(DELAY_KEY);
            if (dw != null && !dw.isNull()) {
                delaySec = dw.getDouble();
            }
        } catch (Exception ignored) {
        }
        return delaySec;
    }

    private static boolean isEnabled() {
        return ModulesConfig.isEnabled(ModuleIds.BCK_WARPS);
    }

    public static void init() throws IOException {
        if (!isEnabled()) return;
        warpFile = new File(getDataPath(), FILE_NAME);
        try {
            if (!warpFile.getParentFile().exists()) warpFile.getParentFile().mkdirs();
            if (!warpFile.exists()) warpFile.createNewFile();
        } catch (IOException ignored) {
        }
        loadWarps();
    }

    public static void loadWarps() {
        if (!isEnabled()) return;
        warps.clear();
        try {
            JsonObject root = BCKUtils.FileUtil.loadJson(warpFile.getPath());
            for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                Warp w = new Warp(o.get("x").getAsDouble(), o.get("y").getAsDouble(), o.get("z").getAsDouble(), o.get("yaw").getAsFloat(), o.get("pitch").getAsFloat(), o.has("dimension") ? o.get("dimension").getAsString() : "minecraft:overworld");
                warps.put(e.getKey(), w);
            }
            // Log après chargement des warps
            BCKLog.info(TITLE, warps.size() + " warp(s) loaded");
        } catch (IOException ex) {
            BCKLog.warn(TITLE, "Unable to load warps: ", ex);
        }
    }

    public static void saveWarps() {
        if (!isEnabled()) return;
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Warp> e : warps.entrySet()) {
            root.add(e.getKey(), e.getValue().toJson());
        }
        try {
            if (warpFile != null) BCKUtils.FileUtil.saveJsonObjectToFile(root, warpFile.getPath());
            else BCKLog.error(TITLE, "Failed to save: §8warpFile §ris §cnull");
        } catch (IOException ex) {
            BCKLog.error(TITLE, "Error saving warps: ", ex);
        }
    }

    public static void setWarp(String name, Player player) {
        if (!isEnabled()) return;
        Warp w = new Warp(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.level().dimension().location().toString());
        warps.put(name, w);
        saveWarps();
        // Envoie un message de succès de création de warp, avec détails
        String template = Component.translatable("command.warps.created_successfully").getString();
        String temp = Component.translatable("command.warps.created_successfully.more").getString();
        temp = temp.replace("<x>", String.valueOf(w.x)).replace("<y>", String.valueOf(w.y)).replace("<z>", String.valueOf(w.z)).replace("<yaw>", String.valueOf(w.yaw)).replace("<pitch>", String.valueOf(w.pitch)).replace("<dimension>", w.dimension);
        String message = template.replace("<name>", name).replace("<more>", BCKUtils.TextUtil.toStyled(temp).getString());
        player.displayClientMessage(BCKUtils.TextUtil.toStyled(message), false);
    }

    public static void setWarp(String name, double x, double y, double z, float yrot, float xrot, ResourceKey key, boolean callback) {
        if (!isEnabled()) return;
        Warp w = new Warp(x, y, z, yrot, xrot, key.toString());
        warps.put(name, w);
        saveWarps();
        if (callback)
            BCKLog.info("WarpManager", "Warp created at x: " + w.x + " y: " + w.y + " z: " + w.z + " yaw: " + w.yaw + " pitch: " + w.pitch + " dimension: " + w.dimension);
    }

    public static void setWarp(String name, double x, double y, double z, float yrot, float xrot, ResourceKey key) {
        setWarp(name, x, y, z, yrot, xrot, key, false);
    }

    public static boolean removeWarp(String name) {
        if (!isEnabled()) return false;
        if (warps.remove(name) != null) {
            saveWarps();
            return true;
        }
        return false;
    }

    public static Set<String> listWarps() {
        if (!isEnabled()) return Collections.emptySet();
        return Collections.unmodifiableSet(warps.keySet());
    }

    /**
     * Retourne les noms de tous les warps sous forme de tableau de chaînes.
     * Utile pour récupérer directement un String[].
     *
     * @return tableau contenant tous les noms de warp
     */
    public static String[] listWarpsArray() {
        Set<String> set = listWarps();
        return set.toArray(new String[0]);
    }

    /**
     * Téléporte le joueur vers un warp donné, après un délai configuré.
     * Annule si le joueur se déplace avant la fin du délai.
     *
     * @param name   Nom du warp
     * @param player Joueur à téléporter
     * @return true si le warp existe
     */
    public static boolean teleportToWarp(String name, Player player) {
        if (!isEnabled()) {
            return false;
        }
        Warp w = warps.get(name);
        if (w == null) {
            return false;
        }
        // Lecture du délai configuré
        double delaySec = 0;
        try {
            DataWrapper dw = (DataWrapper) BCKServerdata.data(DELAY_KEY);
            if (dw != null && !dw.isNull()) {
                delaySec = dw.getDouble();
            }
        } catch (Exception ignored) {
        }
        final double delay = delaySec;
        // Position initiale
        final double startX = player.getX();
        final double startY = player.getY();
        final double startZ = player.getZ();
        // Action de téléportation
        Runnable action = () -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            Level lvl = null;
            if (server != null) {
                ResourceLocation rl = ResourceLocation.tryParse(w.dimension);
                if (rl != null) {
                    ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, rl);
                    lvl = server.getLevel(key);
                }
            }
            if (lvl == null) {
                lvl = player.level();
            }
            player.teleportTo(w.x, w.y, w.z);
            player.setYRot(w.yaw);
            player.setXRot(w.pitch);
            player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warp.teleport_successfully").getString().replace("<name>", name)), false);
        };
        if (delay > 0) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep((long) (delay * 1000L));
                } catch (InterruptedException ignoredInner) {
                }
                if (Math.abs(player.getX() - startX) < 0.1 && Math.abs(player.getY() - startY) < 0.1 && Math.abs(player.getZ() - startZ) < 0.1) {
                    action.run();
                } else {
                    player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warp.cancelled").getString()), false);
                }
            });
            t.setDaemon(true);
            t.start();
        } else {
            action.run();
        }
        return true;
    }

    private static String getDataPath() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        String base = FMLPaths.GAMEDIR.get().toString();
        String world = BCKUtils.ServerUtil.getWorldFolderName();
        if (server == null || server.isSingleplayer()) {
            return base + "/saves/" + world + "/serverdata/";
        }
        return base + "/" + world + "/serverdata/";
    }
}