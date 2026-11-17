package com.hospital.ui;

import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Doctor Management Panel:
 * Allows searching, listing, adding, updating, and safely deleting medical doctors.
 */
public class DoctorPanel extends JPanel {

    private final DoctorService doctorService;
    private final Runnable onDataChanged;

    private DefaultTableModel tableModel;
    private JTable doctorTable;
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private List<Doctor> currentDoctors = new ArrayList<>();

    public DoctorPanel(DoctorService doctorService, Runnable onDataChanged) {
        this.doctorService = doctorService;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        initUI();
        loadDoctors();
    }

    private void initUI() {
        // --- Top Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Doctor Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage hospital physicians, clinical specializations, and contacts");
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleBlock.add(titleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitleLabel);

        JButton addDoctorBtn = UITheme.createPrimaryButton("➕ Add Doctor");
        addDoctorBtn.addActionListener(e -> showDoctorDialog(null));

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(addDoctorBtn, BorderLayout.EAST);

        // --- Controls Panel (Search & Actions) ---
        JPanel controlsCard = new JPanel(new BorderLayout(16, 0));
        controlsCard.setBackground(UITheme.CARD_BG);
        controlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Search controls
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchField = UITheme.createTextField(20);

        searchTypeCombo = new JComboBox<>(new String[]{"Name", "Specialization"});
        searchTypeCombo.setFont(UITheme.FONT_BODY);

        JButton searchBtn = UITheme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> loadDoctors());
        searchField.addActionListener(e -> loadDoctors());

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadDoctors();
        });

        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        searchPanel.add(searchTypeCombo);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);

        // Action Buttons for selected row
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActionsPanel.setOpaque(false);

        JButton editBtn = UITheme.createSecondaryButton("✏️ Edit Selected");
        editBtn.addActionListener(e -> onEditSelected());

        JButton deleteBtn = UITheme.createDangerButton("🗑️ Delete Selected");
        deleteBtn.addActionListener(e -> onDeleteSelected());

        tableActionsPanel.add(editBtn);
        tableActionsPanel.add(deleteBtn);

        controlsCard.add(searchPanel, BorderLayout.WEST);
        controlsCard.add(tableActionsPanel, BorderLayout.EAST);

        // --- Table Panel ---
        String[] cols = {"Doctor ID", "Doctor Name", "Specialization", "Phone", "Email"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        doctorTable = new JTable(tableModel);
        UITheme.styleTable(doctorTable);

        doctorTable.getColumnModel().getColumn(0).setMaxWidth(100);
        doctorTable.getColumnModel().getColumn(2).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Combine into main layout
        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setOpaque(false);
        centerContainer.add(controlsCard, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Loads doctors based on current search parameters via SwingWorker.
     */
    public void loadDoctors() {
        String query = searchField.getText();
        String searchType = (String) searchTypeCombo.getSelectedItem();

        SwingWorker<List<Doctor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Doctor> doInBackground() throws Exception {
                return doctorService.searchDoctors(query, searchType);
            }

            @Override
            protected void done() {
                try {
                    currentDoctors = get();
                    tableModel.setRowCount(0);
                    for (Doctor d : currentDoctors) {
                        tableModel.addRow(new Object[]{
                                d.getDoctorId(),
                                "Dr. " + d.getName(),
                                d.getSpecialization(),
                                d.getPhone() != null ? d.getPhone() : "",
                                d.getEmail() != null ? d.getEmail() : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DoctorPanel.this,
                            "Failed to load doctors: " + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onEditSelected() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a doctor from the table to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Doctor selected = currentDoctors.get(selectedRow);
        showDoctorDialog(selected);
    }

    private void onDeleteSelected() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a doctor from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Doctor selected = currentDoctors.get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete doctor:\n" +
                "Dr. " + selected.getName() + " (" + selected.getSpecialization() + ")?\n\n" +
                "This action cannot be undone.",
                "Confirm Doctor Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    doctorService.deleteDoctor(selected.getDoctorId());
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(DoctorPanel.this,
                                "Doctor deleted successfully.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadDoctors();
                        if (onDataChanged != null) {
                            onDataChanged.run();
                        }
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(DoctorPanel.this,
                                msg, "Cannot Delete Doctor", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Shows modal Add or Edit Doctor dialog.
     */
    private void showDoctorDialog(Doctor existingDoctor) {
        boolean isEdit = (existingDoctor != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Doctor Details" : "Add New Doctor", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(460, 410);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField nameField = UITheme.createTextField(20);
        JComboBox<String> specCombo = new JComboBox<>(new String[]{
                "General Medicine", "Cardiology", "Neurology", "Pediatrics",
                "Orthopedics", "Dermatology", "Gastroenterology", "Ophthalmology",
                "ENT", "Psychiatry", "Radiology", "General Surgery"
        });
        specCombo.setEditable(true); // Allows custom input
        JTextField phoneField = UITheme.createTextField(20);
        JTextField emailField = UITheme.createTextField(20);

        if (isEdit) {
            nameField.setText(existingDoctor.getName());
            specCombo.setSelectedItem(existingDoctor.getSpecialization());
            phoneField.setText(existingDoctor.getPhone());
            emailField.setText(existingDoctor.getEmail());
        }

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Doctor Name *:", nameField);
        addFormRow(formPanel, gbc, row++, "Specialization *:", specCombo);
        addFormRow(formPanel, gbc, row++, "Phone Number:", phoneField);
        addFormRow(formPanel, gbc, row++, "Email Address:", emailField);

        // Dialog Footer Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = UITheme.createPrimaryButton(isEdit ? "Update Doctor" : "Save Doctor");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            Object selectedSpec = specCombo.getSelectedItem();
            String spec = (selectedSpec != null) ? selectedSpec.toString().trim() : "";
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            try {
                if (isEdit) {
                    existingDoctor.setName(name);
                    existingDoctor.setSpecialization(spec);
                    existingDoctor.setPhone(phone);
                    existingDoctor.setEmail(email);
                    doctorService.updateDoctor(existingDoctor);
                } else {
                    Doctor newDoc = new Doctor(name, spec, phone, email);
                    doctorService.addDoctor(newDoc);
                }

                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        isEdit ? "Doctor updated successfully." : "Doctor added successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctors();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage(),
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }
}

