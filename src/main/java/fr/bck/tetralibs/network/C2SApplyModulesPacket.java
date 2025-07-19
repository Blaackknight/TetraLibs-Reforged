package fr.bck.tetralibs.network;


import fr.bck.tetralibs.core.ModuleManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
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

/**
 * Packet « Client → Serveur » : envoie l’état (activé / désactivé)
 * de tous les modules après que le joueur a cliqué sur « Apply ».
 */
public class C2SApplyModulesPacket {

    private final Map<ResourceLocation, Boolean> selection;

    /* ------------------------------------------------------------------ */
    /*  Constructeurs                                                     */
    /* ------------------------------------------------------------------ */
    public C2SApplyModulesPacket(Map<ResourceLocation, Boolean> selection) {
        this.selection = selection;
    }

    /**
     * Côté réseau : lecture.
     */
    public C2SApplyModulesPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, Boolean> tmp = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            tmp.put(buf.readResourceLocation(), buf.readBoolean());
        }
        this.selection = tmp;
    }

    /* ------------------------------------------------------------------ */
    /*  (dé-)sérialisation                                                */
    /* ------------------------------------------------------------------ */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(selection.size());
        selection.forEach((id, flag) -> {
            buf.writeResourceLocation(id);
            buf.writeBoolean(flag);
        });
    }

    public static void encode(C2SApplyModulesPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.selection.size());
        pkt.selection.forEach((id, flag) -> {
            buf.writeResourceLocation(id);
            buf.writeBoolean(flag);
        });
    }

    public static C2SApplyModulesPacket decode(FriendlyByteBuf buf) {
        return new C2SApplyModulesPacket(buf);
    }

    /* ------------------------------------------------------------------ */
    /*  Handler                                                           */
    /* ------------------------------------------------------------------ */
    public static void handle(C2SApplyModulesPacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();          // jamais null côté serveur
            if (sender == null) return;

            // Applique la nouvelle configuration côté serveur
            ModuleManager.applySelection(pkt.selection, sender);
            ModuleManager.applySelection(pkt.selection, sender, true);
            // → à toi de décider si tu renvoies ensuite un S2C pour confirmer.
        });
        ctx.setPacketHandled(true);
    }
}
