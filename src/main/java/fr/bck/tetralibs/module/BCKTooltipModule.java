package fr.bck.tetralibs.module;

import fr.bck.tetralibs.client.tooltip.BCKTooltip;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public final class BCKTooltipModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_TOOLTIP;
    }

    @Override
    public Type type() {
        return Type.CLIENT;
    }

    @Override
    public void onTooltipRender(ItemTooltipEvent event) {
        super.onTooltipRender(event);
        MinecraftForge.EVENT_BUS.register(BCKTooltip.class);
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKTooltip.class), "§aHandlers registered");
    }
}
