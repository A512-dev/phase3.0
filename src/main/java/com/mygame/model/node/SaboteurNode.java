package com.mygame.model.node;

import com.mygame.model.Port;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.ProtectedPacket;
import com.mygame.model.packet.TrojanPacket;

import java.util.Collection;
import java.util.List;

public final class SaboteurNode extends Node {

    private boolean incompatibleRoutingEnabled = true;
    private boolean injectUnitNoiseIfNone = true;



    private static final double INFECT_CHANCE = 0.3;   // 30%
    private double trojanConversionProbability = INFECT_CHANCE;

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
            p.addNoise(1);
        }

        // به احتمال مشخص به TrojanPacket تبدیل شود
        if (Math.random() < INFECT_CHANCE) {
            TrojanPacket trojanP = new TrojanPacket(p); // سازنده‌ای که نسخه اصلی را نگه دارد
            super.enqueuePacket(trojanP);
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
        emitQueued(worldPackets);
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return queue;
    }

    @Override
    public Node copy() {
        return new SaboteurNode(getX(), getY(), getWidth(), getHeight());
    }
}
