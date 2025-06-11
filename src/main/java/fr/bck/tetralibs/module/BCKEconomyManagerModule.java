package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.economy.BCKEconomyManager;
import fr.bck.tetralibs.modules.economy.BCKEconomyManagerEventhandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Set;

public class BCKEconomyManagerModule extends CoreDependentModule {

    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_ECONOMY_MANAGER;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        MinecraftForge.EVENT_BUS.register(new BCKEconomyManagerEventhandler());
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), "§aHandlers registered");
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_USERDATA);
    }

    @Override
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        super.onPlayerLogIn(event);
        Entity entity = event.getEntity();
        LevelAccessor world = event.getEntity().level();
        if (entity instanceof Player _player) BCKEconomyManager.loadBalances(_player, true);
    }

    @Override
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        super.onPlayerLogOut(event);
        Entity entity = event.getEntity();
        LevelAccessor world = event.getEntity().level();
        if (entity instanceof Player _player) BCKEconomyManager.saveBalances(_player, true);
    }
}
