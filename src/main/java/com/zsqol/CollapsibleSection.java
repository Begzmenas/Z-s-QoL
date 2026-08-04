package com.zsqol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.Text;

public final class CollapsibleSection extends JPanel
{
	private static final int SECTION_WIDTH =
			PluginPanel.PANEL_WIDTH
					- PluginPanel.BORDER_OFFSET * 2;

	private static final int HEADER_HEIGHT = 38;
	private static final int SEARCH_HEIGHT = 27;

	private final JButton headerButton;
	private final FixedWidthPanel bodyPanel;
	private final FixedWidthPanel rowsPanel;
	private final PlaceholderTextField searchField;

	private final Map<JComponent, String> searchableRows =
			new LinkedHashMap<>();

	private final String title;
	private boolean expanded;

	public CollapsibleSection(
			String title,
			String searchHint,
			boolean expanded
	)
	{
		this.title = title == null ? "" : title;
		this.expanded = expanded;

		setLayout(new BorderLayout(0, 3));
		setOpaque(false);
		setAlignmentX(Component.LEFT_ALIGNMENT);

		headerButton = new JButton();
		headerButton.setHorizontalAlignment(SwingConstants.LEFT);
		headerButton.setFocusPainted(false);
		headerButton.setFocusable(false);
		headerButton.setForeground(Color.WHITE);
		headerButton.setBackground(new Color(58, 58, 58));
		headerButton.setFont(
				headerButton.getFont().deriveFont(
						Font.BOLD,
						16f
				)
		);

		headerButton.setMargin(
				new Insets(5, 7, 5, 7)
		);

		headerButton.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(new Color(105, 105, 105)),
						BorderFactory.createEmptyBorder(1, 2, 1, 2)
				)
		);

		headerButton.setPreferredSize(
				new Dimension(SECTION_WIDTH, HEADER_HEIGHT)
		);

		headerButton.setMinimumSize(
				new Dimension(SECTION_WIDTH, HEADER_HEIGHT)
		);

		headerButton.setMaximumSize(
				new Dimension(SECTION_WIDTH, HEADER_HEIGHT)
		);

		headerButton.addActionListener(
				event -> setExpanded(!this.expanded)
		);

		bodyPanel = new FixedWidthPanel();
		bodyPanel.setLayout(
				new BoxLayout(bodyPanel, BoxLayout.Y_AXIS)
		);
		bodyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		searchField = new PlaceholderTextField(
				searchHint == null ? "Search..." : searchHint
		);

		searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchField.setPreferredSize(
				new Dimension(SECTION_WIDTH, SEARCH_HEIGHT)
		);
		searchField.setMinimumSize(
				new Dimension(SECTION_WIDTH, SEARCH_HEIGHT)
		);
		searchField.setMaximumSize(
				new Dimension(SECTION_WIDTH, SEARCH_HEIGHT)
		);
		searchField.setToolTipText(searchHint == null ? "Search" : searchHint);

		searchField.getDocument().addDocumentListener(
				new DocumentListener()
				{
					@Override
					public void insertUpdate(DocumentEvent event)
					{
						filterRows();
					}

					@Override
					public void removeUpdate(DocumentEvent event)
					{
						filterRows();
					}

					@Override
					public void changedUpdate(DocumentEvent event)
					{
						filterRows();
					}
				}
		);

		rowsPanel = new FixedWidthPanel();
		rowsPanel.setLayout(
				new BoxLayout(rowsPanel, BoxLayout.Y_AXIS)
		);
		rowsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		bodyPanel.add(searchField);
		bodyPanel.add(Box.createRigidArea(new Dimension(0, 4)));
		bodyPanel.add(rowsPanel);

		add(headerButton, BorderLayout.NORTH);
		add(bodyPanel, BorderLayout.CENTER);

		setExpanded(expanded);
	}

	public CollapsibleSection(String title, boolean expanded)
	{
		this(title, "Search...", expanded);
	}

	public CollapsibleSection(String title)
	{
		this(title, "Search...", false);
	}

	public void addRow(
			JComponent row,
			String searchableText
	)
	{
		if (row == null)
		{
			return;
		}

		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		Dimension rowPreferred =
				row.getPreferredSize();

		int rowHeight =
				rowPreferred == null || rowPreferred.height <= 0
						? 64
						: rowPreferred.height;

		row.setPreferredSize(
				new Dimension(SECTION_WIDTH, rowHeight)
		);
		row.setMinimumSize(
				new Dimension(SECTION_WIDTH, rowHeight)
		);
		row.setMaximumSize(
				new Dimension(SECTION_WIDTH, rowHeight)
		);

		FixedWidthPanel holder =
				new FixedWidthPanel(new BorderLayout());

		holder.setAlignmentX(Component.LEFT_ALIGNMENT);
		holder.setBorder(
				BorderFactory.createEmptyBorder(0, 0, 5, 0)
		);

		holder.setPreferredSize(
				new Dimension(SECTION_WIDTH, rowHeight + 5)
		);
		holder.setMinimumSize(
				new Dimension(SECTION_WIDTH, rowHeight + 5)
		);
		holder.setMaximumSize(
				new Dimension(SECTION_WIDTH, rowHeight + 5)
		);

		holder.add(row, BorderLayout.NORTH);

		searchableRows.put(holder, normalize(searchableText));
		rowsPanel.add(holder);

		filterRows();
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
		bodyPanel.setVisible(expanded);

		headerButton.setText(
				(expanded ? "▼ " : "▶ ") + title
		);

		refresh();
	}

	public void toggle()
	{
		setExpanded(!expanded);
	}

	public void refresh()
	{
		Runnable action =
				() ->
				{
					rowsPanel.revalidate();
					rowsPanel.repaint();

					bodyPanel.revalidate();
					bodyPanel.repaint();

					revalidate();
					repaint();

					Container parent = getParent();

					while (parent != null)
					{
						if (parent instanceof JComponent)
						{
							((JComponent) parent).revalidate();
							((JComponent) parent).repaint();
						}

						parent = parent.getParent();
					}
				};

		if (SwingUtilities.isEventDispatchThread())
		{
			action.run();
		}
		else
		{
			SwingUtilities.invokeLater(action);
		}
	}

	private void filterRows()
	{
		String query = normalize(searchField.getText());

		for (Map.Entry<JComponent, String> entry
				: searchableRows.entrySet())
		{
			boolean visible =
					query.isEmpty()
							|| entry.getValue().contains(query);

			entry.getKey().setVisible(visible);
		}

		refresh();
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

	@Override
	public Dimension getPreferredSize()
	{
		Dimension preferred = super.getPreferredSize();

		return new Dimension(
				SECTION_WIDTH,
				preferred.height
		);
	}

	@Override
	public Dimension getMinimumSize()
	{
		Dimension preferred = getPreferredSize();

		return new Dimension(
				SECTION_WIDTH,
				preferred.height
		);
	}

	@Override
	public Dimension getMaximumSize()
	{
		Dimension preferred = getPreferredSize();

		return new Dimension(
				SECTION_WIDTH,
				preferred.height
		);
	}

	private static final class FixedWidthPanel extends JPanel
	{
		private FixedWidthPanel()
		{
			super();
			setOpaque(false);
		}

		private FixedWidthPanel(java.awt.LayoutManager layout)
		{
			super(layout);
			setOpaque(false);
		}

		@Override
		public Dimension getPreferredSize()
		{
			Dimension preferred =
					super.getPreferredSize();

			return new Dimension(
					SECTION_WIDTH,
					preferred.height
			);
		}

		@Override
		public Dimension getMinimumSize()
		{
			Dimension preferred =
					getPreferredSize();

			return new Dimension(
					SECTION_WIDTH,
					preferred.height
			);
		}

		@Override
		public Dimension getMaximumSize()
		{
			Dimension preferred =
					getPreferredSize();

			return new Dimension(
					SECTION_WIDTH,
					preferred.height
			);
		}
	}

	private static final class PlaceholderTextField extends JTextField
	{
		private final String placeholder;

		private PlaceholderTextField(String placeholder)
		{
			this.placeholder =
					placeholder == null ? "" : placeholder;
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			super.paintComponent(graphics);

			if (!getText().isEmpty()
					|| isFocusOwner()
					|| placeholder.isEmpty())
			{
				return;
			}

			Graphics copy =
					graphics.create();

			try
			{
				copy.setColor(new Color(145, 145, 145));

				FontMetrics metrics =
						copy.getFontMetrics();

				Insets insets =
						getInsets();

				int x =
						insets.left + 2;

				int y =
						(getHeight() - metrics.getHeight()) / 2
								+ metrics.getAscent();

				copy.drawString(placeholder, x, y);
			}
			finally
			{
				copy.dispose();
			}
		}
	}
}
