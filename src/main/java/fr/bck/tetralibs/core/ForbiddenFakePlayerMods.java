package fr.bck.tetralibs.core;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contient la liste des mods connus pour utiliser massivement des FakePlayers.
 * Si l’un de ces mods est présent, le module Tetra doit être désactivé.
 */
public final class ForbiddenFakePlayerMods {
    public static final class Utils {
        // ta couleur
        public static final String color = "§7";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    // Liste des modids (namespaces) à exclure
    private static final List<String> NAMESPACES = List.of(
            "create",
            "industrialforegoing",
            "enderio",
            "enderio_conduits",
            "enderio_integration",
            "pneumaticcraft",
            "immersiveengineering",
            "thermal",
            "thermalexpansion",
            "thermalfoundation",
            "mekanism",
            "mekanismgenerators",
            "botania",
            "agricraft",
            "refinedstorage",
            "refinedstorageaddons",
            "appliedenergistics2",
            "thermalcultivation"
    );

    /**
     * @return Set de ResourceLocation à passer dans excludedMods() 
     *         seule la partie namespace est utilisée pour la vérification de présence du mod.
     */
    public static Set<ResourceLocation> getSet() {
        return NAMESPACES.stream()
                .map(ns -> ResourceLocation.fromNamespaceAndPath(ns, ""))
                .collect(Collectors.toSet());
    }

    private ForbiddenFakePlayerMods() {
        // ne pas instancier
    }
}