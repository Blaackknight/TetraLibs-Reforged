package fr.bck.tetralibs.module;

import net.minecraft.resources.ResourceLocation;




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

public final class ModuleIds {
    private static ResourceLocation rl(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static final ResourceLocation BCK_CORE = rl("tetralibs", "bck_core");
    public static final ResourceLocation BCK_BACKPACK = rl("tetralibs", "bck_backpack");
    public static final ResourceLocation BCK_HOME = rl("tetralibs", "bck_home");
    public static final ResourceLocation BCK_LEVELING = rl("tetralibs", "bck_leveling");
    public static final ResourceLocation BCK_PERMISSIONS = rl("tetralibs", "bck_permissions");
    public static final ResourceLocation BCK_RECIPE = rl("tetralibs", "bck_recipe");
    public static final ResourceLocation BCK_LICH_GUARD = rl("tetralibs", "bck_lich_guard");
    public static final ResourceLocation BCK_SKILL = rl("tetralibs", "bck_skill");
    public static final ResourceLocation BCK_SPAWN = rl("tetralibs", "bck_spawn");
    public static final ResourceLocation BCK_LICH_EYE = rl("tetralibs", "bck_lich_eye");
    public static final ResourceLocation BCK_LICH_CLEAR = rl("tetralibs", "bck_lich_clear");
    public static final ResourceLocation BCK_LICH_WHISPER = rl("tetralibs", "bck_lich_whisper");
    public static final ResourceLocation BCK_SERVERDATA = rl("tetralibs", "bck_serverdata");
    public static final ResourceLocation BCK_USERDATA = rl("tetralibs", "bck_userdata");
    public static final ResourceLocation BCK_ECONOMY_MANAGER = rl("tetralibs", "bck_economy_manager");
    public static final ResourceLocation BCK_WARPS = rl("tetralibs", "bck_warps");

    public static final ResourceLocation BCK_COMBAT = rl("tetralibs", "bck_combat");

    public static final ResourceLocation BCK_VANISH = rl("tetralibs", "bck_vanish");

    private ModuleIds() {
    }
}