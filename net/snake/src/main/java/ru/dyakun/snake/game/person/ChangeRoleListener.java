package ru.dyakun.snake.game.person;

@FunctionalInterface
public interface ChangeRoleListener {
    void onChange(Member newRole);
}
