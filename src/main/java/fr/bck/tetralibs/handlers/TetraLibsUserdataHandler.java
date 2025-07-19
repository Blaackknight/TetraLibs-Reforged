package fr.bck.tetralibs.handlers;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKUserdata;
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

public class TetraLibsUserdataHandler {
    public static void list(Entity entity) {
        if (entity == null) return;
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(Component.literal("Message"), false);
    }

    public static void set(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        BCKUserdata.data((StringArgumentType.getString(arguments, "key")), "set", (StringArgumentType.getString(arguments, "value")), (new Object() {
            public Entity getEntity() {
                try {
                    return EntityArgument.getEntity(arguments, "player");
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.getEntity()).level(), (new Object() {
            public Entity getEntity() {
                try {
                    return EntityArgument.getEntity(arguments, "player");
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }.getEntity()));
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.userdata.set", Component.literal(StringArgumentType.getString(arguments, "key")), Component.literal(StringArgumentType.getString(arguments, "value")), Component.literal(new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity().getName().getString()))), false);
    }

    public static void get(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.userdata.get", Component.literal(StringArgumentType.getString(arguments, "key")), Component.literal(((DataWrapper) BCKUserdata.data((StringArgumentType.getString(arguments, "key")), (new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()).level(), (new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()))).toString()), Component.literal(new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity().getName().getString()))), false);
    }
}
