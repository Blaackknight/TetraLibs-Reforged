package fr.bck.tetralibs;

import fr.bck.tetralibs.config.ModulesClientConfig;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.network.BCKNetworkHandler;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
        fr.bck.tetralibs.init.TetraRegistries.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ModulesConfig.SERVER_SPEC, "tetralibs-server.toml"      // → donnera "tetralibs-server.toml"
        );
        ModulesClientConfig.init();

        // Enregistre ton handler réseau…
        BCKNetworkHandler.register();
        bus.addListener(this::onModConfigLoading);
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

    // End of user code block mod methods
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
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
