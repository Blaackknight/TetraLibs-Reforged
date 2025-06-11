package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.lich.BCKLichClear;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class BCKLichClearModule extends CoreDependentModule {
    private static final Map<Level, Integer> tickCounters = new LinkedHashMap<>();
    private static final Map<Level, Integer> secondCounters = new LinkedHashMap<>();
    private static final Map<Level, Integer> countdownTracker = new LinkedHashMap<>();  // Pour éviter les messages répétés
    private static final Map<Level, Integer> messageTickDelays = new LinkedHashMap<>(); // Gérer les délais entre messages
    private static final Object LOCK = new Object();
    private static final int[] count = new int[]{0};

    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_LICH_CLEAR;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        BCKLichClear.init();
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_SERVERDATA);
    }

    @Override
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        super.onWorldTick(event);
        if (event.phase == TickEvent.Phase.END) {
            if (ServerLifecycleHooks.getCurrentServer() == null || !(event.level instanceof ServerLevel world)) {
                return;
            }

            tickCounters.put(world, tickCounters.getOrDefault(world, 0) + 1);
            messageTickDelays.put(world, messageTickDelays.getOrDefault(world, 0) + 1);

            if (tickCounters.get(world) >= 20) {  // 20 ticks = 1 seconde
                tickCounters.put(world, 0);  // Réinitialise le compteur de ticks
                if (((DataWrapper) BCKServerdata.data("server.lich_clear.cooldown")).isNull() || ((DataWrapper) BCKServerdata.data("server.lich_clear.max_duration")).isNull()) {
                    return;
                }
                int currentSeconds = secondCounters.getOrDefault(world, 0) + 1;
                secondCounters.put(world, currentSeconds);

                int cooldownInSeconds = ((DataWrapper) BCKServerdata.data("server.lich_clear.cooldown")).getInt();

                if (currentSeconds >= cooldownInSeconds - 5 && currentSeconds < cooldownInSeconds) {
                    int remainingSeconds = cooldownInSeconds - currentSeconds;
                    if (countdownTracker.getOrDefault(world, -1) != remainingSeconds && messageTickDelays.get(world) >= 20) {
                        countdownTracker.put(world, remainingSeconds);
                        messageTickDelays.put(world, 0); // Réinitialiser le délai après l'envoi du message
                        if (remainingSeconds <= 5) {
                            broadcastCountdown(world, remainingSeconds);
                        }
                    }
                } else if (currentSeconds == cooldownInSeconds - (cooldownInSeconds / 4) && messageTickDelays.get(world) >= 40) {
                    broadcastMidWarning(world, cooldownInSeconds / 4);
                    messageTickDelays.put(world, 0);  // Réinitialise après l'avertissement intermédiaire
                } else if (currentSeconds >= cooldownInSeconds) {
                    secondCounters.put(world, 0);  // Réinitialise le compteur de secondes
                    countdownTracker.put(world, -1);  // Réinitialise le suivi du décompte
                    messageTickDelays.put(world, 0);  // Réinitialise les délais
                    actionEveryCooldown(world, cooldownInSeconds);
                }
            }
        }
    }

    private static void broadcastCountdown(ServerLevel world, int remainingSeconds) {
        if (remainingSeconds > 0) {  // Évite d’envoyer un message si le temps est incorrect
            String countdownMessage = Component.translatable("tetra_libs.events.lich_clear.countdown").getString().replace("<time>", String.valueOf(remainingSeconds)).replace("<world>", getWorldName(world));

            // Envoie le message uniquement aux joueurs dans ce monde
            for (Player player : world.players()) {
                player.sendSystemMessage(Component.literal(countdownMessage));
            }
        }
    }

    private static void broadcastMidWarning(ServerLevel world, int quarterTime) {
        String midWarningMessage = Component.translatable("tetra_libs.events.lich_clear.mid_warning").getString().replace("<time>", String.valueOf(quarterTime)).replace("<world>", getWorldName(world));

        // Envoie le message uniquement aux joueurs dans ce monde
        for (Player player : world.players()) {
            player.sendSystemMessage(Component.literal(midWarningMessage));
        }
    }

    private static void actionEveryCooldown(ServerLevel world, int cooldown) {
        int maxDuration = ((DataWrapper) BCKServerdata.data("server.lich_clear.max_duration")).getInt();
        count[0] = 0;

        Map<String, Integer> clearedItemsByWorld = new LinkedHashMap<>();
        synchronized (LOCK) {
            if (((DataWrapper) BCKServerdata.data("server.lich_clear.all_worlds")).getBoolean()) {
                ServerLifecycleHooks.getCurrentServer().getAllLevels().forEach(serverLevel -> {
                    int cleared = BCKUtils.ServerUtil.killOldItems(serverLevel, maxDuration);
                    count[0] += cleared;
                    clearedItemsByWorld.put(serverLevel.dimension().location().toString(), cleared);
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichClear.class), "Cleared items in: " + serverLevel.dimension().location() + " -> " + cleared);
                });
            } else {
                for (String worldName : ((DataWrapper) BCKServerdata.data("server.lich_clear.worlds_list")).getStringArray()) {
                    ServerLevel serverLevel = getServerLevel(worldName);
                    if (serverLevel != null) {
                        int cleared = BCKUtils.ServerUtil.killOldItems(serverLevel, maxDuration);
                        count[0] += cleared;
                        clearedItemsByWorld.put(worldName, cleared);
                        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichClear.class), "Cleared items in: " + worldName + " -> " + cleared);
                    }
                }
            }
        }

        broadcastSuccessMessage(world, clearedItemsByWorld, cooldown);
    }

    private static void broadcastSuccessMessage(ServerLevel world, Map<String, Integer> clearedItemsByWorld, int cooldown) {
        StringBuilder hoverTextBuilder = new StringBuilder();
        clearedItemsByWorld.forEach((worldName, itemCount) -> {
            hoverTextBuilder.append(Component.translatable("tetra_libs.events.lich_clear.world_detail").getString().replace("<world>", worldName).replace("<count>", String.valueOf(itemCount))).append("\n");
        });

        Component countComponent = Component.literal("§d" + count[0]).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverTextBuilder.toString().trim()))));

        String rawMessage = Component.translatable("tetra_libs.events.lich_clear.success").getString().replace("<time>", String.valueOf(cooldown)).replace("<world>", getWorldName(world));

        String[] messageParts = rawMessage.split("<count>", 2);
        Component successMessage = Component.empty().append(Component.literal(messageParts[0])).append(countComponent).append(Component.literal(messageParts.length > 1 ? messageParts[1] : ""));

        // Envoie le message uniquement aux joueurs dans ce monde
        for (Player player : world.players()) {
            player.sendSystemMessage(successMessage);
        }
    }

    private static String getWorldName(ServerLevel world) {
        return world.dimension().location().toString(); // Retourne un nom de type "minecraft:overworld"
    }

    public static ServerLevel getServerLevel(String dimensionName) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        try {
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionName));
            return server.getLevel(dimensionKey);
        } catch (Exception e) {
            //BCKLog.throwable("TetraLibs/LichClear", "Invalid dimension: " + dimensionName);
            return null;
        }
    }
}
