package com.mygame.model.node;

import com.mygame.model.Connection;
import com.mygame.model.Port;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.ProtectedPacket;
import com.mygame.model.packet.TrojanPacket;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class SaboteurNode extends Node {

    private boolean incompatibleRoutingEnabled = true;
    private boolean injectUnitNoiseIfNone = true;




    private double noiseChance = 0.15;                  // << much less noise


    private static final double INFECT_CHANCE = 1;   // 30%
    private double trojanConversionProbability = INFECT_CHANCE;
    private static final Random RNG = new Random();


    public SaboteurNode(double x, double y, double w, double h) {
        super(x, y, w, h);
        setNodeType(Type.SABOTEUR);
    }

    public void setIncompatibleRoutingEnabled(boolean v) { this.incompatibleRoutingEnabled = v; }
    public void setInjectUnitNoiseIfNone(boolean v)      { this.injectUnitNoiseIfNone = v; }
    public void setTrojanConversionProbability(double p) {
        if (Double.isNaN(p)) p = 0.0;
        this.trojanConversionProbability = Math.max(0, Math.min(1, p));
    }


    @Override
    public void onDelivered(Packet p) {
        // اگر پکت محافظت‌شده باشد، کاری نکن
        if (p instanceof ProtectedPacket) {
            enqueuePacket(p);
            return;
        }

        // اگر نویز ندارد → یک واحد نویز بده
        if (!p.hasNoise()) { // فرض بر اینه که Packet متد hasNoise() داره
            p.addNoise(0.0);
        }

        // به احتمال مشخص به TrojanPacket تبدیل شود
        if (Math.random() < INFECT_CHANCE) {
            TrojanPacket trojanP = new TrojanPacket(p); // سازنده‌ای که نسخه اصلی را نگه دارد
            enqueuePacket(trojanP);
        }
        else {
            enqueuePacket(p);
        }
    }

    @Override
    public void onDelivered(Packet p, Port at) {
        super.onDelivered(p, at);
        onDelivered(p);
    }

    @Override
    public void onLost(Packet p) {
        // می‌توان لاگ یا آمار ثبت کرد
    }

    @Override
    public void onCollision(Packet a, Packet b) {
        // این نود تأثیری در برخورد ندارد
    }

    @Override
    public void update(double dt, List<Packet> worldPackets) {
        // advance cooldowns on ports
        for (Port port : getPorts()) if (port.isEmitting()) port.tickCooldown(dt);
        if (queue.isEmpty()) return;

        Packet p = ((ArrayDeque<Packet>) queue).peekFirst();
        // Find a VALID opposite output: connected wire whose far end is an INPUT and the port can emit
        Port out = pickOppositeReadyOut(p);
        if (out == null) return; // <<< requirement: if no opposite ports, DO NOT send (keep queued)

        Connection wire = out.getWire();
        if (wire == null) return; // safety

        wire.transmit(p);
        p.setMobile(true);
        worldPackets.add(p);
        out.resetCooldown();
        ((ArrayDeque<Packet>) queue).removeFirst();
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return queue;
    }
    /** outputs that have a wire, and the other side is an INPUT, optionally require canEmit() */
    private List<Port> oppositeConnectedOutputs(boolean requireReady) {
        return outputs.stream()
                .filter(o -> o.getWire() != null && o.getConnectedPort() != null)
                .filter(o -> o.getConnectedPort().getDirection() == Port.PortDirection.INPUT)
                .filter(o -> !requireReady || o.canEmit())
                .collect(Collectors.toList());
    }

    /** pick one opposite-ready output (prefer any that can emit) */
    private Port pickOppositeReadyOut(Packet p) {
        List<Port> ready = oppositeConnectedOutputs(true);

        if (!ready.isEmpty()) return ready.get(RNG.nextInt(ready.size()));
        return null; // per requirement, if none are ready/valid, don’t emit
    }


    @Override
    public Node copy() {
        return new SaboteurNode(getX(), getY(), getWidth(), getHeight());
    }
}
