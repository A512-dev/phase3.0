package server.sim.model.packet.bulkPacket;

import server.sim.core.GameConfig;
import shared.Vector2D;
import server.sim.model.packet.Packet;
import shared.model.PacketShape;

/**
 * A size-1 bit packet produced by a Distributor from a bulk packet.
 * Each bit carries a groupId (to identify its parent bulk) and a color tag
 * so different bulks' bits can be visually distinguished.
 */
public final class BitPacket extends Packet {

    // شناسه‌ی گروه برای وصل‌کردن دوباره‌ی بیت‌ها در Merger
    private final int groupId;

    // تگ رنگ برای تمایز بصری (می‌تونی بجای int از Color هم استفاده کنی)
    private final int colorTag;

    /** همیشه health=1 چون بیت‌ها اندازه‌شان ۱ است. */
    public BitPacket(Vector2D spawn, int groupId, int colorTag) {
        super(spawn, /*health=*/GameConfig.bitPacketLife, GameConfig.bitPacketSize);
        this.groupId = groupId;
        this.colorTag = colorTag;
    }

    public int getGroupId() { return groupId; }

    public int getColorTag() { return colorTag; }

    /** بیت‌ها معمولاً ارزشی ندارند یا خیلی کم؛ 0 نگه می‌داریم. */
    @Override
    public int getCoinValue() { return 1; }

    /** برای نمایش شِمای Messenger/Infinity استفاده می‌شود. */
    @Override
    public PacketShape shape() { return PacketShape.INFINITY; }

    /** کپی عیناً باید group/color و مسیر را حفظ کند. */
    @Override
    public Packet copy() {
        BitPacket clone = new BitPacket(pos.copy(), groupId, colorTag);
        if (getFromPort() != null && getToPort() != null) {
            clone.setRoute(getFromPort(), getToPort());
        }
        clone.setAlive(isAlive());
        return clone;
    }

    /** کمک به Merger: آیا دو بیت متعلق به یک Bulk هستند؟ */
    public boolean sameGroup(BitPacket other) {
        return other != null && this.groupId == other.groupId;
    }
}
