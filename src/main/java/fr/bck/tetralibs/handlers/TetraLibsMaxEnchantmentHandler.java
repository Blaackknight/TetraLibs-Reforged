package fr.bck.tetralibs.handlers;


import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.bck.tetralibs.core.BCKUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;




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

public class TetraLibsMaxEnchantmentHandler {
    public static void apply(CommandContext<CommandSourceStack> arguments, Entity entity) {
        boolean verif = BoolArgumentType.getBool(arguments, "verif");
        ItemStack i = BCKUtils.EntityUtil.getMainHandItem(entity);
        if (i == ItemStack.EMPTY) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.max_enchantment.wrong")), true);
            return;
        }
        BCKUtils.EnchantmentUtil.applyMaxEnchantments(i, verif);
        BCKUtils.EntityUtil.playSound(entity, SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("minecraft", "block.anvil.place")), 10, 2);

        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.max_enchantment.success", Component.literal(i.getDisplayName().getString()))), true);
    }

    public static void apply(Entity entity) {
        ItemStack i = BCKUtils.EntityUtil.getMainHandItem(entity);
        if (i == ItemStack.EMPTY) {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.max_enchantment.wrong")), true);
            return;
        }
        BCKUtils.EnchantmentUtil.applyMaxEnchantments(i);
        BCKUtils.EntityUtil.playSound(entity, SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("minecraft", "block.anvil.place")), 10, 2);

        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.max_enchantment.success", Component.literal(i.getDisplayName().getString()))), true);
    }
}
