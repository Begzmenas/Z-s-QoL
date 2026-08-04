package com.zsqol;

import java.util.Locale;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.Text;

public enum TeleportTarget
{
	// Portal Nexus
	NEXUS_ANNAKARL(TeleportCategory.POH_NEXUS, "Annakarl", "Demonic Ruins"),
	NEXUS_APE_ATOLL_DUNGEON(TeleportCategory.POH_NEXUS, "Ape Atoll Dungeon"),
	NEXUS_ARCEUUS_LIBRARY(TeleportCategory.POH_NEXUS, "Arceuus Library"),
	NEXUS_BARBARIAN_OUTPOST(TeleportCategory.POH_NEXUS, "Barbarian Outpost"),
	NEXUS_BARROWS(TeleportCategory.POH_NEXUS, "Barrows"),
	NEXUS_BATTLEFRONT(TeleportCategory.POH_NEXUS, "Battlefront"),
	NEXUS_TELEPORT_TO_BOAT(TeleportCategory.POH_NEXUS, "Teleport to Boat", "Boat", "Mooring point", "Teleport to Last Boat"),
	NEXUS_CAMELOT(TeleportCategory.POH_NEXUS, "Camelot"),
	NEXUS_CARRALLANGAR(TeleportCategory.POH_NEXUS, "Carrallangar", "Carrallanger", "Graveyard of Shadows"),
	NEXUS_CATHERBY(TeleportCategory.POH_NEXUS, "Catherby"),
	NEXUS_CIVITAS_ILLA_FORTIS(TeleportCategory.POH_NEXUS, "Civitas illa Fortis", "Civitas"),
	NEXUS_DAREEYAK(TeleportCategory.POH_NEXUS, "Dareeyak"),
	NEXUS_DRAYNOR_MANOR(TeleportCategory.POH_NEXUS, "Draynor Manor"),
	NEXUS_EAST_ARDOUGNE(TeleportCategory.POH_NEXUS, "East Ardougne"),
	NEXUS_FALADOR(TeleportCategory.POH_NEXUS, "Falador"),
	NEXUS_FENKENSTRAINS_CASTLE(TeleportCategory.POH_NEXUS, "Fenkenstrain's Castle", "Fenkenstrains Castle", "Fenken' Castle", "Fenken Castle"),
	NEXUS_FISHING_GUILD(TeleportCategory.POH_NEXUS, "Fishing Guild"),
	NEXUS_GHORROCK(TeleportCategory.POH_NEXUS, "Ghorrock", "Frozen Waste Plateau"),
	NEXUS_GRAND_EXCHANGE(TeleportCategory.POH_NEXUS, "Grand Exchange"),
	NEXUS_HARMONY_ISLAND(TeleportCategory.POH_NEXUS, "Harmony Island"),
	NEXUS_ICE_PLATEAU(TeleportCategory.POH_NEXUS, "Ice Plateau"),
	NEXUS_KHARYRLL(TeleportCategory.POH_NEXUS, "Kharyrll", "Canifis"),
	NEXUS_KOUREND_CASTLE(TeleportCategory.POH_NEXUS, "Kourend Castle", "Kourend"),
	NEXUS_LASSAR(TeleportCategory.POH_NEXUS, "Lassar", "Ice Mountain"),
	NEXUS_LUMBRIDGE(TeleportCategory.POH_NEXUS, "Lumbridge"),
	NEXUS_LUNAR_ISLE(TeleportCategory.POH_NEXUS, "Lunar Isle", "Moonclan"),
	NEXUS_MARIM(TeleportCategory.POH_NEXUS, "Marim", "Ape Atoll"),
	NEXUS_MIND_ALTAR(TeleportCategory.POH_NEXUS, "Mind Altar"),
	NEXUS_OURANIA_CAVE(TeleportCategory.POH_NEXUS, "Ourania Cave", "Ourania"),
	NEXUS_PADDEWWA(TeleportCategory.POH_NEXUS, "Paddewwa", "Edgeville Dungeon"),
	NEXUS_PORT_KHAZARD(TeleportCategory.POH_NEXUS, "Port Khazard", "Khazard"),
	NEXUS_RESPAWN(TeleportCategory.POH_NEXUS, "Respawn", "Respawn Teleport"),
	NEXUS_SALVE_GRAVEYARD(TeleportCategory.POH_NEXUS, "Salve Graveyard"),
	NEXUS_SEERS_VILLAGE(TeleportCategory.POH_NEXUS, "Seers' Village", "Seers Village"),
	NEXUS_SENNTISTEN(TeleportCategory.POH_NEXUS, "Senntisten", "Digsite"),
	NEXUS_FORGOTTEN_CEMETERY(TeleportCategory.POH_NEXUS, "The Forgotten Cemetery", "Forgotten Cemetery", "Cemetery"),
	NEXUS_TROLLHEIM(TeleportCategory.POH_NEXUS, "Trollheim"),
	NEXUS_TROLL_STRONGHOLD(TeleportCategory.POH_NEXUS, "Troll Stronghold"),
	NEXUS_VARROCK(TeleportCategory.POH_NEXUS, "Varrock"),
	NEXUS_WATCHTOWER(TeleportCategory.POH_NEXUS, "Watchtower"),
	NEXUS_WATERBIRTH_ISLAND(TeleportCategory.POH_NEXUS, "Waterbirth Island", "Waterbirth"),
	NEXUS_WEISS(TeleportCategory.POH_NEXUS, "Weiss"),
	NEXUS_WEST_ARDOUGNE(TeleportCategory.POH_NEXUS, "West Ardougne"),
	NEXUS_YANILLE(TeleportCategory.POH_NEXUS, "Yanille"),

