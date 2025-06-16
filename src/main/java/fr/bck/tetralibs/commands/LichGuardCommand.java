package fr.bck.tetralibs.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.handlers.BCKLichGuardHandler;
import fr.bck.tetralibs.lich.BCKLichGuard;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;



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
 * Commande « /lich » : gestion des territoires protégés par BCKLichGuard.
 * <p>
 * Syntaxe :
 * - /lich create <nom> <pos1> <pos2>
 * - /lich delete <nom>
 * - /lich flag <nom> <break|place|interact> <true|false>
 * - /lich addmember <nom> <joueur>
 * - /lich removemember <nom> <joueur>
 * - /lich info <nom>
 */
public class LichGuardCommand {
    public static final class Utils {
        // ta couleur
        public static final String color = "§5§n";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    private static final String ARG_NAME = "name";
    private static final String ARG_POS1 = "pos1";
    private static final String ARG_POS2 = "pos2";
    private static final String ARG_FLAG = "flag";
    private static final String ARG_STATE = "state";
    private static final String ARG_PLAYER = "player";

    private LichGuardCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("lich_guard").requires(src -> src.hasPermission(2));

        root.then(Commands.literal("create").then(Commands.argument(ARG_NAME, StringArgumentType.word()).then(Commands.argument(ARG_POS1, BlockPosArgument.blockPos()).then(Commands.argument(ARG_POS2, BlockPosArgument.blockPos()).executes(arguments -> {
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
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(LichGuardCommand.class), "Fatal when creating area -> ", () -> BCKLichGuardHandler.create(ARG_NAME, ARG_POS1, ARG_POS2, arguments, world, finalEntity));
            return 0;
        })))));

        root.then(Commands.literal("delete").then(Commands.argument(ARG_NAME, StringArgumentType.word()).suggests(LichGuardCommand::suggestTerritories).executes(arguments -> {
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
            BCKCore.doRiskyOperation(BCKCore.TITLES_COLORS.title(LichGuardCommand.class), "Fatal when deleting area -> ", () -> BCKLichGuardHandler.delete(ARG_NAME, ARG_POS1, ARG_POS2, arguments, world, finalEntity));
            return 0;
        })));

        root.then(Commands.literal("flag").then(Commands.argument(ARG_NAME, StringArgumentType.word()).suggests(LichGuardCommand::suggestTerritories).then(Commands.argument(ARG_FLAG, StringArgumentType.word()).suggests((ctx, b) -> {
            for (BCKLichGuard.Flag f : BCKLichGuard.Flag.values())
                b.suggest(f.name().toLowerCase(Locale.ROOT));
            return b.buildFuture();
        }).then(Commands.argument(ARG_STATE, BoolArgumentType.bool()).executes(LichGuardCommand::flag)))));

        root.then(Commands.literal("addmember").then(Commands.argument(ARG_NAME, StringArgumentType.word()).suggests(LichGuardCommand::suggestTerritories).then(Commands.argument(ARG_PLAYER, EntityArgument.player()).executes(LichGuardCommand::addMember))));

        root.then(Commands.literal("removemember").then(Commands.argument(ARG_NAME, StringArgumentType.word()).suggests(LichGuardCommand::suggestTerritories).then(Commands.argument(ARG_PLAYER, EntityArgument.player()).executes(LichGuardCommand::removeMember))));

        root.then(Commands.literal("info").then(Commands.argument(ARG_NAME, StringArgumentType.word()).suggests(LichGuardCommand::suggestTerritories).executes(LichGuardCommand::info)));

