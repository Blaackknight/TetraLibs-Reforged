package fr.bck.tetralibs.level;


import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

import java.util.Set;



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

public interface LevelingEntity {

    /**
     * Retourne l'entité courante.
     */
    default LivingEntity self() {
        return (LivingEntity) this;
    }

    default String getStringUUID() {
        return self().getUUID().toString();
    }

    /**
     * Récupère l'instance de ILevelingData via la capability.
     */
    default ILevelingData getLevelingData() {
        return self().getCapability(LevelingDataCapability.LEVELING_DATA_CAPABILITY, null).orElseThrow(() -> new IllegalStateException("Capability not found!"));
    }

    /**
     * Enregistre un type d'expérience dans la capability.
     */
    default void registerExperienceType(String type) {
        getLevelingData().registerExperienceType(type);
    }

    /**
     * Ajoute de l'expérience pour un type donné.
     */
    default void addExperience(String type, double amount, boolean callback) {
        // Enregistre le type si nécessaire
        registerExperienceType(type);

        // Déclenche l'événement ExperienceGainEvent
        double newXP = getLevelingData().getExperience(type) + amount;
        BCKLevelingEvent.ExperienceGainEvent xpEvent = new BCKLevelingEvent.ExperienceGainEvent(self(), type, amount, newXP);
        if (MinecraftForge.EVENT_BUS.post(xpEvent)) {
            return; // L'événement a été annulé
        }

        getLevelingData().addExperience(type, amount);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLeveling.class), "Entity " + self().getUUID() + " gained " + amount + " XP in " + type);
        }
    }

    default void addExperience(String type, double amount) {
        addExperience(type, amount, false);
    }

    /**
     * Retire de l'expérience pour un type donné.
     */
    default void removeExperience(String type, double amount, boolean callback) {
        getLevelingData().removeExperience(type, amount);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLeveling.class), "Entity " + self().getUUID() + " lost " + amount + " XP in " + type);
        }
    }

    default void removeExperience(String type, double amount) {
        removeExperience(type, amount, false);
    }

    /**
     * Retourne le niveau actuel pour un type donné.
     */
    default int getLevel(String type) {
        return getLevelingData().getLevel(type);
    }

    /**
     * Retourne l'expérience actuelle pour un type donné.
     */
    default double getExperience(String type) {
        return getLevelingData().getExperience(type);
    }

    /**
     * Retourne l'expérience nécessaire pour passer au niveau suivant.
     */
    default double getExperienceToNextLevel(String type) {
        return getLevelingData().getExperienceToNextLevel(type);
    }

    /**
     * Ajoute de l'expérience pour un type donné.
     */
    default void addLevel(String type, int amount, boolean callback) {
        getLevelingData().setLevel(type, getLevel(type) + amount);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLeveling.class), "Entity " + self().getUUID() + " gained " + amount + " LEVEL in " + type);
        }
    }

    default void addLevel(String type, int amount) {
        addLevel(type, amount, false);
    }

    /**
     * Effectue le level-up pour un type donné.
     */
    default void levelUp(String type, boolean callback) {
        int currentLevel = getLevel(type);
        int newLevel = currentLevel + 1;

        BCKLevelingEvent.LevelUpEvent event = new BCKLevelingEvent.LevelUpEvent(self(), type, currentLevel, newLevel);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return; // L'événement a été annulé
        }

        getLevelingData().setLevel(type, newLevel);
        getLevelingData().setExperience(type, 0);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLeveling.class), "Entity " + self().getUUID() + " leveled up to " + newLevel + " in " + type);
        }
    }

    default void levelUp(String type) {
        levelUp(type, false);
    }

    /**
     * Définit le niveau pour un type donné.
     */
    default void setLevel(String type, int level) {
        getLevelingData().setLevel(type, level);
    }

    /**
     * Définit l'expérience pour un type donné.
     */
    default void setExperience(String type, double xp) {
        getLevelingData().setExperience(type, xp);
    }

    /**
     * Retourne les types d'expérience suivis.
     */
    default String[] getTrackedExperienceTypes() {
        Set<String> types = getLevelingData().getTrackedExperienceTypes();
        return types.toArray(new String[0]);
    }

    /**
     * Réinitialise les données de leveling pour les types spécifiés.
     */
    default void deleteExperienceData(boolean callback, String... types) {
        for (String type : types) {
            getLevelingData().setExperience(type, 0);
            getLevelingData().setLevel(type, 1);
            if (callback) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLeveling.class), "Deleted XP and level data for entity " + self().getUUID() + " in " + type);
            }
        }
    }

    default void deleteExperienceData(String... types) {
        deleteExperienceData(false, types);
    }

    /**
     * Réinitialise toutes les données de leveling.
     */
    default void resetAllLevelingData(boolean callback) {
        getLevelingData().resetAllData();
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKLeveling.class), "Reset all leveling data for entity " + self().getUUID());
        }
    }

    default void resetAllLevelingData() {
        resetAllLevelingData(false);
    }
}
