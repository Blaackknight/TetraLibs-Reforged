package fr.bck.tetralibs.core;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.bck.tetralibs.data.BCKServerdata;
import fr.bck.tetralibs.data.BCKUserdata;
import fr.bck.tetralibs.network.BCKSoundNetworkHandler;
import fr.bck.tetralibs.network.sound.PlayLoopingSoundPacket;
import fr.bck.tetralibs.network.sound.StopLoopingSoundPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



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

public class BCKUtils {
    public static final class Utils {
        // ta couleur
        public static final String color = "§7";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    public abstract static class ItemUtil {
        /**
         * Convertit une chaîne de type "minecraft:dirt{...}"
         * en un ItemStack contenant l'Item et le NBT spécifié.
         *
         * @param input la chaîne au format "namespace:item{nbt}"
         * @return un ItemStack correspondant ou ItemStack.EMPTY si l'item n'est pas trouvé
         */
        public static ItemStack parseItemStack(String input) {
            // Trouver l'accolade ouvrante pour séparer la partie item ("minecraft:dirt")
            // de la partie NBT ("{display:{...}}")
            int indexOfBrace = input.indexOf('{');

            // S'il n'y a pas d'accolade, c'est qu'on a seulement "minecraft:dirt"
            // sinon, on récupère la sous-chaîne avant l'accolade, et après l'accolade
            String itemPart = (indexOfBrace == -1) ? input : input.substring(0, indexOfBrace);
            String nbtPart = (indexOfBrace == -1) ? "" : input.substring(indexOfBrace);

            // Convertir la partie "minecraft:dirt" en ResourceLocation
            ResourceLocation rl = ResourceLocation.parse(itemPart);
            // Récupérer l'Item dans le registre Forge
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item == null) {
                // Si l'item n'est pas reconnu, on renvoie un stack vide
                return ItemStack.EMPTY;
            }

            // Créer le stack de base
            ItemStack stack = new ItemStack(item);

            // Si on a un tag NBT à parser
            if (!nbtPart.isEmpty()) {
                try {
                    // Parser le NBT à partir de la portion { ... }
                    CompoundTag tag = TagParser.parseTag(nbtPart);
                    // Appliquer le tag au stack
                    stack.setTag(tag);
                } catch (CommandSyntaxException e) {
                    // En cas d'erreur de syntaxe dans la chaîne NBT
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ItemUtil", "Error while reading string nbt", e);
                }
            }

            // Retourne l'ItemStack final
            return stack;
        }

        /**
         * Convertit un ItemStack en chaîne de texte du type "minecraft:dirt{...}".
         *
         * @param stack l'ItemStack à convertir
         * @return la chaîne décrivant l'ItemStack (avec son Item + NBT)
         */
        public static String itemStackToString(ItemStack stack) {
            // Gérer le cas d'un ItemStack vide ou null
            if (stack == null || stack.isEmpty()) {
                return "minecraft:air";
            }

            // Récupérer le ResourceLocation de l'Item
            ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
            // S'il n'est pas trouvé, on gère ça comme un item inconnu => air par défaut
            if (rl == null) {
                return "minecraft:air";
            }

            // Nom de l'item, ex: "minecraft:dirt"
            String itemString = rl.toString();

            // Récupérer le tag NBT actuel
            CompoundTag tag = stack.getTag();
            if (tag != null && !tag.isEmpty()) {
                // Le toString() d'un CompoundTag produit un format Mojangson valide,
                // par ex: {display:{Name:"{\"text\":\"Mon bloc\"}"}}.
                itemString += tag.toString(); // "minecraft:dirt{display:{Name:"{\"text\":\"Mon bloc\"}"}}"
            }

            return itemString;
        }

