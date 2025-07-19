package fr.bck.tetralibs.handlers;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.home.BCKHomeManager;
import fr.bck.tetralibs.permissions.BCKPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
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

public class BCKHomeHandler {
    public static void set(LevelAccessor world, double x, double y, double z, CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String home = StringArgumentType.getString(arguments, "name");
        if ((home).isEmpty()) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.sethome.empty"))), false);
        } else if (BCKHomeManager.has(entity, home)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.sethome.already"))), false);
        } else if (BCKHomeManager.count(entity) < ((DataWrapper) BCKServerdata.data("homes.max_count")).getInt()) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                BCKHomeManager.setHome(_player, (StringArgumentType.getString(arguments, "name")), _player.level().dimension().location().toString(), x, y, z, (entity.getXRot()), (entity.getYRot()));
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.sethome.success", Component.literal(StringArgumentType.getString(arguments, "name")), Component.literal(entity.getName().getString()), Component.literal(String.valueOf(z)), Component.literal(String.valueOf(y)), Component.literal(String.valueOf(x)), Component.literal(entity.level().dimension().location().toString()), Component.literal(String.valueOf(entity.getXRot())), Component.literal(String.valueOf(entity.getYRot())))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.sethome.max", Component.literal(String.valueOf(((DataWrapper) BCKServerdata.data("homes.max_count")).getInt())))), false);
        }
    }

    public static void del(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String home = StringArgumentType.getString(arguments, "name");
        if (!BCKHomeManager.has(entity, home)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.delhome.unknown", Component.literal(home))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                BCKHomeManager.delHome(_player, home);
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.delhome.success", Component.literal(home))), false);
        }
    }

    public static void home(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String home = StringArgumentType.getString(arguments, "name");
        if (!BCKHomeManager.has(entity, home)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.home.unknown", Component.literal(home))), false);
        } else {
            BCKHomeManager.home(entity, home);
        }
    }

    public static void homesPlayerGet(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        Entity pp = null;
        if (BCKPermissions.hasPermission(entity, "homes.view")) {
            if (BCKHomeManager.count((new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity())) == 0) {
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.player.no_homes", Component.literal(new Object() {
                        public Entity getEntity() {
                            try {
                                return EntityArgument.getEntity(arguments, "player");
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }.getEntity().getName().getString()))), false);
            } else {
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.player.homes.top", Component.literal(new Object() {
                        public Entity getEntity() {
                            try {
                                return EntityArgument.getEntity(arguments, "player");
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }.getEntity().getName().getString()), Component.literal(new java.text.DecimalFormat("##.##").format(BCKHomeManager.count((new Object() {
                        public Entity getEntity() {
                            try {
                                return EntityArgument.getEntity(arguments, "player");
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }.getEntity())))))), false);
                if (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity() instanceof Player _player && !_player.level().isClientSide())
                    // Corps (liste des homes)
                    for (int i = 0; i < BCKHomeManager.homes(_player).size(); i++) {
                        com.google.gson.JsonObject home = BCKHomeManager.homes(_player).get(i).getAsJsonObject();
                        String name = home.has("name") ? home.get("name").getAsString() : "Inconnu";
                        String dimension = home.has("dimension") ? home.get("dimension").getAsString() : "Inconnue";
                        double x = home.has("x") ? home.get("x").getAsDouble() : 0;
                        double y = home.has("y") ? home.get("y").getAsDouble() : 0;
                        double z = home.has("z") ? home.get("z").getAsDouble() : 0;
                        _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.player.homes.middle", Component.literal(String.valueOf(i + 1)), Component.literal(name), Component.literal(dimension), Component.literal(String.format("%.2f", x)), Component.literal(String.format("%.2f", y)), Component.literal(String.format("%.2f", z)))), false);
                    }
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.player.homes.bottom")), false);
            }
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("permissions.no_permission", Component.literal("homes.view"))), false);
        }
    }

    public static void homesGet(Entity entity) {
        if (entity == null) return;
        if (BCKHomeManager.count(entity) == 0) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.no_homes", Component.literal(entity.getName().getString()))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.bis.homes.top", Component.literal(entity.getName().getString()), Component.literal(new java.text.DecimalFormat("##.##").format(BCKHomeManager.count(entity))))), false);
            if (entity instanceof Player _player && !_player.level().isClientSide())
                // Corps (liste des homes)
                for (int i = 0; i < BCKHomeManager.homes(_player).size(); i++) {
                    com.google.gson.JsonObject home = BCKHomeManager.homes(_player).get(i).getAsJsonObject();
                    String name = home.has("name") ? home.get("name").getAsString() : "Inconnu";
                    String dimension = home.has("dimension") ? home.get("dimension").getAsString() : "Inconnue";
                    double x = home.has("x") ? home.get("x").getAsDouble() : 0;
                    double y = home.has("y") ? home.get("y").getAsDouble() : 0;
                    double z = home.has("z") ? home.get("z").getAsDouble() : 0;
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.player.homes.middle", Component.literal(String.valueOf(i + 1)), Component.literal(name), Component.literal(dimension), Component.literal(String.format("%.2f", x)), Component.literal(String.format("%.2f", y)), Component.literal(String.format("%.2f", z)))), false);
                }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.homes.player.homes.bottom")), false);
        }
    }
}
