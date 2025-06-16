package fr.bck.tetralibs.backpack;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

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

public final class BCKBackpackUtils {

    /**
     * cherche l’ItemStack dans inventaires + entités d’item
     */
    public static ItemStack findLiveStack(ServerLevel level, UUID id) {
        /* 1. joueurs en ligne */
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            for (ItemStack st : p.getInventory().items) {
                if (id.equals(BCKBackpackInvProvider.getUUID(st))) return st;
            }
        }
        /* 2. entités ItemEntity chargées */
        for (ItemEntity ie : level.getEntities(EntityType.ITEM, e -> true)) {
            if (id.equals(BCKBackpackInvProvider.getUUID(ie.getItem()))) return ie.getItem();
        }
        return ItemStack.EMPTY;
    }

    /**
     * renvoie un sac : live » si trouvé ; sinon le reconstruit depuis la save
     */
    public static ItemStack getOrRestore(ServerLevel level, UUID id) {
        ItemStack live = findLiveStack(level, id);
        if (!live.isEmpty()) return live;

        CompoundTag tag = BCKBackpackData.get(level).retrieve(id);
        if (tag != null) {
            ItemStack restored = ItemStack.of(tag);
            // safety : pas empilable → drop au sol ou donner au joueur
            return restored;
        }
        return ItemStack.EMPTY;
    }

    private BCKBackpackUtils() {
    }
}
