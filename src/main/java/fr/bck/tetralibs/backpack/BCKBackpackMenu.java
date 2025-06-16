package fr.bck.tetralibs.backpack;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;



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

public class BCKBackpackMenu extends AbstractContainerMenu implements BCKBackpackRegistry.IBackpackMenu {

    private final ItemStackHandler handler;           // inventaire du sac
    private final ItemStack boundStack;        // l’item tenu
    private final int rows;              // 1 – 6  lignes

    /**
     * 🔌 Constructeur “network” utilisé par IContainerFactory :
     * lit d’abord l’ItemStack puis le nombre de slots dans le même ordre que writeScreenOpeningData()
     */
    public BCKBackpackMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        // lit l’item et le nombre de slots
        this(windowId, playerInv, buf.readItem(),      // correspond à buf.writeItem(...)
                buf.readVarInt());   // correspond à buf.writeVarInt(...)
    }

    /* ------------------------------------------------------------------ */
    public BCKBackpackMenu(int id, Inventory playerInv, ItemStack stack, int slots) {
        super(BCKBackpackRegistry.getMenuType(stack), id);

        this.boundStack = stack;
        this.rows = slots / 9;

        /* récupère la capability si dispo, sinon stub côté client */
        IItemHandler cap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        this.handler = cap instanceof ItemStackHandler ish ? ish : new ItemStackHandler(slots);

        /* slots du backpack ------------------------------------------ */
        for (int r = 0; r < rows; ++r)
            for (int c = 0; c < 9; ++c)
                this.addSlot(new SlotItemHandler(handler, r * 9 + c, 8 + c * 18, 18 + r * 18));

        /* slots joueur ------------------------------------------------ */
        int yInv = 18 + rows * 18 + 14;
        for (int r = 0; r < 3; ++r)
            for (int c = 0; c < 9; ++c)
                this.addSlot(new Slot(playerInv, r * 9 + c + 9, 8 + c * 18, yInv + r * 18));

        for (int c = 0; c < 9; ++c)
            this.addSlot(new Slot(playerInv, c, 8 + c * 18, yInv + 58));
    }

    /* ------------------------------------------------------------------ */
    @Override
    public boolean stillValid(Player p) {
        return p.isAlive() && p.getInventory().contains(boundStack);
    }

    /* ------------------------------------------------------------------ */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int index) {
        Slot slot = getSlot(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        int containerSlots = handler.getSlots();

        if (index < containerSlots) {                    // sac → joueur
            if (!moveItemStackTo(original, containerSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else {                                         // joueur → sac
            if (!moveItemStackTo(original, 0, containerSlots, false)) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        backup(p);                                       // ← snapshot
        return copy;
    }

    @Override
    public void removed(@NotNull Player p) {
        super.removed(p);
        backup(p);                                       // snapshot à la fermeture
    }

    /* util pour la GUI */
    public int getRows() {
        return rows;
    }

    /* ------------------------------------------------------------------ */

    /**
     * Sauvegarde l’ItemStack + inventaire dans le SavedData global
     */
    private void backup(Player p) {
        if (!(p instanceof ServerPlayer sp)) return;

        ServerLevel lvl = sp.serverLevel();
        UUID id = BCKBackpackInvProvider.getUUID(boundStack);
        if (id == null) return;

        CompoundTag snapshot = boundStack.save(new CompoundTag());
        BCKBackpackData.get(lvl).store(id, snapshot);
    }
}
