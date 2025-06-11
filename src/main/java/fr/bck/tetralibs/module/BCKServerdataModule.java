package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.modules.data.BCKServerdataEventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;

public final class BCKServerdataModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_SERVERDATA;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        MinecraftForge.EVENT_BUS.register(BCKServerdataEventHandler.class);
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKServerdata.class), "§aHandlers registered");
    }

    @Override
    public void onServerStarting(ServerStartingEvent event) {
        super.onServerStarting(event);
    }

    @Override
    public void onWorldLoad(LevelEvent.Load event) {
        super.onWorldLoad(event);
    }
}