	// Jewellery Box: Ring of dueling
	JEWELLERY_EMIRS_ARENA(TeleportCategory.JEWELLERY_BOX, true, "Emir's Arena", "Emirs Arena", "PvP Arena", "Duel Arena"),
	JEWELLERY_CASTLE_WARS(TeleportCategory.JEWELLERY_BOX, true, "Castle Wars"),
	JEWELLERY_FEROX_ENCLAVE(TeleportCategory.JEWELLERY_BOX, true, "Ferox Enclave", "Ferox"),
	JEWELLERY_FORTIS_COLOSSEUM(TeleportCategory.JEWELLERY_BOX, true, "Fortis Colosseum", "Colosseum"),

	// Jewellery Box: Games necklace
	JEWELLERY_BURTHORPE(TeleportCategory.JEWELLERY_BOX, true, "Burthorpe"),
	JEWELLERY_BARBARIAN_OUTPOST(TeleportCategory.JEWELLERY_BOX, false, "Barbarian Outpost"),
	JEWELLERY_CORPOREAL_BEAST(TeleportCategory.JEWELLERY_BOX, true, "Corporeal Beast", "Corporeal Beast's Lair", "Corp"),
	JEWELLERY_TEARS_OF_GUTHIX(TeleportCategory.JEWELLERY_BOX, true, "Tears of Guthix"),
	JEWELLERY_WINTERTODT_CAMP(TeleportCategory.JEWELLERY_BOX, true, "Wintertodt Camp", "Wintertodt"),

	// Jewellery Box: Combat bracelet
	JEWELLERY_WARRIORS_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Warriors' Guild", "Warriors Guild"),
	JEWELLERY_CHAMPIONS_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Champions' Guild", "Champions Guild"),
	JEWELLERY_MONASTERY(TeleportCategory.JEWELLERY_BOX, true, "Monastery"),
	JEWELLERY_RANGING_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Ranging Guild"),

	// Jewellery Box: Skills necklace
	JEWELLERY_FISHING_GUILD(TeleportCategory.JEWELLERY_BOX, false, "Fishing Guild"),
	JEWELLERY_MINING_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Mining Guild"),
	JEWELLERY_CRAFTING_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Crafting Guild"),
	JEWELLERY_COOKS_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Cooks' Guild", "Cooks Guild", "Cooking Guild"),
	JEWELLERY_WOODCUTTING_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Woodcutting Guild"),
	JEWELLERY_FARMING_GUILD(TeleportCategory.JEWELLERY_BOX, true, "Farming Guild"),

	// Jewellery Box: Ring of wealth
	JEWELLERY_GRAND_EXCHANGE(TeleportCategory.JEWELLERY_BOX, false, "Grand Exchange"),
	JEWELLERY_FALADOR_PARK(TeleportCategory.JEWELLERY_BOX, true, "Falador Park"),
	JEWELLERY_MISCELLANIA(TeleportCategory.JEWELLERY_BOX, true, "Miscellania"),
	JEWELLERY_DONDAKANS_ROCK(TeleportCategory.JEWELLERY_BOX, true, "Dondakan's Rock", "Dondakans Rock"),

