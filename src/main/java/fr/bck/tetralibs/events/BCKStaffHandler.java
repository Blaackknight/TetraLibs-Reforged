package fr.bck.tetralibs.events;

import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.init.BCKItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;



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

public class BCKStaffHandler {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (!BCKUtils.EntityUtil.getMainHandItem(event.getEntity()).equals(new ItemStack(BCKItems.BCK_STAFF.get())))
            return;
        LevelAccessor world = event.getLevel();
        Entity entity = event.getEntity();
        BlockPos pos = event.getPos();
        if (world.isClientSide()) return;
        if (entity.isShiftKeyDown()) {
            BCKUtils.EntityUtil.playSound(entity, ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("minecraft:block.respawn_anchor.charge")), 1, 1);
            if (entity instanceof Player _player)
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("bck_staff.right_click.pos2.success", Component.literal(String.valueOf(pos)))), false);

        } else {
            BCKUtils.EntityUtil.playSound(entity, ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("minecraft:entity.experience_orb.pickup")), 1, 1);
            if (entity instanceof Player _player)
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("bck_staff.right_click.pos1.success", Component.literal(String.valueOf(pos)))), false);
        }
        if (event.isCancelable()) {
            event.setCanceled(true);
        } else if (event.hasResult()) {
            event.setResult(Event.Result.DENY);
        }

    }
}
