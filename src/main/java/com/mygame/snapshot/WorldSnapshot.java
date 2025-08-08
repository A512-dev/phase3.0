package com.mygame.snapshot;

import java.util.List;

import com.mygame.model.node.BasicNode;
import com.mygame.service.CoinService;

/** Immutable snapshot of everything the renderer needs for one frame. */
public record WorldSnapshot(
        List<NodeSnapshot> nodes,
        List<ConnectionSnapshot> connections,
        List<PacketSnapshot>             packets,
        HudReadOnly              hud,          // ← **READ-ONLY HUD COPY**
        CoinService coinService,
        boolean                  gameOver,
        boolean                  viewOnlyMode
) {


    public boolean isGameOver() { return gameOver; }
    public boolean isViewOnly() { return viewOnlyMode; }
}
