package fr.bck.tetralibs.modules.permissions;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Tous les @SubscribeEvent du module Permissions.
 * Pas d'annotation @Mod.EventBusSubscriber pour rester inactif
 * tant que le module n'est pas chargé.
 */
public final class PermEventHandler {

    @SubscribeEvent
    public void onServerStart(ServerStartingEvent e) {

    }
}