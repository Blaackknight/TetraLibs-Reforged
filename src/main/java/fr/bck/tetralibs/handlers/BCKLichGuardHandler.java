package fr.bck.tetralibs.handlers;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.lich.BCKLichGuard;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

public class BCKLichGuardHandler {
    public static void create(String ARG_NAME, String ARG_POS1, String ARG_POS2, CommandContext<CommandSourceStack> arguments, LevelAccessor world, Entity entity) {
        if (entity instanceof ServerPlayer _pl && world instanceof ServerLevel _wo) {
            BCKLichGuard lich = BCKLichGuard.get(_wo);
            String name = StringArgumentType.getString(arguments, ARG_NAME);
            BlockPos p1 = new BlockPos(0, 0, 0);
            BlockPos p2 = new BlockPos(0, 0, 0);
            try {
                p1 = BlockPosArgument.getLoadedBlockPos(arguments, ARG_POS1);
                p2 = BlockPosArgument.getLoadedBlockPos(arguments, ARG_POS2);
            } catch (Exception ignored) {
            }
            if (!lich.territoryByName(name).isEmpty()) {
                if (!_pl.level().isClientSide())
                    _pl.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.lich_guard.create.already_exist")), true);
            }

            lich.createTerritory(name, _pl, p1, p2);
            if (!_pl.level().isClientSide())
                _pl.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.lich_guard.create.success")), true);
            arguments.getSource().sendSuccess(() -> Component.literal("§aTerritoire §e" + name + " §acréé."), false);
        }
    }

    public static void delete(String ARG_NAME, String ARG_POS1, String ARG_POS2, CommandContext<CommandSourceStack> arguments, LevelAccessor world, Entity entity) {
        if (entity instanceof ServerPlayer _pl && world instanceof ServerLevel _wo) {
            BCKLichGuard lich = BCKLichGuard.get(_wo);
            String name = StringArgumentType.getString(arguments, ARG_NAME);

            BCKLichGuard.Territory terr = lich.territoryByName(name);
            if (terr.isEmpty()) {
                if (!_pl.level().isClientSide())
                    _pl.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.lich_guard.create.already_exist")), true);
            }
        }
    }
}
