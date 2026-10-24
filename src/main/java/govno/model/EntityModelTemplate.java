package govno.model;

import govno.utils.Direction;

import java.util.Map;

public record EntityModelTemplate(String name, Map<Direction, Map<String, Animation>> animations) {
}