	// Jewellery Box: Amulet of glory
	JEWELLERY_EDGEVILLE(TeleportCategory.JEWELLERY_BOX, true, "Edgeville"),
	JEWELLERY_KARAMJA(TeleportCategory.JEWELLERY_BOX, true, "Karamja"),
	JEWELLERY_DRAYNOR_VILLAGE(TeleportCategory.JEWELLERY_BOX, true, "Draynor Village"),
	JEWELLERY_AL_KHARID(TeleportCategory.JEWELLERY_BOX, true, "Al Kharid", "Al-Kharid"),

	// Quetzal Transport System
	QUETZAL_ALDARIN(TeleportCategory.QUETZAL, "Aldarin"),
	QUETZAL_AUBURNVALE(TeleportCategory.QUETZAL, "Auburnvale", "Auburn Vale", "Auburn Valley"),
	QUETZAL_CIVITAS_ILLA_FORTIS(TeleportCategory.QUETZAL, "Civitas illa Fortis", "Civitas"),
	QUETZAL_HUNTER_GUILD(TeleportCategory.QUETZAL, "Hunter Guild", "Hunters' Guild", "Hunters Guild"),
	QUETZAL_QUETZACALLI_GORGE(TeleportCategory.QUETZAL, "Quetzacalli Gorge"),
	QUETZAL_SUNSET_COAST(TeleportCategory.QUETZAL, "Sunset Coast"),
	QUETZAL_TAL_TEKLAN(TeleportCategory.QUETZAL, "Tal Teklan"),
	QUETZAL_THE_TEOMAT(TeleportCategory.QUETZAL, "The Teomat", "Teomat"),
	QUETZAL_CAM_TORUM_ENTRANCE(TeleportCategory.QUETZAL, "Cam Torum entrance", "Cam Torum Entrance", "Cam Torum"),
	QUETZAL_COLOSSAL_WYRM_REMAINS(TeleportCategory.QUETZAL, "Colossal Wyrm Remains", "Colossal Wyrm"),
	QUETZAL_FORTIS_COLOSSEUM(TeleportCategory.QUETZAL, "Fortis Colosseum", "Colosseum"),
	QUETZAL_KASTORI(TeleportCategory.QUETZAL, "Kastori"),
	QUETZAL_OUTER_FORTIS(TeleportCategory.QUETZAL, "Outer Fortis"),
	QUETZAL_SALVAGER_OVERLOOK(TeleportCategory.QUETZAL, "Salvager Overlook"),

	// Fossil Island Mycelium Transportation System
	MUSHTREE_HOUSE_ON_THE_HILL(TeleportCategory.FOSSIL_MUSHTREE, "House on the Hill", "House on Hill"),
	MUSHTREE_VERDANT_VALLEY(TeleportCategory.FOSSIL_MUSHTREE, "Verdant Valley"),
	MUSHTREE_STICKY_SWAMP(TeleportCategory.FOSSIL_MUSHTREE, "Sticky Swamp", "Tar Swamp", "Sticky Swamp (Tar Swamp)"),
	MUSHTREE_MUSHROOM_MEADOW(TeleportCategory.FOSSIL_MUSHTREE, "Mushroom Meadow", "Mushroom Meadow Mushtree"),

