package com.zsqol;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ZsQoLConfig.GROUP)
public interface ZsQoLConfig extends Config
{
	String GROUP = "zsqol";

	@ConfigItem(
			keyName = "teleportBindingsEnabled",
			name = "Enable teleport bindings",
			description =
					"Enable transport bindings and Fairy Ring Travel Log selection",
			position = 0
	)
	default boolean teleportBindingsEnabled()
	{
		return true;
	}
}
