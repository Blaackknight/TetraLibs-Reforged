package fr.bck.tetralibs.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKSuggestionProvider;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.data.BCKUserdata;
import fr.bck.tetralibs.economy.BCKEconomyManager;
import fr.bck.tetralibs.handlers.*;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import fr.bck.tetralibs.module.ModuleIds;
import fr.bck.tetralibs.page.TetraPage;
import fr.bck.tetralibs.permissions.BCKPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.Collection;
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

public class TetraLibsCommand {
    public static final class Utils {
        // ta couleur
        public static final String color = "§a";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // noeud racine /tetralibs
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("tetralibs").requires(src -> src.hasPermission(4));
        root.then(Commands.literal("help")
                // Si aucun numéro de page n'est spécifié, afficher la page 1 par défaut
                .executes(context -> {
                    int page = 1; // Page par défaut
                    TetraPage.setPlayerPage(context.getSource().getEntity(), page);
                    TetraPage.display(context.getSource(), context.getSource().getEntity());
                    return 1;
                })
                // Si un numéro de page est spécifié, utiliser ce numéro
                .then(Commands.argument("page", IntegerArgumentType.integer(1, 999999999)).executes(context -> {
                    int page = IntegerArgumentType.getInteger(context, "page");
                    if ((int) TetraPage.getPlayerPage(context.getSource().getEntity()) <= TetraPage.getPages.size() - 1) {
                        page = TetraPage.getPages.size();
                    }
                    TetraPage.setPlayerPage(context.getSource().getEntity(), page);
                    TetraPage.display(context.getSource(), context.getSource().getEntity());
                    return 1;
                }))).then(Commands.literal("dev")
                // Si aucun numéro de page n'est spécifié, afficher la page 1 par défaut
                .executes(context -> {
                    TetraLibsDevHandler.execute(context.getSource().getEntity());
                    return 0;
                })).then(Commands.literal("play")
                // Si aucun numéro de page n'est spécifié, afficher la page 1 par défaut
                .executes(context -> {
                    TetraLibsDevHandler.play(Objects.requireNonNull(context.getSource().getEntity()));
                    return 0;
                })).then(Commands.literal("stop")
                // Si aucun numéro de page n'est spécifié, afficher la page 1 par défaut
                .executes(context -> {
                    TetraLibsDevHandler.stop(Objects.requireNonNull(context.getSource().getEntity()));
                    return 0;
                })).then(Commands.literal("text")
                // Si aucun numéro de page n'est spécifié, afficher la page 1 par défaut
                .executes(arguments -> {
                    Level world = arguments.getSource().getUnsidedLevel();
                    double x = arguments.getSource().getPosition().x();
                    double y = arguments.getSource().getPosition().y();
                    double z = arguments.getSource().getPosition().z();
                    Entity entity = arguments.getSource().getEntity();
                    if (entity == null && world instanceof ServerLevel _servLevel)
                        entity = FakePlayerFactory.getMinecraft(_servLevel);
                    Direction direction = Direction.DOWN;
                    if (entity != null) direction = entity.getDirection();
                    assert entity != null;
                    Entity finalEntity = entity;
                    BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when sending dev text -> ", () -> TetraLibsDevHandler.text(finalEntity));
                    return 0;
                })).then(Commands.literal("modules").then(Commands.literal("reload").executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            double x = arguments.getSource().getPosition().x();
            double y = arguments.getSource().getPosition().y();
            double z = arguments.getSource().getPosition().z();
            Entity entity = arguments.getSource().getEntity();
            if (entity == null && world instanceof ServerLevel _servLevel)
                entity = FakePlayerFactory.getMinecraft(_servLevel);
            Direction direction = Direction.DOWN;
            if (entity != null) direction = entity.getDirection();

            assert entity != null;
            Entity finalEntity = entity;
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when reloading modules -> ", () -> TetraLibsModulesHandler.reload(finalEntity));
            return 0;
        })).executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            double x = arguments.getSource().getPosition().x();
            double y = arguments.getSource().getPosition().y();
            double z = arguments.getSource().getPosition().z();
            Entity entity = arguments.getSource().getEntity();
            if (entity == null && world instanceof ServerLevel _servLevel)
                entity = FakePlayerFactory.getMinecraft(_servLevel);
            Direction direction = Direction.DOWN;
            if (entity != null) direction = entity.getDirection();

