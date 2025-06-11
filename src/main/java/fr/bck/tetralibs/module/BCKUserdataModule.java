package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKUserdata;
import fr.bck.tetralibs.modules.data.BCKUserdataEventHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;

public final class BCKUserdataModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_USERDATA;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        MinecraftForge.EVENT_BUS.register(new BCKUserdataEventHandler());
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUserdata.class), "§aHandlers registered");
    }

    @Override
    public void onWorldLoad(LevelEvent.Load event) {
        super.onWorldLoad(event);
        LevelAccessor world = event.getLevel();
        BCKUserdata.worldLoad(world);
    }

    @Override
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        super.onPlayerLogIn(event);
        Entity entity = event.getEntity();
        LevelAccessor world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        if (!((DataWrapper) BCKUserdata.data("first_joined", world, entity)).getBoolean())
            BCKUserdata.data("first_joined", "set", true, world, entity);
    }

    @Override
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        super.onPlayerLogOut(event);
        Entity entity = event.getEntity();
        LevelAccessor world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        // Enregistre les données du joueur
        BCKUserdata.save(world, entity);
    }
}
