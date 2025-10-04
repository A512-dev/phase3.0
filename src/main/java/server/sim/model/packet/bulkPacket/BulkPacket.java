package server.sim.model.packet.bulkPacket;

import server.sim.core.GameConfig;
import server.sim.engine.physics.Vector2D;
import server.sim.model.packet.Packet;

import java.util.UUID;

/** پایه‌ی پکت حجیم؛ گونه‌ها A/B فقط اندازه و سکه را مشخص می‌کنند. */
public class BulkPacket extends Packet {

    public BulkPacket(Vector2D spawn, int payload, double health) {
        super(spawn, /*health*/ health, GameConfig.bulkPacketSize);             // مقدار پایه برای health – دلخواه
        this.payloadSize = payload;             // 8 یا 10 ...
        this.heavyId   = UUID.randomUUID().hashCode();  // گروه‌بندی برای Bitها
    }

    /** کپی عمیق با حفظ heavyId (برای بازتولید در اسنپ‌شات‌ها و …) */
    @Override
    public BulkPacket copy() {
        BulkPacket cp = new BulkPacket(pos.copy(), payloadSize, getHealth());
        cp.heavyId = this.heavyId;              // حفظ گروه
        cp.setVelocity(vel.copy());
        cp.setAcceleration(getAcceleration().copy());
        cp.setProtectedPacket(isProtectedPacket());
        return cp;
    }

    /** پیش‌فرض: سکه برابر اندازه؛ گونه‌های A/B مقدار ثابت خودشان را برمی‌گردانند. */
    @Override
    public int getCoinValue() { return Math.max(1, payloadSize); }

    /** آیکون عمومی برای حجیم‌ها؛ گونه‌ها شکل دقیق را override می‌کنند. */
    @Override
    public Shape shape() { return Shape.HEXAGON; }

    /* قوانین حرکتی خاص (صاف=ثابت، انحنا=شتاب، drift و …) را در MovementSystem اعمال کن. */
}
