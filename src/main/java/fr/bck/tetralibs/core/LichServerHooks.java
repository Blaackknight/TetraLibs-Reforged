package fr.bck.tetralibs.core;

import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.commands.CommandRegister;
import fr.bck.tetralibs.module.TetraModule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;



/*≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
 ≡           Copyright BCK, Inc 2025. (DragClover / Blackknight)                 ≡
 ≡                                                                               ≡
 ≡ Permission is hereby granted, free of charge, to any person obtaining a copy  ≡
 ≡ of this software and associated documentation files (the “Software”), to deal ≡
 ≡ in the Software without restriction, including without limitation the rights  ≡
 ≡ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ≡
 ≡ copies of the Software, and to permit persons to whom the Software is         ≡
 ≡ furnished to do so, subject to the following conditions:                      ≡
 ≡                                                                               ≡
 ≡ The above copyright notice and this permission notice shall be included in    ≡
 ≡ all copies or substantial portions of the Software.                           ≡
 ≡                                                                               ≡
 ≡ THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ≡
 ≡ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ≡
 ≡ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ≡
 ≡ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ≡
 ≡ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ≡
 ≡ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE ≡
 ≡ SOFTWARE.                                                                     ≡
 ≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡*/

/**
 * Hooks côté serveur (bus FORGE).
 * Délègue au ModuleManager pour lancer les modules.
 */
@Mod.EventBusSubscriber(modid = TetralibsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class LichServerHooks {
    public static final class Utils {
        // ta couleur
        public static final String color = "§7";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent e) {
        ModuleManager.processGenericEvent(e, TetraModule::onServerStarting);
        CommandRegister.register(e.getServer().getCommands().getDispatcher());

        // Et on branche le reload (il ne throwera plus, la config est déjà chargée)
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent reload) -> CommandRegister.register(reload.getDispatcher()));

        // 2) Enregistrement initial des commandes
        /**
         CommandDispatcher<CommandSourceStack> dispatcher = e.getServer().getCommands().getDispatcher();
         CommandRegister.register(dispatcher);

         // 3) Brancher un listener pour les reloads (/reload)
         MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent ev) -> {
         CommandRegister.register(ev.getDispatcher());
         });*/
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        ModuleManager.processGenericEvent(e, TetraModule::onServerStopping);
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load e) {
        if (e.getLevel().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onWorldLoad);
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload e) {
        if (e.getLevel().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onWorldUnload);
    }

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save e) {
        if (e.getLevel().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onWorldSave);
    }

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onPlayerLogIn);
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onPlayerLogOut);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent e) {
        if (Objects.requireNonNull(e.getEntity()).level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onBlockPlace);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent e) {
        if (e.getPlayer().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onBlockBreak);
    }

    @SubscribeEvent
    public static void onItemDropped(ItemTossEvent e) {
        if (e.getPlayer().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onItemDropped);
    }

    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent e) {
        if (e.getEntity().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onItemPickup);
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent e) {
        if (e.getEntity().level().isClientSide) return;
        ModuleManager.processGenericEvent(e, TetraModule::onItemExpire);
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent e) {
        if (e.getPlayer().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onChat);
    }

    @SubscribeEvent
    public static void onEntityJump(LivingEvent.LivingJumpEvent e) {
        if (e.getEntity().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onEntityJump);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (e.getEntity().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onRightClickBlock);
    }

    @SubscribeEvent
    public static void onEntityAttacked(LivingAttackEvent e) {
        if (e.getEntity().level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onEntityAttacked);
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent e) {
        var level = e.getParseResults().getContext().getSource().getLevel();
        if (level.isClientSide()) {
            return;
        }
        ModuleManager.processGenericEvent(e, TetraModule::onCommand);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent e) {
        if (e.level.isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onWorldTick);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        ModuleManager.processGenericEvent(e, TetraModule::onServerTick);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.player.level().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onPlayerTick);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.side.isServer()) return;
        ModuleManager.processModules(ModuleManager.Hook.CLIENT);
    }

    @SubscribeEvent
    public static void onPortailCreate(BlockEvent.PortalSpawnEvent e) {
        if (e.getLevel().isClientSide()) return;
        ModuleManager.processGenericEvent(e, TetraModule::onPortalCreate);
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent evt) {
        // À ce stade, la config SERVER est déjà chargée par Forge
        ModuleManager.processModules(ModuleManager.Hook.SERVER);
    }

    @SubscribeEvent
    public static void onItemRender(ItemTooltipEvent e) {
        ModuleManager.processModules(ModuleManager.Hook.CLIENT);
    }

    private LichServerHooks() {
    }
}