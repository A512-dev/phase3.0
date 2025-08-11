package com.mygame.model.packet.bulkPacket;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

import java.util.UUID;

/** پایه‌ی پکت حجیم؛ گونه‌ها A/B فقط اندازه و سکه را مشخص می‌کنند. */
public class BulkPacket extends Packet {

    public BulkPacket(Vector2D spawn, int sizeUnits) {
        super(spawn, /*health*/ 3);             // مقدار پایه برای health – دلخواه
        this.sizeUnits = sizeUnits;             // 8 یا 10 ...
        this.heavyId   = UUID.randomUUID().hashCode();  // گروه‌بندی برای Bitها
    }

    /** کپی عمیق با حفظ heavyId (برای بازتولید در اسنپ‌شات‌ها و …) */
    @Override
    public BulkPacket copy() {
        BulkPacket cp = new BulkPacket(pos.copy(), sizeUnits);
        cp.heavyId = this.heavyId;              // حفظ گروه
        cp.setVelocity(vel.copy());
        cp.setAcceleration(getAcceleration().copy());
        cp.setProtectedPacket(isProtectedPacket());
        return cp;
    }

    /** پیش‌فرض: سکه برابر اندازه؛ گونه‌های A/B مقدار ثابت خودشان را برمی‌گردانند. */
    @Override
    public int getCoinValue() { return Math.max(1, sizeUnits); }

    /** آیکون عمومی برای حجیم‌ها؛ گونه‌ها شکل دقیق را override می‌کنند. */
    @Override
    public Shape shape() { return Shape.HEXAGON; }

    /* قوانین حرکتی خاص (صاف=ثابت، انحنا=شتاب، drift و …) را در MovementSystem اعمال کن. */
}
