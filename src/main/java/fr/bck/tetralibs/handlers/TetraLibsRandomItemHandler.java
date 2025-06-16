package fr.bck.tetralibs.handlers;


import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.bck.tetralibs.core.BCKUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.ItemHandlerHelper;



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

public class TetraLibsRandomItemHandler {
    public static void give(LevelAccessor world, double x, double y, double z, CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        double c = 0;
        ItemStack i = ItemStack.EMPTY;
        for (int index0 = 0; index0 < (int) DoubleArgumentType.getDouble(arguments, "count"); index0++) {
            i = BCKUtils.ItemUtil.getRandomItemStack();
            if ((entity instanceof Player _playerHasItem && _playerHasItem.getInventory().contains(new ItemStack(Blocks.AIR))) || (entity instanceof Player __playerHasItem && __playerHasItem.getInventory().contains(i))) {
                if (entity instanceof Player _player) {
                    ItemStack _setstack = i.copy();
                    _setstack.setCount(1);
                    ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
                }
            } else {
                if (world instanceof ServerLevel _level) {
                    ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, i);
                    entityToSpawn.setPickUpDelay(1);
                    _level.addFreshEntity(entityToSpawn);
                }
            }
        }
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.tetralibs.random_item.success").getString()).replace("<count>", "" + (int) DoubleArgumentType.getDouble(arguments, "count")))), true);
    }

    public static void giveOne(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null) return;
        if (entity instanceof Player _playerHasItem && _playerHasItem.getInventory().contains(new ItemStack(Blocks.AIR))) {
            if (entity instanceof Player _player) {
                ItemStack _setstack = BCKUtils.ItemUtil.getRandomItemStack();
                _setstack.setCount(1);
                ItemHandlerHelper.giveItemToPlayer(_player, _setstack);
            }
        } else {
            if (world instanceof ServerLevel _level) {
                ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, BCKUtils.ItemUtil.getRandomItemStack());
                entityToSpawn.setPickUpDelay(1);
                _level.addFreshEntity(entityToSpawn);
            }
        }
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.tetralibs.random_item.success").getString()).replace("<count>", ("" + 1).replace(".0", "")))), true);

    }
}