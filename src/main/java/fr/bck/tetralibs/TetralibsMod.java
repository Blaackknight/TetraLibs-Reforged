package fr.bck.tetralibs;

import fr.bck.tetralibs.backpack.BCKBackpackRegistry;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.init.BCKItems;
import fr.bck.tetralibs.init.BCKSounds;
import fr.bck.tetralibs.init.BCKTabs;
import fr.bck.tetralibs.init.TetraRegistries;
import fr.bck.tetralibs.network.BCKSoundNetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;



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

@Mod("tetralibs")
public class TetralibsMod {
    public static final Logger LOGGER = LogManager.getLogger(TetralibsMod.class);
    public static final String MODID = "tetralibs";

    public TetralibsMod() {
        // Start of user code block mod constructor
        // End of user code block mod constructor
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Start of user code block mod init
        BCKSounds.REGISTRY.register(bus);
        TetraRegistries.register();
        BCKItems.REGISTRY.register(bus);
        BCKTabs.REGISTRY.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ModulesConfig.SERVER_SPEC, "tetralibs-server.toml"      // → donnera "tetralibs-server.toml"
        );
        BCKBackpackRegistry.init(bus);
        // Enregistre tes handler réseau…
        bus.addListener(this::setupCommon);
        bus.addListener(this::setupClient);
        bus.addListener(this::onModConfigLoading);
        try {
            Class.forName("fr.bck.tetralibs.backpack.BCKBackpackTiers");
        } catch (ClassNotFoundException ignored) {
        }
        // End of user code block mod init
    }

    // Start of user code block mod methods
    private void onModConfigLoading(final ModConfigEvent.Loading event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == ModulesConfig.SERVER_SPEC) {
            // on stocke le chemin exact du fichier
            ModulesConfig.setConfigFile(config.getFullPath());
        }
    }

    private void setupCommon(FMLCommonSetupEvent evt) {
        // sound_control
        evt.enqueueWork(BCKSoundNetworkHandler::registerCommon);
    }

    private void setupClient(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            // Enregistrement des packets sonores uniquement côté client
            //BCKSoundsPackets.register(evt);
        });
    }

    // End of user code block mod methods
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer, Optional<NetworkDirection> direction) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer, direction);
        messageID++;
    }

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0) actions.add(work);
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }
}
