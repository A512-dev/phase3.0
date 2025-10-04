package client.ui;

import client.core.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

public class LevelSelectPanel extends JPanel {

    public LevelSelectPanel(Runnable onBack, IntConsumer onPickLevel) {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 22, 28));

        JLabel title = new JLabel("Select Level", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 1, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 200));
        grid.setOpaque(false);

        for (int i = 1; i <= 5; i++) {
            JButton b = new JButton("Level " + i);
            boolean unlocked = isLevelUnlocked(i);
            b.setEnabled(unlocked);
            if (!unlocked) {
                b.setToolTipText("Finish all previous levels to unlock.");
            }
            final int level = i;
            b.addActionListener(e -> onPickLevel.accept(level));
            grid.add(b);
        }

        add(grid, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        JButton back = new JButton("Back");
        back.addActionListener(e -> onBack.run());
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Level 1 always unlocked. Level n is unlocked if all 1..(n-1) are passed. */
    private boolean isLevelUnlocked(int n) {
        // TODO: 9/4/2025 change the n>=10
        if (n <= 10) return true;
        for (int i = 1; i < n; i++) {
            if (!GameState.isLevelPassed("level" + i)) return false;
        }
        return true;
    }
}
