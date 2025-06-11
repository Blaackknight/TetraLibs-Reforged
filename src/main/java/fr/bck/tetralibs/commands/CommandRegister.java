package fr.bck.tetralibs.commands;


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

import com.mojang.brigadier.CommandDispatcher;
import fr.bck.tetralibs.config.ModulesConfig;
import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.home.BCKHomeManager;
import fr.bck.tetralibs.module.ModuleIds;
import fr.bck.tetralibs.spawn.BCKSpawn;
import fr.bck.tetralibs.warps.BCKWarpManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

/**
 * Centralise l'enregistrement de toutes les commandes,
 * et ne les enregistre que si le module correspondant est actif.
 */
public class CommandRegister {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (isEnabledSafe(ModuleIds.BCK_CORE)) {
            TetraLibsCommand.register(dispatcher);
            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKCore.class), "Commands registered.");
        }

        // /home
        if (isEnabledSafe(ModuleIds.BCK_HOME)) {
            HomeCommand.register(dispatcher);
            SetHomeCommand.register(dispatcher);
            DelHomeCommand.register(dispatcher);
            HomesCommand.register(dispatcher);
            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKHomeManager.class), "Commands registered.");
        }

        if (isEnabledSafe(ModuleIds.BCK_SPAWN)) {
            SpawnCommand.register(dispatcher);
            SetSpawnCommand.register(dispatcher);
            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKSpawn.class), "Commands registered.");
        }

        if (isEnabledSafe(ModuleIds.BCK_WARPS)) {
            WarpCommand.register(dispatcher);
            WarpsCommand.register(dispatcher);
            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKWarpManager.class), "Commands registered.");
        }
    }

    private static boolean isEnabledSafe(ResourceLocation id) {
        try {
            return ModulesConfig.isEnabled(id);
        } catch (IllegalStateException e) {
            // Config pas encore chargée → on ne déclare pas cette commande pour l'instant
            return false;
        }
    }
}
