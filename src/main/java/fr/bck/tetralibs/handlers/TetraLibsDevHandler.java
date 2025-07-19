package fr.bck.tetralibs.handlers;


import fr.bck.tetralibs.commands.TetraLibsCommand;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.init.BCKSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import static fr.bck.tetralibs.core.BCKUtils.ConfigsUtils.dumpServerConfig;




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

public class TetraLibsDevHandler {
    public static void execute(Entity entity) {
        if (!((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem())) {
            if (entity instanceof Player _player && !_player.level().isClientSide()) {
                _player.displayClientMessage(Component.literal(BCKUtils.ItemUtil.itemStackToString(_player.getMainHandItem())), false);
            }
        }
    }

    public static void play(Entity entity) {
        BCKUtils.EntityUtil.playSoundTrackingEntityAndSelf(entity, BCKSounds.MII.get().getLocation(), 1.0f, 1.0f);
    }

    public static void stop(Entity entity) {
        BCKUtils.EntityUtil.stopSoundTrackingEntityAndSelf(entity, BCKSounds.MII.get().getLocation());
    }

    public static void text(Entity entity) {
        dumpServerConfig().forEach((k, v) -> BCKLog.debug(BCKCore.TITLES_COLORS.title(TetraLibsCommand.class), k + " -> " + v));
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("bck_combat.in_combat", Component.literal("20"))), false);
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("bck_combat.in_combat").getString().replace("%1$s", "100")), false);
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(Component.translatable("bck_combat.in_combat", Component.literal(String.valueOf(50))), false);
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(Component.translatable("bck_combat.in_combat", Component.literal(String.valueOf(50)).withStyle(ChatFormatting.DARK_GREEN)), false);
    }
}
