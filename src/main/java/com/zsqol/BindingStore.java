package com.zsqol;

import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class BindingStore
{
	private static final String BINDING_PREFIX = "binding.";
	private static final String LEGACY_POH_BINDING_PREFIX = "pohBinding.";
	private static final String ICON_PREFIX = "nexusIcon.";

	private final ConfigManager configManager;

	@Inject
	public BindingStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public TeleportBinding getBinding(TeleportTarget target)
	{
		String saved = configManager.getConfiguration(
			ZsQoLConfig.GROUP,
			bindingKey(target)
		);

		TeleportBinding binding = TeleportBinding.parse(saved);
		if (!binding.isNone())
		{
			return binding;
		}

		/*
		 * Migrate bindings written by the earlier POH-only build.
		 * Example: pohBinding.arceuus_library -> binding.nexus_arceuus_library
		 */
		String legacyKey = legacyPohBindingKey(target);
		if (legacyKey == null)
		{
			return TeleportBinding.none();
		}

		TeleportBinding legacyBinding = TeleportBinding.parse(
			configManager.getConfiguration(ZsQoLConfig.GROUP, legacyKey)
		);

		if (legacyBinding.isNone())
		{
			return legacyBinding;
		}

		setBinding(target, legacyBinding);
		configManager.unsetConfiguration(ZsQoLConfig.GROUP, legacyKey);
		return legacyBinding;
	}

	public void setBinding(TeleportTarget target, TeleportBinding binding)
	{
		if (binding == null || binding.isNone())
		{
			clearBinding(target);
			return;
		}

		configManager.setConfiguration(
			ZsQoLConfig.GROUP,
			bindingKey(target),
			binding.serialize()
		);
	}

	public void clearBinding(TeleportTarget target)
	{
		configManager.unsetConfiguration(
			ZsQoLConfig.GROUP,
			bindingKey(target)
		);
	}

	public int getNexusIconSpriteId(TeleportTarget target)
	{
		String value = configManager.getConfiguration(
			ZsQoLConfig.GROUP,
			iconKey(target)
		);

		if (value == null || value.trim().isEmpty())
		{
			return -1;
		}

		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ignored)
		{
			return -1;
		}
	}

	public void setNexusIconSpriteId(TeleportTarget target, int spriteId)
	{
		if (target.getCategory() != TeleportCategory.POH_NEXUS || spriteId < 0)
		{
			return;
		}

		configManager.setConfiguration(
			ZsQoLConfig.GROUP,
			iconKey(target),
			Integer.toString(spriteId)
		);
	}

	public boolean isBindingKey(String key)
	{
		return key != null && key.startsWith(BINDING_PREFIX);
	}

	public boolean isIconKey(String key)
	{
		return key != null && key.startsWith(ICON_PREFIX);
	}

	private String bindingKey(TeleportTarget target)
	{
		return BINDING_PREFIX + target.getStableConfigKey();
	}

	private String iconKey(TeleportTarget target)
	{
		return ICON_PREFIX + target.getStableConfigKey();
	}

	private String legacyPohBindingKey(TeleportTarget target)
	{
		if (target.getCategory() != TeleportCategory.POH_NEXUS)
		{
			return null;
		}

		String name = target.name();
		if (!name.startsWith("NEXUS_"))
		{
			return null;
		}

		return LEGACY_POH_BINDING_PREFIX
			+ name.substring("NEXUS_".length()).toLowerCase(Locale.ROOT);
	}
}
