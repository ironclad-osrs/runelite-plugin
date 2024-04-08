package com.ironclad.clangoals;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "ironclad-clan-goals"
)
public class IroncladClanGoalsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private IroncladClanGoalsConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("ironclad-clan-goals started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("ironclad-clan-goals stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "ironclad-clan-goals says " + config.greeting(), null);
		}
	}

	@Provides
	IroncladClanGoalsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IroncladClanGoalsConfig.class);
	}
}
