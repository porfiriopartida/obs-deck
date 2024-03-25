package com.porfiriopartida.ui;

import javax.swing.*;
import java.awt.*;

public class CustomCellRenderer extends DefaultListCellRenderer {
    private int desiredHeight = 30; // Desired height for each cell
    private int padding = 5; // Desired height for each cell

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        // Get the default rendering component
        JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Adjust height
        renderer.setPreferredSize(new Dimension(renderer.getPreferredSize().width, desiredHeight));

        // Add padding
        renderer.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding)); // Add padding to all sides

        return renderer;
    }
}
