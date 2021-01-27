package de.mrgeorgen.wolfSignal.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.mrgeorgen.wolfSignal.Main.*;
import static de.mrgeorgen.wolfSignal.Util.log;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient
{
	@Shadow
	public Screen currentScreen;

	@Inject(at = @At("HEAD"), method = "setCurrentServerEntry")
	private void setCurrentServerEntry(ServerInfo serverInfo, CallbackInfo info)
	{
		//save last known non-null server entry
		if (serverInfo != null)
		{
			lastServerEntry = serverInfo;
		}
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void tick(CallbackInfo info)
	{
		// if not paused, decrements countdown until its negative, succeeds if its 0
		if (ticks >= 0 && --ticks == 0)
		{
			if (lastServerEntry != null)
			{
				MinecraftClient mc = MinecraftClient.getInstance();
				mc.openScreen(new ConnectScreen(new MultiplayerScreen(new TitleScreen()), mc, lastServerEntry));
				if(disconnectedCausedByWolfSignal = signalIndex >= 0) {
					disconnectTicks = 2 * 20;
				}
			}
		}
	}
}
