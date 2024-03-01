package mate.academy.carsharing.service.impl;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.user.UserRegistrationRequestDto;
import mate.academy.carsharing.dto.user.UserResponseDto;
import mate.academy.carsharing.dto.user.UserResponseDtoWithRoles;
import mate.academy.carsharing.dto.user.UserUpdateRoleRequestDto;
import mate.academy.carsharing.exception.EntityNotFoundException;
import mate.academy.carsharing.exception.RegistrationException;
import mate.academy.carsharing.mapper.UserMapper;
import mate.academy.carsharing.model.Role;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.role.RoleRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RegistrationException("Email already registered");
        }
        User newUser = userMapper.toModel(requestDto);
        newUser.setPassword(passwordEncoder.encode(requestDto.password()));
        newUser.setRoles(Set.of(roleRepository.findByName(Role.RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("Can't find role by name: "
                        + Role.RoleName.ROLE_CUSTOMER))));
        User savedUser = userRepository.save(newUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDtoWithRoles updateUserRole(String userEmail,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto) {
        User user = getUserByEmail(userEmail);
        Role roleToAdd = getRoleByRoleName(userUpdateRoleRequestDto.role().getName());
        user.getRoles().add(roleToAdd);
        return userMapper.toDtoWithRoles(userRepository.save(user));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Can't find user by email: " + email));
    }

    private Role getRoleByRoleName(Role.RoleName roleName) {
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new EntityNotFoundException("Can't find role with name=" + roleName));
    }
}
