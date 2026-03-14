package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;
import org.handler.exception.UserNotFoundException;
import org.handler.mapper.UserMapper;
import org.handler.model.User;
import org.handler.repository.UserRepository;
import org.handler.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = new User();
        userMapper.toUser(userRequestDto, user);
        userRepository.save(user);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = findUserById(userId);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Attempting to delete user with ID: {}", userId);

        User user = findUserById(userId);

//        disassociateCasesFromUser(user);

        userRepository.delete(user);

        log.info("Successfully deleted user with ID: {}", userId);
    }

    @Override
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}