package com.hospital.ui;

import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import com.hospital.ui.components.Sidebar;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame hosting the sidebar navigation and
 * card layout container for all hospital management modules.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel contentCards;
    private final Sidebar sidebar;

    private final DashboardPanel dashboardPanel;
    private final PatientPanel patientPanel;
    private final DoctorPanel doctorPanel;
    private final AppointmentPanel appointmentPanel;

    public MainFrame() {
        super("HospitaCare - Hospital Management System");

        // Services initialization
        PatientService patientService = new PatientService();
        DoctorService doctorService = new DoctorService();
        AppointmentService appointmentService = new AppointmentService();

        // Frame setup
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setPreferredSize(new Dimension(1200, 760));
        setLayout(new BorderLayout());

        // Navigation Sidebar
        sidebar = new Sidebar(this::navigateTo);

        // Content Area with CardLayout
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(UITheme.BG_LIGHT);

        // Initialize panels
        dashboardPanel = new DashboardPanel(patientService, doctorService, appointmentService, this::navigateTo);
        patientPanel = new PatientPanel(patientService, this::notifyDataChanged);
        doctorPanel = new DoctorPanel(doctorService, this::notifyDataChanged);
        appointmentPanel = new AppointmentPanel(appointmentService, patientService, doctorService, this::notifyDataChanged);

        // Add panels to card layout
        contentCards.add(dashboardPanel, Sidebar.NAV_DASHBOARD);
        contentCards.add(patientPanel, Sidebar.NAV_PATIENTS);
        contentCards.add(doctorPanel, Sidebar.NAV_DOCTORS);
        contentCards.add(appointmentPanel, Sidebar.NAV_APPOINTMENTS);

        // Add components to frame
        add(sidebar, BorderLayout.WEST);
        add(contentCards, BorderLayout.CENTER);

        // Center on screen
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Refreshes the dashboard metrics and appointment tables.
     */
    public void notifyDataChanged() {
        if (dashboardPanel != null) {
            dashboardPanel.refreshData();
        }
    }

    /**
     * Navigates to the selected section, updates sidebar active state,
     * and refreshes the dashboard when navigated to.
     *
     * @param section Name of the section (e.g. "Dashboard", "Patients", etc.)
     */
    public void navigateTo(String section) {
        sidebar.setActive(section);
        cardLayout.show(contentCards, section);

        // Auto-refresh data when returning to relevant tabs
        if (Sidebar.NAV_DASHBOARD.equals(section)) {
            dashboardPanel.refreshData();
        } else if (Sidebar.NAV_PATIENTS.equals(section)) {
            patientPanel.loadPatients(null);
        } else if (Sidebar.NAV_DOCTORS.equals(section)) {
            doctorPanel.loadDoctors();
        } else if (Sidebar.NAV_APPOINTMENTS.equals(section)) {
            appointmentPanel.loadAppointments();
        }
    }
}