	// Spirit Tree network
	SPIRIT_TREE_GNOME_VILLAGE(TeleportCategory.SPIRIT_TREE, "Tree Gnome Village", "Gnome Village"),
	SPIRIT_TREE_GNOME_STRONGHOLD(TeleportCategory.SPIRIT_TREE, "Gnome Stronghold", "Tree Gnome Stronghold"),
	SPIRIT_TREE_BATTLEFIELD_OF_KHAZARD(TeleportCategory.SPIRIT_TREE, "Battlefield of Khazard", "Khazard Battlefield"),
	SPIRIT_TREE_GRAND_EXCHANGE(TeleportCategory.SPIRIT_TREE, "Grand Exchange"),
	SPIRIT_TREE_FELDIP_HILLS(TeleportCategory.SPIRIT_TREE, "Feldip Hills", "Myths' Guild", "Myths Guild"),
	SPIRIT_TREE_POISON_WASTE(TeleportCategory.SPIRIT_TREE, "Poison Waste"),
	SPIRIT_TREE_PRIFDDINAS(TeleportCategory.SPIRIT_TREE, "Prifddinas"),
	SPIRIT_TREE_LAGUNA_AURORAE(TeleportCategory.SPIRIT_TREE, "Laguna Aurorae"),
	SPIRIT_TREE_ETCETERIA(TeleportCategory.SPIRIT_TREE, "Etceteria"),
	SPIRIT_TREE_PORT_SARIM(TeleportCategory.SPIRIT_TREE, "Port Sarim"),
	SPIRIT_TREE_BRIMHAVEN(TeleportCategory.SPIRIT_TREE, "Brimhaven"),
	SPIRIT_TREE_HOSIDIUS(TeleportCategory.SPIRIT_TREE, "Hosidius"),
	SPIRIT_TREE_FARMING_GUILD(TeleportCategory.SPIRIT_TREE, "Farming Guild"),
	SPIRIT_TREE_YOUR_HOUSE(TeleportCategory.SPIRIT_TREE, "Your house", "Your House", "Player-owned house", "Player owned house"),

	// Fairy Rings: A codes
	FAIRY_RING_AIQ(TeleportCategory.FAIRY_RING, "Mudskipper Point", "AIQ"),
	FAIRY_RING_AIR(TeleportCategory.FAIRY_RING, "(Island) South-east of Ardougne", "AIR"),
	FAIRY_RING_AIS(TeleportCategory.FAIRY_RING, "Auburn Valley", "AIS"),
	FAIRY_RING_AJP(TeleportCategory.FAIRY_RING, "Avium Savannah", "AJP"),
	FAIRY_RING_AJQ(TeleportCategory.FAIRY_RING, "Cave south of Dorgesh-Kaan", "AJQ"),
	FAIRY_RING_AJR(TeleportCategory.FAIRY_RING, "Slayer cave", "AJR", "Fremennik Slayer Dungeon"),
	FAIRY_RING_AJS(TeleportCategory.FAIRY_RING, "Penguins near Miscellania", "AJS"),
	FAIRY_RING_AKP(TeleportCategory.FAIRY_RING, "Necropolis", "AKP"),
	FAIRY_RING_AKQ(TeleportCategory.FAIRY_RING, "Piscatoris Hunter area", "AKQ"),
	FAIRY_RING_AKR(TeleportCategory.FAIRY_RING, "Hosidius Vinery", "AKR"),
	FAIRY_RING_AKS(TeleportCategory.FAIRY_RING, "Feldip Hunter area", "AKS"),
	FAIRY_RING_ALP(TeleportCategory.FAIRY_RING, "(Island) Lighthouse", "ALP"),
	FAIRY_RING_ALQ(TeleportCategory.FAIRY_RING, "Haunted Woods east of Canifis", "ALQ"),
	FAIRY_RING_ALR(TeleportCategory.FAIRY_RING, "Abyssal Area", "ALR"),
	FAIRY_RING_ALS(TeleportCategory.FAIRY_RING, "McGrubor's Wood", "ALS"),

	// Fairy Rings: B codes
	FAIRY_RING_BIP(TeleportCategory.FAIRY_RING, "(Island) South-west of Mort Myre", "BIP"),
	FAIRY_RING_BIQ(TeleportCategory.FAIRY_RING, "Kalphite Hive", "BIQ"),
	FAIRY_RING_BIS(TeleportCategory.FAIRY_RING, "Ardougne Zoo - Unicorns", "BIS"),
	FAIRY_RING_BJP(TeleportCategory.FAIRY_RING, "(Island) Isle of Souls", "BJP"),
	FAIRY_RING_BJR(TeleportCategory.FAIRY_RING, "Realm of the Fisher King", "BJR"),
	FAIRY_RING_BJS(TeleportCategory.FAIRY_RING, "(Island) Near Zul-Andra", "BJS", "Zulrah"),
	FAIRY_RING_BKP(TeleportCategory.FAIRY_RING, "South of Castle Wars", "BKP"),
	FAIRY_RING_BKQ(TeleportCategory.FAIRY_RING, "Enchanted Valley", "BKQ"),
	FAIRY_RING_BKR(TeleportCategory.FAIRY_RING, "Mort Myre Swamp, south of Canifis", "BKR"),
	FAIRY_RING_BKS(TeleportCategory.FAIRY_RING, "Zanaris", "BKS"),
	FAIRY_RING_BLP(TeleportCategory.FAIRY_RING, "TzHaar area", "BLP"),
	FAIRY_RING_BLQ(TeleportCategory.FAIRY_RING, "Yu'biusk", "BLQ"),
	FAIRY_RING_BLR(TeleportCategory.FAIRY_RING, "Legends' Guild", "BLR", "Legends Guild"),
	FAIRY_RING_BLS(TeleportCategory.FAIRY_RING, "South of Mount Quidamortem", "BLS", "Vardorvis", "Stranglewood", "Chambers of Xeric"),

