package ru.dyakun.snake.model.person;

@FunctionalInterface
public interface ChangeRoleListener {
    void onChange(Member newRole);
}
