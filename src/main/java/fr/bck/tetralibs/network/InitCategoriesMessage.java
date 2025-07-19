package fr.bck.tetralibs.network;


import fr.bck.tetralibs.client.screens.BCKCategoryRegistry;
import fr.bck.tetralibs.client.screens.BCKGateCategory;
import fr.bck.tetralibs.client.screens.ModulesCategory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;


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

public class InitCategoriesMessage {

    /**
     * Données minimales à transmettre : ici la map <moduleId, enabled>.
     */
    private final Map<ResourceLocation, Boolean> selection;

    public InitCategoriesMessage(Map<ResourceLocation, Boolean> selection) {
        this.selection = selection;
    }

    public InitCategoriesMessage(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, Boolean> map = new java.util.HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(buf.readResourceLocation(), buf.readBoolean());
        }
        this.selection = map;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(selection.size());
        selection.forEach((id, flag) -> {
            buf.writeResourceLocation(id);
            buf.writeBoolean(flag);
        });
    }

    /* ------------------------------------------------------------------ handler */
    public static void handle(InitCategoriesMessage msg, Supplier<NetworkEvent.Context> ctxSup) {
        ctxSup.get().enqueueWork(() -> {
            // ——— exécution côté CLIENT
            BCKCategoryRegistry.clear();           // reset
            BCKCategoryRegistry.register(new BCKGateCategory());
            BCKCategoryRegistry.register(new ModulesCategory());
            // si d’autres catégories : register(...) ici
        });
        ctxSup.get().setPacketHandled(true);
    }
}
