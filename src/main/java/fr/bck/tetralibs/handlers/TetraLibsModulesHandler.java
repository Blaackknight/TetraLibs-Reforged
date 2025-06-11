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

import fr.bck.tetralibs.commands.CommandRegister;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.ModuleManager;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import fr.bck.tetralibs.module.ModuleIds;
import fr.bck.tetralibs.network.BCKNetworkHandler;
import fr.bck.tetralibs.network.SyncModulesPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TetraLibsModulesHandler {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (world.isClientSide()) return;
        // 1) Récupère tous les modules déclarés dans server-config.toml
        Set<ResourceLocation> configured = ModulesConfig.getConfiguredModules();

        // 2) Prépare tes templates
        StringBuilder txt = new StringBuilder("\n");
        String tplTrue = Component.translatable("command.tetralibs.modules.text.per_module.true").getString();
        String tplFalse = Component.translatable("command.tetralibs.modules.text.per_module.false").getString();
        String tplUnrec = Component.translatable("command.tetralibs.modules.text.per_module.unrecognized").getString();
        String tplMissing = Component.translatable("command.tetralibs.modules.text.per_module.missing").getString();

        // 3) Pour chaque id du fichier de config
        for (ResourceLocation id : configured) {
            boolean existsInModuleManager = ModuleManager.modulesContains(id);
            boolean enabledInConfig = ModulesConfig.isEnabled(id);
            boolean defaultState = ModulesConfig.getDefaultState(id);
            String ID = id.equals(ModuleIds.BCK_CORE) ? BCKCore.Utils.color + id.toString() : id.toString();
            String line;
            if (!existsInModuleManager) {
                line = tplUnrec.replace("<module>", ID);
            } else if (!ModulesConfig.defaultModulesContains(id.getPath())) {
                line = tplMissing.replace("<module>", ID);
            } else {
                String base = enabledInConfig ? tplTrue : tplFalse;
                line = base.replace("<module>", ID).replace("<value>", String.valueOf(enabledInConfig)).replace("<default_value>", String.valueOf(defaultState));
            }

            txt.append(line).append("\n");
        }

        // 4) Cas “aucun module dans le fichier”
        if (configured.isEmpty()) {
            txt.append(Component.translatable("command.tetralibs.modules.text.no_modules_available").getString());
        }

        // 5) Envoie du message
        String listText = txt.toString().trim();
        String header = Component.translatable("command.tetralibs.modules.text").getString().replace("<list>", listText).replace("<num>", String.valueOf(configured.size()));

        if (entity == null) {
            BCKLichWhisper.send(Component.nullToEmpty(header), 6, true);
        } else if (entity instanceof Player _player && !_player.level().isClientSide()) {
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(header), false);
        }
    }

    public static void reload(Entity entity) {
        if (!entity.level().isClientSide()) {
            ModulesConfig.reload();
            BCKLog.info(BCKCore.TITLES_COLORS.title(ModulesConfig.class) + "§6/§aReload", "§etetralibs-server.toml §2rechargé");

            ModuleManager.processModules(ModuleManager.Hook.SERVER);
            BCKLog.info(BCKCore.TITLES_COLORS.title(ModulesConfig.class) + "§6/§aReload", BCKCore.TITLES_COLORS.title(ModuleManager.class) + "s §arechargés §7selon la nouvelle §econfig");
            CommandRegister.register(Objects.requireNonNull(entity.getServer()).getCommands().getDispatcher());

            // Et on branche le reload (il ne throwera plus, la config est déjà chargée)
            MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent reload) -> CommandRegister.register(reload.getDispatcher()));
            // Ne transmettre que les modules désactivés côté serveur
            if ((entity instanceof ServerPlayer sp)) {
                Map<String, Boolean> overrides = ModulesConfig.getConfiguredModules().stream().collect(Collectors.toMap(ResourceLocation::getPath, id -> !ModulesConfig.isEnabled(id)  // true = override en false
                ));
                BCKNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncModulesPacket(overrides));
                BCKLog.info(BCKCore.TITLES_COLORS.title(SyncModulesPacket.class), "§9Server modules §e-> §asynchronized §e-> §dClient");
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.tetralibs.modules.reload").getString())), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((Component.translatable("command.tetralibs.error.client_side").getString())), false);
        }
    }
}
