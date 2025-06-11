package fr.bck.tetralibs.module;


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

import fr.bck.tetralibs.combat.BCKCombat;
import fr.bck.tetralibs.combat.BCKCombatManager;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static fr.bck.tetralibs.combat.BCKCombatManager.getCooldowns;

public class BCKCombatModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_COMBAT;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        super.onServerSetup();
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKCombat.class), "§aHandlers registered");
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_USERDATA, ModuleIds.BCK_SERVERDATA);
    }

    @Override
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        super.onPlayerLogIn(event);
        Entity entity = event.getEntity();
        if (BCKCombat.inCombat(entity)) {
            BCKCombat.kill(entity);
        }
    }

    @Override
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        super.onPlayerLogOut(event);
    }

    @Override
    public void onEntityAttacked(LivingAttackEvent event) {
        super.onEntityAttacked(event);
        Entity entity = event.getEntity();
        Entity sourceentity = event.getSource().getEntity();
        if (sourceentity instanceof ServerPlayer && entity instanceof ServerPlayer) {
            BCKCombatManager.enterCombat(sourceentity);
            BCKCombatManager.enterCombat(entity);
        }
    }

    @Override
    public void onServerTick(TickEvent.ServerTickEvent event) {
        super.onServerTick(event);
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<Map.Entry<UUID, Double>> it = getCooldowns().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Double> entry = it.next();
            double secLeft = entry.getValue() - (1.0 / 20.0);
            UUID playerId = entry.getKey();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (secLeft <= 0 || player == null) {
                // fin de combat
                if (player != null) BCKCombat.exit(player);
                it.remove();
            } else {
                // mise à jour
                entry.setValue(secLeft);
                BCKCombat.setPlayerCombatCooldown(player, secLeft);
                // envoi dans l'action bar via la clé translatable avec argument
                String timeStr = String.format("%.1f", secLeft);
                Component msg = Component.translatable("bck_combat.in_combat", timeStr);
                player.displayClientMessage(msg, true);
            }
        }
    }
}
