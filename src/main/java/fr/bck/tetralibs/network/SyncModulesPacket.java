package fr.bck.tetralibs.network;


/*≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
 ≡              Copyright BCK, Inc 2025. (DragClover / Blackknight)              ≡
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

import fr.bck.tetralibs.config.ModulesClientConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record SyncModulesPacket(Map<String, Boolean> modules) {
    public static final class Utils {
        // ta couleur
        public static final String color = "§e";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static void encode(SyncModulesPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.modules.size());
        pkt.modules.forEach((key, val) -> {
            buf.writeUtf(key);
            buf.writeBoolean(val);
        });
    }

    public static SyncModulesPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, Boolean> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf(32767);
            boolean val = buf.readBoolean();
            map.put(key, val);
        }
        return new SyncModulesPacket(map);
    }

    public static void handle(SyncModulesPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // côté client : appliquer les overrides
            ModulesClientConfig.applyServerOverrides(pkt.modules());
        });
        ctx.get().setPacketHandled(true);
    }
}
