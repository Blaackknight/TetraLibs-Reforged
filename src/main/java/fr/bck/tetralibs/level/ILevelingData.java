package fr.bck.tetralibs.level;


import net.minecraft.nbt.CompoundTag;

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

public interface ILevelingData {

    /**
     * Ajoute une quantité d'expérience pour un type donné.
     */
    void addExperience(String type, double amount);

    /**
     * Ajoute une quantité de niveaux pour un type donné.
     */
    void addLevel(String type, int amount);

    /**
     * Retourne le niveau actuel pour un type d'expérience donné.
     */
    int getLevel(String type);

    /**
     * Définit le niveau pour un type d'expérience donné.
     */
    void setLevel(String type, int level);

    /**
     * Retourne l'expérience courante pour un type d'expérience donné.
     */
    double getExperience(String type);

    /**
     * Définit l'expérience pour un type d'expérience donné.
     */
    void setExperience(String type, double xp);

    /**
     * Retire une quantité d'expérience pour un type donné.
     */
    void removeExperience(String type, double amount);

    /**
     * Retourne la quantité d'expérience nécessaire pour atteindre le niveau suivant.
     */
    double getExperienceToNextLevel(String type);

    /**
     * Réinitialise toutes les données de leveling.
     */
    void resetAllData();

    /**
     * Retourne l'ensemble des types d'expérience actuellement suivis.
     */
    Set<String> getTrackedExperienceTypes();

    /**
     * Sérialise les données de leveling dans un CompoundTag.
     */
    CompoundTag serializeNBT();

    /**
     * Désérialise les données de leveling depuis un CompoundTag.
     */
    void deserializeNBT(CompoundTag nbt);

    /**
     * Enregistre un type d'expérience.
     */
    void registerExperienceType(String type);
}
