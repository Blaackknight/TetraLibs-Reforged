package fr.bck.tetralibs.lich;


import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.permissions.BCKPermissions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;





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
 * « BCKLichGuard » — Gardien funeste des zones protégées pour TetraLibs : Reforged.
 * Ajout 2025‑06 : prise en charge d’un <b>Territory.EMPTY</b> (objet Null) afin de
 * simplifier les vérifications sans avoir recours à Optional.
 */
public class BCKLichGuard extends SavedData {
    public static final class Utils {
        // ta couleur
        public static final String color = "§5§n";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName() + "§r";
    }

    public static final String DATA_NAME = "bck_territories";

    /* =====================================================================
     *  Stockage runtime
     * ===================================================================*/
    private final Map<UUID, Territory> territories = new LinkedHashMap<>();

    /* =====================================================================
     *  Inner class : Territory (Null‑Object pattern)
     * ===================================================================*/
    public static class Territory {
        /* ------------------------  Instance fields  ---------------------*/
        public final UUID id;
        public String name;
        public UUID owner;
        public final Set<UUID> members = new HashSet<>();
        public BlockPos min;
        public BlockPos max;
        public final EnumMap<Flag, Boolean> flags = new EnumMap<>(Flag.class);
        private final boolean empty; // marqueur interne (EMPTY == true)

        /* ------------------------  Constructeurs  -----------------------*/
        private Territory(UUID id, String name, UUID owner, BlockPos min, BlockPos max, boolean empty) {
            this.id = id;
            this.name = name;
            this.owner = owner;
            this.min = min;
            this.max = max;
            this.empty = empty;
            for (Flag f : Flag.values()) flags.put(f, false);
        }

        public Territory(UUID id, String name, UUID owner, BlockPos min, BlockPos max) {
            this(id, name, owner, min, max, false);
        }

        /* ------------------------  Null‑object  --------------------------*/
        public static final Territory EMPTY = new Territory(new UUID(0, 0), "__empty__", new UUID(0, 0), BlockPos.ZERO, BlockPos.ZERO, true) {
            @Override
            public boolean inBounds(BlockPos pos) {
                return false;
            }

            @Override
            public boolean isMember(UUID uuid) {
                return true;
            }

            @Override
            public boolean can(Player player, Flag action) {
                return true;
            }

            @Override
            public CompoundTag toNBT() {
                return new CompoundTag();
            }
        };

        public boolean isEmpty() {
            return empty;
        }

        /* ------------------------  Utilitaires  --------------------------*/
        public boolean inBounds(BlockPos pos) {
            return pos.getX() >= min.getX() && pos.getX() <= max.getX() && pos.getY() >= min.getY() && pos.getY() <= max.getY() && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        }

        public boolean isMember(UUID uuid) {
            return uuid.equals(owner) || members.contains(uuid);
        }

        public boolean can(Player player, Flag action) {
            if (isMember(player.getUUID())) return true; // membres & owner
            return flags.getOrDefault(action, false) && BCKPermissions.hasPermission(player, action.permissionKey);
        }

        /* ------------------------  NBT  ----------------------------------*/
        public CompoundTag toNBT() {
            if (empty) return new CompoundTag(); // on ne sérialise pas EMPTY
            CompoundTag tag = new CompoundTag();
            tag.putUUID("id", id);
            tag.putString("name", name);
            tag.putUUID("owner", owner);

            ListTag memberList = new ListTag();
            for (UUID m : members) memberList.add(NbtUtils.createUUID(m));
            tag.put("members", memberList);

            tag.put("min", NbtUtils.writeBlockPos(min));
            tag.put("max", NbtUtils.writeBlockPos(max));

            CompoundTag flagTag = new CompoundTag();
            for (Map.Entry<Flag, Boolean> e : flags.entrySet()) {
                flagTag.putBoolean(e.getKey().name(), e.getValue());
            }
            tag.put("flags", flagTag);
            return tag;
        }

        public static Territory fromNBT(CompoundTag tag) {
            // Cas vide (terrain non trouvé ou tag vide)
            if (tag.isEmpty()) return EMPTY;
            Territory t = new Territory(tag.getUUID("id"), tag.getString("name"), tag.getUUID("owner"), NbtUtils.readBlockPos(tag.getCompound("min")), NbtUtils.readBlockPos(tag.getCompound("max")));
            ListTag memberList = tag.getList("members", Tag.TAG_INT_ARRAY);
            for (Tag element : memberList) t.members.add(NbtUtils.loadUUID(element));

            CompoundTag flagTag = tag.getCompound("flags");
            for (String key : flagTag.getAllKeys()) {
                try {
                    t.flags.put(Flag.valueOf(key), flagTag.getBoolean(key));
                } catch (IllegalArgumentException ignored) { /* flag inconnu */ }
            }
            return t;
        }
    }

    /* =====================================================================
     *  Enum : Flag → keys BCKPermissions
     * ===================================================================*/
    public enum Flag {
        BLOCK_BREAK("block.break"), BLOCK_PLACE("block.place"), INTERACT("player.interact");

        public final String permissionKey;

        Flag(String permissionKey) {
            this.permissionKey = permissionKey;
        }
    }

    /* =====================================================================
     *  SavedData impl.
     * ===================================================================*/
    public static BCKLichGuard get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BCKLichGuard::load, BCKLichGuard::new, DATA_NAME);
    }

    private static BCKLichGuard load(CompoundTag tag) {
        BCKLichGuard data = new BCKLichGuard();
        ListTag list = tag.getList("territories", Tag.TAG_COMPOUND);
        for (Tag element : list) {
            Territory terr = Territory.fromNBT((CompoundTag) element);
            if (!terr.isEmpty()) data.territories.put(terr.id, terr);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Territory t : territories.values()) list.add(t.toNBT());
        tag.put("territories", list);
        return tag;
    }

    /* =====================================================================
     *  API publique
     * ===================================================================*/
    public Territory createTerritory(String name, Player owner, BlockPos min, BlockPos max) {
        Territory terr = new Territory(UUID.randomUUID(), name, owner.getUUID(), min, max);
        territories.put(terr.id, terr);
        setDirty();
        BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLichGuard.class), "§aNew area §e" + name + " §acreated by §b" + owner.getName().getString());
        return terr;
    }

    public void deleteTerritory(UUID id) {
        territories.remove(id);
        setDirty();
    }

    public Territory territoryAt(BlockPos pos) {
        return territories.values().stream().filter(t -> t.inBounds(pos)).findFirst().orElse(Territory.EMPTY);
    }

    public Territory territoryByName(String name) {
        return territories.values().stream().filter(t -> t.name.equalsIgnoreCase(name)).findFirst().orElse(Territory.EMPTY);
    }

    /**
     * Vérifie si le joueur peut effectuer l’action à la position.
     */
    public boolean canInteract(Player player, Flag flag, BlockPos pos) {
        Territory t = territoryAt(pos);
        return t.can(player, flag); // EMPTY retourne toujours true
    }

    /**
     * Noms de tous les territoires (hors EMPTY)
     */
    public Set<String> getTerritoryNames() {
        Set<String> set = new HashSet<>();
        territories.values().forEach(t -> set.add(t.name));
        return set;
    }

    /* =====================================================================*/
    private BCKLichGuard() {
    } // use factory
}


