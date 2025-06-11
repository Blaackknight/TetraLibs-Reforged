package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.home.BCKHomeManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class BCKHomeModule extends CoreDependentModule {

    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_HOME;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), "§aHandlers registered");
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_SERVERDATA, ModuleIds.BCK_USERDATA);
    }

    @Override
    public void onClientSetup() {
        // Client-only : renderer, GUI, keybinds…
    }
}
