package de.mrgeorgen.wolfSignal;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ServerInfo;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static de.mrgeorgen.wolfSignal.ClientCommands.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class Main implements ModInitializer {
	public static final String MOD_ID = "wolfsignal";
	public static int ticks = -1;
	public static ServerInfo lastServerEntry = null;
	public static boolean pause = false;
	public static int signal = -1;
	public static int disconnectTicks = -1;
	public static int signalIndex = -1;
	public static boolean disconnectedCausedByWolfSignal = false;

	@Override
	public void onInitialize() {
		ClientCommands.register(literal("signal").then(argument("signal", IntegerArgumentType.integer(0, 31)).executes(Main::cmdSignal)));
		ClientTickEvents.END_CLIENT_TICK.register(client -> disconnectTimeout());
	}

	private static int cmdSignal(CommandContext<ClientCommandSource> ctx) {
		signal = ctx.getArgument("signal", int.class);
		disconnectedCausedByWolfSignal = true;
		MinecraftClient.getInstance().world.disconnect();
		signalIndex = 4;
		setTick();
		return SINGLE_SUCCESS;
	}

	private static void disconnectTimeout() {
		if(disconnectTicks >= 0 && --disconnectTicks == 0) {
			MinecraftClient.getInstance().world.disconnect();
			setTick();
		}
	}

	private static void setTick() {
		if(signalIndex >= 0) {
			ticks = (signal & 1 << signalIndex--) != 0 ? 12 * 20 : 6 * 20;
		}
	}
}
