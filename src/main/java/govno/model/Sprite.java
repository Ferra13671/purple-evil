package govno.model;

public record Sprite(String textureId, float width, float height) {

    public Sprite(String textureId) {
        this(textureId, 1, 1);
    }
}
