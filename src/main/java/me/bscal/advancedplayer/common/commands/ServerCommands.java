package me.bscal.advancedplayer.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import me.bscal.advancedplayer.common.items.MultiFoodItem;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ServerCommands
{

    public static final String INGREDIENT_ARG = "ingredient";

    public static void Init()
    {
        CommandRegistrationCallback.EVENT.register(new AddIngredientCommand());
        CommandRegistrationCallback.EVENT.register(new CombindSpoilageItemsAllCommand());
        CommandRegistrationCallback.EVENT.register(new CombindSpoilageItemsCommand());
    }

    static class AddIngredientCommand implements CommandRegistrationCallback, Command<ServerCommandSource>
    {

        @Override
        public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment)
        {
            dispatcher.register(
                    literal("multifood")
                            .then(literal("add")
                                    .then(argument(INGREDIENT_ARG, ItemStackArgumentType.itemStack(registryAccess))
                                            .executes(this))));
        }

        @Override
        public int run(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException
        {
            try
            {
                var player = ctx.getSource().getPlayer();
                if (player == null) return 1;

                var srcStack = ctx.getArgument(INGREDIENT_ARG, ItemStackArgument.class);
                var destStack = player.getMainHandStack();

                if (destStack == null) return 1;
                if (destStack.getItem() instanceof MultiFoodItem multiFoodItem)
                {
                    if (!multiFoodItem.TryAddIngredient(srcStack.getItem(), destStack)) return 1;
                    AdvancedPlayer.LOGGER.info("Successfully added ingredient");
                }
            } catch (Exception e)
            {
                AdvancedPlayer.LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            return 0;
        }
    }

    static class CombindSpoilageItemsAllCommand implements CommandRegistrationCallback, Command<ServerCommandSource>
    {

        @Override
        public int run(CommandContext<ServerCommandSource> context)
        {
            try
            {
                var player = context.getSource().getPlayer();
                if (player == null) return 0;

                ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);
                if (stackInHand == null) return Err(player, "Hold and item in your main hand to combine");

                var stackNbt = stackInHand.getNbt();
                if (stackNbt == null) return Err(player, "MainHand is not a valid item");
                if (!stackNbt.contains(AdvancedPlayer.KEY_ITEMSTACK_SPOIL)) return Err(player, "MainHand is not a valid item");

                boolean isLowestStackInHand;
                long currentTime = player.world.getTime();
                long lowestSpoiledTicks = ((ItemStackMixinInterface) (Object) stackInHand).GetTicksTillSpoiled(currentTime);
                for (int i = 0; i < player.getInventory().size(); ++i)
                {
                    var slotItem = player.getInventory().getStack(i);
                    if (slotItem.isEmpty()) continue;
                    if (slotItem == stackInHand) continue;
                    if (slotItem.getItem() != stackInHand.getItem()) continue;
                    if (slotItem.getCount() + stackInHand.getCount() > stackInHand.getMaxCount()) continue;

                    var slotNbt = slotItem.getNbt();
                    if (slotNbt == null) continue;
                    if (slotNbt.contains(AdvancedPlayer.KEY_ITEMSTACK_IS_SPOILED)) continue;

                    isLowestStackInHand = true;
                    long slotTicksTillSpoiled = ((ItemStackMixinInterface) (Object) slotItem).GetTicksTillSpoiled(currentTime);
                    if (slotTicksTillSpoiled < lowestSpoiledTicks)
                    {
                        lowestSpoiledTicks = slotTicksTillSpoiled;
                        isLowestStackInHand = false;
                    }

                    if (isLowestStackInHand)
                    {
                        slotItem.setNbt(stackNbt);
                    }
                    else
                    {
                        stackInHand.setNbt(slotNbt);
                    }

                    var mergedStack = ItemEntity.merge(stackInHand, slotItem, stackInHand.getMaxCount());
                    stackInHand.setCount(mergedStack.getCount());
                }
            } catch (Exception e)
            {
                AdvancedPlayer.LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment)
        {
            dispatcher.register(literal("food").then(literal("combine").then(literal("all").executes(this))));
        }
    }


    static class CombindSpoilageItemsCommand implements CommandRegistrationCallback, Command<ServerCommandSource>
    {

        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
        {
            try
            {
                var player = context.getSource().getPlayer();
                if (player == null) return 0;

                ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);
                if (stackInHand == null || stackInHand.isEmpty())
                    return Err(player, "MainHand is not a valid item");

                var stackNbt = stackInHand.getNbt();
                if (stackNbt == null) return Err(player, "MainHand is not a valid item");
                if (!stackNbt.contains(AdvancedPlayer.KEY_ITEMSTACK_SPOIL)) return Err(player, "MainHand is not a valid item");

                var stackInOffHand = player.getStackInHand(Hand.OFF_HAND);
                if (stackInOffHand == null || stackInOffHand.isEmpty())
                    return Err(player, "OffHand is not a valid item");

                var offNbt = stackInOffHand.getNbt();
                if (offNbt == null) return Err(player, "OffHand is not a valid item");
                if (!offNbt.contains(AdvancedPlayer.KEY_ITEMSTACK_SPOIL)) return Err(player, "OffHand is not a valid item");

                if (stackInOffHand.getItem() != stackInHand.getItem()) return Err(player, "Items are not the same");
                if (stackInOffHand.getCount() + stackInHand.getCount() > stackInHand.getMaxCount())
                    return Err(player, "Merging items would be greater than max stack count");

                long currentTime = player.world.getTime();
                long handSpoil = ((ItemStackMixinInterface) (Object) stackInHand).GetTicksTillSpoiled(currentTime);
                long offSpoil = ((ItemStackMixinInterface) (Object) stackInOffHand).GetTicksTillSpoiled(currentTime);

                if (handSpoil < offSpoil)
                    stackInOffHand.setNbt(stackNbt);
                else
                    stackInHand.setNbt(offNbt);

                var mergedStack = ItemEntity.merge(stackInHand, stackInOffHand, stackInHand.getMaxCount());
                stackInHand.setCount(mergedStack.getCount());
            } catch (Exception e)
            {
                AdvancedPlayer.LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment)
        {
            dispatcher.register(literal("food").then(literal("combine").then(literal("hand").executes(this))));
        }
    }

    private static int Err(PlayerEntity player, String msg)
    {
        player.sendMessage(Text.of(msg));
        return 1;
    }

}