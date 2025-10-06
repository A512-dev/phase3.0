// client/ui/LevelSelectPanel.java
package client.ui;

import client.core.GameState;
import client.ui.helper.GameUi;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

public final class LevelSelectPanel extends JPanel {

    // Preferred ctor signature used in ClientApp: (onLevelChosen, onBack)
    public LevelSelectPanel(IntConsumer onLevelChosen, Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 22, 28));
        GameUi.applyGameSize(this);  // <- size from GameConfig

        // Title
        JLabel title = new JLabel("Select Level", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 0));
        add(title, BorderLayout.NORTH);

        // Levels grid (1..5)
        JPanel grid = new JPanel(new GridLayout(0, 1, 10, 10));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 200, 20, 200));

        for (int i = 1; i <= 5; i++) {
            final int level = i;
            JButton b = new JButton("Level " + i);
            boolean unlocked = isLevelUnlocked(i);
            b.setEnabled(unlocked);
            if (!unlocked) b.setToolTipText("Finish all previous levels to unlock.");
            b.addActionListener(e -> onLevelChosen.accept(level));
            grid.add(b);
        }
        add(grid, BorderLayout.CENTER);

        // Bottom bar
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        JButton back = new JButton("Back");
        back.addActionListener(e -> onBack.run());
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Level 1 is unlocked; level n unlocks if all 1..(n-1) are passed. */
    private boolean isLevelUnlocked(int n) {
        if (n == 1) return true;
        for (int i = 1; i < n; i++) {
            if (!GameState.isLevelPassed("level" + i)) return false;
        }
        return true;
    }
}
