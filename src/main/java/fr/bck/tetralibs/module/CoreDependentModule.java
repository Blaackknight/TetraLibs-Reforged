package fr.bck.tetralibs.module;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implémente automatiquement la dépendance vers BCK Core.
 * Hérite-en pour tous les modules qui en ont besoin.
 */
public abstract class CoreDependentModule implements TetraModule {

    @Override
    public Set<ResourceLocation> requiredModules() {
        // un seul pré-requis : le core
        return Set.of(ModuleIds.BCK_CORE);
    }
    protected static Set<ResourceLocation> withCore(ResourceLocation... extras) {
	    Set<ResourceLocation> s = new HashSet<>();
	    s.add(ModuleIds.BCK_CORE);
	    Collections.addAll(s, extras);
	    return s;
	}
}