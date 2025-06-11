package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import fr.bck.tetralibs.modules.lich.BCKLichWhisperEventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.Set;

public final class BCKLichWhisperModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_LICH_WHISPER;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        MinecraftForge.EVENT_BUS.register(new BCKLichWhisperEventHandler());
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichWhisper.class), "§aHandlers registered");
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_USERDATA);
    }

    @Override
    public void onClientSetup() {
        // Client-only : renderer, GUI, keybinds…
    }
}
