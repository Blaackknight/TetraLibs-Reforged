package fr.bck.tetralibs.module;

import net.minecraft.resources.ResourceLocation;

public final class BCKBackpackModule extends CoreDependentModule {

    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_BACKPACK;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }
}
