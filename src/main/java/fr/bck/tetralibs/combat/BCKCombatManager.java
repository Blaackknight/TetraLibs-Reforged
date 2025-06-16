package fr.bck.tetralibs.combat;


import fr.bck.tetralibs.TetralibsMod;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashMap;
import java.util.Map;
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

@Mod.EventBusSubscriber(modid = TetralibsMod.MODID)
public class BCKCombatManager {
    // map UUID joueur → secondes restantes avant sortie de combat
    private static final Map<UUID, Double> COOLDOWNS = new LinkedHashMap<>();

    public static Map<UUID, Double> getCooldowns() {
        return COOLDOWNS;
    }

    /**
     * Ajoute un joueur en combat avec une durée (en secondes).
     */
    public static void enterCombat(Entity entity) {
        double durationSec = BCKCombat.getCombatDuration();
        UUID id = entity.getUUID();
        COOLDOWNS.put(id, durationSec);
        BCKCombat.setPlayerInCombat(entity, durationSec);
    }

    /**
     * Force la sortie de combat immédiate.
     */
    public static void forceExit(Entity entity) {
        UUID id = entity.getUUID();
        COOLDOWNS.remove(id);
        BCKCombat.exit(entity);
    }

    /**
     * @return true si le joueur est en combat.
     */
    public static boolean isInCombat(Entity entity) {
        return COOLDOWNS.containsKey(entity.getUUID());
    }

    /**
     * @return secondes restantes, ou 0 si absent.
     */
    public static double getRemainingSeconds(Entity entity) {
        return COOLDOWNS.getOrDefault(entity.getUUID(), 0.0);
    }

    // Constructeur privé pour éviter instanciation
    private BCKCombatManager() {
    }
}

