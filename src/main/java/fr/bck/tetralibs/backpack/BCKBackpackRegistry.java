package fr.bck.tetralibs.backpack;


import fr.bck.tetralibs.TetralibsMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;



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

public final class BCKBackpackRegistry {
    public static final String MODID = TetralibsMod.MODID;

    /**
     * Marqueur pour les menus custom
     */
    public interface IBackpackMenu {
    }

    /**
     * Marqueur pour les écrans custom
     */
    public interface IBackpackScreen {
    }

    /**
     * Factory d’écran typée : M = le menu, S = l’écran (Screen & MenuAccess<M>)
     */
    @FunctionalInterface
    public interface ScreenFactory<M extends BCKBackpackMenu & IBackpackMenu, S extends Screen & MenuAccess<M>> {
        S create(M menu, Inventory inv, Component title);
    }

    // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // DeferredRegister
    // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    private static final Map<ResourceLocation, BCKBackpack> TIERS = new LinkedHashMap<>();

    // ────────────────────────────────────────────────────────────────────────────────
    // Méthode interne générique “tout en un”
    // ────────────────────────────────────────────────────────────────────────────────
    private static <M extends BCKBackpackMenu & IBackpackMenu, S extends Screen & MenuAccess<M>> BCKBackpack registerTierInternal(String id, int slots, RegistryObject<Item> item, @Nullable IContainerFactory<M> menuFactory, ScreenFactory<M, S> screenFactory) {
        // 1) Factory par défaut si on en a pas
        @SuppressWarnings("unchecked") IContainerFactory<M> factory = (menuFactory != null) ? menuFactory : (windowId, inv, buf) -> (M) new BCKBackpackMenu(windowId, inv, buf);

        // 2) Enregistrement du MenuType
        RegistryObject<MenuType<M>> menuRO = MENUS.register(id, () -> IForgeMenuType.create(factory));

        // 3) Création du DTO
        BCKBackpack tier = new BCKBackpack(id, slots, item, (RegistryObject) menuRO, (m, inv, title) -> screenFactory.create((M) m, inv, title));
        TIERS.put(tier.rl(), tier);
        return tier;
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // 1) Enregistrement “tout générique” : pas de custom
    // ────────────────────────────────────────────────────────────────────────────────
    public static BCKBackpack registerTier(String id, int slots, RegistryObject<Item> item) {
        return registerTierInternal(id, slots, item,
                /* menuFactory = */ null,
                /* screenFactory = */ BCKBackpackScreen::new);
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // 2) Enregistrement avec écran custom seulement
    // ────────────────────────────────────────────────────────────────────────────────
    public static <S extends Screen & MenuAccess<BCKBackpackMenu>> BCKBackpack registerTier(String id, int slots, RegistryObject<Item> item, ScreenFactory<BCKBackpackMenu, S> screenFactory) {
        return registerTierInternal(id, slots, item,
                /* menuFactory = */ null,
                /* screenFactory = */ screenFactory);
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // 3) Enregistrement complet menu + écran custom
    // ────────────────────────────────────────────────────────────────────────────────
    public static <M extends BCKBackpackMenu & IBackpackMenu, S extends Screen & MenuAccess<M>> BCKBackpack registerTier(String id, int slots, RegistryObject<Item> item, IContainerFactory<M> menuFactory, ScreenFactory<M, S> screenFactory) {
        return registerTierInternal(id, slots, item,
                /* menuFactory = */ menuFactory,
                /* screenFactory = */ screenFactory);
    }

    // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // Client-side : enregistrement des écrans
    // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            TIERS.values().forEach(t -> {
                // Récupère le MenuType (pas besoin d'annotation ici)
                MenuType<? extends AbstractContainerMenu> menuType = t.menu.get();

                // Enregistrement : on précise le type exact des paramètres de la lambda
                //MenuScreens.register(
                //    menuType,
                //    (BackpackGUIMenu menu, Inventory inv, Component title) ->
                //        t.screenFactory.create(menu, inv, title)
                //);
            });
        });
    }

    // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    // Helpers
    // ––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––
    public static int getSlots(ItemStack stack) {
        return TIERS.values().stream().filter(t -> stack.is(t.item.get())).mapToInt(t -> t.slots).findFirst().orElse(0);
    }

    public static MenuType<?> getMenuType(ItemStack stack) {
        return TIERS.values().stream().filter(t -> stack.is(t.item.get())).map(t -> t.menu.get()).findFirst().orElseThrow();
    }

    public static boolean isBackpack(ItemStack s) {
        return getSlots(s) > 0;
    }

    public static java.util.Collection<BCKBackpack> getTiers() {
        return TIERS.values();
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        MENUS.register(modBus);
    }

    private BCKBackpackRegistry() {
    }
}
