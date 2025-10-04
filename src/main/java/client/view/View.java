package client.view;

import java.awt.Graphics2D;

/**
 * Generic rendering interface for game view components.
 */
public interface View<T> {
    void render(Graphics2D g, T model );
}
