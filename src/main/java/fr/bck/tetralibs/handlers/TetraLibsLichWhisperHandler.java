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
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class TetraLibsLichWhisperHandler {
    public static void set(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        BCKLichWhisper.set(entity, new Object() {
            double convert(String s) {
                try {
                    return Double.parseDouble(s.trim());
                } catch (Exception e) {
                }
                return 0;
            }
        }.convert(StringArgumentType.getString(arguments, "level")), false);
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.tetralibs.lich_whisper.success").getString()).replace("<level>", StringArgumentType.getString(arguments, "level")))), false);
    }

    public static void get(Entity entity) {
        if (entity == null) return;
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.tetralibs.lich_whisper.get").getString()).replace("<level>", "" + BCKLichWhisper.getLogLevel((ServerPlayer) entity)))), false);
    }
}
