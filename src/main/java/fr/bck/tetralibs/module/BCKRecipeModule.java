package fr.bck.tetralibs.module;

import net.minecraft.resources.ResourceLocation;

public final class BCKRecipeModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_RECIPE;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }
}
