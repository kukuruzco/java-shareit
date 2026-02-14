package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(userDto.getEmail()))) {
            throw new IllegalArgumentException("Email уже используется");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        // 1. Находим пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        // 2. ОБНОВЛЯЕМ ИМЯ - если оно передано в запросе
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            if (!userDto.getEmail().equalsIgnoreCase(user.getEmail())) {
                Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                    throw new IllegalArgumentException("Email уже используется");
                }
            }
            user.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(user);

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        userRepository.deleteById(userId);
    }
}
