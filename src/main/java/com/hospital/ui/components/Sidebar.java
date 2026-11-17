package com.hospital.ui.components;

import com.hospital.ui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern dark-themed navigation sidebar for the Hospital Management System.
 */
public class Sidebar extends JPanel {

    public static final String NAV_DASHBOARD = "Dashboard";
    public static final String NAV_PATIENTS = "Patients";
    public static final String NAV_DOCTORS = "Doctors";
    public static final String NAV_APPOINTMENTS = "Appointments";

    private final Consumer<String> onNavigate;
    private final List<NavButton> navButtons = new ArrayList<>();
    private String activeSection = NAV_DASHBOARD;

    public Sidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;

        setPreferredSize(new Dimension(240, 700));
        setBackground(UITheme.SIDEBAR_BG);
        setLayout(new BorderLayout());

        // Header / Logo area
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(28, 24, 28, 24));

        JLabel logoLabel = new JLabel("🏥 HospitaCare");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(logoLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);

        // Navigation Items Panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        addNavItem(menuPanel, "📊", NAV_DASHBOARD);
        addNavItem(menuPanel, "👥", NAV_PATIENTS);
        addNavItem(menuPanel, "👨‍⚕️", NAV_DOCTORS);
        addNavItem(menuPanel, "📅", NAV_APPOINTMENTS);

        // Footer / Exit Panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(16, 14, 20, 14));

        NavButton exitBtn = new NavButton("🚪", "Exit", () -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit the application?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        footerPanel.add(exitBtn, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        // Set initial active state
        setActive(NAV_DASHBOARD);
    }

    private void addNavItem(JPanel container, String icon, String label) {
        NavButton btn = new NavButton(icon, label, () -> {
            setActive(label);
            if (onNavigate != null) {
                onNavigate.accept(label);
            }
        });
        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    public void setActive(String section) {
        this.activeSection = section;
        for (NavButton btn : navButtons) {
            btn.setActive(btn.getLabel().equals(section));
        }
        repaint();
    }

    public String getActiveSection() {
        return activeSection;
    }

    /**
     * Inner class representing an individual styled navigation button.
     */
    private static class NavButton extends JPanel {
        private final String label;
        private final Runnable onClick;
        private boolean isActive = false;
        private boolean isHovered = false;

        private final JLabel iconLabel;
        private final JLabel textLabel;

        public NavButton(String icon, String label, Runnable onClick) {
            this.label = label;
            this.onClick = onClick;

            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            iconLabel.setForeground(UITheme.SIDEBAR_TEXT);

            textLabel = new JLabel(label);
            textLabel.setFont(UITheme.FONT_BOLD);
            textLabel.setForeground(UITheme.SIDEBAR_TEXT);

            add(iconLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) {
                        onClick.run();
                    }
                }
            });
        }

        public String getLabel() {
            return label;
        }

        public void setActive(boolean active) {
            this.isActive = active;
            textLabel.setForeground(active ? Color.WHITE : UITheme.SIDEBAR_TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(UITheme.SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (isHovered) {
                g2.setColor(UITheme.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            super.paintComponent(g);
            g2.dispose();
        }
    }
}

