package fr.bck.tetralibs.handlers;


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

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.bck.tetralibs.core.BCKUtils;
import fr.bck.tetralibs.economy.BCKEconomyManager;
import fr.bck.tetralibs.lich.BCKLichWhisper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class TetraLibsEconomyhandler {
    public abstract static class Bank {
        public static void add(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            Entity ent = new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity();
            double amount = DoubleArgumentType.getDouble(arguments, "amount");
            if (ent instanceof Player) {
                BCKEconomyManager.addBank((Player) ent, amount, true);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.tetralibs.economy.bank.add.success").getString()).replace("<player>", ent.getName().getString())).replace("<amount>", "" + amount))), false);
        }

        public static void remove(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            if ((new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()) instanceof Player) {
                BCKEconomyManager.removeBank((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()), (DoubleArgumentType.getDouble(arguments, "amount")), true);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.tetralibs.economy.bank.remove.success").getString()).replace("<player>", (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()).getName().getString())).replace("<amount>", "" + DoubleArgumentType.getDouble(arguments, "amount")))), false);
        }

        public static void set(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            if ((new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()) instanceof Player) {
                BCKEconomyManager.setBank((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()), (DoubleArgumentType.getDouble(arguments, "amount")), true);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.tetralibs.economy.bank.set.success").getString()).replace("<player>", (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()).getName().getString())).replace("<amount>", "" + DoubleArgumentType.getDouble(arguments, "amount")))), false);
        }

        public static void get(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            Entity ee = null;
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((((Component.translatable("command.tetralibs.economy.bank.get.text").getString()).replace("<amount>", "" + ((new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()) instanceof Player ? BCKEconomyManager.getBank((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity())) : 0))).replace("<player>", (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()).getName().getString())).replace("<formatted>", BCKUtils.NumberUtil.convertNumberToString(((new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()) instanceof Player ? BCKEconomyManager.getBank((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity())) : 0), 2)))), false);
        }
    }

    public abstract static class Pocket {
        public static void add(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            if ((new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()) instanceof Player) {
                BCKEconomyManager.addMoney((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()), (DoubleArgumentType.getDouble(arguments, "amount")), true);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.tetralibs.economy.player.add.success").getString()).replace("<player>", (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()).getName().getString())).replace("<amount>", "" + DoubleArgumentType.getDouble(arguments, "amount")))), false);
            BCKLichWhisper.send(Component.nullToEmpty((((Component.translatable("command.tetralibs.economy.player.add.success").getString()).replace("<player>", (new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()).getName().getString())).replace("<amount>", "" + DoubleArgumentType.getDouble(arguments, "amount")))), 6, true);
        }

        public static void remove(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            if ((new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()) instanceof Player) {
                BCKEconomyManager.removeMoney((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()), (DoubleArgumentType.getDouble(arguments, "amount")), true);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.tetralibs.economy.player.remove.success").getString()).replace("<player>", (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()).getName().getString())).replace("<amount>", "" + DoubleArgumentType.getDouble(arguments, "amount")))), false);
        }

        public static void set(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            if ((new Object() {
                public Entity getEntity() {
                    try {
                        return EntityArgument.getEntity(arguments, "player");
                    } catch (CommandSyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.getEntity()) instanceof Player) {
                BCKEconomyManager.setMoney((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()), (DoubleArgumentType.getDouble(arguments, "amount")), true);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled((((Component.translatable("command.tetralibs.economy.player.set.success").getString()).replace("<player>", (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()).getName().getString())).replace("<amount>", "" + DoubleArgumentType.getDouble(arguments, "amount")))), false);
        }

        public static void get(CommandContext<CommandSourceStack> arguments, Entity entity) {
            if (entity == null) return;
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(BCKUtils.TextUtil.toStyled(((((Component.translatable("command.tetralibs.economy.player.get.text").getString()).replace("<amount>", "" + ((new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()) instanceof Player ? BCKEconomyManager.getMoney((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity())) : 0))).replace("<player>", (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()).getName().getString())).replace("<formatted>", BCKUtils.NumberUtil.convertNumberToString(((new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity()) instanceof Player ? BCKEconomyManager.getMoney((Player) (new Object() {
                    public Entity getEntity() {
                        try {
                            return EntityArgument.getEntity(arguments, "player");
                        } catch (CommandSyntaxException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }.getEntity())) : 0), 2)))), false);
        }
    }
}
