package fr.bck.tetralibs.handlers;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.permissions.BCKPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;




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

public class TetraLibsPermissionsHandler {
    public static void list(Entity entity) {
        if (entity == null) return;
        if (BCKPermissions.getServerPermissions().length >= 1) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.permissions", Component.literal(BCKPermissions.getFormattedServerPermissions("\u00A77- \u00A78%perm%\u00A7r%nl%", "%perm%")))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.permissions.empty")), false);
        }
    }

    public static void playerList(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        try {
            for (Entity entityiterator : EntityArgument.getEntities(arguments, "players")) {
                if (BCKPermissions.getEntityPermissions(entityiterator).length >= 1) {
                    if (entity instanceof Player _player && !_player.level().isClientSide())
                        _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.permissions.player.list", Component.literal(new BCKPermissions().getFormattedPlayerPermissions(entityiterator, "\u00A77- \u00A7d%perm%\u00A7r%nl%", "%perm%")), Component.literal(entityiterator.getName().getString()))), false);
                } else {
                    if (entity instanceof Player _player && !_player.level().isClientSide())
                        _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.permissions.player.list.empty", Component.literal(entityiterator.getName().getString()))), false);
                }
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void playerAdd(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        try {
            for (Entity entityiterator : EntityArgument.getEntities(arguments, "players")) {
                BCKPermissions.addPermission(entityiterator, (StringArgumentType.getString(arguments, "permission")), false);
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.permissions.player.add.success", Component.literal(entityiterator.getName().getString()), Component.literal(StringArgumentType.getString(arguments, "permission")))), false);
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void playerRemove(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        try {
            for (Entity entityiterator : EntityArgument.getEntities(arguments, "players")) {
                BCKPermissions.removePermission(entityiterator, (StringArgumentType.getString(arguments, "permission")), false);
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.permissions.player.remove.success", Component.literal(entityiterator.getName().getString()), Component.literal(StringArgumentType.getString(arguments, "permission")))), false);
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }
}
