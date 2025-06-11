package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.spawn.BCKSpawn;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class BCKSpawnModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_SPAWN;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "§aHandlers registered");
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_SERVERDATA);
    }

    @Override
    public void onClientSetup() {
        // Client-only : renderer, GUI, keybinds…
    }
}
