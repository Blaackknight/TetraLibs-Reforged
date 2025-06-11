package fr.bck.tetralibs.handlers;


/*≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡≡
 ≡              Copyright BCK, Inc 2025. (DragClover / Blackknight)              ≡
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

public class BCKHomeHandler {
    public static void set(LevelAccessor world, double x, double y, double z, CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String home = StringArgumentType.getString(arguments, "name");
        if ((home).isEmpty()) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.sethome.empty").getString())), false);
        } else if (BCKHomeManager.has(entity, home)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.sethome.already").getString())), false);
        } else if (BCKHomeManager.count(entity) < ((DataWrapper) BCKServerdata.data("homes.max_count")).getInt()) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                BCKHomeManager.setHome(_player, (StringArgumentType.getString(arguments, "name")), _player.level().dimension().location().toString(), x, y, z, (entity.getXRot()), (entity.getYRot()));
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((((((((Component.translatable("command.sethome.success").getString()).replace("<name>", StringArgumentType.getString(arguments, "name"))).replace("<player>", entity.getName().getString())).replace("<z>", "" + z)).replace("<y>", "" + y)).replace("<x>", "" + x)).replace("<world>", entity.level().dimension().location().toString())).replace("<pitch>", "" + entity.getXRot())).replace("<yaw>", "" + entity.getYRot()))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.sethome.max").getString()).replace("<count>", "" + ((DataWrapper) BCKServerdata.data("homes.max_count")).getInt()))), false);
        }
    }

    public static void del(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String home = StringArgumentType.getString(arguments, "name");
        if (!BCKHomeManager.has(entity, home)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.delhome.unknown").getString()).replace("<name>", home))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                BCKHomeManager.delHome(_player, home);
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.delhome.success").getString()).replace("<name>", home))), false);
        }
    }

    public static void home(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        String home = StringArgumentType.getString(arguments, "name");
        if (!BCKHomeManager.has(entity, home)) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.home.unknown").getString()).replace("<name>", home))), false);
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
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.homes.player.no_homes").getString()).replace("<player>", (new Object() {
                        public Entity getEntity() {
                            try {
                                return EntityArgument.getEntity(arguments, "player");
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }.getEntity()).getName().getString()))), false);
            } else {
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.homes.player.homes.top").getString()).replace("<player>", (new Object() {
                        public Entity getEntity() {
                            try {
                                return EntityArgument.getEntity(arguments, "player");
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }.getEntity()).getName().getString())).replace("<count>", new java.text.DecimalFormat("##.##").format(BCKHomeManager.count((new Object() {
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
                        // Modèle de texte pour chaque home
                        String template = Component.translatable("command.homes.player.homes.middle").getString();
                        // Remplacement des valeurs dans le modèle
                        String homeText = template.replace("<num>", String.valueOf(i + 1)).replace("<name>", name).replace("<dimension>", dimension).replace("<x>", String.format("%.2f", x)).replace("<y>", String.format("%.2f", y)).replace("<z>", String.format("%.2f", z));
                        _player.displayClientMessage(BCKUtils.TextUtil.toStyled(homeText), false);
                    }
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.homes.player.homes.bottom").getString())), false);
            }
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("permissions.no_permission").getString())), false);
        }
    }

    public static void homesGet(Entity entity) {
        if (entity == null) return;
        if (BCKHomeManager.count(entity) == 0) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.homes.no_homes").getString()).replace("<player>", entity.getName().getString()))), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.homes.bis.homes.top").getString()).replace("<player>", entity.getName().getString())).replace("<count>", new java.text.DecimalFormat("##.##").format(BCKHomeManager.count(entity))))), false);
            if (entity instanceof Player _player && !_player.level().isClientSide())
                // Corps (liste des homes)
                for (int i = 0; i < BCKHomeManager.homes(_player).size(); i++) {
                    com.google.gson.JsonObject home = BCKHomeManager.homes(_player).get(i).getAsJsonObject();
                    String name = home.has("name") ? home.get("name").getAsString() : "Inconnu";
                    String dimension = home.has("dimension") ? home.get("dimension").getAsString() : "Inconnue";
                    double x = home.has("x") ? home.get("x").getAsDouble() : 0;
                    double y = home.has("y") ? home.get("y").getAsDouble() : 0;
                    double z = home.has("z") ? home.get("z").getAsDouble() : 0;
                    // Modèle de texte pour chaque home
                    String template = Component.translatable("command.homes.player.homes.middle").getString();
                    // Remplacement des valeurs dans le modèle
                    String homeText = template.replace("<num>", String.valueOf(i + 1)).replace("<name>", name).replace("<dimension>", dimension).replace("<x>", String.format("%.2f", x)).replace("<y>", String.format("%.2f", y)).replace("<z>", String.format("%.2f", z));
                    _player.displayClientMessage(BCKUtils.TextUtil.toStyled(homeText), false);
                }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.homes.player.homes.bottom").getString())), false);
        }
    }
}
