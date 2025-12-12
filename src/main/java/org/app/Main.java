package org.app;

import org.app.ui.InvoiceGeneratorFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // ignore look and feel issues, fallback to default
            }
            InvoiceGeneratorFrame frame = new InvoiceGeneratorFrame();
            frame.setVisible(true);
        });
    }
}
