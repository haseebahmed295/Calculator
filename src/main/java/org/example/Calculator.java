package org.example;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

public class Calculator {
    private JFrame frame;
    private JTextField textField;
    private double first, second, result;
    private String operation;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new Calculator().frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Calculator() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Calculator");
        try {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage("src/images/calculator.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(300, 400));

        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5); // Keep larger insets for text field

        textField = new RoundedTextField();
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setFont(new Font("Arial", Font.PLAIN, 40));
        textField.setBorder(null); // No border
        textField.setBackground(new Color(240, 240, 240)); // Light gray background
        textField.setForeground(new Color(50, 50, 50)); // Dark text
        textField.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        mainPanel.add(textField, gbc);

        String[][] buttonLabels = {
                {"%", "CE", "C", "⬅"},
                {"¹/ₓ", "x²", "√", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "−"},
                {"1", "2", "3", "+"},
                {"±", "0", "•", "🟰"}
        };

        int row = 1;
        for (String[] rowLabels : buttonLabels) {
            gbc.gridwidth = 1;
            gbc.weightx = 0.25;
            gbc.weighty = 0.16;
            gbc.insets = new Insets(2, 2, 2, 2); // Reduced spacing between buttons
            for (int col = 0; col < rowLabels.length; col++) {
                String label = rowLabels[col];
                gbc.gridx = col;
                gbc.gridy = row;
                int gridWidth = 1;
                if (label.equals("0") && col == 1) gridWidth = 2;
                JButton button = createButton(label, gridWidth, label.equals("C"));
                mainPanel.add(button, gbc);
                if (gridWidth > 1) gbc.gridx += gridWidth - 1;
            }
            row++;
        }

        frame.add(mainPanel);
        frame.pack();
        frame.setSize(400, 600);
        frame.setLocationRelativeTo(null);
    }

    private JButton createButton(String text, int gridWidth, boolean isClearAll) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(text.matches("[0-9]") ? new Color(200, 200, 200) : getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(10, 1));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);

        // Styling based on button type
        if (text.matches("[0-9]")) {
            button.setBackground(new Color(255, 255, 255));
        } else if (text.equals("🟰")) {
            button.setBackground(new Color(0, 120, 215));
            button.setForeground(Color.WHITE);
        } else if (isClearAll) {
            button.setBackground(new Color(255, 100, 100)); // Red for C
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(230, 230, 230));
        }

        button.addActionListener(e -> handleButtonAction(text));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = gridWidth;
        gbc.insets = new Insets(2, 2, 2, 2); // Reduced spacing
        return button;
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.format("%.0f", value); // Whole number, no decimal
        } else {
            return String.format("%.2f", value).replaceAll("0*$", "").replaceAll("\\.$", ""); // Up to 2 decimals, remove trailing zeros
        }
    }

    private void handleButtonAction(String text) {
        switch (text) {
            case "C": // Reset all
                textField.setText("");
                first = 0;
                second = 0;
                operation = null;
                break;
            case "CE": // Clear entry
                textField.setText("");
                break;
            case "⬅": // Backspace
                String current = textField.getText();
                if (!current.isEmpty()) {
                    textField.setText(current.substring(0, current.length() - 1));
                }
                break;
            case "¹/ₓ": // Reciprocal
                try {
                    double value = Double.parseDouble(textField.getText());
                    if (value != 0) {
                        textField.setText(formatNumber(1.0 / value));
                    }
                } catch (NumberFormatException ignored) {}
                break;
            case "x²": // Square
                try {
                    double value = Double.parseDouble(textField.getText());
                    textField.setText(formatNumber(value * value));
                } catch (NumberFormatException ignored) {}
                break;
            case "√": // Square root
                try {
                    double value = Double.parseDouble(textField.getText());
                    if (value >= 0) {
                        textField.setText(formatNumber(Math.sqrt(value)));
                    }
                } catch (NumberFormatException ignored) {}
                break;
            case "±": // Negate
                try {
                    double value = Double.parseDouble(textField.getText());
                    textField.setText(formatNumber(-value));
                } catch (NumberFormatException ignored) {}
                break;
            case "🟰": // Equals
                calculate();
                break;
            case "+": // Plus
            case "−": // Minus
            case "×": // Multiply
            case "÷": // Divide
            case "%": // Modulus
                setOperation(text.equals("÷") ? "/" : text.equals("+") ? "+" : text.equals("−") ? "-" : text.equals("×") ? "x" : "%");
                break;
            case "•": // Decimal point
                if (!textField.getText().contains(".")) {
                    textField.setText(textField.getText() + ".");
                }
                break;
            default: // Numbers
                textField.setText(textField.getText() + text);
                break;
        }
    }

    private void setOperation(String op) {
        try {
            first = Double.parseDouble(textField.getText());
            textField.setText("");
            operation = op;
        } catch (NumberFormatException ignored) {}
    }

    private void calculate() {
        try {
            second = Double.parseDouble(textField.getText());
            switch (operation) {
                case "+" -> result = first + second;
                case "-" -> result = first - second;
                case "x" -> result = first * second;
                case "/" -> result = first / second;
                case "%" -> result = first % second;
            }
            textField.setText(formatNumber(result));
            operation = null;
        } catch (NumberFormatException ignored) {}
    }

    // Custom text field for rounded corners
    private static class RoundedTextField extends JTextField {
        public RoundedTextField() {
            setOpaque(false); // Make non-opaque to allow custom background
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); // Match button radius
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        public void paintBorder(Graphics g) {
            // No border painting
        }
    }

    // Custom panel for background image
    private static class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;

        public BackgroundPanel() {
            try {
                // Replace with the actual path to your image file
                backgroundImage = ImageIO.read(new File("src/images/background.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
                // Fallback to a gray background if image loading fails
                setBackground(new Color(220, 220, 220));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int width = getWidth();
                int height = getHeight();
                double scale = Math.min((double) width / backgroundImage.getWidth(),
                        (double) height / backgroundImage.getHeight());
                int scaledWidth = (int) (backgroundImage.getWidth() * scale);
                int scaledHeight = (int) (backgroundImage.getHeight() * scale);
                g2.drawImage(backgroundImage, (width - scaledWidth) / 2, (height - scaledHeight) / 2,
                        scaledWidth, scaledHeight, null);
                g2.dispose();
            }
        }
    }

    // Custom border for rounded corners (used only for buttons)
    private static class RoundedBorder implements Border {
        private int radius;
        private int thickness;

        RoundedBorder(int radius, int thickness) {
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 100, 100));
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Double(x + thickness / 2, y + thickness / 2,
                    width - thickness, height - thickness, radius, radius));
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2 + thickness, radius / 2 + thickness,
                    radius / 2 + thickness, radius / 2 + thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}