	// Fairy Rings: C codes
	FAIRY_RING_CIP(TeleportCategory.FAIRY_RING, "(Island) Miscellania", "CIP"),
	FAIRY_RING_CIQ(TeleportCategory.FAIRY_RING, "North-west of Yanille", "CIQ"),
	FAIRY_RING_CIR(TeleportCategory.FAIRY_RING, "North-east of the Farming Guild", "CIR", "Mount Karuulm", "Konar"),
	FAIRY_RING_CIS(TeleportCategory.FAIRY_RING, "North of the Arceuus Library", "CIS"),
	FAIRY_RING_CJQ(TeleportCategory.FAIRY_RING, "The Great Conch", "CJQ"),
	FAIRY_RING_CJR(TeleportCategory.FAIRY_RING, "Sinclair Mansion", "CJR", "Falo", "Bard"),
	FAIRY_RING_CKP(TeleportCategory.FAIRY_RING, "Cosmic entity's plane", "CKP"),
	FAIRY_RING_CKQ(TeleportCategory.FAIRY_RING, "Aldarin", "CKQ"),
	FAIRY_RING_CKR(TeleportCategory.FAIRY_RING, "South of Tai Bwo Wannai Village", "CKR"),
	FAIRY_RING_CKS(TeleportCategory.FAIRY_RING, "Canifis", "CKS"),
	FAIRY_RING_CLP(TeleportCategory.FAIRY_RING, "(Island) South of Draynor Village", "CLP"),
	FAIRY_RING_CLR(TeleportCategory.FAIRY_RING, "(Island) Ape Atoll", "CLR"),
	FAIRY_RING_CLS(TeleportCategory.FAIRY_RING, "(Island) Hazelmere's home", "CLS"),

	// Fairy Rings: D codes
	FAIRY_RING_DIP(TeleportCategory.FAIRY_RING, "(Sire Boss) Abyssal Nexus", "DIP", "Abyssal Sire"),
	FAIRY_RING_DIQ(TeleportCategory.FAIRY_RING, "Player-owned house", "DIQ", "POH", "Home"),
	FAIRY_RING_DIR(TeleportCategory.FAIRY_RING, "Gorak's Plane", "DIR"),
	FAIRY_RING_DIS(TeleportCategory.FAIRY_RING, "Wizards' Tower", "DIS", "Wizards Tower"),
	FAIRY_RING_DJP(TeleportCategory.FAIRY_RING, "Tower of Life", "DJP"),
	FAIRY_RING_DJR(TeleportCategory.FAIRY_RING, "Chasm of Fire", "DJR"),
	FAIRY_RING_DKP(TeleportCategory.FAIRY_RING, "South of Musa Point", "DKP"),
	FAIRY_RING_DKR(TeleportCategory.FAIRY_RING, "Edgeville, Grand Exchange", "DKR", "Grand Exchange", "Edgeville"),
	FAIRY_RING_DKS(TeleportCategory.FAIRY_RING, "Polar Hunter area", "DKS"),
	FAIRY_RING_DLP(TeleportCategory.FAIRY_RING, "Grimstone Dungeon", "DLP"),
	FAIRY_RING_DLQ(TeleportCategory.FAIRY_RING, "North of Nardah", "DLQ"),
	FAIRY_RING_DLR(TeleportCategory.FAIRY_RING, "(Island) Poison Waste south of Isafdar", "DLR"),
	FAIRY_RING_DLS(TeleportCategory.FAIRY_RING, "Myreque hideout under The Hollows", "DLS");

	private final TeleportCategory category;
	private final boolean jewelleryAnchor;
	private final String displayName;
	private final String[] matchingNames;