        public static JsonObject toJson(ItemStack stack) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString());
            if (stack.getCount() != 1) obj.addProperty("count", stack.getCount());
            if (stack.hasTag()) {
                assert stack.getTag() != null;
                obj.addProperty("nbt", stack.getTag().toString());
            }
            return obj;
        }

        public static JsonElement itemIdJson(ItemStack stack) {
            return JsonParser.parseString("\"" + ForgeRegistries.ITEMS.getKey(stack.getItem()) + "\"");
        }

        public static ItemStack fromJson(JsonObject json) {
            ResourceLocation id = ResourceLocation.parse(json.get("id").getAsString());
            int count = json.has("count") ? json.get("count").getAsInt() : 1;

            ItemStack stack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(id)), count);

            if (json.has("nbt")) {
                try {
                    CompoundTag tag = TagParser.parseTag(json.get("nbt").getAsString());
                    stack.setTag(tag);
                } catch (CommandSyntaxException e) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ItemUtil", "Invalid NBT in JSON for item " + id, e);
                }
            }
            return stack;
        }

        /* ───────────── CompoundTag ↔ ItemStack ───────────── */

        public static CompoundTag toNBT(ItemStack stack) {
            CompoundTag root = new CompoundTag();
            root.putString("id", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString());
            if (stack.getCount() != 1) root.putInt("Count", stack.getCount());
            if (stack.hasTag()) {
                assert stack.getTag() != null;
                root.put("tag", stack.getTag().copy());
            }
            return root;
        }

        public static ItemStack fromNBT(CompoundTag root) {
            ResourceLocation id = ResourceLocation.parse(root.getString("id"));
            int count = root.contains("Count") ? root.getInt("Count") : 1;
            ItemStack stack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(id)), count);

            if (root.contains("tag")) stack.setTag(root.getCompound("tag").copy());
            return stack;
        }

        /* ─────────────  JSON ↔ CompoundTag (NOUVEAU) ───────────── */

        /**
         * Transforme directement le JSON (format toJson) en CompoundTag binaire.
         */
        public static CompoundTag jsonToNBT(JsonObject json) {
            return toNBT(fromJson(json));
        }

        /**
         * Transforme un CompoundTag (format toNBT) en JSON lisible.
         */
        public static JsonObject nbtToJson(CompoundTag root) {
            return toJson(fromNBT(root));
        }

        /**
         * Ajoute un modificateur d’attaque à un item existant sans supprimer les autres.
         *
         * @param itemstack    L'item auquel ajouter le modificateur.
         * @param damage       La valeur à ajouter en dégâts d’attaque.
         * @param modifierName Le nom du modificateur.
         * @param slot         Le slot où appliquer le modificateur (mainhand, offhand, etc.).
         */
        public static void setAttackDamageModifier(ItemStack itemstack, double damage, String modifierName, String slot) {
            // Crée ou récupère le tag de l’item
            CompoundTag tag = itemstack.getOrCreateTag();

            // Récupérer la liste des modificateurs existants
            ListTag modifiersList = tag.getList("AttributeModifiers", 10); // 10 = TAG_Compound
            // Création du modificateur de dégâts d’attaque
            CompoundTag damageModifier = new CompoundTag();
            damageModifier.putString("AttributeName", "generic.attack_damage");
            damageModifier.putString("Name", modifierName);
            damageModifier.putDouble("Amount", damage);
            damageModifier.putInt("Operation", AttributeModifier.Operation.ADDITION.ordinal());
            damageModifier.putUUID("UUID", UUID.randomUUID());
            // Ajouter le slot si nécessaire
            if (slot != null && !slot.isEmpty()) {
                damageModifier.putString("Slot", slot);
            }

            // Ajouter le nouveau modificateur à la liste
            modifiersList.add(damageModifier);

            // Sauvegarder la liste mise à jour dans les NBT de l’item
            tag.put("AttributeModifiers", modifiersList);
        }

        public static void updateAttackDamageModifier(ItemStack itemstack, double newDamage, String modifierName, String slot) {
            if (itemstack == null || itemstack.isEmpty()) return;

            // Récupère ou crée le tag NBT de l’item
            CompoundTag tag = itemstack.getOrCreateTag();

            // Récupère la liste des modificateurs existants (TAG_Compound = 10)
            ListTag modifiersList = tag.getList("AttributeModifiers", 10);
            boolean updated = false;

            // Parcourt les modificateurs pour trouver celui qui correspond
            for (int i = 0; i < modifiersList.size(); i++) {
                CompoundTag modifier = modifiersList.getCompound(i);
                if ("generic.attack_damage".equals(modifier.getString("AttributeName")) && modifierName.equals(modifier.getString("Name")) && ((slot == null || slot.isEmpty()) || slot.equals(modifier.getString("Slot")))) {

                    // Met à jour la valeur du modificateur
                    modifier.putDouble("Amount", newDamage);
                    updated = true;
                }
            }

            // Si aucun modificateur correspondant n'a été trouvé, on l'ajoute
            if (!updated) {
                setAttackDamageModifier(itemstack, newDamage, modifierName, slot);
            }

            // Sauvegarde la liste mise à jour dans le tag de l’item
            tag.put("AttributeModifiers", modifiersList);
        }

        /**
         * Récupère un objet {@link Item} aléatoire à partir d'une table de loot.
         *
         * @param world             Le monde où la table de loot est chargée.
         * @param lootTableLocation L'emplacement de la table de loot à utiliser.
         * @return Un objet {@link Item} aléatoire provenant de la table de loot.
         */
        public static Item getRandomItemFromLootTable(LevelAccessor world, String lootTableLocation) {
            if (!world.isClientSide() && world instanceof ServerLevel) {
                LootTable lootTable = world.getServer().getLootData().getLootTable(ResourceLocation.parse(lootTableLocation));
                List<ItemStack> items = lootTable.getRandomItems(new LootParams.Builder((ServerLevel) world).create(LootContextParamSets.EMPTY));
                if (!items.isEmpty()) {
                    ItemStack randomItem = items.get(world.getRandom().nextInt(items.size()));
                    return randomItem.getItem();
                } else {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ItemUtil", "No items found in loot table: " + lootTableLocation);
                }
            } else {
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ItemUtil", "Unable to get random item from loot table. Invalid world or client side.");
            }
            return Items.AIR;
        }

        /**
         * Crée un composant {@link Component} représentant un objet {@link Item} avec une quantité spécifiée.
         * Le composant contient le nom affiché de l'objet avec un style de police basé sur le nom de l'élément.
         *
         * @param item  L'item dont on veut créer le composant.
         * @param count La quantité de l'item.
         * @return Un composant de texte représentant l'item.
         */
        public static Component createItemComponent(Item item, int count) {
            ItemStack stack = new ItemStack(item);
            stack.setCount(count);
            stack.setTag(new CompoundTag());
            String itemName = (Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString());
            String[] words = itemName.split(":");
            Component itemText = Component.literal(stack.getDisplayName().getString());
            Style s = itemText.getStyle();
            s.withFont(ResourceLocation.fromNamespaceAndPath(words[0], words[1]));
            return itemText;
        }

        /**
         * Surcharge de la méthode {@link #createItemComponent(Item, int)} sans la possibilité de spécifier la quantité.
         * Par défaut, la quantité est définie à 1.
         *
         * @param item L'item dont on veut créer le composant.
         * @return Un composant de texte représentant l'item avec une quantité de 1.
         */
        public static Component createItemComponent(Item item) {
            return createItemComponent(item, 1);
        }

        public static Item getRandomItem() {
            return Lists.newArrayList(ForgeRegistries.ITEMS.getValues()).get(new Random().nextInt(Lists.newArrayList(ForgeRegistries.ITEMS.getValues()).size()));
        }

        public static ItemStack getRandomItemStack() {
            return new ItemStack(getRandomItem());
        }
    }

    public abstract static class BlockUtil {
        public static JsonObject toJson(BlockState state) {
            JsonObject json = new JsonObject();
            json.addProperty("block", Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock())).toString()); // Identifiant du bloc
            json.addProperty("state", state.toString()); // État complet
            return json;
        }

        public static BlockState fromJson(JsonObject json) {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(json.get("block").getAsString()));
            if (block != null) {
                return block.defaultBlockState(); // Retourne l'état par défaut (peut être étendu pour des états spécifiques)
            }
            return null;
        }

        /**
         * Renvoie un Block aléatoire
         */
        public static Block getRandomBlock = Lists.newArrayList(ForgeRegistries.BLOCKS.getValues()).get(new Random().nextInt(Lists.newArrayList(ForgeRegistries.BLOCKS.getValues()).size()));
        /**
         * Renvoie un BlockState aléatoire
         */
        public static BlockState getRandomBlockState = getRandomBlock.defaultBlockState();
    }

    public abstract static class TextUtil {
        /**
         * Préfixes de packages pour résolution automatique
         * si l’utilisateur ne donne pas le package complet.
         */
        private static final String[] CLASS_PREFIXES = {"fr.bck.tetralibs", "f.bck.tetralibs", "net.minecraft", "net.minecraftforge"};

        public static String universal(String target, Object obj) {
            if (obj == null) {
                return "<" + target + "|hoverText|§8Type: §7null§r\n§6ID: §enull§r\n§2UUID: §anull" + ">";
            }

            String typeName = obj.getClass().getSimpleName();
            String registryId = "§o§n<unknown>§r";
            String uuid;

            if (obj instanceof Item) {
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey((Item) obj);
                registryId = rl != null ? rl.toString() : "§c§m<unregistered item>";
            } else if (obj instanceof Block) {
                ResourceLocation rl = ForgeRegistries.BLOCKS.getKey((Block) obj);
                registryId = rl != null ? rl.toString() : "§c§m<unregistered block>";
            } else if (obj instanceof ItemStack) {
                ItemStack stack = (ItemStack) obj;
                ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
                registryId = (rl != null ? rl.toString() : "§c§m<unregistered item>") + " §dx" + stack.getCount();
            } else if (obj instanceof BlockState) {
                Block block = ((BlockState) obj).getBlock();
                ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
                registryId = rl != null ? rl.toString() : "§c§m<unregistered block>";
            } else if (obj instanceof Entity) {
                Entity e = (Entity) obj;
                ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
                registryId = rl != null ? rl.toString() : "§c§m<unregistered entity>";
                // pour les entités, réutilise leur vrai UUID
                uuid = e.getUUID().toString();
                return universalFormat(target, typeName, registryId, uuid);
            } else if (obj instanceof ResourceLocation) {
                registryId = obj.toString();
            } else {
                // fallback général sur toString()
                registryId = obj.toString();
            }

            // UUID stable à partir du couple type+id
            String name = typeName + ":" + registryId;
            uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)).toString();
            return universalFormat(target, typeName, registryId, uuid);
        }

        private static String universalFormat(String target, String type, String id, String uuid) {
            return "<" + target + "|hoverText|§8Type: §7" + type + "\n" + "§6ID: §e" + id + "\n" + "§2UUID: §a" + uuid + ">";
        }

        public static Component toStyled(String text, boolean callback) {
            if (text == null || text.isEmpty()) {
                return Component.empty();
            }
            /*
             * 1) group(2) = nom de classe dans <class:…>
             * 2) sinon group(3)=displayText, group(4)=clickAction, group(5)=clickValue, group(6)=hoverText
             */
            Pattern pattern = Pattern.compile("<([^|>]+)(?:\\|(?!hoverText)([^|>]+)\\|([^|>]+))?(?:\\|hoverText\\|([^>]+))?>");
            Matcher matcher = pattern.matcher(text);
            MutableComponent styledText = Component.empty();
            int lastIndex = 0;

            while (matcher.find()) {
                // Ajouter le texte avant la balise en appliquant parseLegacyText()
                String beforeText = text.substring(lastIndex, matcher.start());
                if (!beforeText.isEmpty()) {
                    MutableComponent parsedBefore = parseFormat(beforeText);
                    if (!parsedBefore.getString().isEmpty()) { // Éviter les composants vides
                        styledText.append(handleNewLines(parsedBefore));
                        if (callback)
                            BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Text", "Before Text: " + parsedBefore);
                    }
                }

                MutableComponent styledElement;


                // Extraire les informations des balises
                String displayText = matcher.group(1);
                String clickAction = matcher.group(2);
                String clickValue = matcher.group(3);
                String hoverValue = matcher.group(4);

                // Cas spécial des balises uniquement hoverText

                if (callback) {
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Text", "display: " + displayText);
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Text", "action: " + clickAction);
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Text", "click: " + clickValue);
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Text", "hover: " + hoverValue);
                }

                // Appliquer parseLegacyText sur le displayText AVANT d'envoyer à applyStyledText()
                styledElement = applyStyledText(parseFormat(displayText).getString(), clickAction, clickValue, hoverValue);

                if (callback)
                    BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Apply", "Styled Element: " + styledElement);

                styledText.append(styledElement);
                lastIndex = matcher.end();

            }

            // Ajouter le reste du texte après la dernière balise
            String remainingText = text.substring(lastIndex);
            if (!remainingText.isEmpty()) {
                MutableComponent parsedRemaining = parseFormat(remainingText);
                if (!parsedRemaining.getString().isEmpty()) {
                    styledText.append(handleNewLines(parsedRemaining));
                    if (callback)
                        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Text", "After Text: " + parsedRemaining);
                }
            }

            if (callback)
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Text", "Final Styled Text: " + styledText);

            return styledText;
        }

        /**
         * Applique un style au texte donné, y compris les actions de clic et de survol.
         *
         * @param displayText Le texte à styliser.
         * @param clickAction L'action de clic associée au texte.
         * @param clickValue  La valeur de l'action de clic.
         * @param hoverText   Le texte à afficher au survol (peut être une clé de traduction).
         * @return Le texte stylisé avec le clic et les événements de survol appliqués.
         */
        private static MutableComponent applyStyledText(String displayText, String clickAction, String clickValue, String hoverText) {
            Style style = Style.EMPTY;

            // Si il y a une action de clic et que l'action est valide, on l'applique
            if (clickAction != null && !clickAction.isEmpty() && clickValue != null && !clickValue.isEmpty()) {
                ClickEvent.Action action = getClickAction(clickAction);
                if (action != null) {
                    style = style.withClickEvent(new ClickEvent(action, clickValue));
                } else {
                    BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/StyledText", "Unknown clickAction: " + clickAction);
                }
            }

            // Résolution de la clé de traduction pour hoverText, si applicable
            if (hoverText != null && !hoverText.isEmpty()) {
                if (I18n.exists(hoverText)) {
                    hoverText = I18n.get(hoverText);  // Résout la clé de traduction en texte
                }
                style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, toStyled(hoverText)));
            }

            // Log du composant final pour débogage
            //if (!style.isEmpty()) SuperLog.info("TetraLibs/BCKUtils/StyledText", "Final Component: " + Component.literal(displayText).setStyle(style));

            return Component.literal(displayText).setStyle(style);
        }

        public static Component toStyled(String text) {
            return toStyled(text, false);
        }

        // Méthode pour appliquer le format personnalisé à un texte avec des couleurs hexadécimales et des styles supplémentaires
        public static MutableComponent parseFormat(String text) {
            // Expression régulière pour matcher <#FF0000> ou <#FF0000|italic=true|obfuscated=true> etc.
            Pattern pattern = Pattern.compile("<#([0-9A-Fa-f]{6})>(?:\\|(.*?))?>");
            Matcher matcher = pattern.matcher(text);
            MutableComponent builder = Component.empty();  // MutableComponent vide

            int lastIndex = 0;
            while (matcher.find()) {
                // Ajouter le texte avant le match
                String beforeText = text.substring(lastIndex, matcher.start());
                builder.append(Component.literal(beforeText));

                // Extraire la couleur hexadécimale
                String hexColor = matcher.group(1);
                String styles = matcher.group(2);

                // Créer un TextColor avec la couleur hexadécimale
                TextColor color = TextColor.parseColor("#" + hexColor);

                // Créer le composant texte avec la couleur
                MutableComponent newComponent = Component.literal("").withStyle(style -> style.withColor(color));

                // Appliquer les styles comme italique, gras, obfusqué, barré, ou souligné
                if (styles != null) {
                    if (styles.contains("italic=true")) {
                        newComponent = newComponent.withStyle(style -> style.withItalic(true));
                    }
                    if (styles.contains("bold=true")) {
                        newComponent = newComponent.withStyle(style -> style.withBold(true));
                    }
                    if (styles.contains("obfuscated=true")) {
                        newComponent = newComponent.withStyle(style -> style.withObfuscated(true));
                    }
                    if (styles.contains("strike=true")) {
                        newComponent = newComponent.withStyle(style -> style.withStrikethrough(true));
                    }
                    if (styles.contains("underline=true")) {
                        newComponent = newComponent.withStyle(style -> style.withUnderlined(true));
                    }
                }

                // Ajouter le composant stylisé à la construction
                builder.append(newComponent);

                // Mettre à jour l'index
                lastIndex = matcher.end();
            }

            // Ajouter le texte restant après le dernier match
            String remainingText = text.substring(lastIndex);
            builder.append(Component.literal(remainingText));
            return builder;
        }

        /**
         * Cette méthode permet de gérer les sauts de ligne dans un texte en ajoutant un saut de ligne dans le MutableComponent.
         *
         * @param component Le component contenant le texte.
         * @return Un MutableComponent avec les sauts de ligne correctement gérés.
         */
        private static MutableComponent handleNewLines(MutableComponent component) {
            String text = component.getString();
            String[] parts = text.split("\n"); // Divise le texte par les sauts de ligne
            MutableComponent result = Component.empty();  // MutableComponent vide

            for (int i = 0; i < parts.length; i++) {
                // Ajouter la partie du texte en tant que composant
                result = result.append(Component.literal(parts[i]));
                //SuperLog.debug("TetraLibs/BCKUtils/NewLines", "Ajout du texte : " + parts[i]);

                // Ajouter un saut de ligne si ce n'est pas la dernière partie
                if (i < parts.length - 1) {
                    result = result.append(Component.literal("\n"));
                    //SuperLog.debug("TetraLibs/BCKUtils/NewLines", "Ajout du saut de ligne.");
                }
            }
            return result;
        }

        /**
         * Retourne l'action de clic associée à un nom.
         *
         * @param action Le nom de l'action.
         * @return L'action correspondante.
         */
        private static ClickEvent.Action getClickAction(String action) {
            return switch (action.toUpperCase()) {
                case "RUN_COMMAND" -> ClickEvent.Action.RUN_COMMAND;
                case "OPEN_URL" -> ClickEvent.Action.OPEN_URL;
                case "SUGGEST_COMMAND" -> ClickEvent.Action.SUGGEST_COMMAND;
                case "COPY_TO_CLIPBOARD" -> ClickEvent.Action.COPY_TO_CLIPBOARD;
                default -> null;
            };
        }

        /**
         * Convertit un texte au format JSON utilisable par la commande tellraw de Minecraft,
         * en gérant les actions de clic et de survol (hover).
         * Les actions sont formatées sous la forme <ACTION|value|hoverText|optional>.
         *
         * @param text     Le texte à convertir, pouvant inclure des balises de type <ACTION|value|hoverText>.
         * @param callback Si true, un log est effectué pour avertir d'un problème ou afficher la sortie JSON générée.
         * @return Le texte converti au format JSON pour la commande tellraw.
         */
        public static String convertToTellrawFormat(String text, boolean callback) {
            if (text == null || text.isEmpty()) {
                return "[{\"text\":\"\"}]";
            }

            // Remplacer les occurrences de \n par des objets JSON séparés pour la nouvelle ligne
            text = text.replace("\\n", "\"},{\"text\":\"\\n\"},{\"text\":\"");

            // Utilisation de regex pour détecter les patterns de type <ACTION|value|hoverText|optional>
            Pattern pattern = Pattern.compile("<([^\\|]+)\\|([^\\|]+)\\|([^|>]+)(?:\\|\\|hoverText\\|([^>]+))?>|<([^\\|]+)\\|hoverText\\|([^>]+)>");

            Matcher matcher = pattern.matcher(text);
            StringBuilder tellrawJson = new StringBuilder("[");
            int lastIndex = 0;

            // Liste des actions valides pour `clickEvent`
            Set<String> validClickActions = Set.of("open_url", "run_command", "suggest_command", "copy_to_clipboard");

            // Traitement des matchs et génération du JSON
            while (matcher.find()) {
                String beforeText = text.substring(lastIndex, matcher.start());
                if (!beforeText.isEmpty()) {
                    tellrawJson.append("{\"text\":\"").append(escapeSpecialCharacters(beforeText)).append("\"},");
                }

                // Vérifier si c'est une action seule ou une combinaison action + hoverText
                String displayText = matcher.group(1);  // Texte affiché (première partie)
                String clickAction = matcher.group(2); // Type d'action (OPEN_URL, RUN_COMMAND, etc.)
                String clickValue = matcher.group(3);  // Valeur associée à l'action
                String hoverValue = matcher.group(4);  // Texte du hover (optionnel)

                // Cas 1 : Action + HoverText (avec ||hoverText)
                if (displayText != null && clickAction != null && clickValue != null && hoverValue != null) {
                    if (I18n.exists(hoverValue)) hoverValue = Component.translatable(hoverValue).getString();
                    StringBuilder jsonElement = new StringBuilder("{\"text\":\"").append(escapeSpecialCharacters(displayText)).append("\"");

                    String clickActionLower = clickAction.toLowerCase();
                    if (validClickActions.contains(clickActionLower)) {
                        jsonElement.append(",\"clickEvent\":{\"action\":\"").append(escapeSpecialCharacters(clickActionLower)).append("\",\"value\":\"").append(escapeSpecialCharacters(clickValue)).append("\"}");
                    } else if (callback) {
                        BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Tellraw", "Unknown clickAction: " + clickActionLower);
                    }

                    // Ajouter hoverText s'il est présent
                    jsonElement.append(",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"").append(escapeSpecialCharacters(hoverValue)).append("\"}");
                    jsonElement.append("}");
                    tellrawJson.append(jsonElement).append(",");
                }
                // Cas 2 : Action seule
                else if (displayText != null && clickAction != null && clickValue != null) {
                    StringBuilder jsonElement = new StringBuilder("{\"text\":\"").append(escapeSpecialCharacters(displayText)).append("\"");

                    String clickActionLower = clickAction.toLowerCase();
                    if (validClickActions.contains(clickActionLower)) {
                        jsonElement.append(",\"clickEvent\":{\"action\":\"").append(escapeSpecialCharacters(clickActionLower)).append("\",\"value\":\"").append(escapeSpecialCharacters(clickValue)).append("\"}");
                    } else if (callback) {
                        BCKLog.warn(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Tellraw", "Unknown clickAction: " + clickActionLower);
                    }
                    jsonElement.append("}");
                    tellrawJson.append(jsonElement).append(",");
                }
                // Cas 3 : HoverText seul
                else if (displayText != null && hoverValue != null) {
                    if (I18n.exists(hoverValue)) hoverValue = Component.translatable(hoverValue).getString();
                    String jsonElement = "{\"text\":\"" + escapeSpecialCharacters(displayText) + "\"" + ",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"" + escapeSpecialCharacters(hoverValue) + "\"}}";
                    tellrawJson.append(jsonElement).append(",");
                }

                lastIndex = matcher.end();
            }

            // Traiter le reste du texte après le dernier match
            String remainingText = text.substring(lastIndex);
            if (!remainingText.isEmpty()) {
                tellrawJson.append("{\"text\":\"").append(escapeSpecialCharacters(remainingText)).append("\"}");
            } else {
                int lastComma = tellrawJson.lastIndexOf(",");
                if (lastComma != -1) {
                    tellrawJson.deleteCharAt(lastComma);
                }
            }

            // Fermer le tableau JSON
            tellrawJson.append("]");
            if (callback)
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/TextUtil/Tellraw", "Json: " + tellrawJson);

            return tellrawJson.toString();
        }

        /**
         * Surcharge de la méthode {@link #convertToTellrawFormat(String, boolean)} sans la possibilité de spécifier le callback.
         * Par défaut, le callback est désactivé.
         *
         * @param text Le texte à convertir au format tellraw.
         * @return Le texte converti en JSON pour la commande tellraw.
         */
        public static String convertToTellrawFormat(String text) {
            return convertToTellrawFormat(text, false);
        }

        /**
         * Échappe les caractères spéciaux dans le texte (guillemets, barres obliques inverses, etc.).
         * Cette méthode sert à préparer une chaîne pour être utilisée dans un contexte où les caractères spéciaux
         * doivent être échappés (par exemple, dans un JSON ou une commande Minecraft).
         *
         * @param text La chaîne de texte à traiter. Si cette chaîne est {@code null}, une chaîne vide est retournée.
         * @return La chaîne de texte avec les caractères spéciaux échappés. Si le texte est {@code null}, une chaîne vide est retournée.
         */
        private static String escapeSpecialCharacters(String text) {
            // Vérifie si le texte est null, et retourne une chaîne vide si c'est le cas.
            if (text == null) {
                return "";
            }

            // Les lignes ci-dessous échappent les guillemets et les barres obliques inverses.
            // Ces lignes sont actuellement commentées, mais peuvent être activées si nécessaire.

            // Échappe les guillemets (")
            // text = text.replace("\"", "\\\"");  // Remplace les guillemets par leur version échappée (\")

            // Échappe les barres obliques inverses (\)
            // text = text.replace("\\", "\\\\");  // Remplace les barres obliques inverses par leur version échappée (\\)

            // Retourne la chaîne de texte sans modifications si aucune échappement n'est effectué.
            return text;
        }

        public static Component implementHoverText() {
            return Component.empty();
        }
    }

    public abstract static class GuiUtil {
        public static void userdataSaveSlotsItems(String key, Entity entity, boolean callback) {
            if (entity == null) return;

            List<ItemStack> list = getGuiInventory(entity);
            JsonArray jsonArray = new JsonArray();

            for (ItemStack stack : list) {
                JsonObject jsonObject = new JsonObject();
                jsonArray.add(ItemUtil.toJson(stack));
            }

            // Sauvegarde dans Userdata
            BCKUserdata.data(key, "set", jsonArray, entity.level(), entity);

            if (callback)
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/GuiUtil/SlotsSaver/Userdata", "Saving slots in " + key + ": " + jsonArray + " -> " + entity.getStringUUID());
        }

        public static void userdataSaveSlotsItems(String key, Entity entity) {
            userdataSaveSlotsItems(key, entity, false);
        }

        public static void serverdataSaveSlotsItems(String key, Entity entity, boolean callback) {
            if (entity == null) return;

            List<ItemStack> list = getGuiInventory(entity);
            JsonArray jsonArray = new JsonArray();

            for (ItemStack stack : list) {
                JsonObject jsonObject = new JsonObject();
                jsonArray.add(ItemUtil.toJson(stack));
            }

            // Sauvegarde dans Userdata
            BCKServerdata.data(key, "set", jsonArray);

            if (callback)
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/GuiUtil/SlotsSaver/Serverdata", "Saving slots in " + key + ": " + jsonArray);
        }

        public static void serverdataSaveSlotsItems(String key, Entity entity) {
            serverdataSaveSlotsItems(key, entity, false);
        }

        public static void serverdataSaveSlotsItems(String key, List<ItemStack> list, boolean callback) {
            JsonArray jsonArray = new JsonArray();

            for (ItemStack stack : list) {
                JsonObject jsonObject = new JsonObject();
                jsonArray.add(ItemUtil.toJson(stack));
            }

            // Sauvegarde dans Userdata
            BCKServerdata.data(key, "set", jsonArray);

            if (callback)
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/GuiUtil/SlotsSaver/Serverdata", "Saving slots in " + key + ": " + jsonArray);
        }

        public static void serverdataSaveSlotsItems(String key, List<ItemStack> list) {
            serverdataSaveSlotsItems(key, list, false);
        }

        public static void setPlayerSlots(Entity entity, JsonArray jsonArray, boolean callback) {
            if (!(entity instanceof Player player)) return;

            // Vérification que le nombre d'items dans jsonArray correspond aux slots du GUI
            int slotCount = Math.min(jsonArray.size(), player.containerMenu.slots.size());

            for (int i = 0; i < slotCount; i++) {
                ItemStack stack = ItemUtil.fromJson(jsonArray.get(i).getAsJsonObject());

                // Vérification que l'item n'est pas de l'air (évite de remplir avec du vide)
                if (!stack.isEmpty()) {
                    player.containerMenu.slots.get(i).set(stack);
                }
            }

            // Forcer la mise à jour du GUI
            player.containerMenu.broadcastChanges();

            if (callback)
                BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/GuiUtil/SlotsSetter", "Restored " + slotCount + " slots for " + entity.getStringUUID());
        }

        public static void setPlayerSlots(Entity entity, JsonArray jsonArray) {
            setPlayerSlots(entity, jsonArray, false);
        }

        public static List<ItemStack> getGuiInventory(Entity entity) {
            List<ItemStack> guiItems = new ArrayList<>();

            if (entity instanceof Player player) {
                AbstractContainerMenu menu = player.containerMenu;
                // On récupère uniquement les slots qui appartiennent au GUI et pas à l'inventaire du joueur
                int containerSlots = menu.slots.size() - player.getInventory().items.size();

                for (int i = 0; i < containerSlots; i++) {
                    guiItems.add(menu.slots.get(i).getItem().copy());
                }
            }
            return guiItems;
        }

        /**
         * Vide l'intégralité de l'inventaire d'un joueur.
         *
         * @param player Le joueur dont l'inventaire doit être vidé.
         */
        public static void clearInventory(Player player) {
            Inventory inv = player.getInventory(); // Récupère l'inventaire du joueur.
            inv.clearContent(); // Supprime tout le contenu de l'inventaire.
        }

        /**
         * Ajoute un objet spécifique à l'inventaire d'un joueur.
         *
         * @param player Le joueur auquel l'objet doit être ajouté.
         * @param item   L'objet à ajouter.
         */
        public static void addItem(Player player, ItemStack item) {
            Inventory inv = player.getInventory(); // Récupère l'inventaire du joueur.
            inv.add(item); // Ajoute l'objet à l'inventaire.
        }

        /**
         * Supprime un objet spécifique de l'inventaire d'un joueur.
         *
         * @param player Le joueur dont l'objet doit être retiré.
         * @param item   L'objet à retirer.
         */
        public static void removeItem(Player player, ItemStack item) {
            Inventory inv = player.getInventory(); // Récupère l'inventaire du joueur.
            inv.removeItem(item); // Supprime l'objet de l'inventaire.
        }

        /**
         * Récupère tous les objets présents dans l'inventaire d'un joueur.
         *
         * @param player Le joueur dont les objets doivent être récupérés.
         * @return Une liste contenant tous les objets de l'inventaire.
         */
        public static NonNullList<ItemStack> getPlayerItems(Player player) {
            Inventory inv = player.getInventory(); // Récupère l'inventaire du joueur.
            return inv.items; // Retourne la liste des objets.
        }

        /**
         * Vérifie si un joueur possède un objet spécifique dans son inventaire.
         *
         * @param player Le joueur a vérifié.
         * @param item   L'objet à rechercher.
         * @return true si le joueur possède l'objet, false sinon.
         */
        public static Boolean hasItem(Player player, ItemStack item) {
            Inventory inv = player.getInventory(); // Récupère l'inventaire du joueur.
            return inv.contains(item); // Retourne true si l'objet est présent, false sinon.
        }

        /**
         * Compte le nombre d'instances d'un objet spécifique dans l'inventaire d'un joueur.
         *
         * @param player Le joueur dont l'inventaire doit être compté.
         * @param item   L'objet à compter.
         * @return Le nombre d'instances de l'objet dans l'inventaire.
         */
        public static int countItems(Player player, ItemStack item) {
            Inventory inv = player.getInventory(); // Récupère l'inventaire du joueur.
            return inv.countItem(item.getItem()); // Compte et retourne le nombre d'objets.
        }
    }

    public abstract static class EntityUtil {
        /**
         * Permet d'obtenir l'item dans la main principal d'une entité
         *
         * @param entity L'entité à qui on récupère l'item
         * @return L'item dans la main principal
         */
        public static ItemStack getMainHandItem(Entity entity) {
            return (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY);
        }

        /**
         * Permet d'obtenir l'item dans la deuxième main d'une entité
         *
         * @param entity L'entité à qui on récupère l'item
         * @return L'item dans la deuxième main
         */
        public static ItemStack getOffHandItem(Entity entity) {
            return (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY);
        }

        /**
         * Joue un son à un volume et un pitch spécifiés pour une entité vivante.
         * Si l'entité est une instance de {@link LivingEntity}, le son est joué sur cette entité.
         *
         * @param entity     L'entité sur laquelle le son sera joué.
         * @param soundEvent L'événement sonore à jouer.
         * @param volume     Le volume du son (valeur entre 0.0 et 1.0).
         * @param pitch      Le pitch du son (valeur entre 0.5 et 2.0).
         */
        public static void playSound(Entity entity, SoundEvent soundEvent, double volume, double pitch) {
            if (entity instanceof Player _pl && !_pl.level().isClientSide()) {
                _pl.playSound(soundEvent, (float) volume, (float) pitch);
            }
        }

        /**
         * Fait jouer un son au joueur et aux joueurs a proximité (en boucle)
         *
         * @param entity L'entité source
         * @param loc    Location du son
         * @param volume Volume
         * @param pitch  Pitch
         */
        public static void playSoundTrackingEntityAndSelf(Entity entity, ResourceLocation loc, float volume, float pitch) {
            BCKSoundNetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new PlayLoopingSoundPacket(entity.getId(), loc, volume, pitch));
        }

        public static void stopSoundTrackingEntityAndSelf(Entity entity, ResourceLocation loc) {
            BCKSoundNetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new StopLoopingSoundPacket(loc));
        }

        /**
         * Téléporte une entité aux coordonnées spécifiées.
         * Si le paramètre {@code callback} est activé, un message de log est généré pour indiquer le déplacement.
         *
         * @param entity   L'entité a téléporté.
         * @param x        La coordonnée X de destination.
         * @param y        La coordonnée Y de destination.
         * @param z        La coordonnée Z de destination.
         * @param callback Indicateur de log pour signaler que l'entité a été téléportée.
         */
        public static void teleportEntity(Entity entity, double x, double y, double z, boolean callback) {
            entity.teleportTo(x, y, z);
            if (callback) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/EntityUtil", entity.getStringUUID() + " has been teleported to x: " + x + " y: " + y + " z: " + z);
            }
        }

        /**
         * Surcharge de {@link #teleportEntity(Entity, double, double, double, boolean)} sans la possibilité de spécifier le callback.
         * Par défaut, le callback est désactivé.
         *
         * @param entity L'entité à téléporter.
         * @param x      La coordonnée X de destination.
         * @param y      La coordonnée Y de destination.
         * @param z      La coordonnée Z de destination.
         */
        public static void teleportEntity(Entity entity, double x, double y, double z) {
            teleportEntity(entity, x, y, z, false);
        }

        /**
         * Envoie une commande {@code tellraw} à un joueur, lui permettant de recevoir un message personnalisé en jeu.
         * Le message est formaté avec le texte spécifié et peut inclure un callback pour loguer l'opération.
         *
         * @param txt      Le texte à envoyer dans la commande tellraw.
         * @param entity   L'entité (joueur) à qui envoyer la commande.
         * @param world    Le monde dans lequel se trouve l'entité.
         * @param x        La coordonnée X de l'endroit d'exécution de la commande.
         * @param y        La coordonnée Y de l'endroit d'exécution de la commande.
         * @param z        La coordonnée Z de l'endroit d'exécution de la commande.
         * @param callback Indicateur de log pour signaler que le message a été envoyé.
         */
        public static void sendTellraw(String txt, Entity entity, LevelAccessor world, double x, double y, double z, boolean callback) {
            // Crée la commande tellraw
            String command = "tellraw " + entity.getName().getString() + " " + TextUtil.convertToTellrawFormat(txt, callback);
            if (world instanceof ServerLevel _level)
                _level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), (command));
        }

        /**
         * Surcharge de la méthode {@link #sendTellraw(String, Entity, LevelAccessor, double, double, double, boolean)}
         * sans la possibilité de spécifier le callback. Par défaut, le callback est désactivé.
         *
         * @param txt    Le texte à envoyer dans la commande tellraw.
         * @param entity L'entité (joueur) à qui envoyer la commande.
         * @param world  Le monde dans lequel se trouve l'entité.
         * @param x      La coordonnée X de l'endroit d'exécution de la commande.
         * @param y      La coordonnée Y de l'endroit d'exécution de la commande.
         * @param z      La coordonnée Z de l'endroit d'exécution de la commande.
         */
        public static void sendTellraw(String txt, Entity entity, LevelAccessor world, double x, double y, double z) {
            sendTellraw(txt, entity, world, x, y, z, false);
        }

        public static boolean isFakePlayer(Entity entity) {
            if (!(entity instanceof ServerPlayer player)) {
                return false; // Ce n'est pas un joueur
            }
            // Vérification de l'UUID
            UUID uuid = player.getUUID();
            if (uuid.toString().startsWith("0000") || uuid.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
                return true;
            }

            // Vérification du nom
            String name = player.getGameProfile().getName();
            if (name.startsWith("[") || name.contains("Fake") || name.contains("Bot")) {
                return true;
            }

            // Vérification du GameProfile
            return !player.getGameProfile().isComplete();// Ce n'est pas un Fake Player
        }

        /**
         * Définit les dégâts de base d'une entité vivante.
         *
         * @param entity    L'entité à modifier.
         * @param newDamage La nouvelle valeur des dégâts de base.
         */
        public static void setBaseDamage(Entity entity, double newDamage) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(newDamage);
            }
        }

        /**
         * Définit la vitesse d'attaque de base d'une entité vivante.
         *
         * @param entity   L'entité à modifier.
         * @param newSpeed La nouvelle valeur de la vitesse d'attaque de base.
         */
        public static void setBaseAttackSpeed(Entity entity, double newSpeed) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.ATTACK_SPEED)).setBaseValue(newSpeed);
            }
        }

        /**
         * Définit la vitesse de déplacement de base d'une entité vivante.
         *
         * @param entity   L'entité à modifier.
         * @param newSpeed La nouvelle valeur de la vitesse de déplacement de base.
         */
        public static void setBaseSpeed(Entity entity, double newSpeed) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(newSpeed);
            }
        }

        /**
         * Définit la santé maximale de base d'une entité vivante.
         *
         * @param entity    L'entité à modifier.
         * @param newHealth La nouvelle valeur de la santé maximale de base.
         */
        public static void setBaseMaxHealth(Entity entity, double newHealth) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(newHealth);
            }
        }

        /**
         * Définit la force de saut de base d'une entité vivante.
         *
         * @param entity          L'entité à modifier.
         * @param newJumpStrength La nouvelle valeur de la force de saut de base.
         */
        public static void setBaseJumpStrength(Entity entity, double newJumpStrength) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.JUMP_STRENGTH)).setBaseValue(newJumpStrength);
            }
        }

        /**
         * Définit l'armure de base d'une entité vivante.
         *
         * @param entity   L'entité à modifier.
         * @param newArmor La nouvelle valeur de l'armure de base.
         */
        public static void setBaseArmor(Entity entity, double newArmor) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.ARMOR)).setBaseValue(newArmor);
            }
        }

        /**
         * Définit la robustesse de l'armure de base d'une entité vivante.
         *
         * @param entity            L'entité à modifier.
         * @param newArmorToughness La nouvelle valeur de la robustesse de l'armure de base.
         */
        public static void setBaseArmorToughness(Entity entity, double newArmorToughness) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.ARMOR_TOUGHNESS)).setBaseValue(newArmorToughness);
            }
        }

        /**
         * Définit le recul d'attaque de base d'une entité vivante.
         *
         * @param entity             L'entité à modifier.
         * @param newAttackKnockback La nouvelle valeur du recul d'attaque de base.
         */
        public static void setBaseAttackKnockback(Entity entity, double newAttackKnockback) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.ATTACK_KNOCKBACK)).setBaseValue(newAttackKnockback);
            }
        }

        /**
         * Définit la résistance au recul de base d'une entité vivante.
         *
         * @param entity                 L'entité à modifier.
         * @param newKnockbackResistance La nouvelle valeur de la résistance au recul de base.
         */
        public static void setBaseKnockbackResistance(Entity entity, double newKnockbackResistance) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE)).setBaseValue(newKnockbackResistance);
            }
        }

        /**
         * Définit la chance de base d'une entité vivante.
         *
         * @param entity  L'entité à modifier.
         * @param newLuck La nouvelle valeur de la chance de base.
         */
        public static void setBaseLuck(Entity entity, double newLuck) {
            if (entity instanceof LivingEntity livingEntity) {
                Objects.requireNonNull(livingEntity.getAttribute(Attributes.LUCK)).setBaseValue(newLuck);
            }
        }

        /**
         * Récupère la valeur de dégâts de base d'une entité vivante.
         *
         * @param entity L'entité dont on souhaite obtenir la valeur des dégâts de base.
         * @return La valeur des dégâts de base, ou 0.0 si l'entité n'est pas vivante.
         */
        public static double getBaseDamage(Entity entity) {
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
            }
            return 0.0;
        }

        /**
         * Récupère la valeur des dégâts actuels d'une entité vivante.
         *
         * @param entity L'entité dont on souhaite obtenir la valeur des dégâts actuels.
         * @return La valeur des dégâts actuels, ou 0.0 si l'entité n'est pas vivante ou n'a pas cet attribut.
         */
        public static double getDamage(Entity entity) {
            if (entity instanceof LivingEntity livingEntity) {
                AttributeInstance attackDamageInstance = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
                if (attackDamageInstance != null) {
                    return attackDamageInstance.getValue();
                }
                return 0.0;
            }
            return 0.0;
        }

        /**
         * Récupère la valeur de la robustesse de l'armure de base d'une entité vivante.
         *
         * @param entity L'entité dont on souhaite obtenir la valeur de la robustesse de l'armure de base.
         * @return La valeur de la robustesse de l'armure de base, ou 0.0 si l'entité n'est pas vivante.
         */
        public static double getBaseArmorToughness(Entity entity) {
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity.getAttributeBaseValue(Attributes.ARMOR_TOUGHNESS);
            }
            return 0.0;
        }

        /**
         * Récupère la valeur de la force de saut de base d'une entité vivante.
         *
         * @param entity L'entité dont on souhaite obtenir la valeur de la force de saut de base.
         * @return La valeur de la force de saut de base, ou 0.0 si l'entité n'est pas vivante.
         */
        public static double getBaseJumpStrength(Entity entity) {
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity.getAttributeBaseValue(Attributes.JUMP_STRENGTH);
            }
            return 0.0;
        }

        /**
         * Récupère la valeur du recul d'attaque de base d'une entité vivante.
         *
         * @param entity L'entité dont on souhaite obtenir la valeur du recul d'attaque de base.
         * @return La valeur du recul d'attaque de base, ou 0.0 si l'entité n'est pas vivante.
         */
        public static double getBaseArmorKnockback(Entity entity) {
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity.getAttributeBaseValue(Attributes.ATTACK_KNOCKBACK);
            }
            return 0.0;
        }

        /**
         * Récupère la valeur de la résistance au recul de base d'une entité vivante.
         *
         * @param entity L'entité dont on souhaite obtenir la valeur de la résistance au recul de base.
         * @return La valeur de la résistance au recul de base, ou 0.0 si l'entité n'est pas vivante.
         */
        public static double getBaseKnockbackResistance(Entity entity) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                return livingEntity.getAttributeBaseValue(Attributes.KNOCKBACK_RESISTANCE);
            }
            return 0.0;
        }

        /**
         * Récupère la valeur de la chance de base d'une entité vivante.
         *
         * @param entity L'entité dont on souhaite obtenir la valeur de la chance de base.
         * @return La valeur de la chance de base, ou 0.0 si l'entité n'est pas vivante.
         */
        public static double getBaseLuck(Entity entity) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                return livingEntity.getAttributeBaseValue(Attributes.LUCK);
            }
            return 0.0;
        }
    }

    public abstract static class ServerUtil {
        /**
         * Vérifie si un mod est installé dans le jeu.
         *
         * @param modId L'ID du mod à vérifier.
         * @return {@code true} si le mod est installé, sinon {@code false}.
         */
        public static boolean isModInstalled(String modId) {
            return ModList.get().isLoaded(modId);
        }

        /**
         * Définit le message du jour (MOTD) du serveur Minecraft.
         * Si le paramètre {@code callback} est activé, un message de log est généré pour indiquer le changement.
         *
         * @param motd     Le message du jour à afficher sur le serveur.
         * @param callback Indicateur de log pour signaler que le MOTD a été mis à jour.
         */
        public static void setMotd(String motd, boolean callback) {
            ServerLifecycleHooks.getCurrentServer().setMotd(motd);
            if (callback) {
                BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "MOTD set to " + motd);
            }
        }

        /**
         * Surcharge de {@link #setMotd(String, boolean)} sans la possibilité de spécifier le callback.
         * Par défaut, le callback est désactivé.
         *
         * @param motd Le message du jour à définir sur le serveur.
         */
        public static void setMotd(String motd) {
            setMotd(motd, false);
        }

        /**
         * Téléporte une entité dans le monde serveur aux coordonnées spécifiées.
         * Si le paramètre {@code callback} est activé, un message de log est généré pour indiquer le déplacement.
         *
         * @param world    Le monde où se trouve l'entité.
         * @param entity   L'entité à téléporter.
         * @param x        La coordonnée X de destination.
         * @param y        La coordonnée Y de destination.
         * @param z        La coordonnée Z de destination.
         * @param callback Indicateur de log pour signaler que l'entité a été téléportée.
         */
        public static void teleportWorldEntity(LevelAccessor world, Entity entity, double x, double y, double z, boolean callback) {
            if (world instanceof ServerLevel serverWorld) {
                Set<RelativeMovement> movementsVide = EnumSet.noneOf(RelativeMovement.class);
                Set<RelativeMovement> tousLesMovements = EnumSet.allOf(RelativeMovement.class);
                entity.teleportTo(serverWorld, x, y, z, tousLesMovements, 0, 0);
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", entity.getStringUUID() + " has been teleported to x: " + x + " y: " + y + " z: " + z + " in " + serverWorld.getServer().getServerModName());
                }
            } else {
                if (callback) {
                    BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "The current world is not a server world.");
                }
            }
        }

        /**
         * Surcharge de la méthode {@link #teleportWorldEntity(LevelAccessor, Entity, double, double, double, boolean)}
         * sans possibilité de spécifier le callback. Le callback est désactivé par défaut.
         *
         * @param world  Le monde où se trouve l'entité.
         * @param entity L'entité à téléporter.
         * @param x      La coordonnée X de destination.
         * @param y      La coordonnée Y de destination.
         * @param z      La coordonnée Z de destination.
         */
        public static void teleportWorldEntity(LevelAccessor world, Entity entity, double x, double y, double z) {
            teleportWorldEntity(world, entity, x, y, z, false);
        }

        /**
         * Récupère le nom du monde dans lequel se trouve un {@link LevelAccessor}.
         *
         * @param world Le monde dont on veut récupérer le nom.
         * @return Le nom du monde.
         */
        public static String getWorldName(LevelAccessor world) {
            return Objects.requireNonNull(world.getServer()).getWorldData().getLevelName();
        }

        /**
         * Supprime tous les objets au sol qui sont dans le monde depuis un temps défini.
         *
         * @param world  Le monde dans lequel les objets doivent être vérifiés.
         * @param maxAge Le temps maximal (en ticks) qu'un objet peut rester avant d'être supprimé.
         */
        public static int killOldItems(ServerLevel world, int maxAge) {
            final int[] count = new int[1];
            GameModeHandler.executeServerOnlyCode(() -> {
                for (Entity entity : world.getEntities().getAll()) {
                    // Vérifie si l'entité est un ItemEntity (objet au sol)
                    if (entity instanceof ItemEntity itemEntity) {
                        // Vérifie si l'objet est plus ancien que le temps spécifié
                        if (itemEntity.getAge() >= maxAge) {
                            count[0] += itemEntity.getItem().getCount(); // Ajoute la quantité de la pile
                            itemEntity.kill(); // Supprime l'entité
                        }
                        //SuperLog.info("TetraLibs/Util", "" + ItemEntity.ID_TAG + " age: " + itemEntity.getAge());
                    }
                }
            });
            return count[0];
        }

        public static String getWorldFolderName() {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                // récupère le Path vers le dossier du monde
                Path worldPath = server.getWorldPath(LevelResource.ROOT);
                // convertit en chemin absolu, normalise et prend le dernier élément
                Path absolute = worldPath.toAbsolutePath().normalize();
                Path folderName = absolute.getFileName();
                if (folderName != null) {
                    return folderName.toString();
                }
            }
            return "unknown_world";
        }

        /**
         * Récupère une équipe par son nom.
         *
         * @param world    Le monde dans lequel chercher l'équipe.
         * @param teamName Le nom de l'équipe.
         * @return L'objet Team correspondant au nom fourni, ou null si l'équipe n'existe pas.
         */
        public static Team getTeam(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                for (Team team : scoreboard.getPlayerTeams()) {
                    if (team.getName().equals(teamName)) {
                        return team;
                    }
                }
            }
            return null;
        }

        /**
         * Récupère le nombre de joueurs dans une équipe.
         *
         * @param world    Le monde dans lequel chercher l'équipe.
         * @param teamName Le nom de l'équipe.
         * @return Le nombre de joueurs dans l'équipe, ou 0 si l'équipe n'existe pas.
         */
        public static double getPlayersNumber(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                Team team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getPlayers().size();
            }
            return 0.0;
        }

        /**
         * Récupère le nombre total d'équipes.
         *
         * @param world Le monde dans lequel chercher les équipes.
         * @return Le nombre total d'équipes.
         */
        public static double getTeamsNumber(LevelAccessor world) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                return scoreboard.getPlayerTeams().size();
            }
            return 0.0;
        }

        /**
         * Vérifie si une équipe existe dans le monde.
         *
         * @param world    Le monde dans lequel vérifier l'existence de l'équipe.
         * @param teamName Le nom de l'équipe à vérifier.
         * @return true si l'équipe existe, false sinon.
         */
        public static boolean teamExists(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();

                for (Team team : scoreboard.getPlayerTeams()) {
                    if (team.getName().equals(teamName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Récupère le nom de l'équipe d'un joueur.
         *
         * @param world  Le monde dans lequel chercher.
         * @param entity L'entité (joueur) pour laquelle obtenir le nom de l'équipe.
         * @return Le nom de l'équipe, ou "null" si aucune équipe n'est trouvée.
         */
        public static String getTeamName(LevelAccessor world, Entity entity) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                if (entity instanceof LivingEntity livingEntity) {
                    String teamName = "";
                    for (Team team : scoreboard.getPlayerTeams()) {
                        String temp = team.getName();
                        Collection<String> list = team.getPlayers();
                        for (String x : list) {
                            if ((livingEntity.getDisplayName().getString()).equals(x)) {
                                teamName = temp;
                            }
                        }
                        return teamName;
                    }
                }
            }
            return "null";
        }

        /**
         * Modifie les règles de collision d'une équipe.
         *
         * @param world    Le monde dans lequel chercher l'équipe.
         * @param teamName Le nom de l'équipe à modifier.
         * @param rule     La règle de collision à appliquer sous forme de chaîne de caractères.
         * @param callback Si true, affiche un message de log après avoir changé la règle de collision.
         */
        public static void setCollisionRules(LevelAccessor world, String teamName, String rule, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                Team.CollisionRule r = colisionRulesFromString(rule);
                assert team != null;
                assert r != null;
                team.setCollisionRule(r);
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "CollisionRules for " + teamName + " has been set to " + rule);
                }
            }
        }

        /**
         * Modifie les règles de collision d'une équipe.
         *
         * @param world    Le monde dans lequel chercher l'équipe.
         * @param teamName Le nom de l'équipe à modifier.
         * @param rule     La règle de collision à appliquer sous forme de chaîne de caractères.
         */
        public static void setCollisionRules(LevelAccessor world, String teamName, String rule) {
            setCollisionRules(world, teamName, rule, false);
        }

        /**
         * Modifie la couleur d'une équipe.
         *
         * @param world    Le monde dans lequel chercher l'équipe.
         * @param teamName Le nom de l'équipe à modifier.
         * @param color    La couleur à appliquer sous forme de chaîne de caractères.
         * @param callback Si true, affiche un message de log après avoir changé la couleur.
         */
        public static void setColor(LevelAccessor world, String teamName, String color, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                team.setColor(colorFromString(color));
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "Color for " + teamName + " has been set to " + color);
                }
            }
        }

        /**
         * Modifie la couleur d'une équipe.
         *
         * @param world    Le monde dans lequel chercher l'équipe.
         * @param teamName Le nom de l'équipe à modifier.
         * @param color    La couleur à appliquer sous forme de chaîne de caractères.
         */
        public static void setColor(LevelAccessor world, String teamName, String color) {
            setCollisionRules(world, teamName, color, false);
        }

        /**
         * Modifie la visibilité du message de décès d'une équipe.
         *
         * @param world      Le monde dans lequel chercher l'équipe.
         * @param teamName   Le nom de l'équipe à modifier.
         * @param visibility La visibilité du message de décès sous forme de chaîne de caractères.
         * @param callback   Si true, affiche un message de log après avoir changé la visibilité.
         */
        public static void setDeathMessageVisibility(LevelAccessor world, String teamName, String visibility, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                Team.Visibility v = teamVisibilityFromString(visibility);
                assert team != null;
                assert v != null;
                team.setDeathMessageVisibility(v);
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "DeathMessageVisibility for " + teamName + " has been set to " + visibility);
                }
            }
        }

        /**
         * Modifie la visibilité du message de décès d'une équipe.
         *
         * @param world      Le monde dans lequel chercher l'équipe.
         * @param teamName   Le nom de l'équipe à modifier.
         * @param visibility La visibilité du message de décès sous forme de chaîne de caractères.
         */
        public static void setDeathMessageVisibility(LevelAccessor world, String teamName, String visibility) {
            setDeathMessageVisibility(world, teamName, visibility, false);
        }

        /**
         * Définit le nom affiché pour une équipe donnée.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param name     Le nouveau nom affiché.
         * @param callback Si true, enregistre un log pour indiquer que le nom a été modifié.
         */
        public static void setDisplayName(LevelAccessor world, String teamName, String name, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                team.setDisplayName(Component.literal(name)); // Met à jour le nom affiché de l'équipe.
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "DisplayName for " + teamName + " has been set to " + name);
                }
            }
        }

        /**
         * Définit le nom affiché pour une équipe donnée sans log de callback.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param name     Le nouveau nom affiché.
         */
        public static void setDisplayName(LevelAccessor world, String teamName, String name) {
            setDisplayName(world, teamName, name, false);
        }

        /**
         * Définit si les membres de l'équipe peuvent se causer des dégâts entre eux (FriendlyFire).
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param fire     Si true, les membres de l'équipe peuvent se causer des dégâts entre eux.
         * @param callback Si true, enregistre un log pour indiquer que l'option a été modifiée.
         */
        public static void setFriendlyFire(LevelAccessor world, String teamName, Boolean fire, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                team.setAllowFriendlyFire(fire); // Active ou désactive le friendly fire pour l'équipe.
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "FriendlyFire for " + teamName + " has been set to " + fire);
                }
            }
        }

        /**
         * Définit si les membres de l'équipe peuvent se causer des dégâts entre eux sans log de callback.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param fire     Si true, les membres de l'équipe peuvent se causer des dégâts entre eux.
         */
        public static void setFriendlyFire(LevelAccessor world, String teamName, Boolean fire) {
            setFriendlyFire(world, teamName, fire, false);
        }

        /**
         * Définit la visibilité des étiquettes de nom pour les membres d'une équipe.
         *
         * @param world      Le monde dans lequel l'équipe est située.
         * @param teamName   Le nom de l'équipe.
         * @param visibility La nouvelle visibilité de l'étiquette de nom.
         * @param callback   Si true, enregistre un log pour indiquer que la visibilité a été modifiée.
         */
        public static void setNametagVisibility(LevelAccessor world, String teamName, String visibility, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                Team.Visibility v = teamVisibilityFromString(visibility);
                assert v != null;
                assert team != null;
                team.setNameTagVisibility(v); // Met à jour la visibilité des étiquettes de nom.
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "NametagVisibility for " + teamName + " has been set to " + visibility);
                }
            }
        }

        /**
         * Définit la visibilité des étiquettes de nom sans log de callback.
         *
         * @param world      Le monde dans lequel l'équipe est située.
         * @param teamName   Le nom de l'équipe.
         * @param visibility La nouvelle visibilité de l'étiquette de nom.
         */
        public static void setNametagVisibility(LevelAccessor world, String teamName, String visibility) {
            setNametagVisibility(world, teamName, visibility, false);
        }

        /**
         * Définit le préfixe des joueurs dans une équipe.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param prefix   Le préfixe à appliquer.
         * @param callback Si true, enregistre un log pour indiquer que le préfixe a été modifié.
         */
        public static void setPrefix(LevelAccessor world, String teamName, String prefix, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                team.setPlayerPrefix(Component.literal(prefix)); // Met à jour le préfixe de l'équipe.
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "Prefix for " + teamName + " has been set to " + prefix);
                }
            }
        }

        /**
         * Définit le préfixe des joueurs dans une équipe sans log de callback.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param prefix   Le préfixe à appliquer.
         */
        public static void setPrefix(LevelAccessor world, String teamName, String prefix) {
            setPrefix(world, teamName, prefix, false);
        }

        /**
         * Définit si les membres de l'équipe peuvent voir les invisibles alliés.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param see      Si true, les membres de l'équipe peuvent voir les invisibles alliés.
         * @param callback Si true, enregistre un log pour indiquer que la visibilité a été modifiée.
         */
        public static void setSeeFriendlyInvisibles(LevelAccessor world, String teamName, Boolean see, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                team.setSeeFriendlyInvisibles(see); // Active ou désactive la possibilité de voir les invisibles alliés.
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "SeeFriendlyInvisibles for " + teamName + " has been set to " + see);
                }
            }
        }

        /**
         * Définit si les membres de l'équipe peuvent voir les invisibles alliés sans log de callback.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param see      Si true, les membres de l'équipe peuvent voir les invisibles alliés.
         */
        public static void setSeeFriendlyInvisibles(LevelAccessor world, String teamName, Boolean see) {
            setSeeFriendlyInvisibles(world, teamName, see, false);
        }

        /**
         * Définit le suffixe des joueurs dans une équipe.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param suffix   Le suffixe à appliquer.
         * @param callback Si true, enregistre un log pour indiquer que le suffixe a été modifié.
         */
        public static void setSuffix(LevelAccessor world, String teamName, String suffix, boolean callback) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                team.setPlayerSuffix(Component.literal(suffix)); // Met à jour le suffixe de l'équipe.
                if (callback) {
                    BCKLog.info(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/ServerUtil", "Suffix for " + teamName + " has been set to " + suffix);
                }
            }
        }

        /**
         * Définit le suffixe des joueurs dans une équipe sans log de callback.
         *
         * @param world    Le monde dans lequel l'équipe est située.
         * @param teamName Le nom de l'équipe.
         * @param suffix   Le suffixe a appliqué.
         */
        public static void setSuffix(LevelAccessor world, String teamName, String suffix) {
            setSuffix(world, teamName, suffix, false);
        }

        /**
         * Récupère la règle de collision brute pour une équipe donnée.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return La règle de collision de l'équipe.
         */
        public static Team.CollisionRule getRawCollisionRules(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getCollisionRule();
            }
            return Team.CollisionRule.ALWAYS;
        }

        /**
         * Récupère la règle de collision sous forme de chaîne pour une équipe donnée.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return La règle de collision sous forme de chaîne.
         */
        public static String getStringCollisionRules(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return colisionRulesToString(team.getCollisionRule());
            }
            return "always";
        }

        /**
         * Récupère la couleur brute de l'équipe.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return La couleur de l'équipe.
         */
        public static ChatFormatting getRawColor(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getColor();
            }
            return ChatFormatting.WHITE;
        }

        /**
         * Récupère la couleur de l'équipe sous forme de chaîne.
         *
         * @param world     L'environnement de jeu.
         * @param teamName  Le nom de l'équipe.
         * @param lowercase Si vrai, retourne la couleur en minuscules.
         * @return La couleur de l'équipe sous forme de chaîne.
         */
        public static String getStringColor(LevelAccessor world, String teamName, Boolean lowercase) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                String color = "" + team.getColor();
                if (lowercase) {
                    color = color.toLowerCase();
                }
                return color;
            }
            if (lowercase) {
                return "white";
            } else {
                return "WHITE";
            }
        }

        /**
         * Récupère la visibilité des messages de mort pour une équipe.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return La visibilité des messages de mort.
         */
        public static Team.Visibility getRawDeathMessageVisibility(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getDeathMessageVisibility();
            }
            return Team.Visibility.ALWAYS;
        }

        /**
         * Récupère la visibilité des messages de mort sous forme de chaîne.
         *
         * @param world     L'environnement de jeu.
         * @param teamName  Le nom de l'équipe.
         * @param lowercase Si vrai, retourne la visibilité en minuscules.
         * @return La visibilité des messages de mort sous forme de chaîne.
         */
        public static String getStringDeathMessageVisibility(LevelAccessor world, String teamName, Boolean lowercase) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                String msg = "" + team.getDeathMessageVisibility();
                if (lowercase) {
                    msg = msg.toLowerCase();
                }
                return msg;
            }
            if (lowercase) {
                return "always";
            } else {
                return "ALWAYS";
            }
        }

        /**
         * Récupère le nom affiché pour une équipe.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return Le nom affiché de l'équipe.
         */
        public static String getDisplayName(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getDisplayName().getString();
            }
            return "null";
        }

        /**
         * Récupère la visibilité des étiquettes de nom pour une équipe.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return La visibilité des étiquettes de nom.
         */
        public static Team.Visibility getRawNametagVisibility(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getNameTagVisibility();
            }
            return Team.Visibility.ALWAYS;
        }

        /**
         * Récupère la visibilité des étiquettes de nom sous forme de chaîne.
         *
         * @param world     L'environnement de jeu.
         * @param teamName  Le nom de l'équipe.
         * @param lowercase Si vrai, retourne la visibilité en minuscules.
         * @return La visibilité des étiquettes de nom sous forme de chaîne.
         */
        public static String getStringNametagVisibility(LevelAccessor world, String teamName, Boolean lowercase) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                String msg = "" + team.getNameTagVisibility();
                if (lowercase) {
                    msg = msg.toLowerCase();
                }
                return msg;
            }
            if (lowercase) {
                return "always";
            } else {
                return "ALWAYS";
            }
        }

        /**
         * Récupère le préfixe d'une équipe.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return Le préfixe de l'équipe.
         */
        public static String getPrefix(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getPlayerPrefix().getString();
            }
            return "null";
        }

        /**
         * Récupère le suffixe d'une équipe.
         *
         * @param world    L'environnement de jeu.
         * @param teamName Le nom de l'équipe.
         * @return Le suffixe de l'équipe.
         */
        public static String getSuffix(LevelAccessor world, String teamName) {
            if (world instanceof Level level) {
                Scoreboard scoreboard = level.getScoreboard();
                PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                assert team != null;
                return team.getPlayerSuffix().getString();
            }
            return "null";
        }

        /**
         * Convertit une chaîne en une règle de collision pour une équipe.
         *
         * @param str La chaîne représentant la règle de collision.
         * @return La règle de collision correspondante.
         */
        public static Team.CollisionRule colisionRulesFromString(String str) {
            return switch (str.toLowerCase()) {
                case "always" -> Team.CollisionRule.ALWAYS;
                case "never" -> Team.CollisionRule.NEVER;
                case "pushotherteams" -> Team.CollisionRule.PUSH_OTHER_TEAMS;
                case "pushownteam" -> Team.CollisionRule.PUSH_OWN_TEAM;
                default -> null;
            };
        }

        /**
         * Convertit une règle de collision en une chaîne.
         *
         * @param str La règle de collision à convertir.
         * @return La règle de collision sous forme de chaîne.
         */
        public static String colisionRulesToString(Team.CollisionRule str) {
            if (str == Team.CollisionRule.ALWAYS) {
                return "always";
            } else if (str == Team.CollisionRule.NEVER) {
                return "never";
            } else if (str == Team.CollisionRule.PUSH_OTHER_TEAMS) {
                return "pushotherteams";
            } else if (str == Team.CollisionRule.PUSH_OWN_TEAM) {
                return "pushownteam";
            } else {
                return "null";
            }
        }

        /**
         * Convertit une chaîne en une couleur de texte Minecraft.
         *
         * @param str La chaîne représentant la couleur.
         * @return La couleur correspondante sous forme de `ChatFormatting`.
         */
        public static ChatFormatting colorFromString(String str) {
            return switch (str.toLowerCase()) {
                case "aqua" -> ChatFormatting.AQUA;
                case "black" -> ChatFormatting.BLACK;
                case "blue" -> ChatFormatting.BLUE;
                case "dark_aqua" -> ChatFormatting.DARK_AQUA;
                case "dark_blue" -> ChatFormatting.DARK_BLUE;
                case "dark_gray" -> ChatFormatting.DARK_GRAY;
                case "dark_green" -> ChatFormatting.DARK_GREEN;
                case "dark_purple" -> ChatFormatting.DARK_PURPLE;
                case "dark_red" -> ChatFormatting.DARK_RED;
                case "gold" -> ChatFormatting.GOLD;
                case "gray" -> ChatFormatting.GRAY;
                case "green" -> ChatFormatting.GREEN;
                case "light_purple" -> ChatFormatting.LIGHT_PURPLE;
                case "red" -> ChatFormatting.RED;
                case "reset" -> ChatFormatting.RESET;
                case "yellow" -> ChatFormatting.YELLOW;
                default -> ChatFormatting.WHITE;
            };
        }

        /**
         * Convertit une chaîne en une visibilité de l'équipe.
         *
         * @param str La chaîne représentant la visibilité de l'équipe.
         * @return La visibilité de l'équipe correspondante.
         */
        public static Team.Visibility teamVisibilityFromString(String str) {
            return switch (str.toLowerCase()) {
                case "always" -> Team.Visibility.ALWAYS;
                case "never" -> Team.Visibility.NEVER;
                case "hideforotherteams" -> Team.Visibility.HIDE_FOR_OTHER_TEAMS;
                case "hideforownteam" -> Team.Visibility.HIDE_FOR_OWN_TEAM;
                default -> null;
            };
        }
    }

    public abstract static class ClientUtil {
        /**
         * Définit la valeur de gamma pour le joueur, modifiant la luminosité du jeu.
         *
         * @param gammaValue La valeur de gamma à définir (généralement entre 0 et 1).
         */
        public static void setPlayerGamma(double gammaValue) {
            Minecraft mc = Minecraft.getInstance();
            Options gameSettings = mc.options;

            gameSettings.gamma().set(gammaValue);

            gameSettings.save();
        }
    }

    public abstract static class GenericUtil {
        /**
         * Génère un UUID personnalisé de la longueur spécifiée.
         * L'UUID est généré à l'aide d'une séquence aléatoire de caractères hexadécimaux.
         *
         * @param size La taille souhaitée de l'UUID. La taille doit être supérieure à zéro.
         * @return Un UUID aléatoire sous forme de chaîne de caractères.
         */
        public static String randomUUID(int size) {
            SecureRandom random = new SecureRandom();
            StringBuilder uuidBuilder = new StringBuilder();

            int dashPositions = 8;
            int remainingSize = size;

            for (int i = 0; i < dashPositions; i++) {
                int groupSize = (i == dashPositions - 1) ? remainingSize : 4;
                remainingSize -= groupSize;

                for (int j = 0; j < groupSize; j++) {
                    int randomChar = random.nextInt(16);
                    uuidBuilder.append(Integer.toHexString(randomChar));
                }

                if (i < dashPositions - 1) {
                    uuidBuilder.append("-");
                }
            }

            return uuidBuilder.toString();
        }

        /**
         * Surcharge de la méthode {@link #randomUUID(int)} sans la possibilité de spécifier la taille.
         * Par défaut, la taille de l'UUID est de 36 caractères.
         *
         * @return Un UUID aléatoire de taille 36.
         */
        public static String randomUUID() {
            return randomUUID(36);
        }

        public static List<String> getModsUsing(String id) {
            return ModList.get().getMods().stream().filter(modInfo -> modInfo.getDependencies().stream().anyMatch(dep -> dep.getModId().equals(id))).map(IModInfo::getModId).toList();
        }
    }

    public abstract static class FileUtil {
        /**
         * Charge un objet JSON depuis un fichier spécifié. Si le fichier n'existe pas ou est vide,
         * un objet JSON vide est retourné.
         *
         * @param filePath Le chemin du fichier JSON à charger.
         * @return L'objet JSON chargé depuis le fichier.
         * @throws IOException Si une erreur de lecture du fichier se produit.
         */
        public static JsonObject loadJson(String filePath) throws IOException {
            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) {
                return new JsonObject(); // Retourne un objet JSON vide si le fichier n'existe pas ou est vide
            }

            try (Reader reader = Files.newBufferedReader(file.toPath())) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }

        /**
         * Sauvegarde un objet JSON dans un fichier.
         * Cette méthode utilise la bibliothèque Gson pour sérialiser l'objet JSON au format texte
         * et l'écrire dans le fichier spécifié.
         *
         * @param jsonObject L'objet JSON à sauvegarder.
         * @param filePath   Le chemin du fichier où sauvegarder l'objet JSON.
         * @throws IOException Si une erreur d'écriture du fichier se produit, une exception est levée.
         */
        public static void saveJsonObjectToFile(JsonObject jsonObject, String filePath) throws IOException {
            try (Writer writer = Files.newBufferedWriter(Paths.get(filePath))) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(jsonObject, writer); // Sérialisation de l'objet JSON et écriture dans le fichier
            }
        }

        public static Object getFromJson(JsonObject jsonObject, String keyPath) {
            try {
                // Divise le chemin imbriqué en un tableau de clés
                String[] keys = keyPath.split("\\.");
                JsonElement currentElement = jsonObject;

                // Parcourt chaque clé pour descendre dans l'objet JSON
                for (String key : keys) {
                    // Si l'élément courant est un objet JSON et qu'il contient la clé actuelle
                    if (currentElement.isJsonObject() && currentElement.getAsJsonObject().has(key)) {
                        // Passe à l'élément suivant dans la hiérarchie
                        currentElement = currentElement.getAsJsonObject().get(key);
                    } else {
                        // Si une clé n'existe pas, retourne null
                        return null;
                    }
                }

                // Détermine le type de la valeur trouvée et retourne la valeur correspondante
                if (currentElement.isJsonPrimitive()) {
                    JsonPrimitive primitive = currentElement.getAsJsonPrimitive();
                    if (primitive.isString()) {
                        return primitive.getAsString(); // Retourne une chaîne
                    } else if (primitive.isNumber()) {
                        Number number = primitive.getAsNumber();
                        return (number.doubleValue() % 1 == 0) ? number.intValue() : number.doubleValue(); // Retourne un entier ou un double
                    } else if (primitive.isBoolean()) {
                        return primitive.getAsBoolean(); // Retourne un booléen
                    }
                } else if (currentElement.isJsonObject()) {
                    return currentElement.getAsJsonObject(); // Retourne un objet JSON
                } else if (currentElement.isJsonArray()) {
                    JsonArray jsonArray = currentElement.getAsJsonArray();

                    // Vérifie si tous les éléments du tableau sont des chaînes
                    boolean allStrings = true;
                    for (JsonElement element : jsonArray) {
                        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                            allStrings = false;
                            break;
                        }
                    }

                    // Si tous les éléments sont des chaînes, retourne un tableau de chaînes
                    if (allStrings) {
                        String[] result = new String[jsonArray.size()];
                        for (int i = 0; i < jsonArray.size(); i++) {
                            result[i] = jsonArray.get(i).getAsString();
                        }
                        return result; // Retourne un tableau de chaînes
                    }

                    // Si le tableau contient d'autres types, retourne un tableau générique d'objets
                    Object[] result = new Object[jsonArray.size()];
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonElement element = jsonArray.get(i);
                        result[i] = parseJsonElement(element);
                    }
                    return result; // Retourne un tableau d'objets
                }

                return null; // Si le type n'est pas pris en charge, retourne null
            } catch (Exception e) {
                BCKLog.error(BCKCore.TITLES_COLORS.title(BCKUtils.class) + "§6/FileUtil", "Error parsing JSON for keyPath '§8" + keyPath + "§r': ", e);
                return null;
            }
        }

        // Méthode auxiliaire pour analyser un élément JSON
        public static Object parseJsonElement(JsonElement element) {
            if (element.isJsonPrimitive()) {
                JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isString()) {
                    return primitive.getAsString();
                } else if (primitive.isNumber()) {
                    Number number = primitive.getAsNumber();
                    return (number.doubleValue() % 1 == 0) ? number.intValue() : number.doubleValue();
                } else if (primitive.isBoolean()) {
                    return primitive.getAsBoolean();
                }
            } else if (element.isJsonObject()) {
                return element.getAsJsonObject();
            } else if (element.isJsonArray()) {
                return element.getAsJsonArray();
            }
            return null;
        }
    }

    public abstract static class EnchantmentUtil {
        /**
         * Applique un enchantement aléatoire à un objet (ItemStack).
         * <p>
         * Cette méthode choisit un enchantement aléatoire parmi tous les enchantements disponibles
         * dans le jeu et lui attribue un niveau d'enchantement aléatoire, en respectant les limites
         * minimales et maximales de niveau de l'enchantement choisi.
         * <p>
         * Si le paramètre `verif` est activé, l'enchantement n'est appliqué que si l'objet
         * peut recevoir cet enchantement.
         *
         * @param item  L'objet (ItemStack) sur lequel l'enchantement sera appliqué.
         * @param min   Niveau minimum
         * @param max   Niveau maximum
         * @param level Niveau définit
         * @param verif Si true, vérifie que l'enchantement peut être appliqué à l'objet avant de l'appliquer.
         * @return L'objet (ItemStack) après l'application de l'enchantement.
         */
        public static ItemStack applyRandomEnchantment(ItemStack item, int min, int max, int level, boolean verif) {
            List<Enchantment> enchantments = Lists.newArrayList(ForgeRegistries.ENCHANTMENTS.getValues());
            Random rand = new Random();
            Enchantment enchantment = enchantments.get(rand.nextInt(enchantments.size()));
            int enchantmentLevel = 1;
            if (min > 0 && max > 0) enchantmentLevel = (int) Mth.nextDouble(RandomSource.create(), min, max);
            else if (level > 0) enchantmentLevel = level;
            else
                enchantmentLevel = (int) Mth.nextDouble(RandomSource.create(), enchantment.getMinLevel(), enchantment.getMaxLevel());

            if (verif) {
                if (enchantment.canEnchant(item)) {
                    item.enchant(enchantment, enchantmentLevel);
                }
            } else {
                // Applique l'enchantement même sans vérification.
                item.enchant(enchantment, enchantmentLevel);
            }
            return item;
        }

        public static ItemStack applyRandomEnchantment(ItemStack item, int min, int max, int level) {
            return applyRandomEnchantment(item, min, max, level, false);
        }

        public static ItemStack applyRandomEnchantment(ItemStack item, int min, int max) {
            return applyRandomEnchantment(item, min, max, -1, false);
        }

        public static ItemStack applyRandomEnchantment(ItemStack item) {
            return applyRandomEnchantment(item, -1, -1, -1, false);
        }

        public static ItemStack applyRandomEnchantment(ItemStack item, int min, int max, boolean verif) {
            return applyRandomEnchantment(item, min, max, -1, verif);
        }

        public static ItemStack applyRandomEnchantment(ItemStack item, boolean verif) {
            return applyRandomEnchantment(item, -1, -1, -1, verif);
        }

        /**
         * Applique tous les enchantements possibles avec leurs niveaux maximums à un ItemStack.
         *
         * @param stack L'ItemStack sur lequel appliquer les enchantements.
         * @return L'ItemStack avec tous les enchantements appliqués.
         */
        public static ItemStack applyMaxEnchantments(ItemStack stack, boolean verif) {
            if (stack == null || stack.isEmpty()) {
                return stack; // Retourne le stack tel quel s'il est vide ou null
            }
            // Carte des enchantements avec leur niveau maximal
            Map<Enchantment, Integer> enchantments = new HashMap<>();

            // Parcours de tous les enchantements enregistrés dans le jeu
            for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
                if (verif) {
                    if (enchantment.canEnchant(stack)) {
                        enchantments.put(enchantment, enchantment.getMaxLevel());
                    }
                } else {
                    enchantments.put(enchantment, enchantment.getMaxLevel());
                }
            }

            // Applique tous les enchantements au stack
            EnchantmentHelper.setEnchantments(enchantments, stack);

            return stack; // Retourne le stack avec les enchantements
        }

        public static ItemStack applyMaxEnchantments(ItemStack stack) {
            return applyMaxEnchantments(stack, false);
        }
    }

    public abstract static class EffectUtil {
        /**
         * Applique un effet aléatoire à une entité vivante.
         * <p>
         * Cette méthode choisit aléatoirement un effet parmi ceux enregistrés dans le jeu.
         * Si le paramètre <code>`blacklist`</code> est activé, certains effets spécifiques (comme la guérison ou
         * le malus) seront exclus du tirage.
         *
         * @param victim    L'entité vivante (LivingEntity) à laquelle l'effet sera appliqué.
         * @param duration  La durée de l'effet en ticks (20 ticks = 1 seconde).
         * @param level     Le niveau de l'effet (random de base)
         * @param blacklist Si true, exclut les effets de guérison (HEAL) et de dommage (HARM).
         */
        public static void applyRandomEffect(Entity victim, int duration, int level, boolean blacklist) {
            if (victim instanceof LivingEntity living) {
                List<MobEffect> effects = Lists.newArrayList(ForgeRegistries.MOB_EFFECTS.getValues());

                if (blacklist) {
                    effects.remove(MobEffects.HEAL);
                    effects.remove(MobEffects.HARM);
                }

                MobEffect randomEffect = effects.get((int) (Math.random() * effects.size()));

                if (level < 0) level = (int) Mth.nextDouble(RandomSource.create(), 0, 10);

                living.addEffect(new MobEffectInstance(randomEffect, duration, level));
            }
        }

        public static void applyRandomEffect(Entity victim, int duration, int level) {
            applyRandomEffect(victim, duration, level, false);
        }

        public static void applyRandomEffect(Entity victim, int duration) {
            applyRandomEffect(victim, duration, -1, false);
        }

        public static void applyRandomEffect(Entity victim, int duration, boolean blacklist) {
            applyRandomEffect(victim, duration, -1, blacklist);
        }
    }

    public abstract static class NumberUtil {
        /**
         * Convertit un nombre en une chaîne de caractères avec un format adapté à la taille du nombre,
         * en arrondissant à un certain nombre de décimales.
         * Le format est adapté en fonction des intervalles : milliers, millions, milliards, trillions.
         *
         * @param number        Le nombre à convertir.
         * @param decimalDigits Le nombre de chiffres après la virgule à afficher.
         * @return La chaîne représentant le nombre avec son format adapté.
         */
        public static String convertNumberToString(double number, int decimalDigits) {
            // Si le nombre est inférieur à 1000, retourne simplement le nombre sous forme de chaîne.
            if (number < 1000) {
                return String.valueOf(number);
            }
            // Si le nombre est compris entre 1000 et 1 million, formate en milliers.
            else if (number < 1_000_000) {
                return String.format("%." + decimalDigits + "f" + Component.translatable("number_converter.thousand").getString(), number / 1000.0);
            }
            // Si le nombre est compris entre 1 million et 1 milliard, formate en millions.
            else if (number < 1_000_000_000) {
                return String.format("%." + decimalDigits + "f" + Component.translatable("number_converter.million").getString(), number / 1_000_000.0);
            }
            // Si le nombre est compris entre 1 milliard et 1 trillion, formate en milliards.
            else if (number < 1_000_000_000_000L) {
                return String.format("%." + decimalDigits + "f" + Component.translatable("number_converter.billion").getString(), number / 1_000_000_000.0);
            }
            // Si le nombre est supérieur ou égal à 1 trillion, formate en trillions.
            else {
                return String.format("%." + decimalDigits + "f" + Component.translatable("number_converter.trillion").getString(), number / 1_000_000_000_000.0);
            }
        }

        /**
         * Convertit un nombre en une chaîne de caractères avec un format adapté,
         * en arrondissant à 1 chiffre après la virgule.
         * Cette méthode appelle {@link #convertNumberToString(double, int)} avec un nombre de décimales fixé à 1.
         *
         * @param number Le nombre à convertir.
         * @return La chaîne représentant le nombre avec son format adapté.
         */
        public static String convertNumberToString(double number) {
            return convertNumberToString(number, 1);
        }
    }

    public abstract static class ParticleUtil {
        public static void showDeniedParticles(ServerPlayer player, SimpleParticleType particle, BlockPos pos) {
            ServerLevel level = player.serverLevel();

            /* 1. Choisissez le type de particule :
               - SOUL    : bleu/vert fantomatique
               - SMOKE   : fumée grise
               - ENCHANT : petites étoiles violettes
               - CRIT    : éclat blanc
             */

            /* 2. Appelez l'overload ciblé :
                  sendParticles(ServerPlayer cible, ParticleOptions type, boolean longDistance,
                                double x, double y, double z,
                                int count,
                                double offsetX, double offsetY, double offsetZ,
                                double speed)
               - longDistance = false  → inutile de le transmettre à > 32 blocs
               - count        = nb de particules
               - offsets      = « épaisseur » du nuage (0.25 = ¼ bloc)
               - speed        = vélocité (0.0 = statique)
            */
            level.sendParticles(player,                // 👈 uniquement ce joueur
                    particle, false,                 // longDistance
                    pos.getX() + 0.5,      // centre du bloc
                    pos.getY() + 1.0, pos.getZ() + 0.5, 20,                    // quantité
                    0.2, 0.3, 0.2,         // offsets XYZ
                    0.01                   // vitesse
            );
        }
    }
}