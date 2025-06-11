package fr.bck.tetralibs.module;

import net.minecraft.resources.ResourceLocation;

public final class BCKSkillModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_SKILL;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

}
