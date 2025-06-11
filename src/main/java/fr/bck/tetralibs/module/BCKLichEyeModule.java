package fr.bck.tetralibs.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.core.GameModeHandler;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.lich.BCKLichEye;
import fr.bck.tetralibs.modules.lich.BCKLichEyeEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class BCKLichEyeModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_LICH_EYE;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        MinecraftForge.EVENT_BUS.register(BCKLichEyeEventHandler.class);
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichEye.class), "§aHandlers registered");
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_SERVERDATA);
    }

    @Override
    public void onServerStarting(ServerStartingEvent event) {
        super.onServerStarting(event);
        BCKLichEye.deploy();
    }

    @Override
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        super.onBlockPlace(event);
        containersAdded(null, event.getLevel(), event.getState(), event.getEntity(), event.getPos());
        blocksAdded(null, event.getLevel(), event.getState(), event.getEntity(), event.getPos());
    }

    @Override
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        super.onBlockBreak(event);
        containersRemoved(null, event.getLevel(), event.getState(), event.getPlayer(), event.getPos());
        blocksRemoved(null, event.getLevel(), event.getState(), event.getPlayer(), event.getPos());
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        super.onRightClickBlock(event);
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        executeBis(event, event.getLevel(), event.getLevel().getBlockState(event.getPos()), event.getEntity(), event.getPos());
    }

    @Override
    public void onPortalCreate(BlockEvent.PortalSpawnEvent event) {
        super.onPortalCreate(event);
        LevelAccessor world = event.getLevel();
        BlockPos pos = event.getPos();

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        portalCreated(event, world, x, y, z);
    }

    private static void executeBis(@Nullable Event event, LevelAccessor world, BlockState blockstate, Entity entity, BlockPos pos) {
        if (entity == null) return;

        // Vérifie si le code est exécuté côté serveur
        if (world.isClientSide()) {
            //SuperLog.info("LichEye", "L'événement est capturé côté client. Aucun traitement n'est effectué.");
            return;
        }

        GameModeHandler.executeServerOnlyCode(() -> {
            if ((entity instanceof Player || entity instanceof ServerPlayer) && ((DataWrapper) BCKServerdata.data("lich_eye.containers_logger")).getBoolean()) {
                containersOppened(world, blockstate, entity, pos);
            }
        });
    }

    private static void portalCreated(@Nullable Event event, LevelAccessor world, int x, int y, int z) {
        // Récupérer l'heure actuelle
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = now.format(formatter);

        String dimension = null;
        // Récupérer la dimension
        if (world instanceof ServerLevel serverLevel) {
            dimension = serverLevel.dimension().location().toString();
        }

        // Initialisation des structures JSON si nécessaires
        BCKLichEye.portalsInit();

        JsonArray array = ((DataWrapper) BCKLichEye.data("events.portals")).getJsonArray();

        // Construire l'objet JSON pour le log
        JsonObject portalLog = new JsonObject();
        portalLog.addProperty("dimension", dimension);
        portalLog.addProperty("at", formattedTime);

        array = BCKLichEye.updateJsonObjectInArray(array, world, x, y, z, "datas", portalLog);

        // Sauvegarder le tableau modifié
        BCKLichEye.data("events.portals", "set", array);
    }

    private static void containersAdded(@Nullable Event event, LevelAccessor world, BlockState blockstate, Entity entity, BlockPos pos) {
        if (entity == null) return;
        GameModeHandler.executeServerOnlyCode(() -> {
            if ((entity instanceof Player || entity instanceof ServerPlayer) && ((DataWrapper) BCKServerdata.data("lich_eye.containers_logger")).getBoolean()) {
                Block block = blockstate.getBlock();
                if (block instanceof BaseEntityBlock) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof Container) {
                        BlockEntityType<?> type = blockEntity.getType();

                        // Récupérer un nom lisible pour le type
                        String modId = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)).getNamespace();
                        String typeName = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)).getPath();

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedTime = now.format(formatter);

                        // Initialisation des structures JSON si nécessaires
                        BCKLichEye.containersInit(modId, typeName);

                        JsonArray array = ((DataWrapper) BCKLichEye.data("containers." + modId + "." + typeName)).getJsonArray();

                        //SuperLog.info("LichEye/Add", array);
                        // Créer les données à insérer ou mettre à jour
                        JsonObject data = new JsonObject();
                        data.addProperty("author", entity.getStringUUID());
                        data.addProperty("at", formattedTime);

                        // Mettre à jour ou ajouter les données avec `updateJsonObjectInArray`
                        array = BCKLichEye.updateJsonObjectInArray(array, world, pos.getX(), pos.getY(), pos.getZ(), "placedBy", data);

                        // Sauvegarder le tableau modifié
                        BCKLichEye.data("containers." + modId + "." + typeName, "set", array);

                        //SuperLog.info("LichEye", "Container ajouté ou mis à jour : " + typeName + " aux coordonnées (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ").");
                    }
                }
            }
        });
    }

    private static void blocksAdded(@Nullable Event event, LevelAccessor world, BlockState blockstate, Entity entity, BlockPos pos) {
        if (entity == null) return;

        GameModeHandler.executeServerOnlyCode(() -> {
            if ((entity instanceof Player || entity instanceof ServerPlayer) && !((DataWrapper) BCKServerdata.data("lich_eye.blocks_logger")).getJsonArray().isEmpty()) {

                Block block = blockstate.getBlock();
                JsonArray blocks = ((DataWrapper) BCKServerdata.data("lich_eye.blocks_logger")).getJsonArray();

                // Vérifie si le bloc actuel est dans le JsonArray des blocs surveillés
                boolean isBlockLogged = false;
                for (JsonElement element : blocks) {
                    if (element.isJsonPrimitive() && element.getAsString().equals(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())) {
                        isBlockLogged = true;
                        break;
                    }
                }

                if (!isBlockLogged) {
                    return; // Si le bloc n'est pas surveillé, quitte la méthode
                }

                String modId = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getNamespace();
                String blockName = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = now.format(formatter);

                // Initialisation des structures JSON si nécessaires
                BCKLichEye.blocksInit(modId, blockName);

                JsonArray array = ((DataWrapper) BCKLichEye.data("events.blocks." + modId + "." + blockName)).getJsonArray();

                // Créer les données à insérer ou mettre à jour
                JsonObject data = new JsonObject();
                data.addProperty("player", entity.getStringUUID());
                data.addProperty("at", formattedTime);

                // Mettre à jour ou ajouter les données avec `updateJsonObjectInArray`
                array = BCKLichEye.updateJsonObjectInArray(array, world, pos.getX(), pos.getY(), pos.getZ(), "placedBy", data);

                // Sauvegarder le tableau modifié
                BCKLichEye.data("events.blocks." + modId + "." + blockName, "set", array);

                // Journalisation (optionnelle)
                //SuperLog.info("LichEye", "Bloc surveillé ajouté ou mis à jour : " + blockName + " aux coordonnées (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ").");
            }
        });
    }

    private static void blocksRemoved(@Nullable Event event, LevelAccessor world, BlockState blockstate, Entity entity, BlockPos pos) {
        if (entity == null) return;

        GameModeHandler.executeServerOnlyCode(() -> {
            if ((entity instanceof Player || entity instanceof ServerPlayer) && !((DataWrapper) BCKServerdata.data("lich_eye.blocks_logger")).getJsonArray().isEmpty()) {

                Block block = blockstate.getBlock();
                JsonArray blocks = ((DataWrapper) BCKServerdata.data("lich_eye.blocks_logger")).getJsonArray();

                // Vérifie si le bloc actuel est dans le JsonArray des blocs surveillés
                boolean isBlockLogged = false;
                for (JsonElement element : blocks) {
                    if (element.isJsonPrimitive() && element.getAsString().equals(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).toString())) {
                        isBlockLogged = true;
                        break;
                    }
                }

                if (!isBlockLogged) {
                    return; // Si le bloc n'est pas surveillé, quitte la méthode
                }

                String modId = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getNamespace();
                String blockName = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = now.format(formatter);

                // Initialisation des structures JSON si nécessaires
                BCKLichEye.blocksInit(modId, blockName);

                JsonArray array = ((DataWrapper) BCKLichEye.data("events.blocks." + modId + "." + blockName)).getJsonArray();

                // Créer les données à insérer ou mettre à jour
                JsonObject data = new JsonObject();
                data.addProperty("player", entity.getStringUUID());
                data.addProperty("at", formattedTime);

                // Mettre à jour ou ajouter les données avec `updateJsonObjectInArray`
                array = BCKLichEye.updateJsonObjectInArray(array, world, pos.getX(), pos.getY(), pos.getZ(), "brokenBy", data);

                // Sauvegarder le tableau modifié
                BCKLichEye.data("events.blocks." + modId + "." + blockName, "set", array);

                // Journalisation (optionnelle)
                //SuperLog.info("LichEye", "Bloc surveillé ajouté ou mis à jour : " + blockName + " aux coordonnées (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ").");
            }
        });
    }

    private static void containersRemoved(@Nullable Event event, LevelAccessor world, BlockState blockstate, Entity entity, BlockPos pos) {
        if (entity == null) return;
        GameModeHandler.executeServerOnlyCode(() -> {
            if ((entity instanceof Player || entity instanceof ServerPlayer) && ((DataWrapper) BCKServerdata.data("lich_eye.containers_logger")).getBoolean()) {
                Block block = blockstate.getBlock();
                if (block instanceof BaseEntityBlock) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity instanceof Container) {
                        BlockEntityType<?> type = blockEntity.getType();

                        // Récupérer un nom lisible pour le type
                        String modId = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)).getNamespace();
                        String typeName = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)).getPath();

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedTime = now.format(formatter);

                        BCKLichEye.containersInit(modId, typeName);

                        JsonArray array = ((DataWrapper) BCKLichEye.data("containers." + modId + "." + typeName)).getJsonArray();

                        //SuperLog.info("LichEye/Remove", array);

                        JsonObject data = new JsonObject();
                        data.addProperty("author", entity.getStringUUID());
                        data.addProperty("at", formattedTime);

                        array = BCKLichEye.updateJsonObjectInArray(array, world, pos.getX(), pos.getY(), pos.getZ(), "brokenBy", data);

                        BCKLichEye.data("containers." + modId + "." + typeName, "set", array);
                    }
                }

            }
        });
    }

    private static void containersOppened(LevelAccessor world, BlockState blockstate, Entity entity, BlockPos pos) {
        Block block = blockstate.getBlock();
        if (block instanceof BaseEntityBlock) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Container) {
                BlockEntityType<?> type = blockEntity.getType();

                // Récupérer un nom lisible pour le type
                String modId = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)).getNamespace();
                String typeName = Objects.requireNonNull(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)).getPath();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = now.format(formatter);

                // Initialisation des données si nécessaire
                BCKLichEye.containersInit(modId, typeName);

                // Récupérer ou créer l'objet JSON correspondant aux coordonnées
                JsonArray array = ((DataWrapper) BCKLichEye.data("containers." + modId + "." + typeName)).getJsonArray();
                JsonObject container = BCKLichEye.getJsonObjectByCoordinates(array, pos.getX(), pos.getY(), pos.getZ());
                if (container == null) {
                    container = new JsonObject();
                    container.addProperty("x", pos.getX());
                    container.addProperty("y", pos.getY());
                    container.addProperty("z", pos.getZ());
                    array.add(container);
                }

                // Ajouter ou mettre à jour l'entrée dans "openedBy"
                JsonArray openedByArray = container.has("interactions") && container.get("interactions").isJsonArray() ? container.getAsJsonArray("interactions") : new JsonArray();

                boolean entryUpdated = false;
                for (JsonElement element : openedByArray) {
                    if (element.isJsonObject()) {
                        JsonObject entry = element.getAsJsonObject();
                        String uuid = entry.has("uuid") ? entry.get("uuid").getAsString() : null;

                        // Si l'entrée correspond à l'utilisateur
                        if (uuid != null && uuid.equals(entity.getStringUUID())) {
                            entryUpdated = true;

                            // Si "at" existe, le convertir en "ats"
                            if (entry.has("at")) {
                                JsonArray ats = new JsonArray();
                                ats.add(entry.get("at").getAsString()); // Ajoute l'ancien timestamp
                                entry.remove("at"); // Supprime le champ "at"
                                entry.add("ats", ats); // Ajoute le tableau "ats"
                            }

                            // Si "ats" existe, ajoute le nouveau timestamp en s'assurant de l'ordre
                            if (entry.has("ats") && entry.get("ats").isJsonArray()) {
                                JsonArray ats = entry.getAsJsonArray("ats");
                                ats.add(formattedTime);
                                entry.add("ats", sortTimestamps(ats)); // Trie les timestamps
                            } else {
                                // Ajoute le premier timestamp si "ats" n'existe pas encore
                                JsonArray ats = new JsonArray();
                                ats.add(formattedTime);
                                entry.add("ats", ats);
                            }
                            break;
                        }
                    }
                }

                // Si aucune entrée n'existe pour cet utilisateur, en créer une
                if (!entryUpdated) {
                    JsonObject openedByEntry = new JsonObject();
                    openedByEntry.addProperty("uuid", entity.getStringUUID());
                    openedByEntry.addProperty("at", formattedTime);
                    openedByArray.add(openedByEntry);
                }

                // Mettre à jour la clé "openedBy"
                container.add("interactions", openedByArray);

                // Sauvegarder les modifications
                BCKLichEye.data("containers." + modId + "." + typeName, "set", array);

                //SuperLog.info("LichEye", "Container ouvert par " + entity.getStringUUID() + " aux coordonnées (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ").");
            }
        }
    }

    /**
     * Trie les timestamps dans un JsonArray.
     *
     * @param timestamps Le tableau de timestamps à trier.
     * @return Un JsonArray contenant les timestamps triés.
     */
    private static JsonArray sortTimestamps(JsonArray timestamps) {
        List<String> sortedList = new ArrayList<>();
        for (JsonElement element : timestamps) {
            sortedList.add(element.getAsString());
        }
        sortedList.sort(Comparator.naturalOrder()); // Trie les timestamps dans l'ordre croissant
        JsonArray sortedArray = new JsonArray();
        for (String timestamp : sortedList) {
            sortedArray.add(timestamp);
        }
        return sortedArray;
    }
}
