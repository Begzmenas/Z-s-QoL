package com.zsqol;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public final class TeleportBinding
{
	private static final int SUPPORTED_MODIFIERS =
		InputEvent.CTRL_DOWN_MASK
			| InputEvent.SHIFT_DOWN_MASK
			| InputEvent.ALT_DOWN_MASK
			| InputEvent.META_DOWN_MASK
			| InputEvent.ALT_GRAPH_DOWN_MASK;

	public enum Type
	{
		NONE,
		KEYBOARD,
		MOUSE
	}

	private static final TeleportBinding NONE =
		new TeleportBinding(Type.NONE, KeyEvent.VK_UNDEFINED, 0, 0);

	private final Type type;
	private final int keyCode;
	private final int modifiers;
	private final int mouseButton;

	private TeleportBinding(Type type, int keyCode, int modifiers, int mouseButton)
	{
		this.type = type;
		this.keyCode = keyCode;
		this.modifiers = normalizeModifiers(modifiers);
		this.mouseButton = mouseButton;
	}

	public static TeleportBinding none()
	{
		return NONE;
	}

	public static TeleportBinding keyboard(int keyCode, int modifiers)
	{
		if (keyCode == KeyEvent.VK_UNDEFINED)
		{
			return NONE;
		}

		return new TeleportBinding(Type.KEYBOARD, keyCode, modifiers, 0);
	}

	public static TeleportBinding mouse(int mouseButton)
	{
		if (mouseButton < 4)
		{
			return NONE;
		}

		return new TeleportBinding(Type.MOUSE, KeyEvent.VK_UNDEFINED, 0, mouseButton);
	}

	public static TeleportBinding parse(String value)
	{
		if (value == null || value.trim().isEmpty())
		{
			return NONE;
		}

		try
		{
			String[] parts = value.split("\\|");
			if (parts.length == 3 && "K".equals(parts[0]))
			{
				return keyboard(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
			}

			if (parts.length == 2 && "M".equals(parts[0]))
			{
				return mouse(Integer.parseInt(parts[1]));
			}
		}
		catch (NumberFormatException ignored)
		{
			// Invalid saved data becomes Not set.
		}

		return NONE;
	}

	public String serialize()
	{
		switch (type)
		{
			case KEYBOARD:
				return "K|" + keyCode + "|" + modifiers;
			case MOUSE:
				return "M|" + mouseButton;
			default:
				return "";
		}
	}

	public boolean matches(KeyEvent event)
	{
		return type == Type.KEYBOARD
			&& event.getID() == KeyEvent.KEY_PRESSED
			&& event.getKeyCode() == keyCode
			&& normalizeModifiers(event.getModifiersEx()) == modifiers;
	}

	public boolean matchesMouse(int button)
	{
		return type == Type.MOUSE && mouseButton == button;
	}

	public boolean isNone()
	{
		return type == Type.NONE;
	}

	public int getModifiers()
	{
		return modifiers;
	}

	public String getDisplayName()
	{
		switch (type)
		{
			case KEYBOARD:
				return getKeyboardDisplayName();
			case MOUSE:
				return "Mouse " + mouseButton;
			default:
				return "Not set";
		}
	}

	private String getKeyboardDisplayName()
	{
		StringBuilder result = new StringBuilder();
		if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0)
		{
			result.append("Ctrl+");
		}
		if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0)
		{
			result.append("Shift+");
		}
		if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0)
		{
			result.append("Alt+");
		}
		if ((modifiers & InputEvent.META_DOWN_MASK) != 0)
		{
			result.append("Meta+");
		}
		if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0)
		{
			result.append("AltGr+");
		}
		result.append(KeyEvent.getKeyText(keyCode));
		return result.toString();
	}

	private static int normalizeModifiers(int modifiers)
	{
		return modifiers & SUPPORTED_MODIFIERS;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof TeleportBinding))
		{
			return false;
		}

		TeleportBinding binding = (TeleportBinding) other;
		return type == binding.type
			&& keyCode == binding.keyCode
			&& modifiers == binding.modifiers
			&& mouseButton == binding.mouseButton;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(type, keyCode, modifiers, mouseButton);
	}
}
