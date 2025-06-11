package fr.bck.tetralibs.module;

import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.ModuleManager;
import fr.bck.tetralibs.network.BCKNetworkHandler;
import fr.bck.tetralibs.network.SyncModulesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.stream.Collectors;

import static fr.bck.tetralibs.core.BCKUtils.ServerUtil.getWorldFolderName;

public final class BCKCoreModule implements TetraModule {

    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_CORE;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        BCKCore.init();
    }

    @Override
    public void onWorldLoad(LevelEvent.Load event) {
        TetraModule.super.onWorldLoad(event);
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKCore.class), "§7WorldName: §d" + getWorldFolderName());
    }

    @Override
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        TetraModule.super.onPlayerLogIn(event);
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        // Ne transmettre que les modules désactivés côté serveur
        Map<String, Boolean> overrides = ModulesConfig.getConfiguredModules().stream()
                // 1) on ne garde que ceux dont on retrouve bien un TetraModule…
                .map(rl -> rl) // rl est un ResourceLocation
                .filter(rl -> ModuleManager.getModule(rl).map(mod -> mod.type() == TetraModule.Type.CLIENT).orElse(false))
                // 2) on collecte en Map<path, override>
                .collect(Collectors.toMap(ResourceLocation::getPath, rl -> !ModulesConfig.isEnabled(rl)  // true = on force false côté client
                ));

        BCKNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncModulesPacket(overrides));
    }
}