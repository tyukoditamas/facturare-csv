package org.app.ui;

import org.app.model.BusinessMode;
import org.app.service.InvoiceGenerationService;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InvoiceGeneratorFrame extends JFrame {
    private final JRadioButton fanRadio = new JRadioButton("FAN", true);
    private final JRadioButton assetRadio = new JRadioButton("ASSET");
    private final JButton browseButton = new JButton("Browse Excel...");
    private final JButton changeOutputButton = new JButton("Change CSV...");
    private final JTextArea consoleArea = new JTextArea(12, 80);
    private final JTextField outputPathField = new JTextField();
    private Path outputCsvPath;

    private final InvoiceGenerationService generationService = new InvoiceGenerationService();

    public InvoiceGeneratorFrame() {
        super("Program Facturare - Trendyol");
        initUi();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.add(new JLabel("Mode:"));
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(fanRadio);
        modeGroup.add(assetRadio);
        controlsPanel.add(fanRadio);
        controlsPanel.add(assetRadio);
        controlsPanel.add(browseButton);
        add(controlsPanel, BorderLayout.NORTH);

        outputPathField.setEditable(false);
        outputPathField.setColumns(35);
        JPanel outputPanel = new JPanel(new BorderLayout(6, 6));
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output CSV"));
        outputPanel.add(outputPathField, BorderLayout.CENTER);
        outputPanel.add(changeOutputButton, BorderLayout.EAST);
        add(outputPanel, BorderLayout.CENTER);

        consoleArea.setEditable(false);
        consoleArea.setLineWrap(true);
        consoleArea.setWrapStyleWord(true);
        JScrollPane consoleScrollPane = new JScrollPane(consoleArea);
        consoleScrollPane.setPreferredSize(new Dimension(700, 250));
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console"));
        add(consoleScrollPane, BorderLayout.SOUTH);

        browseButton.addActionListener(e -> onBrowse());
        changeOutputButton.addActionListener(e -> onChangeOutput());

        setSize(720, 420);
        setLocationRelativeTo(null);
    }

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Excel data file");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xls", "xlsx"));
        int selection = chooser.showOpenDialog(this);
        if (selection == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Path derivedOutput = deriveOutputPath(file);
            updateOutputPath(derivedOutput, true);
            startProcessing(file);
        }
    }

    private void startProcessing(File file) {
        BusinessMode mode = fanRadio.isSelected() ? BusinessMode.FAN : BusinessMode.ASSET;
        appendLog(String.format("Selected mode: %s", mode.getDisplayName()));
        appendLog("Reading file: " + file.getAbsolutePath());
        appendLog("Target CSV: " + outputCsvPath.toAbsolutePath());
        setControlsEnabled(false);

        SwingWorker<Path, String> worker = new SwingWorker<Path, String>() {
            @Override
            protected Path doInBackground() {
                try {
                    return generationService.generate(file, mode, outputCsvPath, this::publish);
                } catch (Exception ex) {
                    publish("ERROR: " + ex.getMessage());
                    return null;
                }
            }

            @Override
            protected void process(List<String> chunks) {
                chunks.forEach(InvoiceGeneratorFrame.this::appendLog);
            }

            @Override
            protected void done() {
                setControlsEnabled(true);
                try {
                    Path csvPath = get();
                    if (csvPath != null) {
                        appendLog("Completed. Output file: " + csvPath.toAbsolutePath());
                    } else {
                        appendLog("Processing finished with errors.");
                    }
                } catch (Exception e) {
                    appendLog("ERROR: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void setControlsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            fanRadio.setEnabled(enabled);
            assetRadio.setEnabled(enabled);
            browseButton.setEnabled(enabled);
            changeOutputButton.setEnabled(enabled);
        });
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(message + System.lineSeparator());
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    private void onChangeOutput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CSV output file");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setSelectedFile(outputCsvPath.toFile());
        int selection = chooser.showSaveDialog(this);
        if (selection == JFileChooser.APPROVE_OPTION) {
            updateOutputPath(chooser.getSelectedFile().toPath(), true);
        }
    }

    private Path defaultOutputPath() {
        return Paths.get(System.getProperty("user.dir"))
                .resolve("facturare.csv");
    }

    private Path deriveOutputPath(File excelFile) {
        Path excelPath = excelFile.toPath().toAbsolutePath().normalize();
        Path parent = excelPath.getParent();
        Path baseDir = parent != null ? parent : Paths.get(System.getProperty("user.dir"));
        return baseDir.resolve("facturare.csv");
    }

    private void updateOutputPath(Path newPath, boolean logChange) {
        outputCsvPath = newPath.toAbsolutePath().normalize();
        outputPathField.setText(outputCsvPath.toString());
        if (logChange) {
            appendLog("Output CSV set to: " + outputCsvPath);
        }
    }
}
