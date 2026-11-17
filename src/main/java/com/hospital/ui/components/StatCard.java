package com.hospital.ui.components;

import com.hospital.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable Statistic Card component for displaying dashboard metrics.
 */
public class StatCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;

    /**
     * Constructs a StatCard.
     *
     * @param title card title (e.g. "Total Patients")
     * @param initialValue initial metric count
     * @param subtitle descriptive tag (e.g. "Registered records")
     * @param accentColor accent indicator color
     */
    public StatCard(String title, int initialValue, String subtitle, Color accentColor) {
        this.accentColor = accentColor;

        setLayout(new BorderLayout());
        setBackground(UITheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Top Header panel: Title + Accent Pill
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);

        // Small colored indicator square
        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        indicator.setPreferredSize(new Dimension(10, 10));
        indicator.setOpaque(false);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(indicator, BorderLayout.EAST);

        // Center Value panel
        valueLabel = new JLabel(String.valueOf(initialValue));
        valueLabel.setFont(UITheme.FONT_STAT_VALUE);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);

        // Bottom Subtitle panel
        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UITheme.FONT_SMALL);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(valueLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(subtitleLabel);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Dynamically updates the displayed number.
     *
     * @param value new count
     */
    public void setValue(int value) {
        valueLabel.setText(String.format("%,d", value));
    }

    /**
     * Sets custom text value.
     */
    public void setValue(String value) {
        valueLabel.setText(value);
    }
}

