package mate.academy.carsharing.service.impl;

import static mate.academy.carsharing.util.TestUtils.NOT_VALID_EMAIL;
import static mate.academy.carsharing.util.TestUtils.VALID_EMAIL;
import static mate.academy.carsharing.util.TestUtils.VALID_FIRST_NAME;
import static mate.academy.carsharing.util.TestUtils.VALID_ID;
import static mate.academy.carsharing.util.TestUtils.VALID_NEW_LAST_NAME;
import static mate.academy.carsharing.util.TestUtils.VALID_ROLE;
import static mate.academy.carsharing.util.TestUtils.createValidRole;
import static mate.academy.carsharing.util.TestUtils.createValidUser;
import static mate.academy.carsharing.util.TestUtils.createValidUserRegistrationRequestDto;
import static mate.academy.carsharing.util.TestUtils.createValidUserResponseDto;
import static mate.academy.carsharing.util.TestUtils.createValidUserUpdateInfoRequestDto;
import static mate.academy.carsharing.util.TestUtils.createValidUserUpdateRoleRequestDto;
import static mate.academy.carsharing.util.TestUtils.createValidUserWithRoleResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import mate.academy.carsharing.dto.user.UserRegistrationRequestDto;
import mate.academy.carsharing.dto.user.UserResponseDto;
import mate.academy.carsharing.dto.user.UserResponseDtoWithRoles;
import mate.academy.carsharing.dto.user.UserUpdateInfoRequestDto;
import mate.academy.carsharing.dto.user.UserUpdateRoleRequestDto;
import mate.academy.carsharing.exception.EntityNotFoundException;
import mate.academy.carsharing.exception.RegistrationException;
import mate.academy.carsharing.mapper.UserMapper;
import mate.academy.carsharing.model.Role;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.role.RoleRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("register() method works")
    public void register_WithValidUserRegistrationRequestDto_ReturnUserResponseDto() {
        UserRegistrationRequestDto requestDto = createValidUserRegistrationRequestDto();
        User newUser = createValidUser();
        UserResponseDto expected = createValidUserResponseDto();
        when(userRepository.findByEmail(requestDto.email()))
                .thenReturn(Optional.empty());
        when(userMapper.toModel(requestDto))
                .thenReturn(newUser);
        when(passwordEncoder.encode(requestDto.password()))
                .thenReturn("HashedPassword");
        when(roleRepository.findByName(any(Role.RoleName.class)))
                .thenReturn(Optional.of(createValidRole(VALID_ID, Role.RoleName.ROLE_CUSTOMER)));
        User savedUser = createValidUser();
        when(userRepository.save(newUser))
                .thenReturn(savedUser);
        when(userMapper.toDto(savedUser))
                .thenReturn(expected);

        UserResponseDto actual = userService.register(requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("updateUserRole() method works")
    public void updateUserRole_WithValidUserUpdateRoleRequestDto_ReturnUserResponseDtoWithRoles() {
        User user = createValidUser();
        Role role = createValidRole(VALID_ID, Role.RoleName.ROLE_CUSTOMER);
        user.setRoles(Set.of(role));
        UserUpdateRoleRequestDto requestDto = createValidUserUpdateRoleRequestDto();
        UserResponseDtoWithRoles expected = createValidUserWithRoleResponseDto();
        when(userRepository.findById(VALID_ID))
                .thenReturn(Optional.of(createValidUser()));
        when(roleRepository.findByName(VALID_ROLE))
                .thenReturn(Optional.of(role));
        when(userRepository.save(user))
                .thenReturn(user);
        when(userMapper.toDtoWithRoles(user))
                .thenReturn(expected);

        UserResponseDtoWithRoles actual = userService.updateUserRole(VALID_ID, requestDto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("updateUserInfo() method works")
    public void updateUserInfo_WithValidEmailAndUserUpdateInfoRequestDto_ReturnUserResponseDto() {
        User user = createValidUser();
        UserResponseDto expected = new UserResponseDto(
                VALID_ID,
                VALID_EMAIL,
                VALID_FIRST_NAME,
                VALID_NEW_LAST_NAME
        );

        UserUpdateInfoRequestDto requestDto = createValidUserUpdateInfoRequestDto();
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(expected);

        UserResponseDto actual = userService.updateUserInfo(VALID_EMAIL, requestDto);

        verify(userMapper).toDto(user);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getUserInfo() method works")
    public void getUserInfo_WithValidEmail_ReturnUserResponseDtoWithRoles() {
        User user = createValidUser();
        UserResponseDtoWithRoles expected = createValidUserWithRoleResponseDto();
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(user));
        when(userMapper.toDtoWithRoles(user))
                .thenReturn(expected);

        UserResponseDtoWithRoles actual = userService.getUserInfo(VALID_EMAIL);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("register() method for already registered user throws RegistrationException")
    public void register_WithAlreadyRegisteredUser_ThrowRegistrationException() {
        UserRegistrationRequestDto requestDto = createValidUserRegistrationRequestDto();
        when(userRepository.findByEmail(requestDto.email()))
                .thenReturn(Optional.of(createValidUser()));

        assertThrows(RegistrationException.class, () -> userService.register(requestDto));
    }

    @Test
    @DisplayName("updateUserRole() method with invalid 'Role' throws EntityNotFoundException")
    public void updateUserRole_WithNotValidRole_ThrowEntityNotFoundException() {
        User user = createValidUser();
        when(userRepository.findById(VALID_ID))
                .thenReturn(Optional.of(user));
        when(roleRepository.findByName(any(Role.RoleName.class)))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUserRole(VALID_ID,
                new UserUpdateRoleRequestDto(
                        createValidRole(VALID_ID, Role.RoleName.ROLE_CUSTOMER))));
    }

    @Test
    @DisplayName("getUserInfo() method with invalid 'email' throws EntityNotFoundException")
    void getUserInfo_WithNotValidEmail_ThrowEntityNotFoundException() {
        when(userRepository.findByEmail(NOT_VALID_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserInfo(NOT_VALID_EMAIL));
    }
}
