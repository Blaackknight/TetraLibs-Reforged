package fr.bck.tetralibs.backpack;


import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
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

public class BCKBackpackInvProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("tetra_libs", "inventory");

    private static final String INV_TAG = "Inventory";
    private static final String UUID_TAG = "BackpackUUID";

    private final ItemStack stack;
    private final ItemStackHandler handler;
    private final LazyOptional<IItemHandler> opt;

    /* ------------------------------------------------------------------ */
    public BCKBackpackInvProvider(ItemStack stack, int slots) {
        this.stack = stack;

        /* ① UUID permanent ------------------------------------------- */
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(UUID_TAG)) tag.putUUID(UUID_TAG, UUID.randomUUID());

        /* ② Inventaire interne -------------------------------------- */
        this.handler = new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                stack.setTag(serializeNBT());          // maj immédiate
                // ~~plus de backup ici~~
            }
        };
        if (tag.contains(INV_TAG)) handler.deserializeNBT(tag.getCompound(INV_TAG));

        this.opt = LazyOptional.of(() -> handler);
    }

    /* helper UUID ----------------------------------------------------- */
    public static @Nullable UUID getUUID(ItemStack stack) {
        return stack.hasTag() && Objects.requireNonNull(stack.getTag()).hasUUID(UUID_TAG) ? stack.getTag().getUUID(UUID_TAG) : null;
    }

    /* Capability lookup ---------------------------------------------- */
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? opt.cast() : LazyOptional.empty();
    }

    /* Sérialisation NBT ---------------------------------------------- */
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(INV_TAG, handler.serializeNBT());
        return tag;                                    // UUID déjà présent
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(INV_TAG)) handler.deserializeNBT(nbt.getCompound(INV_TAG));
    }
}
