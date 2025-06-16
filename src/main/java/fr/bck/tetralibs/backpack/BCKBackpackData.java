package fr.bck.tetralibs.backpack;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



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

public class BCKBackpackData extends SavedData {

    private static final String ID = "BCKBackpacks";

    private final Map<UUID, CompoundTag> backups = new HashMap<>();

    /* ---------- accès global ---------------------------------------- */
    public static BCKBackpackData get(ServerLevel lvl) {
        return lvl.getServer().overworld()                           // n’importe quel niveau
                .getDataStorage().computeIfAbsent(BCKBackpackData::load, BCKBackpackData::new, ID);
    }

    /* ---------- lecture / écriture NBT ------------------------------ */
    private static BCKBackpackData load(CompoundTag nbt) {
        BCKBackpackData data = new BCKBackpackData();
        CompoundTag map = nbt.getCompound("Backups");
        map.getAllKeys().forEach(k -> data.backups.put(UUID.fromString(k), map.getCompound(k)));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag map = new CompoundTag();
        backups.forEach((u, tag) -> map.put(u.toString(), tag.copy()));
        nbt.put("Backups", map);
        return nbt;
    }

    /* ---------- API publiques --------------------------------------- */
    public void store(UUID id, CompoundTag itemTag) {
        backups.put(id, itemTag.copy());
        setDirty();
    }

    public CompoundTag retrieve(UUID id) {
        return backups.get(id);
    }
}
