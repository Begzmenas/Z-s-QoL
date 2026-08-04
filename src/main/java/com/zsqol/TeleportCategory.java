package com.zsqol;

import java.util.Locale;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.Text;

public enum TeleportCategory
{
	POH_NEXUS(
		"POH Teleporter",
		"✦",
		"Search POH teleports...",
		-1
	),
	JEWELLERY_BOX(
		"Jewellery Box",
		"◇",
		"Search Jewellery Box teleports...",
		-1
	),
	QUETZAL(
		"Quetzal Transport",
		"Q",
		"Search Quetzal landing sites...",
		ItemID.HG_QUETZALWHISTLE_BASIC,
		"quetzal",
		"quetzal transport system",
		"quetzal transportation system",
		"quetzal destinations",
		"quetzal landing sites",
		"landing sites"
	),
	FOSSIL_MUSHTREE(
		"Fossil Island Mushtrees",
		"M",
		"Search Mushtree destinations...",
		ItemID.NECKLACE_OF_DIGSITE_5,
		"mycelium transportation system",
		"magic mushtree",
		"mushtree",
		"mushroom",
		"fossil island",
		"fossil island transportation"
	),
	SPIRIT_TREE(
		"Spirit Trees",
		"T",
		"Search Spirit Tree destinations...",
		ItemID.POH_SUPERIOR_GARDEN_TELEPORT_TREE,
		"spirit tree locations",
		"spirit tree teleports",
		"spirit trees",
		"spirit tree"
	),
	FAIRY_RING(
		"Fairy Rings",
		"F",
		"Search Fairy Ring code or destination...",
		-1
	);

	private final String displayName;
	private final String iconText;
	private final String searchHint;
	private final int defaultItemIconId;
	private final String[] menuTitleHints;

	TeleportCategory(
		String displayName,
		String iconText,
		String searchHint,
		int defaultItemIconId,
		String... menuTitleHints
	)
	{
		this.displayName = displayName;
		this.iconText = iconText;
		this.searchHint = searchHint;
		this.defaultItemIconId = defaultItemIconId;
		this.menuTitleHints = menuTitleHints;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getIconText()
	{
		return iconText;
	}

	public String getSearchHint()
	{
		return searchHint;
	}

	public int getDefaultItemIconId()
	{
		return defaultItemIconId;
	}

	public boolean isGenericMenuCategory()
	{
		return this == QUETZAL
			|| this == FOSSIL_MUSHTREE
			|| this == SPIRIT_TREE;
	}

	public boolean matchesMenuTitle(String title)
	{
		if (!isGenericMenuCategory())
		{
			return false;
		}

		String normalizedTitle = normalize(title);
		if (normalizedTitle.isEmpty())
		{
			return false;
		}

		for (String hint : menuTitleHints)
		{
			String normalizedHint = normalize(hint);
			if (normalizedTitle.equals(normalizedHint)
				|| normalizedTitle.contains(normalizedHint))
			{
				return true;
			}
		}

		return false;
	}

	private static String normalize(String value)
	{
		if (value == null)
		{
			return "";
		}

		return Text.removeTags(value)
			.replace('\u00A0', ' ')
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}
}
