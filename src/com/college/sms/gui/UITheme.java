package com.college.sms.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Centralized UI styling and theming utility.
 * Uses standard Java Swing styling for a clean, modern, and readable desktop experience.
 */
public class UITheme {
    // Color Palette
    public static final Color PRIMARY = new Color(37, 99, 235);       // Royal Blue
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color PRIMARY_DARK = new Color(30, 58, 138);  // Deep Navy
    public static final Color SUCCESS = new Color(22, 163, 74);       // Emerald Green
    public static final Color DANGER = new Color(220, 38, 38);        // Crimson Red
    public static final Color WARNING = new Color(217, 119, 6);       // Amber
    public static final Color BACKGROUND = new Color(248, 250, 252);  // Soft Slate Gray
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240);// Light Gray Border

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Styles a standard action button with background color and hover feedback.
     */
    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * Styles a text field with clean padding and soft borders.
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT_REGULAR);
        textField.setForeground(TEXT_DARK);
        textField.setBackground(Color.WHITE);
        textField.setCaretColor(PRIMARY);
        textField.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    /**
     * Styles a JTable with clear padding, alternating rows, and header styling.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(219, 234, 254)); // Soft blue highlight
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Center cell renderer for IDs and numeric values
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }

    /**
     * Creates a consistent padded card border.
     */
    public static Border createCardBorder(String title) {
        Border line = new LineBorder(BORDER_COLOR, 1, true);
        Border margin = new EmptyBorder(8, 8, 8, 8);
        if (title != null && !title.isEmpty()) {
            return BorderFactory.createTitledBorder(line, " " + title + " ",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    FONT_HEADER, PRIMARY_DARK);
        }
        return new CompoundBorder(line, margin);
    }
}
