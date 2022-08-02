package me.bscal.advancedplayer.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.items.MultiFoodItem;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ServerCommands
{

	public static final String INGREDIENT_ARG = "ingredient";

	public static void Init()
	{
		CommandRegistrationCallback.EVENT.register(new AddIngredientCommand());
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
			}
			catch (Exception e)
			{
				AdvancedPlayer.LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
			return 0;
		}

	}
}