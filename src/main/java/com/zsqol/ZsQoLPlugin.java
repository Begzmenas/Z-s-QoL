package com.zsqol;

import com.google.inject.Provides;
import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Z's QoL",
	description = "Personal transport bindings and Fairy Ring Travel Log selection",
	tags = {"poh", "portal", "nexus", "jewellery", "quetzal", "mushtree", "spirit tree", "fairy ring", "teleport", "hotkey"}
)
public class ZsQoLPlugin extends Plugin implements KeyListener
{
	private static final int FIRST_SIDE_MOUSE_BUTTON = 4;
	private static final int ACTIVATION_COOLDOWN_CLIENT_CYCLES = 20;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ZsQoLConfig config;

	@Inject
	private BindingStore bindingStore;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ItemManager itemManager;

	private volatile Map<TeleportTarget, TeleportBinding> personalBindings =
		Collections.emptyMap();

	/*
	 * Direct actions for destinations that are currently present in an open
	 * Portal Nexus or Jewellery Box menu. These do not depend on the house
	 * owner's native letter/number shortcuts.
	 */
	private final Map<TeleportTarget, TeleportMenuAction> availableActions =
		new ConcurrentHashMap<>();
	private volatile boolean portalNexusOpen;
	private volatile boolean jewelleryBoxOpen;
	private volatile boolean fairyRingOpen;
	private volatile boolean loggedIn;
	private volatile boolean gameInputFocused = true;
	private volatile TeleportCategory activeGenericCategory;
	private volatile String activeGenericMenuTitle = "";
	private volatile TeleportTarget capturingTarget;

	private volatile int suppressedCaptureKeyCode = KeyEvent.VK_UNDEFINED;
	private volatile char suppressedCaptureCharacter = KeyEvent.CHAR_UNDEFINED;

	private final Set<Integer> blockedKeyCodes = ConcurrentHashMap.newKeySet();
	private final Set<Character> blockedTypedCharacters = ConcurrentHashMap.newKeySet();
	private final Set<Integer> blockedMouseButtons = ConcurrentHashMap.newKeySet();

	/*
	 * Accessed only from the RuneLite client thread.
	 */
	private int activationBlockedUntilCycle;

	private ZsQoLPanel panel;
	private NavigationButton navigationButton;

