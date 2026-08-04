package com.zsqol;

import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FairyRingTravelLogTest
{
	@Test
	public void matchesExactCodeAndDestinationWithTags()
	{
		assertTrue(FairyRingTravelLog.matchesEntry(
			"AIQ",
			"Mudskipper Point",
			"<br>Asgarnia: Mudskipper Point",
			"<col=ff9040>A I Q</col>",
			new String[]{"Use code", "Add Favourite"}
		));
	}

	@Test
	public void matchesCjqOnlyWithTheGreatConch()
	{
		assertTrue(FairyRingTravelLog.matchesEntry(
			"CJQ",
			"The Great Conch",
			"<br>Islands: The Great Conch",
			"<col=ff9040>C J Q</col>",
			new String[]{"Use code"}
		));
		assertFalse(FairyRingTravelLog.matchesEntry(
			"CJQ",
			"The Great Conch",
			"Islands: Great Conch",
			"C J Q",
			new String[]{"Use code"}
		));
	}

	@Test
	public void rejectsSimilarDestinationAndWrongCode()
	{
		assertFalse(FairyRingTravelLog.matchesEntry(
			"AIQ",
			"Mudskipper Point",
			"Asgarnia: Mudskipper Cave",
			"A I Q",
			new String[]{"Use code"}
		));
		assertFalse(FairyRingTravelLog.matchesEntry(
			"AIQ",
			"Mudskipper Point",
			"Asgarnia: Mudskipper Point",
			"C J Q",
			new String[]{"Use code"}
		));
	}

	@Test
	public void rejectsInvalidDestination()
	{
		assertFalse(FairyRingTravelLog.matchesEntry(
			"XYZ",
			"",
			"Unknown",
			"X Y Z",
			new String[]{"Use code"}
		));
	}

	@Test
	public void rejectsEntryWithoutExistingUseCodeAction()
	{
		assertFalse(FairyRingTravelLog.matchesEntry(
			"CJQ",
			"The Great Conch",
			"Islands: The Great Conch",
			"C J Q",
			new String[]{"Examine"}
		));
		assertFalse(FairyRingTravelLog.matchesEntry(
			"CJQ",
			"The Great Conch",
			"Islands: The Great Conch",
			"C J Q",
			null
		));
	}

	@Test
	public void acceptsOffscreenRowsButRejectsHiddenOrMissingRows()
	{
		assertFalse(FairyRingTravelLog.isActionable(
			true,
			new Rectangle(110, 110, 100, 20)
		));
		assertTrue(FairyRingTravelLog.isActionable(
			false,
			new Rectangle(110, -300, 100, 20)
		));
		assertFalse(FairyRingTravelLog.isActionable(false, null));
	}

	@Test
	public void suppressesClosedFocusedAndRepeatedHotkeys()
	{
		assertTrue(
			FairyRingTravelLog.shouldHandleHotkey(true, true, false, false)
		);
		assertFalse(
			FairyRingTravelLog.shouldHandleHotkey(false, true, false, false)
		);
		assertFalse(
			FairyRingTravelLog.shouldHandleHotkey(true, false, false, false)
		);
		assertFalse(
			FairyRingTravelLog.shouldHandleHotkey(true, true, true, false)
		);
		assertFalse(
			FairyRingTravelLog.shouldHandleHotkey(true, true, false, true)
		);
	}

	@Test
	public void firstDeliberatePressRunsExactlyOneExistingOperation()
	{
		AtomicInteger operations = new AtomicInteger();
		boolean activated = FairyRingTravelLog.activateOnce(
			true,
			operations::incrementAndGet
		);

		assertTrue(activated);
		assertEquals(1, operations.get());
	}

	@Test
	public void missingWidgetNeverRunsOperation()
	{
		AtomicInteger operations = new AtomicInteger();
		boolean activated = FairyRingTravelLog.activateOnce(
			false,
			operations::incrementAndGet
		);

		assertFalse(activated);
		assertEquals(0, operations.get());
	}

	@Test
	public void missingWidgetOrSearchFilteredEntryNeverRunsOperation()
	{
		AtomicInteger operations = new AtomicInteger();
		boolean activated = FairyRingTravelLog.activateOnce(
			false,
			operations::incrementAndGet
		);

		assertFalse(activated);
		assertEquals(0, operations.get());
	}

	@Test
	public void bindingMatchesOnlyItsPressedKeyEvent()
	{
		TeleportBinding binding = TeleportBinding.keyboard(KeyEvent.VK_F6, 0);
		Canvas source = new Canvas();
		KeyEvent pressed = new KeyEvent(
			source,
			KeyEvent.KEY_PRESSED,
			1L,
			0,
			KeyEvent.VK_F6,
			KeyEvent.CHAR_UNDEFINED
		);
		KeyEvent released = new KeyEvent(
			source,
			KeyEvent.KEY_RELEASED,
			2L,
			0,
			KeyEvent.VK_F6,
			KeyEvent.CHAR_UNDEFINED
		);
		KeyEvent unrelated = new KeyEvent(
			source,
			KeyEvent.KEY_PRESSED,
			3L,
			0,
			KeyEvent.VK_F7,
			KeyEvent.CHAR_UNDEFINED
		);

		assertTrue(binding.matches(pressed));
		assertFalse(binding.matches(released));
		assertFalse(binding.matches(unrelated));
	}
}
