package fr.bck.tetralibs.handlers;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKServerdata;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;



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

public class TetraLibsServerdataHandler {
    public static void get(CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.tetralibs.serverdata.get").getString()).replace("<key>", StringArgumentType.getString(arguments, "key"))).replace("<value>", ((DataWrapper) BCKServerdata.data((StringArgumentType.getString(arguments, "key")))).toString()))), false);

    }

    public static void set(LevelAccessor world, double x, double y, double z, CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null) return;
        // Récupération des arguments
        String key = StringArgumentType.getString(arguments, "key");
        String rawValue = StringArgumentType.getString(arguments, "value");

        Object value;

        // Vérifie si la valeur est un JSON valide
        try {
            JsonObject jsonObject = JsonParser.parseString(rawValue).getAsJsonObject();
            value = jsonObject; // La valeur est déjà un JSON
        } catch (JsonSyntaxException | IllegalStateException e) {
            // Si ce n'est pas un JSON, traite la valeur normalement
            if (rawValue.matches("-?\\d+")) {
                value = Integer.parseInt(rawValue); // Entier
            } else if (rawValue.matches("-?\\d*\\.\\d+")) {
                value = Double.parseDouble(rawValue); // Décimal
            } else if (rawValue.equalsIgnoreCase("true") || rawValue.equalsIgnoreCase("false")) {
                value = Boolean.parseBoolean(rawValue); // Booléen
            } else {
                value = rawValue; // Chaîne
            }
        }

        // Enregistre la valeur dans Serverdata
        BCKServerdata.data(key, "set", value);

        // Message de confirmation
        if (entity instanceof Player _player && !_player.level().isClientSide())
            _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((Component.translatable("command.tetralibs.serverdata.set").getString()).replace("<key>", key)).replace("<value>", rawValue)), false);
    }

    public static void list(Entity entity) {

    }
}
