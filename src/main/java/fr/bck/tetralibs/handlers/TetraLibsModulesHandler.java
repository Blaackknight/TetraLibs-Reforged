package fr.bck.tetralibs.handlers;


import fr.bck.tetralibs.commands.CommandRegister;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.*;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import fr.bck.tetralibs.module.ModuleIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;




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

public class TetraLibsModulesHandler {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (world.isClientSide()) return;              // 1. seulement serveur

        /* 1) modules déclarés dans le toml */
        Set<ResourceLocation> configured = ModulesConfig.getConfiguredModules();

        /* 2) construit la “liste” sous forme de Component */
        MutableComponent listComp = Component.empty().append("\n");

        for (ResourceLocation id : configured) {
            boolean present = ModuleManager.modulesContains(id);
            boolean enabled = ModulesConfig.isEnabled(id);
            boolean defaultVal = ModulesConfig.getDefaultState(id);

            Component idComp = id.equals(ModuleIds.BCK_CORE) ? Component.literal(BCKCore.Utils.color + id) : Component.literal(id.toString());

            Component line = switch (0) {       // juste pour le pattern-matching
                default -> {                    // on choisit la bonne key
                    if (!present)
                        yield Component.translatable("command.tetralibs.modules.text.per_module.unrecognized", idComp);
                    if (!ModulesConfig.defaultModulesContains(id.getPath()))
                        yield Component.translatable("command.tetralibs.modules.text.per_module.missing", idComp);
                    // true / false (+ 3 paramètres)
                    yield Component.translatable(enabled ? "command.tetralibs.modules.text.per_module.true" : "command.tetralibs.modules.text.per_module.false", idComp, Component.literal(String.valueOf(enabled)), Component.literal(String.valueOf(defaultVal)));
                }
            };

            listComp.append(line).append("\n");
        }

        if (configured.isEmpty()) {
            listComp.append(Component.translatable("command.tetralibs.modules.text.no_modules_available"));
        }

        /* 3) header avec PLACEHOLDERS dans l’ordre num (= %1$s) puis liste (= %2$s) */
        //  ⚠ Assure-toi que la clé ressemble à :
        //  "command.tetralibs.modules.text": "§6[...] Configurations: §b§l§o(%1$s)§r\n%2$s"
        Component header = Component.translatable("command.tetralibs.modules.text", Component.literal(String.valueOf(configured.size())), // %1$s
                listComp                                            // %2$s
        );

        /* 4) envoi */
        if (entity instanceof Player p && !p.level().isClientSide()) {
            p.displayClientMessage(header, false);
        } else {
            // console / absence de joueur
            BCKLichWhisper.send(header, 6, true);
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
                BCKLog.info(BCKCore.TITLES_COLORS.title(TetraLibs.class), "§9Server modules §e-> §asynchronized §e-> §dClient");
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.modules.reload")), false);
        } else {
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(Component.translatable("command.tetralibs.error.client_side")), false);
        }
    }
}
