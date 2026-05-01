package org.handler.service.impl;

import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;
import org.handler.exception.UserNotFoundException;
import org.handler.mapper.UserMapper;
import org.handler.model.User;
import org.handler.model.enums.UserType;
import org.handler.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldMapSaveAndReturnResponse_WhenRequestIsValid() {
        UserRequestDto requestDto = UserRequestDto.builder()
                .username("johndoe")
                .password("secret12")
                .email("john@test.com")
                .type(UserType.CLIENT.name())
                .build();

        UserResponseDto expectedResponse = UserResponseDto.builder()
                .id(1L)
                .username("johndoe")
                .email("john@test.com")
                .type(UserType.CLIENT.name())
                .build();

        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(expectedResponse);

        UserResponseDto actual = userService.createUser(requestDto);

        assertSame(expectedResponse, actual);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).toUser(eq(requestDto), userCaptor.capture());
        verify(userRepository).save(userCaptor.getValue());
        verify(userMapper).toUserResponseDto(userCaptor.getValue());
    }


    @Test
    void getUserById_ShouldReturnMappedResponse_WhenUserExists() {
        User user = createUser(10L, "alice", "alice@test.com", UserType.CLIENT);
        UserResponseDto expected = UserResponseDto.builder().id(10L).username("alice").build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.getUserById(10L);

        assertSame(expected, actual);
        verify(userRepository).findById(10L);
        verify(userMapper).toUserResponseDto(user);
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserMissing() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(404L));

        verify(userMapper, never()).toUserResponseDto(any());
    }


    @Test
    void getAllUsers_ShouldReturnMappedUserResponses_WhenUsersExist() {
        User userOne = createUser(1L, "u1", "u1@test.com", UserType.CLIENT);
        User userTwo = createUser(2L, "u2", "u2@test.com", UserType.ADMIN);

        UserResponseDto dtoOne = UserResponseDto.builder().id(1L).username("u1").build();
        UserResponseDto dtoTwo = UserResponseDto.builder().id(2L).username("u2").build();

        when(userRepository.findAll()).thenReturn(List.of(userOne, userTwo));
        when(userMapper.toUserResponseDto(userOne)).thenReturn(dtoOne);
        when(userMapper.toUserResponseDto(userTwo)).thenReturn(dtoTwo);

        List<UserResponseDto> actual = userService.getAllUsers();

        assertEquals(2, actual.size());
        assertSame(dtoOne, actual.get(0));
        assertSame(dtoTwo, actual.get(1));
        verify(userRepository).findAll();
        verify(userMapper).toUserResponseDto(userOne);
        verify(userMapper).toUserResponseDto(userTwo);
    }


    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        User existing = createUser(30L, "tom", "tom@test.com", UserType.CLIENT);
        when(userRepository.findById(30L)).thenReturn(Optional.of(existing));

        userService.deleteUser(30L);

        verify(userRepository).delete(existing);
    }


    @Test
    void findUserById_ShouldReturnUser_WhenUserExists() {
        User existing = createUser(50L, "sam", "sam@test.com", UserType.ADMIN);
        when(userRepository.findById(50L)).thenReturn(Optional.of(existing));

        User actual = userService.findUserById(50L);

        assertSame(existing, actual);
        verify(userRepository).findById(50L);
    }

    @Test
    void findUserById_ShouldThrowUserNotFoundException_WhenUserMissing() {
        when(userRepository.findById(51L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(51L));
    }

    @Test
    void usernameExists_ShouldReturnTrue_WhenRepositoryReportsUsernameExists() {
        when(userRepository.existsByUsername("jane")).thenReturn(true);

        boolean exists = userService.usernameExists("jane");

        assertTrue(exists);
        verify(userRepository).existsByUsername("jane");
    }

    @Test
    void emailExists_ShouldReturnFalse_WhenRepositoryReportsEmailDoesNotExist() {
        when(userRepository.existsByEmail("none@test.com")).thenReturn(false);

        boolean exists = userService.emailExists("none@test.com");

        assertFalse(exists);
        verify(userRepository).existsByEmail("none@test.com");
    }


    private User createUser(Long id, String username, String email, UserType type) {
        return User.builder()
                .id(id)
                .username(username)
                .password("encoded")
                .email(email)
                .type(type)
                .build();
    }
}
