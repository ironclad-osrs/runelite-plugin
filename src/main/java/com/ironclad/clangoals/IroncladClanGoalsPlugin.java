package com.ironclad.clangoals;

import com.google.inject.Provides;
import com.ironclad.clangoals.batches.KillBatchQueue;
import com.ironclad.clangoals.batches.LootBatchQueue;
import com.ironclad.clangoals.batches.QueueItem;
import com.ironclad.clangoals.batches.XpBatchQueue;
import com.ironclad.clangoals.service.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import okhttp3.OkHttpClient;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "IronClad Clan Goals"
)
public class IroncladClanGoalsPlugin extends Plugin
{
	@Inject
	private OkHttpClient httpClient;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private IroncladClanGoalsConfig config;

	public ApiService service;
	protected XpBatchQueue xpBatchQueue;
	protected KillBatchQueue killBatchQueue;
	protected LootBatchQueue lootBatchQueue;

	private boolean getLocalPlayer = false;

	private final HashMap<String, Number> xpMap = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		// If API key is set then attempt to validated.
		if (!config.apiKey().isEmpty()) {
			service = new ApiService(httpClient, config.apiKey());

			xpBatchQueue = new XpBatchQueue();
			xpBatchQueue.setApi(service);

			killBatchQueue = new KillBatchQueue();
			killBatchQueue.setApi(service);

			lootBatchQueue = new LootBatchQueue();
			lootBatchQueue.setApi(service);
			lootBatchQueue.setItemManager(itemManager);
		}
	}

    @Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		final GameState state = gameStateChanged.getGameState();

		switch(state)
		{
			case LOGIN_SCREEN:
			case HOPPING:
			case LOGGING_IN:
			case LOGIN_SCREEN_AUTHENTICATOR:
				xpMap.clear();
				xpBatchQueue.flush();
				lootBatchQueue.flush();
				killBatchQueue.flush();
			case CONNECTION_LOST:
				xpBatchQueue.flush();
				lootBatchQueue.flush();
				killBatchQueue.flush();
		}

		if (state == GameState.LOGGED_IN)
		{
			getLocalPlayer = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (getLocalPlayer) {
			Player player = client.getLocalPlayer();

			// Get the player name and account hash on
			// first game tick. Persist this against the
			// API key as we want to know who helped
			// contribute to the clan goal.
			service.updatePlayer(client.getAccountHash(), player);

			// Keep batch queues up to date with the
			// active account hash.
			xpBatchQueue.setAccount(client.getAccountHash());
			lootBatchQueue.setAccount(client.getAccountHash());
			killBatchQueue.setAccount(client.getAccountHash());

			getLocalPlayer = false;
		}

		xpBatchQueue.incrementCooldown();
		lootBatchQueue.incrementCooldown();
		killBatchQueue.incrementCooldown();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		final String skill = event.getSkill().getName();
		final int xp = event.getXp();

		if (!ClanUtils.isMemberOfClan(client)) {
			log.warn("Attempted to log xp when not a clan member.");
		} else {

			// We don't want to log skills straightaway
			// as we get flooded with current xp on login.
			if (xpMap.containsKey(skill)) {

				// Exit early if XP hasn't actually changed.
				if (xpMap.get(skill).equals(xp)) {
					return;
				}

				// If the API key is valid, and auto-join is enabled
				// then we begin to log xp after initial xp change.
				if (service.verified && config.autoJoin()) {
					xpBatchQueue.addItem(new QueueItem(event));
				}
			}
		}

		xpMap.put(skill, xp);
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived npcLootReceived)
	{
		if (!ClanUtils.isMemberOfClan(client)) {
			log.warn("Attempted to log npc kill when not a clan member.");
			return;
		}

		if (service.verified && config.autoJoin()) {

			// Check that at least one item was dropped from the NPC.
			if (!npcLootReceived.getItems().isEmpty()) {

				// Check that player is not within a restricted region.
				if (
						!WorldUtils.isPlayerWithinMapRegion(client, WorldUtils.LAST_MAN_STANDING_REGIONS) &&
						!WorldUtils.isPlayerWithinMapRegion(client, WorldUtils.SOUL_WARS_REGIONS)
				) {
					lootBatchQueue.addItem(new QueueItem(npcLootReceived));
				}
			}

			// Always track NPC kills.
			killBatchQueue.addItem(new QueueItem(new PluginNPC(npcLootReceived.getNpc())));
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!Objects.equals(event.getGroup(), "ironcladclangoals")) {
			return;
		}

		String key = event.getKey();
		String newValue = event.getNewValue();

		// If the API key changes then attempt
		// to authenticate with the API again.
		if (Objects.equals(key, "apiKey") && !newValue.isEmpty()) {
			service = new ApiService(httpClient, newValue);
			xpBatchQueue.setApi(service);
			killBatchQueue.setApi(service);
			lootBatchQueue.setApi(service);
		}
	}

	@Provides
	IroncladClanGoalsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IroncladClanGoalsConfig.class);
	}

}