        dispatcher.register(root);
    }

    private static int create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BCKLichGuard lich = BCKLichGuard.get(level);

        String name = StringArgumentType.getString(ctx, ARG_NAME);
        BlockPos p1 = BlockPosArgument.getLoadedBlockPos(ctx, ARG_POS1);
        BlockPos p2 = BlockPosArgument.getLoadedBlockPos(ctx, ARG_POS2);

        if (!lich.territoryByName(name).isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cNom déjà pris."));
            return 0;
        }

        lich.createTerritory(name, player, p1, p2);
        ctx.getSource().sendSuccess(() -> Component.literal("§aTerritoire §e" + name + " §acréé."), false);
        return 1;
    }

    private static int delete(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        BCKLichGuard lich = BCKLichGuard.get(level);
        String name = StringArgumentType.getString(ctx, ARG_NAME);

        BCKLichGuard.Territory terr = lich.territoryByName(name);
        if (terr.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cTerritoire introuvable."));
            return 0;
        }
        lich.deleteTerritory(terr.id);
        ctx.getSource().sendSuccess(() -> Component.literal("§cTerritoire §e" + name + " §csupprimé."), false);
        return 1;
    }

    private static int flag(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        BCKLichGuard lich = BCKLichGuard.get(level);
        String name = StringArgumentType.getString(ctx, ARG_NAME);
        String flagStr = StringArgumentType.getString(ctx, ARG_FLAG).toUpperCase(Locale.ROOT);
        boolean state = BoolArgumentType.getBool(ctx, ARG_STATE);

        BCKLichGuard.Territory terr = lich.territoryByName(name);
        if (terr.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cTerritoire introuvable."));
            return 0;
        }
        BCKLichGuard.Flag flag;
        try {
            flag = BCKLichGuard.Flag.valueOf(flagStr);
        } catch (IllegalArgumentException ex) {
            ctx.getSource().sendFailure(Component.literal("§cFlag invalide."));
            return 0;
        }
        terr.flags.put(flag, state);
        lich.setDirty();
        ctx.getSource().sendSuccess(() -> Component.literal("§7" + flag.name().toLowerCase(Locale.ROOT) + " → " + (state ? "§aautorisé" : "§cinterdit")), false);
        return 1;
    }

    private static int addMember(CommandContext<CommandSourceStack> ctx) {
        return toggleMember(ctx, true);
    }

    private static int removeMember(CommandContext<CommandSourceStack> ctx) {
        return toggleMember(ctx, false);
    }

    private static int toggleMember(CommandContext<CommandSourceStack> ctx, boolean add) {
        ServerLevel level = ctx.getSource().getLevel();
        BCKLichGuard lich = BCKLichGuard.get(level);
        String name = StringArgumentType.getString(ctx, ARG_NAME);
        ServerPlayer target;
        try {
            target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(Component.literal("§cJoueur introuvable."));
            return 0;
        }

        BCKLichGuard.Territory terr = lich.territoryByName(name);
        if (terr.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cTerritoire introuvable."));
            return 0;
        }
        UUID uuid = target.getUUID();
        boolean changed = add ? terr.members.add(uuid) : terr.members.remove(uuid);
        if (changed) {
            lich.setDirty();
            ctx.getSource().sendSuccess(() -> Component.literal((add ? "§aAjouté §7" : "§cRetiré §7") + target.getName().getString() + " dans §e" + name), false);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("§cAucune modification."));
        return 0;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        BCKLichGuard lich = BCKLichGuard.get(level);
        String name = StringArgumentType.getString(ctx, ARG_NAME);

        BCKLichGuard.Territory terr = lich.territoryByName(name);
        if (terr.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cTerritoire introuvable."));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("§6--- §e" + terr.name + " §6---"), false);
        String ownerName = level.getPlayerByUUID(terr.owner) != null ? Objects.requireNonNull(level.getPlayerByUUID(terr.owner)).getName().getString() : terr.owner.toString();
        ctx.getSource().sendSuccess(() -> Component.literal("§7Owner : §e" + ownerName), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7Membres : §e" + terr.members.size()), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7Flags : §e" + terr.flags), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7Min : §e" + terr.min + "  §7Max : §e" + terr.max), false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestTerritories(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ServerLevel level = ctx.getSource().getLevel();
        Set<String> names = BCKLichGuard.get(level).getTerritoryNames();
        names.forEach(builder::suggest);
        return builder.buildFuture();
    }
}