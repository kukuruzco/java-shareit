package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserById(Long userId);

    UserDto updateUser(Long userId, UserDto userDto);

    List<UserDto> getAllUsers();

    void deleteUser(Long userId);
}
