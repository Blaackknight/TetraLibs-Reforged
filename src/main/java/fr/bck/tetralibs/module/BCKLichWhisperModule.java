package fr.bck.tetralibs.module;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import fr.bck.tetralibs.modules.lich.BCKLichWhisperEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
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

public final class BCKLichWhisperModule extends CoreDependentModule {
    @Override
    public ResourceLocation id() {
        return ModuleIds.BCK_LICH_WHISPER;
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

    @Override
    public void onServerSetup() {
        MinecraftForge.EVENT_BUS.register(new BCKLichWhisperEventHandler());
        BCKLog.debug(BCKCore.TITLES_COLORS.title(BCKLichWhisper.class), "§aHandlers registered");
    }

    @Override
    public Set<ResourceLocation> requiredModules() {
        return withCore(ModuleIds.BCK_USERDATA);
    }

    @Override
    public void onClientSetup() {
        // Client-only : renderer, GUI, keybinds…
    }

    @Override
    public void onCommand(CommandEvent event) {
        super.onCommand(event);
        Entity sender = event.getParseResults().getContext().getSource().getEntity();
        String command = event.getParseResults().getReader().getString();

        String[] datas = command.split(" ");
        String converted = ("server.command." + datas[0]);
        String player = "Server";
        if (sender != null) player = sender.getDisplayName().getString();
        BCKLichWhisper.send(BCKUtils.TextUtil.toStyled(((Component.translatable("lich_whisper.on_command", Component.literal(BCKUtils.TextUtil.universal(player, sender)), Component.literal(command))))), 4);  // Niveau de log 4 pour une information de niveau intermédiaire.
    }

    @Override
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        super.onBlockPlace(event);
        Entity player = event.getEntity() instanceof EnderMan ? event.getEntity() : (Player) event.getEntity();
        BlockPos pos = event.getPos();
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        LevelAccessor world = event.getLevel();
        BlockState block = event.getState();
        MinecraftServer serv = world.getServer();
        assert serv != null;
        WorldData data = serv.getWorldData();
        assert player != null;
        Component logMessage = BCKUtils.TextUtil.toStyled(Component.translatable("lich_whisper.on_block_place", Component.literal(BCKUtils.TextUtil.universal(player.getDisplayName().getString(), player)), Component.literal(String.valueOf(x)), Component.literal(String.valueOf(y)), Component.literal(String.valueOf(z)), Component.literal(data.getLevelName()), Component.literal(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block.getBlock())).toString())));
        BCKLichWhisper.send(logMessage, 5);
    }

    @Override
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        super.onBlockBreak(event);
        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        LevelAccessor world = event.getLevel();
        BlockState block = event.getState();
        MinecraftServer serv = world.getServer();
        assert serv != null;
        WorldData data = serv.getWorldData();
        Component logMessage = BCKUtils.TextUtil.toStyled(Component.translatable("lich_whisper.on_block_break", Component.literal(BCKUtils.TextUtil.universal(player.getDisplayName().getString(), player)), Component.literal(String.valueOf(x)), Component.literal(String.valueOf(y)), Component.literal(String.valueOf(z)), Component.literal(data.getLevelName()), Component.literal(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block.getBlock())).toString())));
        BCKLichWhisper.send(logMessage, 5);
    }

    @Override
    public void onItemDropped(ItemTossEvent event) {
        super.onItemDropped(event);
        Player player = event.getPlayer();
        ItemStack item = event.getEntity().getItem();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        LevelAccessor world = player.level();
        MinecraftServer serv = world.getServer();
        assert serv != null;
        WorldData data = serv.getWorldData();
        BCKLichWhisper.send(BCKUtils.TextUtil.toStyled(Component.translatable("lich_whisper.on_item_drop", Component.literal(BCKUtils.TextUtil.universal(player.getDisplayName().getString(), player)), Component.literal(String.valueOf(x)), Component.literal(String.valueOf(y)), Component.literal(String.valueOf(z)), Component.literal(data.getLevelName()), Component.literal(BCKUtils.ItemUtil.createItemComponent(item.getItem()).getString()), Component.literal(String.valueOf(item.getCount())))), 5);
    }

    @Override
    public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        super.onItemPickup(event);
        Player player = event.getEntity();
        ItemStack item = event.getStack();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        LevelAccessor world = player.level();
        MinecraftServer server = world.getServer();
        assert server != null;
        WorldData data = server.getWorldData();
        BCKLichWhisper.send(BCKUtils.TextUtil.toStyled(Component.translatable("lich_whisper.on_item_pickup", Component.literal(BCKUtils.TextUtil.universal(player.getDisplayName().getString(), player)), Component.literal(String.valueOf(x)), Component.literal(String.valueOf(y)), Component.literal(String.valueOf(z)), Component.literal(data.getLevelName()), Component.literal(BCKUtils.ItemUtil.createItemComponent(item.getItem()).getString()), Component.literal(String.valueOf(item.getCount())))), 5);
    }

    @Override
    public void onItemExpire(ItemExpireEvent event) {
        super.onItemExpire(event);
        Entity entity = event.getEntity();
        ItemStack item = event.getEntity().getItem();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        LevelAccessor world = entity.level();
        MinecraftServer serv = world.getServer();
        assert serv != null;
        WorldData data = serv.getWorldData();
        BCKLichWhisper.send(BCKUtils.TextUtil.toStyled(Component.translatable("lich_whisper.on_item_expire", Component.literal(BCKUtils.TextUtil.universal(entity.getDisplayName().getString(), entity)), Component.literal(String.valueOf(x)), Component.literal(String.valueOf(y)), Component.literal(String.valueOf(z)), Component.literal(data.getLevelName()), Component.literal(BCKUtils.ItemUtil.createItemComponent(item.getItem()).getString()), Component.literal(String.valueOf(item.getCount())))), 5);
    }

    @Override
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        super.onRightClickBlock(event);
        LevelAccessor world = event.getLevel();
        MinecraftServer serv = world.getServer();
        assert serv != null;
        WorldData data = serv.getWorldData();
        double x = event.getPos().getX();
        double y = event.getPos().getY();
        double z = event.getPos().getZ();
        BlockState blockstate = event.getLevel().getBlockState(event.getPos());
        Entity entity = event.getEntity();
        if (entity == null) return;
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (!entity.isShiftKeyDown() && blockstate.is(BlockTags.create(ResourceLocation.parse("forge:chests")))) {
            BCKLichWhisper.send(BCKUtils.TextUtil.toStyled(Component.translatable("lich_whisper.on_chest_open", Component.literal(BCKUtils.TextUtil.universal(entity.getDisplayName().getString(), entity)), Component.literal(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(blockstate.getBlock())).toString()), Component.literal(String.valueOf(z)), Component.literal(String.valueOf(y)), Component.literal(String.valueOf(x)), Component.literal(data.getLevelName()))), 5);
        }
    }
}
