package stayablox.purpleevil.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector2d;
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

    public boolean contains(double x, double y) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
    }

    public Vector2d getPenetrationVector(AABB other) {
        double cx1 = (this.minX + this.maxX) * 0.5;
        double cy1 = (this.minY + this.maxY) * 0.5;
        double cx2 = (other.minX + other.maxX) * 0.5;
        double cy2 = (other.minY + other.maxY) * 0.5;

        double dx = cx2 - cx1;
        double dy = cy2 - cy1;

        double halfW1 = (this.maxX - this.minX) * 0.5;
        double halfH1 = (this.maxY - this.minY) * 0.5;
        double halfW2 = (other.maxX - other.minX) * 0.5;
        double halfH2 = (other.maxY - other.minY) * 0.5;

        double overlapX = halfW1 + halfW2 - Math.abs(dx);
        double overlapY = halfH1 + halfH2 - Math.abs(dy);

        double EPSILON = 0.0001;

        if (overlapX > 0 && overlapY > 0) {
            // Выталкиваем по той оси, где пересечение МЕНЬШЕ
            if (overlapX < overlapY) {
                double signX = dx != 0 ? Math.signum(dx) : (this.minX < other.minX ? -1 : 1);
                return new Vector2d(-signX * (overlapX + EPSILON), 0);
            } else {
                double signY = dy != 0 ? Math.signum(dy) : (this.minY < other.minY ? -1 : 1);
                return new Vector2d(0, -signY * (overlapY + EPSILON));
            }
        }
        return null;
    }

    public Vector2d getPenetrationVector(AABB other, boolean resolveX) {
        double cx1 = (this.minX + this.maxX) * 0.5;
        double cy1 = (this.minY + this.maxY) * 0.5;
        double cx2 = (other.minX + other.maxX) * 0.5;
        double cy2 = (other.minY + other.maxY) * 0.5;

        double dx = cx2 - cx1;
        double dy = cy2 - cy1;

        double halfW1 = (this.maxX - this.minX) * 0.5;
        double halfH1 = (this.maxY - this.minY) * 0.5;
        double halfW2 = (other.maxX - other.minX) * 0.5;
        double halfH2 = (other.maxY - other.minY) * 0.5;

        double overlapX = halfW1 + halfW2 - Math.abs(dx);
        double overlapY = halfH1 + halfH2 - Math.abs(dy);

        double EPSILON = 0.0001;

        if (overlapX > 0 && overlapY > 0) {
            if (resolveX) {
                // Если dx == 0, смотрим, кто левее, чтобы определить направление выталкивания
                double signX = dx != 0 ? Math.signum(dx) : (this.minX < other.minX ? -1 : 1);
                return new Vector2d(-signX * (overlapX + EPSILON), 0);
            } else {
                // Если dy == 0, смотрим, кто ниже
                double signY = dy != 0 ? Math.signum(dy) : (this.minY < other.minY ? -1 : 1);
                return new Vector2d(0, -signY * (overlapY + EPSILON));
            }
        }
        return null;
    }
}
