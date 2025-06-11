package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.level.BCKLeveling;
import net.minecraft.resources.ResourceLocation;

public final class BCKLevelingModule extends CoreDependentModule {

    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_LEVELING;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLeveling.class), "§aHandlers registered");
    }
}
