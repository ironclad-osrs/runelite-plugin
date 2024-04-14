package com.ironclad.clangoals;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ironcladclangoals")
public interface IroncladClanGoalsConfig extends Config
{
	@ConfigItem(
			keyName = "apiKey",
			name = "API Key",
			description = "The API key used to authenticate with IronClad's API",
			position = 1
	)
	default String apiKey()
	{
		return "";
	}

	@ConfigItem(
			keyName = "autoJoin",
			name = "Auto Join",
			description = "Automatically join any in-progress goals",
			position = 2
	)
	default boolean autoJoin()
	{
		return true;
	}
}
