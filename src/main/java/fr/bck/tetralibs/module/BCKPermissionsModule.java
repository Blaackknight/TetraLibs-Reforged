package fr.bck.tetralibs.module;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import fr.bck.tetralibs.TetralibsMod;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.*;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import fr.bck.tetralibs.modules.permissions.PermEventHandler;
import fr.bck.tetralibs.permissions.BCKPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;




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
 * Module « Permissions » – dépend de BCK Core.
 */
public final class BCKPermissionsModule extends CoreDependentModule {

    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_PERMISSIONS;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public Set<ResourceLocation> excludedMods() {
        return ForbiddenFakePlayerMods.getSet();
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_SERVERDATA, ModuleIds.BCK_USERDATA);
    }

    @Override
    public void onServerSetup() {
        MinecraftForge.EVENT_BUS.register(new PermEventHandler());
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§aHandlers registered");
    }

    @Override
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        super.onPlayerLogIn(event);
        Entity entity = event.getEntity();
        LevelAccessor world = entity.level();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        BCKPermissions.defaultPermissions(entity, 2);

        MinecraftServer server = world.getServer();
        assert server != null;
        if (server.isSingleplayer()) {
            BCKPermissions.addPermission(entity, BCKPermissions.supreme_permission, true);
        }
    }

    @Override
    public void onServerStarting(ServerStartingEvent event) {
        super.onServerStarting(event);
        BCKServerdata.init();
        BCKServerdata.load();
        BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§2Server starting§f, registering permissions...");

        BCKPermissions.addServerPermission("bck", true);
        BCKPermissions.addServerPermission("op", true);
        BCKPermissions.addServerPermission("block.place", true);
        BCKPermissions.addServerPermission("block.break", true);
        BCKPermissions.addServerPermission("item.drop", true);
        BCKPermissions.addServerPermission("item.pickup", true);
        BCKPermissions.addServerPermission("player.send_message", true);
        BCKPermissions.addServerPermission("player.move", true);
        BCKPermissions.addServerPermission("player.jump", true);
        BCKPermissions.addServerPermission("player.interact", true);
        BCKPermissions.addServerPermission("player.attack", true);
        BCKPermissions.addServerPermission("homes.view", true);
        BCKPermissions.addServerPermission("warps.create", true);
        BCKPermissions.addServerPermission("warps.remove", true);
        BCKPermissions.addServerPermission("warps.view", true);
        BCKPermissions.addServerPermission("warps.use", true);
        BCKPermissions.addServerPermission("region.claim", true);
        BCKPermissions.addServerPermission("region.build", true);
        BCKPermissions.addServerPermission("region.admin", true);
        BCKPermissions.addServerPermission("vanish.use", true);
        BCKPermissions.addServerPermission("bck_management.open", true);

        MinecraftServer server = event.getServer();
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
        Collection<CommandNode<CommandSourceStack>> commands = dispatcher.getRoot().getChildren();

        for (CommandNode<CommandSourceStack> command : commands) {
            // Récupère le nom de la commande.
            String commandName = command.getName();
            // Crée une permission spécifique à la commande et l'ajoute.
            String perm = "server.command." + commandName;
            BCKPermissions.addServerPermission(perm, true);
        }
        BCKLog.info(BCKCore.TITLES_COLORS.title(BCKPermissions.class), "§b" + ((DataWrapper) BCKServerdata.data("server.permissions")).getStringArray().length + " §7server permission(s) §aloaded §e" + Arrays.toString(((DataWrapper) BCKServerdata.data("server.permissions")).getStringArray()));
    }

    @Override
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        super.onBlockPlace(event);
        Entity entity = event.getEntity();
        if (entity == null) return;
        if (!BCKPermissions.hasPermission(entity, "block.place")) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            } else if (event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("block.place"))), false);
        }
    }

    @Override
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        super.onBlockBreak(event);
        Entity entity = event.getPlayer();
        if (entity == null) return;
        if (!BCKPermissions.hasPermission(entity, "block.break")) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            } else if (event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("block.break"))), false);
        }
    }

    @Override
    public void onItemDropped(ItemTossEvent event) {
        super.onItemDropped(event);
        Entity entity = event.getPlayer();
        if (entity == null) return;
        if (!BCKPermissions.hasPermission(entity, "item.drop")) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            } else if (event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("item.drop"))), false);
        }
    }

    @Override
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        super.onItemPickup(event);
        Entity entity = event.getEntity();
        if (entity == null) return;
        if (!BCKPermissions.hasPermission(entity, "item.pickup")) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            } else if (event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @Override
    public void onChat(ServerChatEvent event) {
        super.onChat(event);
        Player entity = event.getPlayer();
        if (entity == null) return;
        if (!BCKPermissions.hasPermission(entity, "player.send_message")) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            } else if (event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
            if (!entity.level().isClientSide())
                entity.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("player.send_message"))), false);
        }
    }

    @Override
    public void onEntityJump(LivingEvent.LivingJumpEvent event) {
        super.onEntityJump(event);
        Entity entity = event.getEntity();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        if (!BCKPermissions.hasPermission(entity, "player.jump") && (entity instanceof Player || entity instanceof ServerPlayer)) {
            if (!entity.onGround()) {
                BCKUtils.EntityUtil.teleportEntity(entity, x, (y - 0.5), z, false);
            }
        }
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        super.onRightClickBlock(event);
        Entity entity = event.getEntity();
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (entity == null) return;

        if (!BCKPermissions.hasPermission(entity, "player.interact")) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            } else if (event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
            TetralibsMod.queueServerWork(5, () -> {
                if (entity instanceof Player _player) _player.closeContainer();
            });
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("player.interact"))), false);
        }
    }

    @Override
    public void onEntityAttacked(LivingAttackEvent event) {
        super.onEntityAttacked(event);
        if (event != null && event.getEntity() != null) {
            Entity sourceentity = event.getSource().getEntity();
            if (sourceentity == null) return;
            if (sourceentity.level().isClientSide()) return;

            if (BCKUtils.EntityUtil.isFakePlayer(sourceentity)) {
                if (((DataWrapper) BCKServerdata.data("server.allow_fake_players")).getBoolean()) {
                    return;
                }
            }
            if (!BCKPermissions.hasPermission(sourceentity, "player.attack") && (sourceentity instanceof Player)) {
                if (event.isCancelable()) {
                    event.setCanceled(true);
                } else if (event.hasResult()) {
                    event.setResult(Event.Result.DENY);
                }
                if (sourceentity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("player.attack"))), false);
            }
        }
    }

    @Override
    public void onCommand(CommandEvent event) {
        super.onCommand(event);
        Entity sender = event.getParseResults().getContext().getSource().getEntity();
        String command = event.getParseResults().getReader().getString();

        String[] datas = command.split(" ");
        String converted = ("server.command." + datas[0]);

        // Par défaut, on considère que la commande provient du serveur si l'émetteur est nul.
        String player = "Server";

        // Si l'émetteur est un joueur, on récupère son nom.
        if (sender != null) player = sender.getDisplayName().getString();

        if ((sender instanceof Player || sender instanceof ServerPlayer) && !BCKPermissions.hasPermission(sender, converted)) {
            if (event.isCancelable()) {
                event.setCanceled(true);
                if (ModulesConfig.isEnabled(ModuleIds.BCK_LICH_WHISPER))
                    BCKLichWhisper.send(BCKUtils.TextUtil.toStyled((Component.translatable("lich_whisper.on_try_command", Component.literal(BCKUtils.TextUtil.universal(player, sender)), Component.literal(command)))), 4);
            }
            if (sender instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal(converted))), false);
        }
    }
}