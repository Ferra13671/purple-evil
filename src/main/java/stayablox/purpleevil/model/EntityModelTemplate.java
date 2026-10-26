package stayablox.purpleevil.model;

import stayablox.purpleevil.utils.Direction;

import java.util.Map;

public record EntityModelTemplate(String name, Map<Direction, Map<String, Animation>> animations) {
}
