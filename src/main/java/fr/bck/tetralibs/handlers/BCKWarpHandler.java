package fr.bck.tetralibs.handlers;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.permissions.BCKPermissions;
import fr.bck.tetralibs.warps.BCKWarpManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;




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

public class BCKWarpHandler {
    public static void use(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String name = (new Object() {
            public String getMessage() {
                try {
                    return MessageArgument.getMessage(arguments, "name").getString();
                } catch (CommandSyntaxException ignored) {
                    return "";
                }
            }
        }).getMessage();

        if (!BCKPermissions.hasPermission(entity, "warps.use")) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("warps.use"))), false);
        } else if ((name).isEmpty()) {
            Player _player = (Player) entity;
            if (!_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warp.unknown_name")), false);
        } else if (!BCKWarpManager.listWarps().contains(name)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warp.not_exist", Component.literal(name))), false);
        } else if (entity instanceof Player _player && BCKWarpManager.teleportToWarp(name, _player)) {
            if (!_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warp.teleporting", Component.literal(name), Component.literal(new java.text.DecimalFormat("##.##").format(BCKWarpManager.teleportDelay())))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warp.teleporting_error", Component.literal(name))), false);
        }
    }

    public static void create(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String name = (new Object() {
            public String getMessage() {
                try {
                    return MessageArgument.getMessage(arguments, "name").getString();
                } catch (CommandSyntaxException ignored) {
                    return "";
                }
            }
        }).getMessage();
        if (!BCKPermissions.hasPermission(entity, "warps.create")) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("warps.create"))), false);
        } else if ((name).isEmpty()) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warps.unknown_name")), false);
        } else if (BCKWarpManager.listWarps().contains(name)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warps.already_exist", Component.literal(name))), false);
        } else {
            if (entity instanceof Player _player) BCKWarpManager.setWarp(name, _player);
        }
    }

    public static void remove(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String name = (new Object() {
            public String getMessage() {
                try {
                    return MessageArgument.getMessage(arguments, "name").getString();
                } catch (CommandSyntaxException ignored) {
                    return "";
                }
            }
        }).getMessage();
        if (!BCKPermissions.hasPermission(entity, "warps.remove")) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("warps.remove"))), false);
        } else if ((name).isEmpty()) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warps.unknown_name")), false);
        } else if (!BCKWarpManager.listWarps().contains(name)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warps.not_exist", Component.literal(name))), false);
        } else if (BCKWarpManager.removeWarp(name)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warps.removed_successfully", Component.literal(name))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warps.removing_error", Component.literal(name))), false);
        }
    }

    public static void list(LevelAccessor world, double x, double y, double z, CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String[] list = BCKWarpManager.listWarpsArray();
        if (!BCKPermissions.hasPermission(entity, "warps.view")) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("warps.view"))), false);
        } else if (list.length < 1) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.warps.list.empty")), false);
        }
    }
}
