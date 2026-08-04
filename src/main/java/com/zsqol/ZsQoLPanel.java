package com.zsqol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

public class ZsQoLPanel extends PluginPanel
{
	private static final int PANEL_WIDTH =
			PluginPanel.PANEL_WIDTH;

	private static final int ROW_WIDTH =
			PANEL_WIDTH - PluginPanel.BORDER_OFFSET * 2;

	private static final int CARD_HEIGHT = 68;
	private static final int HEADER_TEXT_WIDTH = ROW_WIDTH - 22;

	private static final Dimension ICON_SIZE =
			new Dimension(32, 28);

	private static final Dimension CLEAR_SIZE =
			new Dimension(26, 26);

	private static final Color CARD_BORDER =
			new Color(65, 65, 65);

	private static final Color POH_ORANGE =
			new Color(255, 167, 38);

	private static final Color NON_POH_BLUE =
			new Color(96, 200, 255);

	private static final Color FAIRY_BLUE =
			new Color(70, 200, 255);

	private static final Color NAME_COLOR =
			new Color(235, 235, 235);

	private static final Color NAME_DETAIL_COLOR =
			new Color(145, 205, 225);

	private final ZsQoLPlugin plugin;
	private final BindingStore bindingStore;
	private final ItemManager itemManager;

	@SuppressWarnings("unused")
	private final SpriteManager spriteManager;

	private final Map<TeleportTarget, JButton> bindingButtons =
			new EnumMap<>(TeleportTarget.class);

	private final Map<TeleportTarget, JLabel> iconLabels =
			new EnumMap<>(TeleportTarget.class);

	private final JLabel statusLabel =
			new JLabel("", SwingConstants.CENTER);

	private TeleportTarget capturingTarget;

	private int capturedPanelMouseButton =
			MouseEvent.NOBUTTON;

	private int swallowedPanelMouseClickButton =
			MouseEvent.NOBUTTON;