	TeleportTarget(TeleportCategory category, String displayName, String... aliases)
	{
		this(category, false, displayName, aliases);
	}

	TeleportTarget(TeleportCategory category, boolean jewelleryAnchor, String displayName, String... aliases)
	{
		this.category = category;
		this.jewelleryAnchor = jewelleryAnchor;
		this.displayName = displayName;
		this.matchingNames = new String[aliases.length + 1];
		this.matchingNames[0] = displayName;
		System.arraycopy(aliases, 0, this.matchingNames, 1, aliases.length);
	}

	public TeleportCategory getCategory()
	{
		return category;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getFairyRingCode()
	{
		if (category != TeleportCategory.FAIRY_RING)
		{
			return "";
		}

		String prefix = "FAIRY_RING_";
		String enumName = name();

		if (!enumName.startsWith(prefix))
		{
			return "";
		}

		return enumName.substring(prefix.length());
	}

	public String getSearchText()
	{
		StringBuilder result =
			new StringBuilder(displayName);

		String fairyRingCode =
			getFairyRingCode();

		if (!fairyRingCode.isEmpty())
		{
			result
				.append(' ')
				.append(fairyRingCode);
		}

		for (String matchingName : matchingNames)
		{
			result
				.append(' ')
				.append(matchingName);
		}

		return result.toString();
	}

	public boolean isJewelleryAnchor()
	{
		return jewelleryAnchor;
	}

	public boolean matches(String widgetValue)
	{
		String normalizedWidgetValue = normalize(widgetValue);
		if (normalizedWidgetValue.isEmpty())
		{
			return false;
		}

		for (String matchingName : matchingNames)
		{
			String normalizedMatchingName = normalize(matchingName);
			if (normalizedWidgetValue.equals(normalizedMatchingName))
			{
				return true;
			}

			if (normalizedMatchingName.length() >= 5
				&& normalizedWidgetValue.contains(normalizedMatchingName))
			{
				return true;
			}
		}

		return false;
	}


	public String getStableConfigKey()
	{
		return name().toLowerCase(Locale.ROOT);
	}

	public int getItemIconId()
	{
		if (category != TeleportCategory.JEWELLERY_BOX)
		{
			return category.getDefaultItemIconId();
		}

		return getJewelleryItemIconId();
	}

	public int getJewelleryItemIconId()
	{
		switch (this)
		{
			case JEWELLERY_EMIRS_ARENA:
			case JEWELLERY_CASTLE_WARS:
			case JEWELLERY_FEROX_ENCLAVE:
			case JEWELLERY_FORTIS_COLOSSEUM:
				return ItemID.RING_OF_DUELING_8;

			case JEWELLERY_BURTHORPE:
			case JEWELLERY_BARBARIAN_OUTPOST:
			case JEWELLERY_CORPOREAL_BEAST:
			case JEWELLERY_TEARS_OF_GUTHIX:
			case JEWELLERY_WINTERTODT_CAMP:
				return ItemID.NECKLACE_OF_MINIGAMES_8;

			case JEWELLERY_WARRIORS_GUILD:
			case JEWELLERY_CHAMPIONS_GUILD:
			case JEWELLERY_MONASTERY:
			case JEWELLERY_RANGING_GUILD:
				return ItemID.JEWL_BRACELET_OF_COMBAT_4;

			case JEWELLERY_FISHING_GUILD:
			case JEWELLERY_MINING_GUILD:
			case JEWELLERY_CRAFTING_GUILD:
			case JEWELLERY_COOKS_GUILD:
			case JEWELLERY_WOODCUTTING_GUILD:
			case JEWELLERY_FARMING_GUILD:
				return ItemID.JEWL_NECKLACE_OF_SKILLS_4;

			case JEWELLERY_GRAND_EXCHANGE:
			case JEWELLERY_FALADOR_PARK:
			case JEWELLERY_MISCELLANIA:
			case JEWELLERY_DONDAKANS_ROCK:
				return ItemID.RING_OF_WEALTH_5;

			case JEWELLERY_EDGEVILLE:
			case JEWELLERY_KARAMJA:
			case JEWELLERY_DRAYNOR_VILLAGE:
			case JEWELLERY_AL_KHARID:
				return ItemID.AMULET_OF_GLORY_4;

			default:
				return -1;
		}
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