	private final MouseAdapter mouseListener = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent event)
		{
			return handleMousePressed(event);
		}

		@Override
		public MouseEvent mouseReleased(MouseEvent event)
		{
			if (blockedMouseButtons.contains(event.getButton()))
			{
				event.consume();
			}
			return event;
		}

		@Override
		public MouseEvent mouseClicked(MouseEvent event)
		{
			if (blockedMouseButtons.remove(event.getButton()))
			{
				event.consume();
			}
			return event;
		}
	};

	@Override
	protected void startUp()
	{
		reloadBindings();
		keyManager.registerKeyListener(this);
		mouseManager.registerMouseListener(0, mouseListener);

		SwingUtilities.invokeLater(() ->
		{
			panel = new ZsQoLPanel(
				this,
				bindingStore,
				spriteManager,
				itemManager
			);
			navigationButton = NavigationButton.builder()
				.tooltip("Z's QoL")
				.icon(createNavigationIcon())
				.priority(8)
				.panel(panel)
				.build();
			clientToolbar.addNavigation(navigationButton);
		});

		log.info("Z's QoL started with direct teleport-menu actions");
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(this);
		mouseManager.unregisterMouseListener(mouseListener);

		SwingUtilities.invokeLater(() ->
		{
			if (navigationButton != null)
			{
				clientToolbar.removeNavigation(navigationButton);
			}

			panel = null;
			navigationButton = null;
		});

		capturingTarget = null;
		availableActions.clear();
		clearInterfaceState();
		blockedKeyCodes.clear();
		blockedTypedCharacters.clear();
		blockedMouseButtons.clear();
		activationBlockedUntilCycle = 0;
		loggedIn = false;
		gameInputFocused = true;
		log.info("Z's QoL stopped");
	}

	/*
	 * Generic travel menus (Spirit Trees, Fossil Island Mushtrees, and
	 * Quetzal transport) use RuneLite's shared MENU_SETUP scripts. Capture
	 * the title before the rows are created, then capture each row afterwards.
	 */
	@Subscribe(priority = 1.0f)
	public void onScriptPreFired(ScriptPreFired event)
	{
		switch (event.getScriptId())
		{
			case ZsQoLScriptID.MENU_SETUP:
				log.debug("Generic menu setup script fired: {}", event.getScriptId());
				beginGenericMenu(readLegacyMenuTitle());
				break;

			case ZsQoLScriptID.MENU_NEW_SETUP:
				log.debug("Generic menu setup script fired: {}", event.getScriptId());
				beginGenericMenu(readNewMenuTitle(event));
				break;

			default:
				break;
		}
	}

	/*
	 * Capture each destination as the game creates its menu row. The stored
	 * resume widget lets us invoke the exact row later even when that row has
	 * no native hotkey assigned by the house owner.
	 */
	@Subscribe(priority = 1.0f)
	public void onScriptPostFired(ScriptPostFired event)
	{
		switch (event.getScriptId())
		{
			case ZsQoLScriptID.TELENEXUS_CREATE_TELELINE:
				registerMenuAction(
					TeleportCategory.POH_NEXUS,
					client.getScriptActiveWidget(),
					client.getScriptDotWidget()
				);
				break;

			case ZsQoLScriptID.POH_JEWELLERY_BOX_ADD_BUTTON:
				registerMenuAction(
					TeleportCategory.JEWELLERY_BOX,
					client.getScriptActiveWidget(),
					client.getScriptDotWidget()
				);
				break;

			case ZsQoLScriptID.MENU_CREATEENTRY:
			case ZsQoLScriptID.MENU_NEW_CREATEENTRY:
				log.debug("Generic menu row script fired: {}", event.getScriptId());
				registerGenericMenuAction(client.getScriptActiveWidget());
				break;

			default:
				break;
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		removeStaleMenuActions();
		captureOpenGenericMenuActions();
		captureOpenMushtreeMenuActions();
		captureOpenQuetzalMenuActions();
		refreshInterfaceState();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		loggedIn = event.getGameState() == GameState.LOGGED_IN;
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			fairyRingOpen = false;
			gameInputFocused = true;
			blockedKeyCodes.clear();
			blockedTypedCharacters.clear();
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		reloadBindings();
		if (panel != null)
		{
			panel.refreshAllBindings();
			panel.reloadIcons();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!ZsQoLConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		if (bindingStore.isBindingKey(event.getKey()))
		{
			reloadBindings();
			if (panel != null)
			{
				panel.refreshAllBindings();
			}
		}
		else if (bindingStore.isIconKey(event.getKey()) && panel != null)
		{
			panel.reloadIcons();
		}
	}

	public void beginBindingCapture(TeleportTarget target)
	{
		capturingTarget = target;
		if (panel != null)
		{
			panel.setCapturing(target);
		}
	}

	public synchronized void clearBinding(TeleportTarget target)
	{
		bindingStore.clearBinding(target);
		capturingTarget = null;
		reloadBindings();
		if (panel != null)
		{
			panel.bindingCleared(target);
		}
	}

	public void captureMouseBinding(TeleportTarget target, int mouseButton)
	{
		if (target == null
			|| mouseButton < FIRST_SIDE_MOUSE_BUTTON
			|| capturingTarget != target)
		{
			return;
		}

		assignBinding(target, TeleportBinding.mouse(mouseButton));
	}

	private synchronized void assignBinding(TeleportTarget target, TeleportBinding binding)
	{
		TeleportTarget displaced = null;

		for (Map.Entry<TeleportTarget, TeleportBinding> entry : personalBindings.entrySet())
		{
			if (entry.getKey() != target
				&& entry.getKey().getCategory() == target.getCategory()
				&& entry.getValue().equals(binding))
			{
				displaced = entry.getKey();
				bindingStore.clearBinding(displaced);
				break;
			}
		}

		bindingStore.setBinding(target, binding);
		capturingTarget = null;
		reloadBindings();

		if (panel != null)
		{
			panel.bindingSaved(target, binding, displaced);
		}
	}

	private void reloadBindings()
	{
		Map<TeleportTarget, TeleportBinding> loaded =
			new EnumMap<>(TeleportTarget.class);

		for (TeleportTarget target : TeleportTarget.values())
		{
			TeleportBinding binding = bindingStore.getBinding(target);
			if (!binding.isNone())
			{
				loaded.put(target, binding);
			}
		}

		personalBindings = Collections.unmodifiableMap(loaded);
	}

	@Override
	public void keyPressed(KeyEvent event)
	{
		dispatchKeyEvent(event);
	}

	@Override
	public void keyTyped(KeyEvent event)
	{
		dispatchKeyEvent(event);
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
		dispatchKeyEvent(event);
	}

	@Override
	public void focusLost()
	{
		blockedKeyCodes.clear();
		blockedTypedCharacters.clear();
		suppressedCaptureKeyCode = KeyEvent.VK_UNDEFINED;
		suppressedCaptureCharacter = KeyEvent.CHAR_UNDEFINED;
	}

	private boolean dispatchKeyEvent(KeyEvent event)
	{
		if (handleSuppressedCaptureEvent(event))
		{
			return true;
		}

		TeleportTarget capture = capturingTarget;
		if (capture != null)
		{
			return handleBindingCaptureKey(event, capture);
		}

		if (!config.teleportBindingsEnabled())
		{
			return false;
		}

		Canvas canvas = client.getCanvas();
		if (canvas == null || !canvas.isFocusOwner())
		{
			return false;
		}

		TeleportCategory activeCategory = getActiveCategory();
		switch (event.getID())
		{
			case KeyEvent.KEY_PRESSED:
				return activeCategory == null
					? false
					: handleTeleportKeyPressed(event, activeCategory);

			case KeyEvent.KEY_TYPED:
				return handleTeleportKeyTyped(event);

			case KeyEvent.KEY_RELEASED:
				return handleTeleportKeyReleased(event);

			default:
				return false;
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.FAIRYRINGS
			|| event.getGroupId() == InterfaceID.FAIRYRINGS_LOG)
		{
			fairyRingOpen = false;
		}
	}

	private boolean handleBindingCaptureKey(KeyEvent event, TeleportTarget target)
	{
		if (event.getID() != KeyEvent.KEY_PRESSED)
		{
			event.consume();
			return true;
		}

		int keyCode = event.getKeyCode();
		if (isModifierOnlyKey(keyCode))
		{
			event.consume();
			return true;
		}

		rememberCaptureSuppression(event);

		if (keyCode == KeyEvent.VK_ESCAPE)
		{
			capturingTarget = null;
			if (panel != null)
			{
				panel.captureCancelled();
			}
			event.consume();
			return true;
		}

		if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
		{
			clearBinding(target);
			event.consume();
			return true;
		}

		assignBinding(target, TeleportBinding.keyboard(keyCode, event.getModifiersEx()));
		event.consume();
		return true;
	}

	private boolean handleSuppressedCaptureEvent(KeyEvent event)
	{
		if (suppressedCaptureKeyCode == KeyEvent.VK_UNDEFINED)
		{
			return false;
		}

		if (event.getID() == KeyEvent.KEY_TYPED)
		{
			char typed = Character.toLowerCase(event.getKeyChar());
			if (suppressedCaptureCharacter == KeyEvent.CHAR_UNDEFINED
				|| typed == Character.toLowerCase(suppressedCaptureCharacter))
			{
				event.consume();
				return true;
			}
		}

		if (event.getID() == KeyEvent.KEY_PRESSED
			&& event.getKeyCode() == suppressedCaptureKeyCode)
		{
			event.consume();
			return true;
		}

		if (event.getID() == KeyEvent.KEY_RELEASED
			&& event.getKeyCode() == suppressedCaptureKeyCode)
		{
			suppressedCaptureKeyCode = KeyEvent.VK_UNDEFINED;
			suppressedCaptureCharacter = KeyEvent.CHAR_UNDEFINED;
			event.consume();
			return true;
		}

		return false;
	}

	private void rememberCaptureSuppression(KeyEvent event)
	{
		suppressedCaptureKeyCode = event.getKeyCode();
		suppressedCaptureCharacter = getExpectedTypedCharacter(event);
	}

	private boolean handleTeleportKeyPressed(KeyEvent event, TeleportCategory category)
	{
		int keyCode = event.getKeyCode();
		boolean keyAlreadyBlocked = blockedKeyCodes.contains(keyCode);

		if (category == TeleportCategory.FAIRY_RING
			&& !FairyRingTravelLog.shouldHandleHotkey(
				loggedIn,
				fairyRingOpen,
				gameInputFocused,
				keyAlreadyBlocked
			))
		{
			if (keyAlreadyBlocked)
			{
				event.consume();
				return true;
			}
			return false;
		}

		if (keyAlreadyBlocked)
		{
			event.consume();
			return true;
		}

		TeleportTarget target = findKeyboardTarget(event, category);
		if (target == null)
		{
			return false;
		}

		/*
		 * Always block the original input. This prevents the house owner's
		 * normal shortcut for the same key from activating another destination.
		 */
		blockOriginalKey(event);

		if (category == TeleportCategory.FAIRY_RING)
		{
			selectFairyRingTravelLogAsync(target);
		}
		else if (availableActions.containsKey(target))
		{
			activateTargetAsync(target);
		}
		else
		{
			log.debug(
				"Binding pressed for '{}' in {}, but no captured action is available",
				target.getDisplayName(),
				category.getDisplayName()
			);
		}

		/*
		 * If the destination is not present in this house, do nothing silently.
		 */
		return true;
	}

	private boolean handleTeleportKeyTyped(KeyEvent event)
	{
		char typed = Character.toLowerCase(event.getKeyChar());
		if (blockedTypedCharacters.contains(typed))
		{
			event.consume();
			return true;
		}

		return false;
	}

	private boolean handleTeleportKeyReleased(KeyEvent event)
	{
		int keyCode = event.getKeyCode();
		if (!blockedKeyCodes.remove(keyCode))
		{
			return false;
		}

		char character = getExpectedTypedCharacter(event);
		if (character != KeyEvent.CHAR_UNDEFINED)
		{
			blockedTypedCharacters.remove(Character.toLowerCase(character));
		}

		event.consume();
		return true;
	}

	private MouseEvent handleMousePressed(MouseEvent event)
	{
		int button = event.getButton();
		if (button < FIRST_SIDE_MOUSE_BUTTON)
		{
			return event;
		}

		TeleportTarget capture = capturingTarget;
		if (capture != null)
		{
			blockedMouseButtons.add(button);
			assignBinding(capture, TeleportBinding.mouse(button));
			event.consume();
			return event;
		}

		if (!config.teleportBindingsEnabled())
		{
			return event;
		}

		TeleportCategory category = getActiveCategory();
		if (category == null)
		{
			return event;
		}

		TeleportTarget target = findMouseTarget(button, category);
		if (target == null)
		{
			return event;
		}

		if (category == TeleportCategory.FAIRY_RING
			&& gameInputFocused)
		{
			return event;
		}

		blockedMouseButtons.add(button);
		event.consume();

		if (category == TeleportCategory.FAIRY_RING)
		{
			selectFairyRingTravelLogAsync(target);
		}
		else if (availableActions.containsKey(target))
		{
			activateTargetAsync(target);
		}
		else
		{
			log.debug(
				"Mouse binding pressed for '{}' in {}, but no captured action is available",
				target.getDisplayName(),
				category.getDisplayName()
			);
		}

		return event;
	}

	private TeleportTarget findKeyboardTarget(KeyEvent event, TeleportCategory category)
	{
		for (Map.Entry<TeleportTarget, TeleportBinding> entry : personalBindings.entrySet())
		{
			if (entry.getKey().getCategory() == category
				&& entry.getValue().matches(event))
			{
				return entry.getKey();
			}
		}

		return null;
	}

	private TeleportTarget findMouseTarget(int button, TeleportCategory category)
	{
		for (Map.Entry<TeleportTarget, TeleportBinding> entry : personalBindings.entrySet())
		{
			if (entry.getKey().getCategory() == category
				&& entry.getValue().matchesMouse(button))
			{
				return entry.getKey();
			}
		}

		return null;
	}

	private TeleportCategory getActiveCategory()
	{
		if (portalNexusOpen)
		{
			return TeleportCategory.POH_NEXUS;
		}

		if (jewelleryBoxOpen)
		{
			return TeleportCategory.JEWELLERY_BOX;
		}

		if (fairyRingOpen)
		{
			return TeleportCategory.FAIRY_RING;
		}

		TeleportCategory genericCategory = activeGenericCategory;
		if (genericCategory != null && hasAvailableActions(genericCategory))
		{
			return genericCategory;
		}

		return null;
	}

	private String readLegacyMenuTitle()
	{
		int stackSize = client.getObjectStackSize();
		Object[] stack = client.getObjectStack();
		if (stack == null || stackSize <= 0 || stackSize > stack.length)
		{
			return "";
		}

		Object title = stack[stackSize - 1];
		return title instanceof String ? (String) title : "";
	}

	private String readNewMenuTitle(ScriptPreFired event)
	{
		Object[] arguments = event.getScriptEvent() == null
			? null
			: event.getScriptEvent().getArguments();

		if (arguments == null || arguments.length <= 1 || !(arguments[1] instanceof String))
		{
			return "";
		}

		return (String) arguments[1];
	}

	private void captureOpenGenericMenuActions()
	{
		Widget legacyTitleContainer = client.getWidget(
			InterfaceID.Menu.LJ_LAYER2
		);

		captureGenericMenuRows(
			findGenericMenuTitle(legacyTitleContainer),
			client.getWidget(InterfaceID.Menu.LJ_LAYER1)
		);

		Widget newTitleWidget = client.getWidget(
			InterfaceID.MenuNew.TITLE
		);

		captureGenericMenuRows(
			newTitleWidget == null ? "" : cleanText(newTitleWidget.getText()),
			client.getWidget(InterfaceID.MenuNew.TEXT)
		);
	}

	private String findGenericMenuTitle(Widget widget)
	{
		if (widget == null)
		{
			return "";
		}

		Set<Widget> visited = Collections.newSetFromMap(
			new IdentityHashMap<>()
		);

		return findGenericMenuTitle(widget, visited);
	}

	private String findGenericMenuTitle(
		Widget widget,
		Set<Widget> visited
	)
	{
		if (widget == null || !visited.add(widget))
		{
			return "";
		}

		String text = cleanText(widget.getText());
		if (findGenericCategoryByTitle(text) != null)
		{
			return text;
		}

		String childTitle = findGenericMenuTitle(
			widget.getDynamicChildren(),
			visited
		);
		if (!childTitle.isEmpty())
		{
			return childTitle;
		}

		childTitle = findGenericMenuTitle(
			widget.getStaticChildren(),
			visited
		);
		if (!childTitle.isEmpty())
		{
			return childTitle;
		}

		return findGenericMenuTitle(
			widget.getNestedChildren(),
			visited
		);
	}

	private String findGenericMenuTitle(
		Widget[] widgets,
		Set<Widget> visited
	)
	{
		if (widgets == null)
		{
			return "";
		}

		for (Widget widget : widgets)
		{
			String title = findGenericMenuTitle(widget, visited);
			if (!title.isEmpty())
			{
				return title;
			}
		}

		return "";
	}

	private void captureGenericMenuRows(
		String title,
		Widget rowsContainer
	)
	{
		TeleportCategory category = findGenericCategoryByTitle(title);
		if (category == null || rowsContainer == null)
		{
			return;
		}

		Widget[] rows = rowsContainer.getDynamicChildren();
		if (rows == null || rows.length == 0)
		{
			return;
		}

		if (activeGenericCategory != category)
		{
			clearGenericMenuActions();
			activeGenericCategory = category;
			activeGenericMenuTitle = cleanText(title);
		}

		boolean firstActionForCategory = !hasAvailableActions(category);
		boolean capturedAny = false;

		for (Widget row : rows)
		{
			if (row == null)
			{
				continue;
			}

			String rowText = cleanText(row.getText());
			TeleportTarget target = findTarget(category, rowText);
			if (target == null)
			{
				continue;
			}

			capturedAny = true;
			storeMenuAction(target, row, false);
		}

		if (capturedAny && firstActionForCategory)
		{
			log.debug(
				"{} destinations recovered from the open '{}' widget tree",
				category.getDisplayName(),
				activeGenericMenuTitle
			);
		}
	}

	private void captureOpenQuetzalMenuActions()
	{
		boolean captured = captureQuetzalMenuActions(
			InterfaceID.QuetzalMenu.UNIVERSE,
			InterfaceID.QuetzalMenu.ICONS
		);

		captured |= captureQuetzalMenuActions(
			InterfaceID.QuetzalwhistleMenu.UNIVERSE,
			InterfaceID.QuetzalwhistleMenu.ICONS
		);

		if (captured)
		{
			activeGenericCategory = TeleportCategory.QUETZAL;
			activeGenericMenuTitle = "Quetzal Transport System";
		}
	}

	private void captureOpenMushtreeMenuActions()
	{
		Widget root = client.getWidget(InterfaceID.FossilMushtrees.UNIVERSE);
		Widget content = client.getWidget(InterfaceID.FossilMushtrees.CONTENT);

		if (root == null || root.isHidden() || content == null)
		{
			return;
		}

		Widget[] destinationRows = content.getStaticChildren();
		if (destinationRows == null || destinationRows.length == 0)
		{
			return;
		}

		boolean firstActionForCategory = !hasAvailableActions(
			TeleportCategory.FOSSIL_MUSHTREE
		);
		boolean capturedAny = false;

		for (Widget destinationRow : destinationRows)
		{
			if (destinationRow == null)
			{
				continue;
			}

			Widget[] rowChildren = destinationRow.getStaticChildren();
			if (rowChildren == null || rowChildren.length < 2)
			{
				continue;
			}

			Widget actionWidget = rowChildren[0];
			String rowText = cleanText(rowChildren[1].getText());
			TeleportTarget target = findTarget(
				TeleportCategory.FOSSIL_MUSHTREE,
				rowText
			);
			int shortcutKeyCode = readLeadingShortcutKeyCode(rowText);

			if (target == null
				|| shortcutKeyCode == KeyEvent.VK_UNDEFINED
				|| actionWidget == null
				|| actionWidget.getOnKeyListener() == null)
			{
				continue;
			}

			capturedAny = true;
			storeKeyListenerAction(target, actionWidget, shortcutKeyCode);
		}

		if (capturedAny)
		{
			activeGenericCategory = TeleportCategory.FOSSIL_MUSHTREE;
			activeGenericMenuTitle = "Mycelium Transportation System";

			if (firstActionForCategory)
			{
				log.debug("Fossil Island Mushtree destination buttons captured from interface group {}", root.getId() >>> 16);
			}
		}
	}

	private int readLeadingShortcutKeyCode(String rowText)
	{
		for (int index = 0; index < rowText.length(); index++)
		{
			char character = rowText.charAt(index);
			if (Character.isDigit(character))
			{
				return KeyEvent.getExtendedKeyCodeForChar(character);
			}

			if (Character.isLetter(character))
			{
				break;
			}
		}

		return KeyEvent.VK_UNDEFINED;
	}

	private boolean captureQuetzalMenuActions(
		int rootComponentId,
		int iconsComponentId
	)
	{
		Widget root = client.getWidget(rootComponentId);
		Widget icons = client.getWidget(iconsComponentId);

		if (root == null || root.isHidden() || icons == null)
		{
			return false;
		}

		Widget[] destinationWidgets = icons.getDynamicChildren();
		if (destinationWidgets == null || destinationWidgets.length == 0)
		{
			return false;
		}

		boolean firstActionForCategory = !hasAvailableActions(
			TeleportCategory.QUETZAL
		);
		boolean capturedAny = false;

		for (Widget destinationWidget : destinationWidgets)
		{
			if (destinationWidget == null)
			{
				continue;
			}

			String destinationText = getWidgetActionText(destinationWidget);
			TeleportTarget target = findTarget(
				TeleportCategory.QUETZAL,
				destinationText
			);

			if (target == null)
			{
				continue;
			}

			capturedAny = true;
			storeMenuAction(target, destinationWidget, true);
		}

		if (capturedAny && firstActionForCategory)
		{
			log.debug("Quetzal destination buttons captured from interface group {}", rootComponentId >>> 16);
		}

		return capturedAny;
	}

	private String getWidgetActionText(Widget widget)
	{
		StringBuilder text = new StringBuilder();
		appendWidgetText(text, widget.getText());
		appendWidgetText(text, widget.getName());

		String[] actions = widget.getActions();
		if (actions != null)
		{
			for (String action : actions)
			{
				appendWidgetText(text, action);
			}
		}

		return cleanText(text.toString());
	}

	private void appendWidgetText(StringBuilder result, String value)
	{
		String cleaned = cleanText(value);
		if (cleaned.isEmpty())
		{
			return;
		}

		if (result.length() > 0)
		{
			result.append(' ');
		}

		result.append(cleaned);
	}

	private void beginGenericMenu(String title)
	{
		clearGenericMenuActions();
		activeGenericMenuTitle = cleanText(title);
		activeGenericCategory = findGenericCategoryByTitle(activeGenericMenuTitle);

		if (activeGenericCategory != null)
		{
			log.info(
				"{} menu opened: '{}'",
				activeGenericCategory.getDisplayName(),
				activeGenericMenuTitle
			);
		}
		else if (!activeGenericMenuTitle.isEmpty())
		{
			log.debug("Generic travel menu setup: '{}'", activeGenericMenuTitle);
		}
	}

	private void registerGenericMenuAction(Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		String rowText = cleanText(widget.getText());
		TeleportCategory category = activeGenericCategory;
		TeleportTarget target = category == null ? null : findTarget(category, rowText);

		log.debug(
			"Generic menu row captured: title='{}', category={}, text='{}'",
			activeGenericMenuTitle,
			category == null ? "none" : category.getDisplayName(),
			rowText
		);

		if (target == null)
		{
			target = findGenericTarget(rowText);
		}

		if (target == null)
		{
			log.debug("No generic teleport target matched row '{}'", rowText);
			return;
		}

		category = target.getCategory();
		boolean firstActionForCategory = !hasAvailableActions(category);

		if (activeGenericCategory != category)
		{
			activeGenericCategory = category;
		}

		registerMenuAction(category, widget, widget);
		log.debug(
			"Generic menu row '{}' matched {}",
			rowText,
			target.name()
		);

		if (firstActionForCategory)
		{
			log.info(
				"{} destinations captured{}",
				category.getDisplayName(),
				activeGenericMenuTitle.isEmpty()
					? ""
					: ": '" + activeGenericMenuTitle + "'"
			);
		}
	}

	private TeleportCategory findGenericCategoryByTitle(String title)
	{
		for (TeleportCategory category : TeleportCategory.values())
		{
			if (category.matchesMenuTitle(title))
			{
				return category;
			}
		}

		return null;
	}

	private TeleportTarget findGenericTarget(String displayedDestination)
	{
		for (TeleportCategory category : TeleportCategory.values())
		{
			if (!category.isGenericMenuCategory())
			{
				continue;
			}

			TeleportTarget target = findTarget(category, displayedDestination);
			if (target != null)
			{
				return target;
			}
		}

		return null;
	}

	private void clearGenericMenuActions()
	{
		for (Map.Entry<TeleportTarget, TeleportMenuAction> entry : availableActions.entrySet())
		{
			if (entry.getKey().getCategory().isGenericMenuCategory())
			{
				availableActions.remove(entry.getKey(), entry.getValue());
			}
		}
	}

	private boolean hasAvailableActions(TeleportCategory category)
	{
		for (TeleportTarget target : availableActions.keySet())
		{
			if (target.getCategory() == category)
			{
				return true;
			}
		}

		return false;
	}

	private void selectFairyRingTravelLogAsync(TeleportTarget target)
	{
		clientThread.invokeLater(() -> selectFairyRingTravelLog(target));
	}

	private void selectFairyRingTravelLog(TeleportTarget target)
	{
		/*
		 * One binding invokes at most one existing Travel Log "Use code"
		 * operation. Off-screen rows remain real, non-hidden game widgets, so
		 * they can be selected without adding a scroll action. This never
		 * rotates individual dials, writes varbits, or invokes Teleport.
		 */
		boolean configureFound = getVisibleFairyRingConfirmWidget() != null;
		Widget travelLogUniverse = getVisibleWidget(InterfaceID.FairyringsLog.UNIVERSE);
		Widget travelLogContents = getVisibleWidget(InterfaceID.FairyringsLog.CONTENTS);
		boolean interfaceReady = configureFound
			&& travelLogUniverse != null
			&& travelLogContents != null;

		if (client.getGameState() != GameState.LOGGED_IN
			|| client.getLocalPlayer() == null
			|| !interfaceReady
			|| client.getFocusedInputFieldWidget() != null
			|| target == null
			|| target.getCategory() != TeleportCategory.FAIRY_RING)
		{
			return;
		}

		Widget travelLogEntry = findFairyRingTravelLogEntry(target);
		int useCodeActionIdentifier = travelLogEntry == null
			? -1
			: FairyRingTravelLog.getUseCodeActionIdentifier(travelLogEntry.getActions());

		if (!FairyRingTravelLog.activateOnce(
			travelLogEntry != null
				&& useCodeActionIdentifier > 0,
			travelLogEntry == null || useCodeActionIdentifier < 1
				? null
				: () -> client.menuAction(
					travelLogEntry.getIndex(),
					travelLogEntry.getId(),
					MenuAction.CC_OP,
					useCodeActionIdentifier,
					travelLogEntry.getItemId(),
					"Use code",
					travelLogEntry.getName()
				)
		))
		{
			return;
		}

		TeleportBinding binding = personalBindings.get(target);
		log.debug(
			"Fairy Ring Travel Log action submitted: code={}, destination={}, bindingKey=binding.{}, binding={}",
			target.getFairyRingCode(),
			target.getDisplayName(),
			target.getStableConfigKey(),
			binding == null ? "unknown" : binding.getDisplayName()
		);
	}

	private Widget findFairyRingTravelLogEntry(TeleportTarget target)
	{
		Widget universe = getVisibleWidget(InterfaceID.FairyringsLog.UNIVERSE);
		Widget contents = getVisibleWidget(InterfaceID.FairyringsLog.CONTENTS);
		if (universe == null || contents == null)
		{
			return null;
		}

		ArrayDeque<Widget> remaining = new ArrayDeque<>();
		Set<Widget> visited = Collections.newSetFromMap(
			new IdentityHashMap<>()
		);
		remaining.add(universe);

		while (!remaining.isEmpty())
		{
			Widget widget = remaining.removeFirst();
			if (widget == null || !visited.add(widget))
			{
				continue;
			}

			if (FairyRingTravelLog.isActionable(
					widget.isHidden(),
					widget.getBounds()
				)
				&& matchesFairyRingTravelLogEntry(widget, target))
			{
				return widget;
			}

			addWidgets(remaining, widget.getDynamicChildren());
			addWidgets(remaining, widget.getStaticChildren());
			addWidgets(remaining, widget.getNestedChildren());
		}

		return null;
	}

	private boolean matchesFairyRingTravelLogEntry(
		Widget widget,
		TeleportTarget target
	)
	{
		String code = target.getFairyRingCode();
		String destination = target.getDisplayName();
		String[] actions = widget.getActions();
		return FairyRingTravelLog.matchesEntry(
			code,
			destination,
			widget.getText(),
			widget.getName(),
			actions
		) || FairyRingTravelLog.matchesEntry(
			code,
			destination,
			collectWidgetText(widget),
			"",
			actions
		);
	}

	private String collectWidgetText(Widget widget)
	{
		StringBuilder text = new StringBuilder();
		for (Widget child : widget.getNestedChildren())
		{
			appendWidgetText(text, child);
		}
		return text.toString();
	}

	private void appendWidgetText(StringBuilder result, Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		if (widget.getText() != null)
		{
			result.append(' ').append(widget.getText());
		}
		if (widget.getName() != null)
		{
			result.append(' ').append(widget.getName());
		}
	}

	private void addWidgets(ArrayDeque<Widget> destination, Widget[] widgets)
	{
		if (widgets == null)
		{
			return;
		}

		for (Widget widget : widgets)
		{
			if (widget != null)
			{
				destination.addLast(widget);
			}
		}
	}

	private boolean isFairyRingConfigurationInterfaceOpen()
	{
		return getVisibleFairyRingConfirmWidget() != null
			&& getVisibleWidget(InterfaceID.FairyringsLog.CONTENTS) != null;
	}

	private Widget getVisibleFairyRingConfirmWidget()
	{
		Widget confirm = client.getWidget(
			InterfaceID.Fairyrings.CONFIRM
		);

		return confirm != null && !confirm.isHidden()
			? confirm
			: null;
	}

	private Widget getVisibleWidget(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		return widget != null && !widget.isHidden() ? widget : null;
	}

	private void activateTargetAsync(TeleportTarget target)
	{
		clientThread.invokeLater(() -> activateTarget(target));
	}

	private void activateTarget(TeleportTarget target)
	{
		TeleportMenuAction action = availableActions.get(target);
		if (action == null || !isWidgetGroupLoaded(action.groupId))
		{
			log.debug("No live action is available for '{}'", target.getDisplayName());
			return;
		}

		if (activationBlockedUntilCycle >= client.getGameCycle())
		{
			return;
		}

		Widget actionWidget = action.actionWidget;
		if (actionWidget == null)
		{
			return;
		}

		if (action.keyListenerKeyCode != KeyEvent.VK_UNDEFINED)
		{
			Object[] listener = actionWidget.getOnKeyListener();
			if (listener == null || listener.length <= 1)
			{
				log.debug("Mushtree action widget for '{}' has no keyboard listener", target.getDisplayName());
				return;
			}

			Object[] arguments = listener.clone();
			arguments[1] = action.keyListenerKeyCode;
			client.runScript(arguments);
		}
		else if (action.widgetOperation)
		{
			String option = getFirstWidgetAction(actionWidget);
			if (option.isEmpty())
			{
				log.debug("Quetzal action widget for '{}' has no operation", target.getDisplayName());
				return;
			}

			client.menuAction(
				actionWidget.getIndex(),
				actionWidget.getId(),
				MenuAction.CC_OP,
				1,
				actionWidget.getItemId(),
				option,
				""
			);
		}
		else
		{
			client.runScript(
				ZsQoLScriptID.CC_RESUME_PAUSEBUTTON,
				actionWidget.getId(),
				actionWidget.getIndex()
			);
		}

		activationBlockedUntilCycle =
			client.getGameCycle() + ACTIVATION_COOLDOWN_CLIENT_CYCLES;

		log.info("{} direct teleport: '{}'",
			target.getCategory().getDisplayName(),
			target.getDisplayName());
	}

	private void registerMenuAction(
		TeleportCategory category,
		Widget textWidget,
		Widget resumeWidget
	)
	{
		if (textWidget == null || resumeWidget == null)
		{
			return;
		}

		TeleportTarget target = findTarget(category, cleanText(textWidget.getText()));
		if (target == null)
		{
			return;
		}

		storeMenuAction(target, resumeWidget, false);

		if (category == TeleportCategory.POH_NEXUS)
		{
			discoverNexusIcon(textWidget, target);
		}

		refreshInterfaceState();
	}

	private boolean storeMenuAction(
		TeleportTarget target,
		Widget actionWidget,
		boolean widgetOperation
	)
	{
		if (target == null || actionWidget == null)
		{
			return false;
		}

		return storeMenuAction(
			target,
			actionWidget,
			widgetOperation,
			KeyEvent.VK_UNDEFINED
		);
	}

	private boolean storeKeyListenerAction(
		TeleportTarget target,
		Widget actionWidget,
		int shortcutKeyCode
	)
	{
		return storeMenuAction(
			target,
			actionWidget,
			false,
			shortcutKeyCode
		);
	}

	private boolean storeMenuAction(
		TeleportTarget target,
		Widget actionWidget,
		boolean widgetOperation,
		int keyListenerKeyCode
	)
	{
		if (target == null || actionWidget == null)
		{
			return false;
		}

		TeleportMenuAction existing = availableActions.get(target);
		if (existing != null
			&& existing.actionWidget == actionWidget
			&& existing.widgetOperation == widgetOperation
			&& existing.keyListenerKeyCode == keyListenerKeyCode)
		{
			return false;
		}

		int groupId = actionWidget.getId() >>> 16;
		TeleportMenuAction previous = availableActions.put(
			target,
			new TeleportMenuAction(
				groupId,
				actionWidget,
				widgetOperation,
				keyListenerKeyCode
			)
		);

		if (previous == null)
		{
			log.debug(
				"Stored {} action for '{}' from widget {}:{}",
				keyListenerKeyCode != KeyEvent.VK_UNDEFINED
					? "key-listener"
					: widgetOperation ? "widget-operation" : "resume",
				target.getDisplayName(),
				actionWidget.getId(),
				actionWidget.getIndex()
			);
		}

		return previous == null;
	}

	private String getFirstWidgetAction(Widget widget)
	{
		String[] actions = widget.getActions();
		if (actions == null)
		{
			return "";
		}

		for (String action : actions)
		{
			String cleaned = cleanText(action);
			if (!cleaned.isEmpty())
			{
				return cleaned;
			}
		}

		return "";
	}

	private void removeStaleMenuActions()
	{
		for (Map.Entry<TeleportTarget, TeleportMenuAction> entry
			: availableActions.entrySet())
		{
			TeleportMenuAction action = entry.getValue();
			if (!isWidgetGroupLoaded(action.groupId))
			{
				availableActions.remove(entry.getKey(), action);
			}
		}
	}

	private boolean isWidgetGroupLoaded(int groupId)
	{
		for (var entry : client.getComponentTable())
		{
			if (entry.getId() == groupId)
			{
				return true;
			}
		}

		return false;
	}

	private void refreshInterfaceState()
	{
		loggedIn = client.getGameState() == GameState.LOGGED_IN;
		String chatInput = client.getVarcStrValue(VarClientID.CHATINPUT);
		gameInputFocused = client.getFocusedInputFieldWidget() != null
			|| chatInput != null && !chatInput.isEmpty();

		Widget nexusUniverse = client.getWidget(InterfaceID.TelenexusTeleport.UNIVERSE);
		boolean nexusWidgetOpen = nexusUniverse != null && !nexusUniverse.isHidden();

		boolean hasNexusActions = hasAvailableActions(TeleportCategory.POH_NEXUS);
		boolean hasJewelleryActions = hasAvailableActions(TeleportCategory.JEWELLERY_BOX);

		portalNexusOpen =
			nexusWidgetOpen
				|| hasNexusActions;

		jewelleryBoxOpen =
			hasJewelleryActions;

		fairyRingOpen =
			isFairyRingConfigurationInterfaceOpen();

		TeleportCategory genericCategory = activeGenericCategory;
		boolean genericMenuOpen = genericCategory != null
			&& hasAvailableActions(genericCategory);

		if (!genericMenuOpen)
		{
			activeGenericCategory = null;
			activeGenericMenuTitle = "";
		}

		if (
			!portalNexusOpen
				&& !jewelleryBoxOpen
				&& !fairyRingOpen
				&& !genericMenuOpen
		)
		{
			clearInterfaceState();
		}
	}

	private void clearInterfaceState()
	{
		portalNexusOpen = false;
		jewelleryBoxOpen = false;
		fairyRingOpen = false;
		activeGenericCategory = null;
		activeGenericMenuTitle = "";
	}

	private TeleportTarget findTarget(
		TeleportCategory category,
		String displayedDestination
	)
	{
		String normalizedDisplayed = normalize(displayedDestination);

		for (TeleportTarget target : TeleportTarget.values())
		{
			if (target.getCategory() == category
				&& normalize(target.getDisplayName()).equals(normalizedDisplayed))
			{
				return target;
			}
		}

		for (TeleportTarget target : TeleportTarget.values())
		{
			if (target.getCategory() == category
				&& target.matches(displayedDestination))
			{
				return target;
			}
		}

		return null;
	}

	private void blockOriginalKey(KeyEvent event)
	{
		blockedKeyCodes.add(event.getKeyCode());
		char character = getExpectedTypedCharacter(event);
		if (character != KeyEvent.CHAR_UNDEFINED)
		{
			blockedTypedCharacters.add(Character.toLowerCase(character));
		}
		event.consume();
	}

	private boolean isModifierOnlyKey(int keyCode)
	{
		return keyCode == KeyEvent.VK_CONTROL
			|| keyCode == KeyEvent.VK_SHIFT
			|| keyCode == KeyEvent.VK_ALT
			|| keyCode == KeyEvent.VK_META
			|| keyCode == KeyEvent.VK_ALT_GRAPH;
	}

	private char getExpectedTypedCharacter(KeyEvent event)
	{
		char character = event.getKeyChar();
		if (character != KeyEvent.CHAR_UNDEFINED)
		{
			return Character.toLowerCase(character);
		}

		int keyCode = event.getKeyCode();
		if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)
		{
			return Character.toLowerCase((char) keyCode);
		}

		if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9)
		{
			return (char) keyCode;
		}

		if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9)
		{
			return (char) ('0' + keyCode - KeyEvent.VK_NUMPAD0);
		}

		return KeyEvent.CHAR_UNDEFINED;
	}

	private void discoverNexusIcon(Widget textWidget, TeleportTarget target)
	{
		if (bindingStore.getNexusIconSpriteId(target) >= 0)
		{
			return;
		}

		int spriteId = findNearestSpriteId(textWidget);
		if (spriteId < 0)
		{
			return;
		}

		bindingStore.setNexusIconSpriteId(target, spriteId);
		if (panel != null)
		{
			panel.updateNexusIcon(target, spriteId);
		}
	}

	private int findNearestSpriteId(Widget textWidget)
	{
		Rectangle targetBounds = textWidget.getBounds();
		if (targetBounds == null)
		{
			return -1;
		}

		Widget container = textWidget.getParent();
		for (int depth = 0; depth < 3 && container != null; depth++)
		{
			SpriteCandidate candidate = new SpriteCandidate();
			Set<Widget> visited = Collections.newSetFromMap(new IdentityHashMap<>());
			findSpriteCandidate(container, targetBounds, visited, candidate);

			if (candidate.spriteId >= 0 && candidate.verticalDistance <= 28)
			{
				return candidate.spriteId;
			}

			container = container.getParent();
		}

		return -1;
	}

	private void findSpriteCandidate(
		Widget widget,
		Rectangle targetBounds,
		Set<Widget> visited,
		SpriteCandidate best
	)
	{
		if (widget == null || !visited.add(widget) || widget.isHidden())
		{
			return;
		}

		int spriteId = widget.getSpriteId();
		if (spriteId >= 0)
		{
			Rectangle bounds = widget.getBounds();
			if (bounds != null)
			{
				int targetY = targetBounds.y + targetBounds.height / 2;
				int candidateY = bounds.y + bounds.height / 2;
				int targetX = targetBounds.x + targetBounds.width / 2;
				int candidateX = bounds.x + bounds.width / 2;
				int verticalDistance = Math.abs(targetY - candidateY);
				int score = verticalDistance * 1000 + Math.abs(targetX - candidateX);

				if (score < best.score)
				{
					best.score = score;
					best.spriteId = spriteId;
					best.verticalDistance = verticalDistance;
				}
			}
		}

		findSpriteChildren(widget.getDynamicChildren(), targetBounds, visited, best);
		findSpriteChildren(widget.getStaticChildren(), targetBounds, visited, best);
		findSpriteChildren(widget.getNestedChildren(), targetBounds, visited, best);
	}

	private void findSpriteChildren(
		Widget[] children,
		Rectangle targetBounds,
		Set<Widget> visited,
		SpriteCandidate best
	)
	{
		if (children == null)
		{
			return;
		}

		for (Widget child : children)
		{
			findSpriteCandidate(child, targetBounds, visited, best);
		}
	}

	private String cleanText(String value)
	{
		if (value == null)
		{
			return "";
		}

		return Text.removeTags(value)
			.replace('\u00A0', ' ')
			.replaceAll("\\s+", " ")
			.trim();
	}

	private String normalize(String value)
	{
		return cleanText(value)
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}

	private BufferedImage createNavigationIcon()
	{
		return ImageUtil.resizeImage(
			ImageUtil.loadImageResource(
				ZsQoLPlugin.class,
				"icon.png"
			),
			16,
			16
		);
	}

	@Provides
	ZsQoLConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZsQoLConfig.class);
	}

	private static final class TeleportMenuAction
	{
		private final int groupId;
		private final Widget actionWidget;
		private final boolean widgetOperation;
		private final int keyListenerKeyCode;

		private TeleportMenuAction(
			int groupId,
			Widget actionWidget,
			boolean widgetOperation,
			int keyListenerKeyCode
		)
		{
			this.groupId = groupId;
			this.actionWidget = actionWidget;
			this.widgetOperation = widgetOperation;
			this.keyListenerKeyCode = keyListenerKeyCode;
		}
	}

	private static final class SpriteCandidate
	{
		private int spriteId = -1;
		private int verticalDistance = Integer.MAX_VALUE;
		private int score = Integer.MAX_VALUE;
	}
}
