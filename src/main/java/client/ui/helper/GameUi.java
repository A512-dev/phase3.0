// client/ui/helper/GameUi.java
package client.ui.helper;

import javax.swing.*;
import java.awt.*;
import server.sim.core.GameConfig; // or your actual location

public final class GameUi {
    private GameUi(){}

    public static Dimension gameWindowSize() {
        return new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
    }

    public static void applyGameSize(JComponent comp) {
        Dimension d = gameWindowSize();
        comp.setPreferredSize(d);
        comp.setMinimumSize(d);
        comp.setMaximumSize(d);
        comp.setSize(d);
    }

    public static void showSized(JFrame frame, JComponent content) {
        applyGameSize(content);
        frame.setContentPane(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }
}
