package com.hospital.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Central theme definition providing consistent colors, fonts,
 * and styling helpers across all Swing screens.
 */
public final class UITheme {

    // Primary Colors
    public static final Color PRIMARY = new Color(37, 99, 235);        // #2563EB Royal Blue
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);   // #1D4ED8
    public static final Color PRIMARY_LIGHT = new Color(239, 246, 255);// #EFF6FF

    // Sidebar & Dark Theme
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);      // #0F172A Deep Navy
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);   // #1E293B
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);  // Active accent
    public static final Color SIDEBAR_TEXT = new Color(226, 232, 240);  // Light slate text

    // Background & Surfaces
    public static final Color BG_LIGHT = new Color(248, 250, 252);     // #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // #E2E8F0

    // Typography Colors
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // #0F172A
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);// #64748B
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // #94A3B8

    // Status Colors
    public static final Color SUCCESS = new Color(16, 185, 129);       // #10B981 Emerald
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);
    public static final Color WARNING = new Color(245, 158, 11);       // #F59E0B Amber
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);         // #EF4444 Rose Red
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);
    public static final Color INFO = new Color(6, 182, 212);           // #06B6D4 Cyan

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD, 28);

    private UITheme() {}

    /**
     * Styles a JTable with modern hospital styling:
     * 34px row height, custom navy header, striped rows, and selection colors.
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(FONT_BODY);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(219, 234, 254)); // #DBEAFE
        table.setSelectionForeground(TEXT_PRIMARY);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(new Color(241, 245, 249)); // #F1F5F9
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        // Custom cell renderer with alternating row striping and padding
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    /**
     * Creates a styled primary button (royal blue).
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    /**
     * Creates a styled secondary/outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    /**
     * Creates a styled danger button (red) for deletion/cancellation.
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setBackground(DANGER);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DANGER),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    /**
     * Creates a standardized text field with subtle border and inner padding.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }
}

