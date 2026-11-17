package com.hospital.ui;

import com.hospital.model.Appointment;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.ui.components.Sidebar;
import com.hospital.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dashboard Panel displaying high-level hospital statistics,
 * today's scheduled appointments, and upcoming appointments.
 */
public class DashboardPanel extends JPanel {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final Consumer<String> navigationHandler;

    // Stat cards
    private StatCard totalPatientsCard;
    private StatCard totalDoctorsCard;
    private StatCard totalAppointmentsCard;
    private StatCard todayAppointmentsCard;

    // Tables
    private DefaultTableModel todayTableModel;
    private DefaultTableModel upcomingTableModel;
    private JTable todayTable;
    private JTable upcomingTable;

    public DashboardPanel(PatientService patientService, DoctorService doctorService,
                          AppointmentService appointmentService, Consumer<String> navigationHandler) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.navigationHandler = navigationHandler;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Hospital Dashboard");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Live overview of patients, doctors, and appointment schedules");
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);

        // Header Action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        JButton bookBtn = UITheme.createPrimaryButton("➕ Book Appointment");
        bookBtn.addActionListener(e -> {
            if (navigationHandler != null) {
                navigationHandler.accept(Sidebar.NAV_APPOINTMENTS);
            }
        });

        actionsPanel.add(refreshBtn);
        actionsPanel.add(bookBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        // Center Content: Stats + Tables
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // Stats Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        statsGrid.setPreferredSize(new Dimension(800, 115));

        totalPatientsCard = new StatCard("Total Patients", 0, "Active registered patients", UITheme.PRIMARY);
        totalDoctorsCard = new StatCard("Total Doctors", 0, "Registered medical staff", UITheme.SUCCESS);
        totalAppointmentsCard = new StatCard("Total Appointments", 0, "All-time bookings", UITheme.WARNING);
        todayAppointmentsCard = new StatCard("Today's Appointments", 0, "Scheduled for today", UITheme.INFO);

        statsGrid.add(totalPatientsCard);
        statsGrid.add(totalDoctorsCard);
        statsGrid.add(totalAppointmentsCard);
        statsGrid.add(todayAppointmentsCard);

        mainContent.add(statsGrid);
        mainContent.add(Box.createVerticalStrut(24));

        // Two Tables side-by-side or stacked in a split grid
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setOpaque(false);

        // Left Card: Today's Appointments
        JPanel todayCard = createTableCard("Today's Schedule", createTodayTablePanel());
        // Right Card: Upcoming Appointments
        JPanel upcomingCard = createTableCard("Upcoming Scheduled Appointments", createUpcomingTablePanel());

        tablesPanel.add(todayCard);
        tablesPanel.add(upcomingCard);

        mainContent.add(tablesPanel);

        add(headerPanel, BorderLayout.NORTH);
        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createTableCard(String title, JComponent tableComp) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SUBTITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(tableComp, BorderLayout.CENTER);
        return card;
    }

    private JScrollPane createTodayTablePanel() {
        String[] cols = {"Time", "Patient", "Doctor", "Status"};
        todayTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        todayTable = new JTable(todayTableModel);
        UITheme.styleTable(todayTable);
        applyStatusBadgeRenderer(todayTable, 3);

        JScrollPane scrollPane = new JScrollPane(todayTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JScrollPane createUpcomingTablePanel() {
        String[] cols = {"Date", "Time", "Patient", "Doctor", "Status"};
        upcomingTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        upcomingTable = new JTable(upcomingTableModel);
        UITheme.styleTable(upcomingTable);
        applyStatusBadgeRenderer(upcomingTable, 4);

        JScrollPane scrollPane = new JScrollPane(upcomingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private void applyStatusBadgeRenderer(JTable table, int statusColIndex) {
        table.getColumnModel().getColumn(statusColIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                String status = (value != null) ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UITheme.FONT_BOLD);

                if (!isSelected) {
                    if ("SCHEDULED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.WARNING);
                    } else if ("COMPLETED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.SUCCESS);
                    } else if ("CANCELLED".equalsIgnoreCase(status)) {
                        c.setForeground(UITheme.DANGER);
                    } else {
                        c.setForeground(UITheme.TEXT_SECONDARY);
                    }
                }
                return c;
            }
        });
    }

    /**
     * Refreshes all statistics and table data asynchronously using SwingWorker
     * to keep the UI perfectly responsive.
     */
    public void refreshData() {
        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                int totalPatients = patientService.getTotalPatientsCount();
                int totalDoctors = doctorService.getTotalDoctorsCount();
                int totalAppointments = appointmentService.getTotalAppointmentsCount();
                int todayAppointmentsCount = appointmentService.getTodayAppointmentsCount();

                List<Appointment> todayList = appointmentService.getTodayAppointments();
                List<Appointment> upcomingList = appointmentService.getUpcomingAppointments(10);

                return new DashboardData(totalPatients, totalDoctors, totalAppointments,
                        todayAppointmentsCount, todayList, upcomingList);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    totalPatientsCard.setValue(data.totalPatients);
                    totalDoctorsCard.setValue(data.totalDoctors);
                    totalAppointmentsCard.setValue(data.totalAppointments);
                    todayAppointmentsCard.setValue(data.todayAppointmentsCount);

                    // Update Today's table
                    todayTableModel.setRowCount(0);
                    for (Appointment a : data.todayList) {
                        todayTableModel.addRow(new Object[]{
                                a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "--:--",
                                a.getPatientName() != null ? a.getPatientName() : "ID #" + a.getPatientId(),
                                a.getDoctorName() != null ? "Dr. " + a.getDoctorName() : "ID #" + a.getDoctorId(),
                                a.getStatus()
                        });
                    }

                    // Update Upcoming table
                    upcomingTableModel.setRowCount(0);
                    for (Appointment a : data.upcomingList) {
                        upcomingTableModel.addRow(new Object[]{
                                a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "--",
                                a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "--:--",
                                a.getPatientName() != null ? a.getPatientName() : "ID #" + a.getPatientId(),
                                a.getDoctorName() != null ? "Dr. " + a.getDoctorName() : "ID #" + a.getDoctorId(),
                                a.getStatus()
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Dashboard refresh error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static class DashboardData {
        final int totalPatients;
        final int totalDoctors;
        final int totalAppointments;
        final int todayAppointmentsCount;
        final List<Appointment> todayList;
        final List<Appointment> upcomingList;

        DashboardData(int totalPatients, int totalDoctors, int totalAppointments,
                      int todayAppointmentsCount, List<Appointment> todayList, List<Appointment> upcomingList) {
            this.totalPatients = totalPatients;
            this.totalDoctors = totalDoctors;
            this.totalAppointments = totalAppointments;
            this.todayAppointmentsCount = todayAppointmentsCount;
            this.todayList = todayList;
            this.upcomingList = upcomingList;
        }
    }
}

