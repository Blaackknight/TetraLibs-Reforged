package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.events.BCKStaffHandler;
import fr.bck.tetralibs.lich.BCKLichGuard;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;
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

public final class BCKLichGuardModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_LICH_GUARD;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_PERMISSIONS);
    }

    @Override
    public void onServerSetup() {
        super.onServerSetup();
        MinecraftForge.EVENT_BUS.register(BCKStaffHandler.class);
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichGuard.class), "§aHandlers registered");
    }

    @Override
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        super.onBlockBreak(event);                      // garde la logique du module parent

        // Ne traite que côté serveur
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // Vérifie la protection
        if (BCKLichGuard.get(level).canInteract(event.getPlayer(), BCKLichGuard.Flag.BLOCK_BREAK, event.getPos()))
            return; // action autorisée → on laisse faire

        // Action bloquée
        event.setCanceled(true);

        // Message
        event.getPlayer().displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("tetra_libs.events.lich_guard.protected").getString()), true);

        // Particules + son (cool-down)
        if (event.getPlayer() instanceof ServerPlayer sp && shouldNotify(sp)) {
            BCKUtils.ParticleUtil.showDeniedParticles(sp, ParticleTypes.ENCHANT, event.getPos());
            sp.playSound(SoundEvents.SHIELD_BLOCK, 1f, 1f);
        }
    }

    @Override
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        super.onBlockPlace(event);

        // Ne traite que côté serveur
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        if (!(event.getEntity() instanceof ServerPlayer _pl)) return;

        // Vérifie la protection
        if (BCKLichGuard.get(level).canInteract(_pl, BCKLichGuard.Flag.BLOCK_BREAK, event.getPos()))
            return; // action autorisée → on laisse faire

        // Action bloquée
        event.setCanceled(true);

        // Message
        _pl.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("tetra_libs.events.lich_guard.protected").getString()), true);

        // Particules + son (cool-down)
        if (event.getEntity() instanceof ServerPlayer sp && shouldNotify(sp)) {
            BCKUtils.ParticleUtil.showDeniedParticles(sp, ParticleTypes.ENCHANT, event.getPos());
            sp.playSound(SoundEvents.SHIELD_BLOCK, 1f, 1f);
        }
    }

    // -----------------------------------------------------------------------------
    // Cool-down pour éviter le spam : un même joueur ne reçoit l'avertissement
    // qu'une fois tous les 10 ticks (≈ 0,5 s).
    // -----------------------------------------------------------------------------
    private static final Map<UUID, Long> LAST_DENY = new HashMap<>();

    /**
     * @return true si on peut notifier ce tick-ci, false si c'est encore trop tôt
     */
    private static boolean shouldNotify(ServerPlayer p) {
        long now = p.level().getGameTime();            // tick actuel
        return LAST_DENY.merge(p.getUUID(), now, (old, n) -> (n - old > 10) ? n : old) == now;
    }
}
