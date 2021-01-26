package de.mrgeorgen.wolfSignal;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.*;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static de.mrgeorgen.wolfSignal.ClientCommands.*;
import static de.mrgeorgen.wolfSignal.Util.*;
import static net.minecraft.command.arguments.NbtTagArgumentType.nbtTag;
import static net.minecraft.util.Formatting.*;

public class Main implements ModInitializer
{
	public static final String MOD_ID = "wolfsignal";
	public static int[] delayList = { 3, 10, 30, 60 };
	public static int ticks = -1;
	public static int attempt = -1;
	public static ServerInfo lastServerEntry = null;
	public static boolean pause = false;

	@Override
	public void onInitialize()
	{
		ClientCommands.register(literal("reload").executes(Main::cmdReload));
		ClientCommands.register(literal("config").then(argument("delayList", nbtTag()).executes(Main::cmdConfig)));
		loadConfig();
	}

	private static int cmdReload(CommandContext<ClientCommandSource> ctx)
	{
		loadConfig();
		return SINGLE_SUCCESS;
	}

	private static int cmdConfig(CommandContext<ClientCommandSource> ctx)
	{
		Tag tag = ctx.getArgument("delayList", Tag.class);
		try
		{
			// if tag is not a list or a list not containing integers it will pass null or an empty list
			setDelayList(tag instanceof AbstractListTag ? ((AbstractListTag<? extends Tag>) tag).stream().filter(IntTag.class::isInstance).map(IntTag.class::cast).mapToInt(IntTag::getInt).toArray() : null);
			saveConfig();
			send(colored("Current configuration: " + Arrays.toString(delayList), GREEN));
		}
		catch (IOException | IllegalArgumentException ex)
		{
			send(err(ex));
		}
		return SINGLE_SUCCESS;
	}

	private static void loadConfig()
	{
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
		try
		{
			setDelayList(new Gson().fromJson(Files.newBufferedReader(configPath), int[].class));
			send(colored("Current configuration: " + Arrays.toString(delayList), GREEN));
		}
		catch (IOException | IllegalArgumentException | JsonParseException ex)
		{
			send(err(ex));
			try
			{
				send(colored("Creating default config...", GREEN));
				saveConfig();
			}
			catch (IOException ex2)
			{
				send(err(ex2));
			}
		}
	}

	private static void saveConfig() throws IOException
	{
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
		File configFile = configPath.toFile();
		// if file already exists or could successfully be created
		if (configFile.exists() || configFile.createNewFile())
		{
			Files.write(configPath, new Gson().toJson(delayList).getBytes());
			send(colored("Saved config", GREEN));
		}
	}

	private static void setDelayList(int[] delayList) throws IllegalArgumentException
	{
		// if null or empty or contains negatives or zeros
		if (delayList == null || delayList.length == 0 || IntStream.of(delayList).anyMatch(i -> i <= 0))
			throw new IllegalArgumentException("delayList must be a non-empty list of strictly positive integers");
		Main.delayList = delayList;
	}

	public static void resetAttempts()
	{
		ticks = -1;
		attempt = -1;
		log("reset");
	}

	public static String getMessage()
	{
		return attempt < 0 ? "Could not reconnect" : String.format("Reconnect in %d...", ticks / 20 + 1);
	}

	public static int getColor()
	{
		return Optional.of(attempt < 0 ? RED : GREEN).filter(Formatting::isColor).map(Formatting::getColorValue).orElse(0xFFFFFF);
	}
}
