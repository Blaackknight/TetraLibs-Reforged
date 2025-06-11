package fr.bck.tetralibs.module;

import net.minecraft.resources.ResourceLocation;

public final class BCKRegionModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_REGION;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

}
