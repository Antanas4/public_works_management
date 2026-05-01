package org.handler.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.handler.dto.request.LoginRequestDto;
import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;
import org.handler.exception.EmailAlreadyExistsException;
import org.handler.exception.UserNotFoundException;
import org.handler.exception.UsernameAlreadyExistsException;
import org.handler.mapper.UserMapper;
import org.handler.model.User;
import org.handler.model.UserPrincipal;
import org.handler.model.enums.UserType;
import org.handler.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_ShouldAuthenticateAndReturnUserResponse_WhenCredentialsAreValid() {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("john")
                .password("secret")
                .build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        User user = createUser(1L, "john", "john@test.com");
        UserPrincipal principal = new UserPrincipal(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        UserResponseDto expected = UserResponseDto.builder().id(1L).username("john").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userMapper.toUserResponseDto(user)).thenReturn(expected);

        UserResponseDto actual = authService.login(loginRequest, request, response);

        assertSame(expected, actual);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertSame(authentication, SecurityContextHolder.getContext().getAuthentication());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userMapper).toUserResponseDto(user);
    }

    @Test
    void login_ShouldThrowBadCredentialsException_WhenAuthenticationFails() {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("john")
                .password("wrong")
                .build();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, request, response));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void login_ShouldThrowNullPointerException_WhenLoginRequestIsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThrows(NullPointerException.class, () -> authService.login(null, request, response));

        verifyNoInteractions(authenticationManager, userMapper);
    }

    @Test
    void register_ShouldEncodePasswordAndCreateUser_WhenRequestIsValid() {
        UserRequestDto request = UserRequestDto.builder()
                .username("new_user")
                .password("plain-pass")
                .email("new@test.com")
                .type(UserType.CLIENT.name())
                .build();

        UserResponseDto expected = UserResponseDto.builder()
                .id(20L)
                .username("new_user")
                .email("new@test.com")
                .type(UserType.CLIENT.name())
                .build();

        when(userService.usernameExists("new_user")).thenReturn(false);
        when(userService.emailExists("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-pass")).thenReturn("encoded-pass");
        when(userService.createUser(request)).thenReturn(expected);

        UserResponseDto actual = authService.register(request);

        assertSame(expected, actual);
        assertEquals("encoded-pass", request.getPassword());
        verify(userService).usernameExists("new_user");
        verify(userService).emailExists("new@test.com");
        verify(passwordEncoder).encode("plain-pass");
        verify(userService).createUser(request);
    }

    @Test
    void register_ShouldUseBcryptEncodedPassword_BeforeCreatingUser() {
        UserRequestDto request = UserRequestDto.builder()
                .username("new_user")
                .password("plain-pass")
                .email("new@test.com")
                .type(UserType.CLIENT.name())
                .build();

        BCryptPasswordEncoder realBcrypt = new BCryptPasswordEncoder();

        when(userService.usernameExists("new_user")).thenReturn(false);
        when(userService.emailExists("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-pass")).thenAnswer(invocation -> realBcrypt.encode(invocation.getArgument(0)));
        when(userService.createUser(request)).thenReturn(UserResponseDto.builder().id(1L).build());

        authService.register(request);

        assertNotEquals("plain-pass", request.getPassword());
        assertTrue(request.getPassword().startsWith("$2"));
        assertTrue(realBcrypt.matches("plain-pass", request.getPassword()));
        verify(passwordEncoder).encode("plain-pass");
        verify(userService).createUser(request);
    }

    @Test
    void register_ShouldThrowUsernameAlreadyExistsException_WhenUsernameIsTaken() {
        UserRequestDto request = UserRequestDto.builder()
                .username("existing")
                .password("password")
                .email("new@test.com")
                .type(UserType.CLIENT.name())
                .build();

        when(userService.usernameExists("existing")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> authService.register(request));

        verify(userService).usernameExists("existing");
        verify(userService, never()).emailExists(any());
        verifyNoInteractions(passwordEncoder);
        verify(userService, never()).createUser(any());
    }

    @Test
    void register_ShouldThrowEmailAlreadyExistsException_WhenEmailIsTaken() {
        UserRequestDto request = UserRequestDto.builder()
                .username("free_username")
                .password("password")
                .email("existing@test.com")
                .type(UserType.CLIENT.name())
                .build();

        when(userService.usernameExists("free_username")).thenReturn(false);
        when(userService.emailExists("existing@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userService).usernameExists("free_username");
        verify(userService).emailExists("existing@test.com");
        verifyNoInteractions(passwordEncoder);
        verify(userService, never()).createUser(any());
    }

    @Test
    void register_ShouldPropagateException_WhenPasswordEncodingFails() {
        UserRequestDto request = UserRequestDto.builder()
                .username("new_user")
                .password("plain-pass")
                .email("new@test.com")
                .type(UserType.CLIENT.name())
                .build();

        when(userService.usernameExists("new_user")).thenReturn(false);
        when(userService.emailExists("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-pass")).thenThrow(new IllegalStateException("Encoder failure"));

        assertThrows(IllegalStateException.class, () -> authService.register(request));

        verify(userService).usernameExists("new_user");
        verify(userService).emailExists("new@test.com");
        verify(passwordEncoder).encode("plain-pass");
        verify(userService, never()).createUser(any());
    }

    @Test
    void register_ShouldThrowNullPointerException_WhenRequestIsNull() {
        assertThrows(NullPointerException.class, () -> authService.register(null));

        verifyNoInteractions(userService, passwordEncoder);
    }

    @Test
    void getCurrentUser_ShouldReturnMappedUser_WhenPrincipalIsPresent() {
        User user = createUser(44L, "anna", "anna@test.com");
        UserPrincipal principal = new UserPrincipal(user);
        UserResponseDto expected = UserResponseDto.builder().id(44L).username("anna").build();

        when(userMapper.toUserResponseDto(user)).thenReturn(expected);

        UserResponseDto actual = authService.getCurrentUser(principal);

        assertSame(expected, actual);
        verify(userMapper).toUserResponseDto(user);
    }

    @Test
    void getCurrentUser_ShouldThrowUserNotFoundException_WhenPrincipalIsNull() {
        assertThrows(UserNotFoundException.class, () -> authService.getCurrentUser(null));

        verifyNoInteractions(userMapper);
    }

    @Test
    void logout_ShouldInvalidateSessionAndClearSecurityContext_WhenSessionExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);

        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        authService.logout(request);

        verify(request, times(2)).getSession(false);
        verify(session).invalidate();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_ShouldClearSecurityContextWithoutInvalidating_WhenSessionDoesNotExist() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        authService.logout(request);

        verify(request).getSession(false);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_ShouldThrowNullPointerException_WhenRequestIsNull() {
        assertThrows(NullPointerException.class, () -> authService.logout(null));
    }

    private User createUser(Long id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .password("encoded")
                .email(email)
                .type(UserType.CLIENT)
                .build();
    }
}
