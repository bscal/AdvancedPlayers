package me.bscal.advancedplayer.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.bscal.advancedplayer.AdvancedPlayer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TraitCommands
{

    public static void Init()
    {
        CommandRegistrationCallback.EVENT.register(new TraitAddCmd());
    }

    static class TraitAddCmd implements CommandRegistrationCallback, Command<ServerCommandSource>
    {

        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
        {
            String arg = context.getArgument("trait_name", String.class);

            AdvancedPlayer.LOGGER.info(arg);

            var trait = AdvancedPlayer.APPlayerManager.TraitsRegister.get(arg);
            if (trait != null)
            {
                AdvancedPlayer.LOGGER.info("Command found trait!");
                var apPlayer = AdvancedPlayer.APPlayerManager.UUIDToPlayerMap.get(context.getSource().getPlayer().getUuid());
                apPlayer.AddTrait(trait);
            }

            return 0;
        }

        @Override
        public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment)
        {
            dispatcher.register(literal("traits").then(literal("add").then(argument("trait_name", StringArgumentType.word()).executes(this))));
        }
    }

}
