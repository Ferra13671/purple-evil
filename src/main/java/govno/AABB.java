package govno;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector2dc;

@AllArgsConstructor
@Getter
public class AABB {
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    public AABB(AABB aabb) {
        set(aabb);
    }

    public double getXLength() {
        return getMaxX() - getMinX();
    }

    public double getYLength() {
        return getMaxY() - getMinY();
    }

    public AABB set(AABB aabb) {
        this.minX = aabb.minX;
        this.minY = aabb.minY;
        this.maxX = aabb.maxX;
        this.maxY = aabb.maxY;

        return this;
    }

    public AABB move(Vector2dc delta) {
        this.minX += delta.x();
        this.minY += delta.y();
        this.maxX += delta.x();
        this.maxY += delta.y();

        return this;
    }
}
