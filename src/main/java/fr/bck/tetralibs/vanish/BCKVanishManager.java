package fr.bck.tetralibs.vanish;


import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class BCKVanishManager {
    private static final Set<UUID> VANISHED = new HashSet<>();

    /* ===============  API  ================= */

    public static boolean isVanished(ServerPlayer p) {
        return VANISHED.contains(p.getUUID());
    }

    /**
     * Appelé par la commande ou par ton GUI admin
     */
    public static void setVanished(ServerPlayer p, boolean on) {
        if (on == isVanished(p)) return;          // rien à faire

        if (on) {
            VANISHED.add(p.getUUID());
            hideFromEveryone(p);
            p.setInvisible(true);
            p.getAbilities().flying = true;       // optionnel
            p.noPhysics = true;
        } else {
            VANISHED.remove(p.getUUID());
            showToEveryone(p);
            p.setInvisible(false);
            p.noPhysics = false;
        }
        p.onUpdateAbilities();
    }

    /* ===============  Implémentation  ================= */

    /**
     * Cache le joueur à tout le monde SAUF lui-même
     */
    private static void hideFromEveryone(ServerPlayer target) {
        var players = target.server.getPlayerList().getPlayers();
        for (ServerPlayer other : players) {
            if (other == target) continue;
            other.connection.send(new ClientboundRemoveEntitiesPacket(target.getId()));
            other.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(target.getUUID())));
        }
    }

    /**
     * Ré-envoie les paquets d’apparition / tab list
     */
    private static void showToEveryone(ServerPlayer target) {
        var players = target.server.getPlayerList().getPlayers();
        ClientboundPlayerInfoUpdatePacket info = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target));
        ClientboundAddPlayerPacket spawn = new ClientboundAddPlayerPacket(target);
        for (ServerPlayer other : players) {
            if (other == target) continue;
            other.connection.send(info);
            other.connection.send(spawn);
        }
    }

    /* ---- 1) empêche un nouveau joueur de commencer à track un vanish ---- */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking e) {
        if (!(e.getTarget() instanceof ServerPlayer target)) return;
        if (!isVanished(target) || e.getEntity() == target) return;
        e.setCanceled(true);              // stop Forge -> pas de packets spawn
    }
}
