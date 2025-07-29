// Jason He
// 2025
// AutoExtract.java
/// Version 2

package com.yourcompany.autoextract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;
import java.util.logging.*;

public class AutoExtract {
    private static final Logger LOGGER = Logger.getLogger(AutoExtract.class.getName());

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;
    private JTextField searchField;
    private File currentFile;
    private StructuredData currentData;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Couldn't set look-and-feel", e);
        }
        SwingUtilities.invokeLater(() -> new AutoExtract().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("AutoExtract Medical Services");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 850);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(createToolBar(), BorderLayout.NORTH);
        frame.add(createMainContent(), BorderLayout.CENTER);
        frame.add(createStatusBar(), BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        JButton browse = new JButton("📂 Browse");
        JButton load = new JButton("🔄 Load");
        JButton summary = new JButton("📝 Summary");
        JButton exportCsv = new JButton("📤 Export CSV");

        browse.addActionListener(e -> onBrowse());
        load.addActionListener(e -> onLoad());
        summary.addActionListener(e -> onGenerateSummary());
        exportCsv.addActionListener(e -> onExportCSV());

        toolbar.add(browse);
        toolbar.add(load);
        toolbar.add(summary);
        toolbar.add(exportCsv);
        return toolbar;
    }

    private JPanel createMainContent() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel searchPanel = new JPanel(new BorderLayout());
        JLabel searchLabel = new JLabel("🔎 Search: ");
        searchField = new JTextField();
        searchField.setToolTipText("Filter fields...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"Hide", "Field", "Value"};
        tableModel = new DefaultTableModel(cols, 0) {
            public Class<?> getColumnClass(int col) {
                return (col == 0) ? Boolean.class : String.class;
            }
            public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(900);

        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Ready.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        return statusPanel;
    }

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            statusLabel.setText(" Selected: " + currentFile.getName());
        }
    }

    private void onLoad() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(frame, "Please browse and select a file first.");
            return;
        }

        try {
            currentData = extractFromFile(currentFile.getAbsolutePath());
            refreshTable(currentData);
            statusLabel.setText(" Loaded data from: " + currentFile.getName());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load file", ex);
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onGenerateSummary() {
        if (currentData == null) {
            JOptionPane.showMessageDialog(frame, "Load a file first.");
            return;
        }
        JTextArea area = new JTextArea(currentData.generateSummary());
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        JOptionPane.showMessageDialog(frame, scrollPane, "Structured Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTable(StructuredData data) {
        tableModel.setRowCount(0);
        Map<String, String> map = data.toMap();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            tableModel.addRow(new Object[]{false, entry.getKey(), entry.getValue()});
        }
    }

    private void applyFilter() {
        String term = searchField.getText().trim().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        if (term.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(term), 1, 2));
        }
        table.setRowSorter(sorter);
    }

    private void onExportCSV() {
        if (currentData == null) {
            JOptionPane.showMessageDialog(frame, "Nothing to export.");
            return;
        }

        JFileChooser saver = new JFileChooser();
        saver.setSelectedFile(new File("structured_data.csv"));
        if (saver.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = saver.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("Field,Value");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if ((Boolean) tableModel.getValueAt(i, 0)) continue;
                    String field = (String) tableModel.getValueAt(i, 1);
                    String value = (String) tableModel.getValueAt(i, 2);
                    pw.println("\"" + field + "\",\"" + value.replace("\"", "\"\"") + "\"");
                }
                statusLabel.setText(" Exported to: " + file.getName());
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Export failed", ex);
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private StructuredData extractFromFile(String filePath) throws IOException {
        String text = filePath.endsWith(".pdf") ? extractTextFromPDF(filePath) : extractTextFromTXT(filePath);
        StructuredData d = new StructuredData();
        d.patientName = find(text, "(?i)Name\\s*[:\\-]?\\s*(.+)");
        d.age = find(text, "(?i)Age\\s*[:\\-]?\\s*(\\d{1,3})");
        d.gender = find(text, "(?i)(?:Sex|Gender)\\s*[:\\-]?\\s*(Male|Female|Other)");
        d.dob = find(text, "(?i)DOB\\s*[:\\-]?\\s*(\\d{1,2}/\\d{1,2}/\\d{2,4})");
        d.mrn = find(text, "(?i)MRN\\s*[:\\-]?\\s*([A-Za-z0-9]+)");
        d.chiefComplaint = findBlock(text, "(?i)Chief Complaint", 300);
        d.diagnosis = findBlock(text, "(?i)(Impression|Diagnosis)", 300);
        d.problemList = findBlock(text, "(?i)Problem List", 400);
        d.assessmentPlan = findBlock(text, "(?i)(Assessment & Plan|Assessment and Plan)", 500);
        d.medications = findBlock(text, "(?i)Medications", 300);
        d.allergies = findBlock(text, "(?i)Allergies", 300);
        d.pastMedicalHistory = findBlock(text, "(?i)(Past Medical History|PMH)", 400);
        d.familyHistory = findBlock(text, "(?i)Family History", 400);
        d.socialHistory = findBlock(text, "(?i)Social History", 400);
        d.physicalExam = findBlock(text, "(?i)(Physical Exam|Physical Examination)", 500);
        d.bp = find(text, "(?i)(BP|Blood Pressure)\\s*[:\\-]?\\s*(\\d{2,3}/\\d{2,3})");
        d.hr = find(text, "(?i)(HR|Heart Rate)\\s*[:\\-]?\\s*(\\d{2,3})");
        d.temp = find(text, "(?i)(Temp|Temperature)\\s*[:\\-]?\\s*(\\d{2,3}(\\.\\d)?)(°?F)?");
        d.rr = find(text, "(?i)(RR|Respiratory Rate)\\s*[:\\-]?\\s*(\\d{2})");
        d.vitals = findBlock(text, "(?i)(Vitals|Vital Signs)", 400);
        return d;
    }

    private String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument doc = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractTextFromTXT(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private String find(String text, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        return m.find() ? m.group(m.groupCount()) : "";
    }

    private String findBlock(String text, String headerPattern, int maxLen) {
        Pattern p = Pattern.compile(headerPattern + "[:\\n\\r]+(.{1," + maxLen + "})", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim().replaceAll("[\\r\\n]+", " ") : "";
    }

    // Container for structured fields
    static class StructuredData {
        String patientName, age, gender, dob, mrn;
        String chiefComplaint, diagnosis, problemList, assessmentPlan;
        String medications, allergies, pastMedicalHistory, familyHistory;
        String socialHistory, vitals, bp, hr, temp, rr, physicalExam;

        Map<String, String> toMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("Patient Name", patientName);
            map.put("Age", age);
            map.put("Gender", gender);
            map.put("DOB", dob);
            map.put("MRN", mrn);
            map.put("Chief Complaint", chiefComplaint);
            map.put("Diagnosis", diagnosis);
            map.put("Problem List", problemList);
            map.put("Assessment & Plan", assessmentPlan);
            map.put("Medications", medications);
            map.put("Allergies", allergies);
            map.put("Past Medical History", pastMedicalHistory);
            map.put("Family History", familyHistory);
            map.put("Social History", socialHistory);
            map.put("Vitals", vitals);
            map.put("BP", bp);
            map.put("HR", hr);
            map.put("Temp", temp);
            map.put("RR", rr);
            map.put("Physical Exam", physicalExam);
            return map;
        }

        void setField(String key, String value) {
            switch (key) {
                case "Patient Name": patientName = value; break;
                case "Age": age = value; break;
                case "Gender": gender = value; break;
                case "DOB": dob = value; break;
                case "MRN": mrn = value; break;
                case "Chief Complaint": chiefComplaint = value; break;
                case "Diagnosis": diagnosis = value; break;
                case "Problem List": problemList = value; break;
                case "Assessment & Plan": assessmentPlan = value; break;
                case "Medications": medications = value; break;
                case "Allergies": allergies = value; break;
                case "Past Medical History": pastMedicalHistory = value; break;
                case "Family History": familyHistory = value; break;
                case "Social History": socialHistory = value; break;
                case "Vitals": vitals = value; break;
                case "BP": bp = value; break;
                case "HR": hr = value; break;
                case "Temp": temp = value; break;
                case "RR": rr = value; break;
                case "Physical Exam": physicalExam = value; break;
            }
        }

        String generateSummary() {
            StringBuilder sb = new StringBuilder();
            toMap().forEach((k, v) -> {
                if (v != null && !v.isEmpty())
                    sb.append(k).append(": ").append(v).append("\n");
            });
            return sb.toString();
        }
    }
}