	private final MouseAdapter sideButtonCaptureListener =
			new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent event)
				{
					if (capturingTarget == null
							|| event.getButton() < 4)
					{
						return;
					}

					capturedPanelMouseButton =
							event.getButton();

					plugin.captureMouseBinding(
							capturingTarget,
							event.getButton()
					);

					event.consume();
				}

				@Override
				public void mouseReleased(MouseEvent event)
				{
					if (event.getButton()
							!= capturedPanelMouseButton)
					{
						return;
					}

					swallowedPanelMouseClickButton =
							capturedPanelMouseButton;

					capturedPanelMouseButton =
							MouseEvent.NOBUTTON;

					event.consume();
				}

				@Override
				public void mouseClicked(MouseEvent event)
				{
					if (event.getButton()
							!= swallowedPanelMouseClickButton)
					{
						return;
					}

					swallowedPanelMouseClickButton =
							MouseEvent.NOBUTTON;

					event.consume();
				}
			};

	public ZsQoLPanel(
			ZsQoLPlugin plugin,
			BindingStore bindingStore,
			SpriteManager spriteManager,
			ItemManager itemManager
	)
	{
		this.plugin = plugin;
		this.bindingStore = bindingStore;
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;

		buildPanel();
	}

	private void buildPanel()
	{
		removeAll();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel title = new JLabel(
				"Z's QoL",
				new ImageIcon(
						ImageUtil.resizeImage(
								ImageUtil.loadImageResource(
										ZsQoLPanel.class,
										"icon.png"
								),
								24,
								24
						)
				),
				SwingConstants.CENTER
		);

		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		title.setForeground(Color.WHITE);
		title.setFont(
				title.getFont().deriveFont(
						Font.BOLD,
						16f
				)
		);
		title.setIconTextGap(8);
		title.setPreferredSize(new Dimension(ROW_WIDTH, 30));
		title.setMinimumSize(new Dimension(ROW_WIDTH, 30));
		title.setMaximumSize(new Dimension(ROW_WIDTH, 30));

		JPanel explanation = createWrappedTextBox(
				"Expand a category and assign bindings. "
						+ "Bindings work only while the matching travel menu is open.",
				Color.LIGHT_GRAY,
				92
		);

		explanation.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel warning = createWrappedTextBox(
				"Disable Better Teleport Menu while using this plugin.",
				new Color(255, 183, 77),
				64
		);

		warning.setAlignmentX(Component.LEFT_ALIGNMENT);

		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		statusLabel.setForeground(Color.LIGHT_GRAY);
		statusLabel.setFont(
				statusLabel.getFont().deriveFont(Font.PLAIN, 13f)
		);
		statusLabel.setPreferredSize(
				new Dimension(ROW_WIDTH, 64)
		);
		statusLabel.setMinimumSize(
				new Dimension(ROW_WIDTH, 64)
		);
		statusLabel.setMaximumSize(
				new Dimension(ROW_WIDTH, 64)
		);
		statusLabel.setVisible(false);

		add(title);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(explanation);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(warning);
		add(Box.createRigidArea(new Dimension(0, 8)));
		add(statusLabel);
		add(Box.createRigidArea(new Dimension(0, 8)));
		add(createSectionDescription(
				"Teleport hotkeys",
				"Use while the matching travel menu is open."
		));
		add(Box.createRigidArea(new Dimension(0, 6)));

		Map<TeleportCategory, CollapsibleSection> sections =
				new EnumMap<>(TeleportCategory.class);

		for (TeleportCategory category : TeleportCategory.values())
		{
			sections.put(
					category,
					new CollapsibleSection(
							category.getDisplayName(),
							category.getSearchHint(),
							false
					)
			);
		}

		for (TeleportTarget target : TeleportTarget.values())
		{
			CollapsibleSection section =
					sections.get(target.getCategory());

			if (section == null)
			{
				continue;
			}

			section.addRow(
					createTargetCard(target),
					target.getSearchText()
			);
		}

		TeleportCategory[] categories =
				TeleportCategory.values();

		for (int index = 0;
		     index < categories.length;
		     index++)
		{
			add(sections.get(categories[index]));

			if (index < categories.length - 1)
			{
				add(
						Box.createRigidArea(
								new Dimension(0, 6)
						)
				);
			}
		}

		refreshAllBindings();
		loadAllIcons();
		installMouseCaptureListeners(this);

		revalidate();
		repaint();
	}

	private JPanel createWrappedTextBox(
			String text,
			Color color,
			int height
	)
	{
		JPanel box = new JPanel(new BorderLayout());
		box.setBackground(new Color(42, 42, 42));
		box.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(new Color(82, 82, 82)),
						BorderFactory.createEmptyBorder(7, 8, 7, 8)
				)
		);

		JTextArea textArea = createWrappedTextArea(text, color, 14f);
		box.add(textArea, BorderLayout.CENTER);
		box.setPreferredSize(new Dimension(ROW_WIDTH, height));
		box.setMinimumSize(new Dimension(ROW_WIDTH, height));
		box.setMaximumSize(new Dimension(ROW_WIDTH, height));
		return box;
	}

	private JPanel createSectionDescription(String title, String description)
	{
		JPanel box = new JPanel(new BorderLayout(0, 2));
		box.setBackground(new Color(42, 42, 42));
		box.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(new Color(74, 74, 74)),
						BorderFactory.createEmptyBorder(5, 7, 5, 7)
				)
		);
		box.setAlignmentX(Component.LEFT_ALIGNMENT);
		box.setPreferredSize(new Dimension(ROW_WIDTH, 70));
		box.setMinimumSize(new Dimension(ROW_WIDTH, 70));
		box.setMaximumSize(new Dimension(ROW_WIDTH, 70));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setFont(
				titleLabel.getFont().deriveFont(Font.BOLD, 16f)
		);

		JTextArea descriptionLabel = createWrappedTextArea(
				description,
				new Color(220, 220, 220),
				13f
		);
		descriptionLabel.setToolTipText(description);

		box.add(titleLabel, BorderLayout.NORTH);
		box.add(descriptionLabel, BorderLayout.CENTER);
		return box;
	}

	private JTextArea createWrappedTextArea(
			String text,
			Color color,
			float fontSize
	)
	{
		JTextArea textArea = new JTextArea(text == null ? "" : text);
		textArea.setEditable(false);
		textArea.setFocusable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setOpaque(false);
		textArea.setForeground(color);
		textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, fontSize));
		textArea.setMargin(new Insets(0, 0, 0, 0));
		return textArea;
	}

	private void setStatusText(String text)
	{
		String safeText = text == null ? "" : text;
		statusLabel.setVisible(!safeText.isEmpty());
		statusLabel.setText(
				"<html><div style='text-align:center;width:"
						+ HEADER_TEXT_WIDTH
						+ "px'>"
						+ escapeHtml(safeText)
						+ "</div></html>"
		);
		statusLabel.setToolTipText(safeText);
	}

	private static String escapeHtml(String value)
	{
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	private JPanel createTargetCard(TeleportTarget target)
	{
		JPanel card = new JPanel();

		card.setLayout(
				new BoxLayout(card, BoxLayout.Y_AXIS)
		);

		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		card.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(CARD_BORDER),
						BorderFactory.createEmptyBorder(4, 4, 4, 4)
				)
		);

		card.setAlignmentX(Component.LEFT_ALIGNMENT);

		card.setPreferredSize(
				new Dimension(ROW_WIDTH, CARD_HEIGHT)
		);

		card.setMinimumSize(
				new Dimension(ROW_WIDTH, CARD_HEIGHT)
		);

		card.setMaximumSize(
				new Dimension(ROW_WIDTH, CARD_HEIGHT)
		);

		JPanel topRow =
				new JPanel(new BorderLayout(6, 0));

		topRow.setOpaque(false);
		topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		topRow.setPreferredSize(
				new Dimension(ROW_WIDTH - 8, 30)
		);
		topRow.setMinimumSize(
				new Dimension(ROW_WIDTH - 8, 30)
		);
		topRow.setMaximumSize(
				new Dimension(ROW_WIDTH - 8, 30)
		);

		JLabel icon =
				createIconLabel(target);

		JPanel name =
				createNamePanel(target);

		topRow.add(icon, BorderLayout.WEST);
		topRow.add(name, BorderLayout.CENTER);

		JPanel bottomRow =
				new JPanel(new BorderLayout(4, 0));

		bottomRow.setOpaque(false);
		bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		bottomRow.setPreferredSize(
				new Dimension(ROW_WIDTH - 8, 26)
		);
		bottomRow.setMinimumSize(
				new Dimension(ROW_WIDTH - 8, 26)
		);
		bottomRow.setMaximumSize(
				new Dimension(ROW_WIDTH - 8, 26)
		);

		JButton bindingButton =
				createBindingButton(target);

		JButton clearButton =
				createClearButton(target);

		bottomRow.add(bindingButton, BorderLayout.CENTER);
		bottomRow.add(clearButton, BorderLayout.EAST);

		card.add(topRow);
		card.add(Box.createRigidArea(new Dimension(0, 4)));
		card.add(bottomRow);

		return card;
	}

	private JLabel createIconLabel(TeleportTarget target)
	{
		JLabel icon = new JLabel(
				"",
				SwingConstants.CENTER
		);

		icon.setPreferredSize(ICON_SIZE);
		icon.setMinimumSize(ICON_SIZE);
		icon.setMaximumSize(ICON_SIZE);
		icon.setToolTipText(target.getDisplayName());

		iconLabels.put(target, icon);

		applyStableIcon(target, icon);

		return icon;
	}

	private JPanel createNamePanel(TeleportTarget target)
	{
		JPanel namePanel =
				new JPanel(new BorderLayout(0, 0));

		namePanel.setOpaque(false);

		JLabel name =
				new JLabel(target.getDisplayName());

		name.setForeground(NAME_COLOR);
		name.setToolTipText(target.getDisplayName());

		name.setFont(
				name.getFont().deriveFont(
						Font.BOLD,
						13f
				)
		);

		namePanel.add(name, BorderLayout.CENTER);

		if (target.getCategory() == TeleportCategory.FAIRY_RING)
		{
			String code = target.getFairyRingCode();

			JLabel detail = new JLabel(
					"Fairy ring · " + code
			);

			detail.setForeground(NAME_DETAIL_COLOR);
			detail.setFont(
					detail.getFont().deriveFont(
							Font.PLAIN,
							10f
					)
			);
			detail.setToolTipText(
					code + " — " + target.getDisplayName()
			);

			namePanel.add(detail, BorderLayout.SOUTH);
			namePanel.setToolTipText(detail.getToolTipText());
		}
		else
		{
			namePanel.setToolTipText(target.getDisplayName());
		}

		return namePanel;
	}

	private JButton createBindingButton(TeleportTarget target)
	{
		JButton bindingButton =
				new JButton("Not set");

		bindingButton.setFocusable(false);
		bindingButton.setMargin(
				new Insets(2, 4, 2, 4)
		);

		bindingButton.setPreferredSize(
				new Dimension(150, 26)
		);

		bindingButton.setMinimumSize(
				new Dimension(80, 26)
		);

		bindingButton.setMaximumSize(
				new Dimension(Integer.MAX_VALUE, 26)
		);

		bindingButton.addActionListener(
				event -> plugin.beginBindingCapture(target)
		);

		bindingButtons.put(target, bindingButton);

		return bindingButton;
	}

	private JButton createClearButton(TeleportTarget target)
	{
		JButton clearButton =
				new JButton("×");

		clearButton.setFocusable(false);
		clearButton.setPreferredSize(CLEAR_SIZE);
		clearButton.setMinimumSize(CLEAR_SIZE);
		clearButton.setMaximumSize(CLEAR_SIZE);
		clearButton.setMargin(
				new Insets(1, 1, 1, 1)
		);

		clearButton.setToolTipText(
				"Remove only this binding"
		);

		clearButton.addActionListener(
				event -> plugin.clearBinding(target)
		);

		return clearButton;
	}

	private void applyStableIcon(
			TeleportTarget target,
			JLabel icon
	)
	{
		icon.setIcon(null);
		icon.setBorder(null);
		icon.setOpaque(false);

		TeleportCategory category =
				target.getCategory();

		if (category == TeleportCategory.POH_NEXUS)
		{
			/*
			 * Fallback only. loadAllIcons() replaces this with the real
			 * House teleport tablet item icon.
			 */
			icon.setText("⌂");
			icon.setForeground(POH_ORANGE);
			icon.setFont(
					icon.getFont().deriveFont(
							Font.BOLD,
							16f
					)
			);

			return;
		}

		if (category == TeleportCategory.FAIRY_RING)
		{
			icon.setText(target.getFairyRingCode());
			icon.setForeground(FAIRY_BLUE);
			icon.setFont(
					icon.getFont().deriveFont(
							Font.BOLD,
							9f
					)
			);
			icon.setBorder(
					BorderFactory.createCompoundBorder(
							BorderFactory.createLineBorder(
									new Color(45, 145, 175)
							),
							BorderFactory.createEmptyBorder(1, 1, 1, 1)
					)
			);

			return;
		}

		icon.setText(category.getIconText());
		icon.setForeground(NON_POH_BLUE);
		icon.setFont(
				icon.getFont().deriveFont(
						Font.BOLD,
						13f
				)
		);
	}

	private void loadAllIcons()
	{
		for (TeleportTarget target : iconLabels.keySet())
		{
			JLabel label =
					iconLabels.get(target);

			if (label == null)
			{
				continue;
			}

			applyStableIcon(target, label);

			if (target.getCategory() == TeleportCategory.POH_NEXUS)
			{
				loadFixedItemIcon(
						target,
						getPohNexusItemIconId(target)
				);

				continue;
			}

			if (target.getCategory() == TeleportCategory.FAIRY_RING)
			{
				continue;
			}

			loadItemIcon(target);
		}
	}

	private void loadFixedItemIcon(
			TeleportTarget target,
			int itemId
	)
	{
		JLabel label =
				iconLabels.get(target);

		if (label == null || itemId < 0)
		{
			return;
		}

		label.setText("");
		label.setIcon(null);
		label.setBorder(null);
		label.setOpaque(false);

		itemManager.getImage(itemId).addTo(label);
	}

	private void loadItemIcon(TeleportTarget target)
	{
		JLabel label =
				iconLabels.get(target);

		int itemId =
				target.getItemIconId();

		if (label == null || itemId < 0)
		{
			return;
		}

		label.setText("");
		label.setIcon(null);
		label.setBorder(null);
		label.setOpaque(false);

		itemManager.getImage(itemId).addTo(label);
	}

	private int getPohNexusItemIconId(TeleportTarget target)
	{
		switch (target)
		{
			case NEXUS_ANNAKARL:
				return ItemID.TABLET_ANNAKARL;
			case NEXUS_BARBARIAN_OUTPOST:
				return ItemID.LUNAR_TABLET_BARBARIAN_TELEPORT;
			case NEXUS_CAMELOT:
				return ItemID.POH_TABLET_CAMELOTTELEPORT;
			case NEXUS_CARRALLANGAR:
				return ItemID.TABLET_CARRALLANGAR;
			case NEXUS_CATHERBY:
				return ItemID.LUNAR_TABLET_CATHERBY_TELEPORT;
			case NEXUS_CIVITAS_ILLA_FORTIS:
				return ItemID.POH_TABLET_FORTISTELEPORT;
			case NEXUS_DAREEYAK:
				return ItemID.TABLET_DAREEYAK;
			case NEXUS_EAST_ARDOUGNE:
				return ItemID.POH_TABLET_ARDOUGNETELEPORT;
			case NEXUS_FALADOR:
				return ItemID.POH_TABLET_FALADORTELEPORT;
			case NEXUS_FISHING_GUILD:
				return ItemID.LUNAR_TABLET_FISHING_GUILD_TELEPORT;
			case NEXUS_GHORROCK:
				return ItemID.TABLET_GHORROCK;
			case NEXUS_ICE_PLATEAU:
				return ItemID.LUNAR_TABLET_ICE_PLATEAU_TELEPORT;
			case NEXUS_KHARYRLL:
				return ItemID.TABLET_KHARYLL;
			case NEXUS_KOUREND_CASTLE:
				return ItemID.POH_TABLET_KOURENDTELEPORT;
			case NEXUS_LASSAR:
				return ItemID.TABLET_LASSAR;
			case NEXUS_LUMBRIDGE:
				return ItemID.POH_TABLET_LUMBRIDGETELEPORT;
			case NEXUS_LUNAR_ISLE:
				return ItemID.LUNAR_TABLET_MOONCLAN_TELEPORT;
			case NEXUS_OURANIA_CAVE:
				return ItemID.LUNAR_TABLET_OURANIA_TELEPORT;
			case NEXUS_PADDEWWA:
				return ItemID.TABLET_PADDEWA;
			case NEXUS_PORT_KHAZARD:
				return ItemID.LUNAR_TABLET_KHAZARD_TELEPORT;
			case NEXUS_SENNTISTEN:
				return ItemID.TABLET_SENNTISTEN;
			case NEXUS_TELEPORT_TO_BOAT:
				return ItemID.POH_TABLET_TELEPORTMETOBOAT;
			case NEXUS_VARROCK:
				return ItemID.POH_TABLET_VARROCKTELEPORT;
			case NEXUS_WATCHTOWER:
				return ItemID.POH_TABLET_WATCHTOWERTELEPORT;
			case NEXUS_WATERBIRTH_ISLAND:
				return ItemID.LUNAR_TABLET_WATERBIRTH_TELEPORT;
			default:
				return ItemID.POH_TABLET_TELEPORTTOHOUSE;
		}
	}

	public void updateNexusIcon(
			TeleportTarget target,
			int spriteId
	)
	{
		/*
		 * ZsQoLPlugin still calls this while scanning Nexus rows.
		 * Ignore spriteId because the scanner can capture rune-cost icons
		 * or other interface decorations instead of destination icons.
		 *
		 * Use the stable item-icon map instead.
		 */
		if (target == null
				|| target.getCategory() != TeleportCategory.POH_NEXUS)
		{
			return;
		}

		runOnEdt(
				() ->
						loadFixedItemIcon(
								target,
								getPohNexusItemIconId(target)
						)
		);
	}

	public void reloadIcons()
	{
		runOnEdt(this::loadAllIcons);
	}

	private void installMouseCaptureListeners(Component component)
	{
		component.addMouseListener(sideButtonCaptureListener);

		if (!(component instanceof Container))
		{
			return;
		}

		for (Component child : ((Container) component).getComponents())
		{
			installMouseCaptureListeners(child);
		}
	}

	public void setCapturing(TeleportTarget target)
	{
		runOnEdt(
				() ->
				{
					capturingTarget = target;
					refreshAllBindings();

					setStatusText(
							"Set "
									+ target.getDisplayName()
									+ ". Press a key or Mouse 4/5. "
									+ "Esc cancels; Delete clears."
					);
				}
		);
	}

	public void bindingSaved(
			TeleportTarget target,
			TeleportBinding binding,
			TeleportTarget displaced
	)
	{
		runOnEdt(
				() ->
				{
					capturingTarget = null;
					refreshBinding(target);

					if (displaced != null)
					{
						refreshBinding(displaced);

						setStatusText(
								binding.getDisplayName()
										+ " moved from "
										+ displaced.getDisplayName()
										+ " to "
										+ target.getDisplayName()
						);
					}
					else
					{
						setStatusText(
								target.getDisplayName()
										+ " → "
										+ binding.getDisplayName()
						);
					}
				}
		);
	}

	public void bindingCleared(TeleportTarget target)
	{
		runOnEdt(
				() ->
				{
					capturingTarget = null;
					refreshBinding(target);

					setStatusText(
							target.getDisplayName()
									+ " binding removed."
					);
				}
		);
	}

	public void captureCancelled()
	{
		runOnEdt(
				() ->
				{
					TeleportTarget previous =
							capturingTarget;

					capturingTarget = null;

					if (previous != null)
					{
						refreshBinding(previous);
					}

					setStatusText(
							"Binding selection cancelled."
					);
				}
		);
	}

	public void refreshAllBindings()
	{
		runOnEdt(
				() ->
				{
					for (TeleportTarget target : bindingButtons.keySet())
					{
						refreshBinding(target);
					}
				}
		);
	}

	private void refreshBinding(TeleportTarget target)
	{
		JButton button =
				bindingButtons.get(target);

		if (button == null)
		{
			return;
		}

		if (target == capturingTarget)
		{
			button.setText("Press input…");
			button.setToolTipText(
					"Press a keyboard key or Mouse 4/5"
			);
			return;
		}

		TeleportBinding binding =
				bindingStore.getBinding(target);

		button.setText(binding.getDisplayName());

		button.setToolTipText(
				binding.isNone()
						? "Set a keyboard or side-mouse binding"
						: "Current binding: "
						+ binding.getDisplayName()
		);
	}

	private void runOnEdt(Runnable action)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			action.run();
		}
		else
		{
			SwingUtilities.invokeLater(action);
		}
	}
}