            Entity finalEntity = entity;
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when getting modules -> ", () -> TetraLibsModulesHandler.execute(world, x, y, z, finalEntity));
            return 0;
        })).then(Commands.literal("randomitem").then(Commands.argument("count", DoubleArgumentType.doubleArg(1)).executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            double x = arguments.getSource().getPosition().x();
            double y = arguments.getSource().getPosition().y();
            double z = arguments.getSource().getPosition().z();
            Entity entity = arguments.getSource().getEntity();
            if (entity == null && world instanceof ServerLevel _servLevel)
                entity = FakePlayerFactory.getMinecraft(_servLevel);
            Direction direction = Direction.DOWN;
            if (entity != null) direction = entity.getDirection();

            Entity finalEntity = entity;
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when giving random item -> ", () -> TetraLibsRandomItemHandler.give(world, x, y, z, arguments, finalEntity));
            return 0;
        })).executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            double x = arguments.getSource().getPosition().x();
            double y = arguments.getSource().getPosition().y();
            double z = arguments.getSource().getPosition().z();
            Entity entity = arguments.getSource().getEntity();
            if (entity == null && world instanceof ServerLevel _servLevel)
                entity = FakePlayerFactory.getMinecraft(_servLevel);
            Direction direction = Direction.DOWN;
            if (entity != null) direction = entity.getDirection();

            Entity finalEntity = entity;
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when giving 1 random item -> ", () -> TetraLibsRandomItemHandler.giveOne(world, x, y, z, finalEntity));
            return 0;
        })).then(Commands.literal("randomenchantment").executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            double x = arguments.getSource().getPosition().x();
            double y = arguments.getSource().getPosition().y();
            double z = arguments.getSource().getPosition().z();
            Entity entity = arguments.getSource().getEntity();
            if (entity == null && world instanceof ServerLevel _servLevel)
                entity = FakePlayerFactory.getMinecraft(_servLevel);
            Direction direction = Direction.DOWN;
            if (entity != null) direction = entity.getDirection();

            Entity finalEntity = entity;
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when applying random enchantment -> ", () -> TetraLibsRandomEnchantmentHandler.execute(finalEntity));
            return 0;
        })).then(Commands.literal("maxenchantment").then(Commands.argument("verif", BoolArgumentType.bool()).executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            double x = arguments.getSource().getPosition().x();
            double y = arguments.getSource().getPosition().y();
            double z = arguments.getSource().getPosition().z();
            Entity entity = arguments.getSource().getEntity();
            if (entity == null && world instanceof ServerLevel _servLevel)
                entity = FakePlayerFactory.getMinecraft(_servLevel);
            Direction direction = Direction.DOWN;
            if (entity != null) direction = entity.getDirection();

            Entity finalEntity = entity;
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when applying max enchantment -> ", () -> TetraLibsMaxEnchantmentHandler.apply(arguments, finalEntity));
            return 1;
        })).executes(arguments -> {
            Level world = arguments.getSource().getUnsidedLevel();
            double x = arguments.getSource().getPosition().x();
            double y = arguments.getSource().getPosition().y();
            double z = arguments.getSource().getPosition().z();
            Entity entity = arguments.getSource().getEntity();
            if (entity == null && world instanceof ServerLevel _servLevel)
                entity = FakePlayerFactory.getMinecraft(_servLevel);
            Direction direction = Direction.DOWN;
            if (entity != null) direction = entity.getDirection();

            Entity finalEntity = entity;
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when applying max enchantment -> ", () -> TetraLibsMaxEnchantmentHandler.apply(finalEntity));
            return 0;
        }));

        if (ModulesConfig.isEnabled(ModuleIds.BCK_SERVERDATA)) {
            root.then(Commands.literal("serverdata").then(Commands.literal("list").executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when getting serverdata list -> ", () -> TetraLibsServerdataHandler.list(finalEntity));
                return 0;
            })).then(Commands.literal("set").then(Commands.argument("key", StringArgumentType.word()).suggests((context, builder) -> {
                //Entity player = EntityArgument.getPlayer(context, "player");

                return BCKSuggestionProvider.createSuggestionProviderFromList(BCKServerdata.getKeys()).getSuggestions(context, builder);
            }).then(Commands.argument("value", StringArgumentType.greedyString()).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when setting serverdata key -> ", () -> TetraLibsServerdataHandler.set(world, x, y, z, arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("get").then(Commands.argument("key", StringArgumentType.word()).suggests((context, builder) -> {
                //Entity player = EntityArgument.getPlayer(context, "player");

                return BCKSuggestionProvider.createSuggestionProviderFromList(BCKServerdata.getKeys()).getSuggestions(context, builder);
            }).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when getting serverdata key -> ", () -> TetraLibsServerdataHandler.get(arguments, finalEntity));
                return 0;
            }))));
            BCKLog.debug(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), BCKCore.TITLES_COLORS.title(BCKServerdata.class) + " §earguments §aloaded§7.");
        }

        if (ModulesConfig.isEnabled(ModuleIds.BCK_USERDATA)) {
            root.then(Commands.literal("userdata").then(Commands.literal("list").executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when getting userdata list -> ", () -> TetraLibsUserdataHandler.list(finalEntity));
                return 0;
            })).then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("key", StringArgumentType.word()).suggests((context, builder) -> {
                Entity player = EntityArgument.getPlayer(context, "player");

                return BCKSuggestionProvider.createSuggestionProviderFromList(BCKUserdata.getKeys(player)).getSuggestions(context, builder);
            }).then(Commands.argument("value", StringArgumentType.string()).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when setting userdata key -> ", () -> TetraLibsUserdataHandler.set(arguments, finalEntity));
                return 0;
            }))))).then(Commands.literal("get").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("key", StringArgumentType.word()).suggests((context, builder) -> {
                Entity player = EntityArgument.getPlayer(context, "player");

                return BCKSuggestionProvider.createSuggestionProviderFromList(BCKUserdata.getKeys(player)).getSuggestions(context, builder);
            }).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when getting userdata key -> ", () -> TetraLibsUserdataHandler.get(arguments, finalEntity));
                return 0;
            })))));
            BCKLog.debug(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), BCKCore.TITLES_COLORS.title(BCKUserdata.class) + " §earguments §aloaded§7.");
        }

        if (ModulesConfig.isEnabled(ModuleIds.BCK_ECONOMY_MANAGER)) {
            root.then(Commands.literal("economy").then(Commands.literal("bank").then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1)).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager adding to bank -> ", () -> TetraLibsEconomyhandler.Bank.add(arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("remove").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1)).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager removing from bank -> ", () -> TetraLibsEconomyhandler.Bank.remove(arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0)).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager setting bank -> ", () -> TetraLibsEconomyhandler.Bank.set(arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("get").then(Commands.argument("player", EntityArgument.player()).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager getting bank -> ", () -> TetraLibsEconomyhandler.Bank.get(arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("player").then(Commands.literal("add").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1)).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager adding to pocket -> ", () -> TetraLibsEconomyhandler.Pocket.add(arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("remove").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.1)).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager removing from pocket -> ", () -> TetraLibsEconomyhandler.Pocket.remove(arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("set").then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("amount", DoubleArgumentType.doubleArg(0)).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager setting pocket -> ", () -> TetraLibsEconomyhandler.Pocket.set(arguments, finalEntity));
                return 0;
            })))).then(Commands.literal("get").then(Commands.argument("player", EntityArgument.player()).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when economy_manager getting pocket -> ", () -> TetraLibsEconomyhandler.Pocket.get(arguments, finalEntity));
                return 0;
            })))));
            BCKLog.debug(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), BCKCore.TITLES_COLORS.title(BCKEconomyManager.class) + " §earguments §aloaded§7.");
        }

        if (ModulesConfig.isEnabled(ModuleIds.BCK_LICH_WHISPER)) {
            root.then(Commands.literal("lichwhisper").then(Commands.argument("level", StringArgumentType.word()).suggests((context, builder) -> {
                //EntityArgument player = EntityArgument.player();

                return BCKSuggestionProvider.createSuggestionProviderFromList(BCKLichWhisper.defaultLog).getSuggestions(context, builder);
            }).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when lich_whisper setting -> ", () -> TetraLibsLichWhisperHandler.set(arguments, finalEntity));
                return 0;
            })).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when lich_whisper getting -> ", () -> TetraLibsLichWhisperHandler.get(finalEntity));
                return 0;
            }));
            BCKLog.debug(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), BCKCore.TITLES_COLORS.title(BCKLichWhisper.class) + " §earguments §aloaded§7.");
        }

        if (ModulesConfig.isEnabled(ModuleIds.BCK_PERMISSIONS)) {
            root.then(Commands.literal("permissions").executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when permissions getting list -> ", () -> TetraLibsPermissionsHandler.list(finalEntity));
                return 0;
            }).then(Commands.argument("players", EntityArgument.players()).then(Commands.literal("list").executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when permissions getting player list -> ", () -> TetraLibsPermissionsHandler.playerList(arguments, finalEntity));
                return 0;
            })).then(Commands.literal("add").then(Commands.argument("permission", StringArgumentType.word()).suggests((context, builder) -> {
                Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                if (players.size() == 1) {
                    Entity player = (Entity) players.iterator().next();
                    return BCKSuggestionProvider.createSuggestionProvider("server_permissions_without_player_permissions", player).getSuggestions(context, builder);
                } else {
                    return BCKSuggestionProvider.createSuggestionProviderFromList(BCKPermissions.getServerPermissions()).getSuggestions(context, builder);
                }
            }).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when permissions adding -> ", () -> TetraLibsPermissionsHandler.playerAdd(arguments, finalEntity));
                return 0;
            }))).then(Commands.literal("remove").then(Commands.argument("permission", StringArgumentType.word()).suggests((context, builder) -> {
                Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
                if (players.size() == 1) {
                    Entity player = (Entity) players.iterator().next();
                    return BCKSuggestionProvider.createSuggestionProvider("player_permissions", player).getSuggestions(context, builder);
                } else {
                    return BCKSuggestionProvider.createSuggestionProviderFromList(BCKPermissions.getServerPermissions()).getSuggestions(context, builder);
                }
            }).executes(arguments -> {
                Level world = arguments.getSource().getUnsidedLevel();
                double x = arguments.getSource().getPosition().x();
                double y = arguments.getSource().getPosition().y();
                double z = arguments.getSource().getPosition().z();
                Entity entity = arguments.getSource().getEntity();
                if (entity == null && world instanceof ServerLevel _servLevel)
                    entity = FakePlayerFactory.getMinecraft(_servLevel);
                Direction direction = Direction.DOWN;
                if (entity != null) direction = entity.getDirection();

                Entity finalEntity = entity;
                BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), "Fatal when permissions removing -> ", () -> TetraLibsPermissionsHandler.playerRemove(arguments, finalEntity));
                return 0;
            })))));
            BCKLog.debug(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), BCKCore.TITLES_COLORS.title(BCKPermissions.class) + " §earguments §aloaded§7.");
        }
        dispatcher.register(root);
    }
}
