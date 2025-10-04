// client/ui/ConnectPanel.java
package client.ui;

import client.net.NetClient;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class ConnectPanel extends JPanel {
    private final JTextField host = new JTextField("127.0.0.1", 12);
    private final JTextField port = new JTextField("7777", 5);
    private final JButton    btnConnect = new JButton("Connect");
    private final JButton    btnOffline = new JButton("Play Offline");
    private final JLabel     status     = new JLabel("Not connected");

    public ConnectPanel(Consumer<NetClient> onConnected, Runnable onOffline) {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);

        g.gridy=0; add(new JLabel("Server IP:"), g);
        g.gridy=1; add(host, g);
        g.gridy=2; add(new JLabel("Port:"), g);
        g.gridy=3; add(port, g);
        g.gridy=4; add(btnConnect, g);
        g.gridy=5; add(btnOffline, g);
        g.gridy=6; add(status, g);

        btnConnect.addActionListener(e -> {
            String h = host.getText().trim();
            int    p = Integer.parseInt(port.getText().trim());
            NetClient c = new NetClient(h, p);
            status.setText("Connecting…");
            if (c.connect()) {
                status.setText("Connected");
                onConnected.accept(c);
            } else {
                status.setText("Failed to connect");
                JOptionPane.showMessageDialog(this,
                        "Could not reach server.\nTry again or Play Offline.",
                        "Connection failed", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnOffline.addActionListener(e -> onOffline.run());
    }
}
