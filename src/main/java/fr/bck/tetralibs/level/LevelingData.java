package fr.bck.tetralibs.level;


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

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LevelingData implements ILevelingData {
    private final Map<String, Double> xpMap = new HashMap<>();
    private final Map<String, Integer> levelMap = new HashMap<>();

    @Override
    public void addExperience(String type, double amount) {
        // Assurez-vous que le type est enregistré
        registerExperienceType(type);
        double currentXp = xpMap.getOrDefault(type, 0.0);
        double newXp = currentXp + amount;
        int currentLevel = levelMap.getOrDefault(type, 1);
        double xpToNext = getExperienceToNextLevel(type);

        // Gestion du level-up (en conservant le surplus d'XP)
        while (newXp >= xpToNext) {
            newXp -= xpToNext;
            currentLevel++;
            xpToNext = 10 * currentLevel * currentLevel; // Exemple de formule : 10 * (niveau)^2
        }
        xpMap.put(type, newXp);
        levelMap.put(type, currentLevel);
    }

    @Override
    public void addLevel(String type, int level) {
        setLevel(type, (getLevel(type) + level));
    }

    @Override
    public int getLevel(String type) {
        return levelMap.getOrDefault(type, 1);
    }

    @Override
    public void setLevel(String type, int level) {
        levelMap.put(type, level);
    }

    @Override
    public double getExperience(String type) {
        return xpMap.getOrDefault(type, 0.0);
    }

    @Override
    public void setExperience(String type, double xp) {
        xpMap.put(type, xp);
    }

    @Override
    public void removeExperience(String type, double amount) {
        double currentXp = xpMap.getOrDefault(type, 0.0);
        double newXp = Math.max(0, currentXp - amount);
        xpMap.put(type, newXp);
    }

    @Override
    public double getExperienceToNextLevel(String type) {
        int level = getLevel(type);
        return 10 * level * level;
    }

    @Override
    public void resetAllData() {
        xpMap.clear();
        levelMap.clear();
    }

    @Override
    public Set<String> getTrackedExperienceTypes() {
        Set<String> types = new HashSet<>();
        types.addAll(xpMap.keySet());
        types.addAll(levelMap.keySet());
        return types;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        CompoundTag xpTag = new CompoundTag();
        for (Map.Entry<String, Double> entry : xpMap.entrySet()) {
            xpTag.putDouble(entry.getKey(), entry.getValue());
        }
        CompoundTag levelTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : levelMap.entrySet()) {
            levelTag.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("xpMap", xpTag);
        nbt.put("levelMap", levelTag);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        xpMap.clear();
        levelMap.clear();
        if (nbt.contains("xpMap", 10)) { // 10 = TAG_Compound
            CompoundTag xpTag = nbt.getCompound("xpMap");
            for (String key : xpTag.getAllKeys()) {
                xpMap.put(key, xpTag.getDouble(key));
            }
        }
        if (nbt.contains("levelMap", 10)) {
            CompoundTag levelTag = nbt.getCompound("levelMap");
            for (String key : levelTag.getAllKeys()) {
                levelMap.put(key, levelTag.getInt(key));
            }
        }
    }

    @Override
    public void registerExperienceType(String type) {
        // Si le type n'est pas déjà enregistré, on l'initialise avec XP=0 et niveau=1
        if (!xpMap.containsKey(type)) {
            xpMap.put(type, 0.0);
        }
        if (!levelMap.containsKey(type)) {
            levelMap.put(type, 1);
        }
    }
}
