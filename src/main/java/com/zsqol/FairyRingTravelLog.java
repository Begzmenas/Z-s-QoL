package com.zsqol;

import java.awt.Rectangle;
import java.util.Locale;
import net.runelite.client.util.Text;

final class FairyRingTravelLog
{
	private FairyRingTravelLog()
	{
	}

	static boolean matchesEntry(
		String expectedCode,
		String expectedDestination,
		String widgetText,
		String widgetName,
		String[] actions
	)
	{
		String code = normalize(expectedCode).replace(" ", "");
		String destination = normalize(expectedDestination);
		if (code.length() != 3
			|| destination.isEmpty()
			|| getUseCodeActionIdentifier(actions) < 1)
		{
			return false;
		}

		String normalizedName = normalize(widgetName);
		String normalizedText = normalize(widgetText);
		String spacedCode = code.charAt(0)
			+ " " + code.charAt(1)
			+ " " + code.charAt(2);
		boolean codeMatches = normalizedName.equals(code)
			|| normalizedName.equals(spacedCode)
			|| startsWithIdentity(normalizedText, code)
			|| startsWithIdentity(normalizedText, spacedCode);
		boolean destinationMatches = containsIdentity(normalizedText, destination)
			|| containsIdentity(normalizedName, destination);
		return codeMatches && destinationMatches;
	}

	static boolean isActionable(
		boolean hidden,
		Rectangle entryBounds
	)
	{
		return !hidden
			&& entryBounds != null
			&& entryBounds.width > 0
			&& entryBounds.height > 0;
	}

	static boolean shouldHandleHotkey(
		boolean loggedIn,
		boolean interfaceOpen,
		boolean inputFocused,
		boolean keyAlreadyBlocked
	)
	{
		return loggedIn
			&& interfaceOpen
			&& !inputFocused
			&& !keyAlreadyBlocked;
	}

	static boolean activateOnce(
		boolean entryAvailable,
		Runnable existingWidgetOperation
	)
	{
		if (!entryAvailable || existingWidgetOperation == null)
		{
			return false;
		}

		/*
		 * This invokes at most one existing Travel Log widget listener.
		 * It never rotates individual dials or invokes Teleport.
		 */
		existingWidgetOperation.run();
		return true;
	}

	static String normalize(String value)
	{
		if (value == null)
		{
			return "";
		}

		return Text.removeTags(value)
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", " ")
			.trim()
			.replaceAll("\\s+", " ");
	}

	static int getUseCodeActionIdentifier(String[] actions)
	{
		if (actions == null)
		{
			return -1;
		}

		for (int index = 0; index < actions.length; index++)
		{
			if ("use code".equals(normalize(actions[index])))
			{
				return index + 1;
			}
		}

		return -1;
	}

	private static boolean startsWithIdentity(String text, String identity)
	{
		return text.equals(identity) || text.startsWith(identity + " ");
	}

	private static boolean containsIdentity(String text, String identity)
	{
		return text.equals(identity)
			|| text.startsWith(identity + " ")
			|| text.endsWith(" " + identity)
			|| text.contains(" " + identity + " ");
	}
}
