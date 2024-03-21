package mate.academy.carsharing.service.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import mate.academy.carsharing.model.TelegramUserInfo;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.telegram.TelegramUserInfoRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.telegram.TelegramMessageEvent;
import mate.academy.carsharing.telegram.TelegramUserState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class TelegramUserServiceTest {
    private static final String REGISTRATION_SUCCESS = """
            Registration success. I know next commands: '/checkRentals'
            """;
    private static final String VALID_EMAIL = "testemail@mail.com";
    private static final String INVALID_EMAIL = "NOT VALID EMAIL";
    private static final Long VALID_ID = 1L;
    private static final Long VALID_CHAT_ID = 123456L;
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    @Mock
    private UserRepository userRepository;
    @Mock
    private TelegramUserInfoRepository telegramUserInfoRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Captor
    private ArgumentCaptor<User> captor;
    @InjectMocks
    private TelegramUserServiceImpl telegramUserService;

    private User createValidUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setLastName(VALID_LAST_NAME);
        user.setFirstName(VALID_FIRST_NAME);
        user.setPassword(VALID_PASSWORD);
        user.setRoles(new HashSet<>());
        return user;
    }

    private TelegramUserState createValidTelegramUserState() {
        return new TelegramUserState(
                VALID_ID,
                VALID_EMAIL,
                true
        );
    }

    @Test
    @DisplayName("registerNewUser() method with invalid 'email' sends error message")
    public void registerNewUser_WithValidChatIdAndInvalidEmail_SendErrorMessage() {
        telegramUserService.registerNewUser(VALID_CHAT_ID, INVALID_EMAIL);

        verify(eventPublisher).publishEvent(
                new TelegramMessageEvent(VALID_CHAT_ID,
                        "Not valid email, please send again"));
    }

    @Test
    @DisplayName("registerNewUser() method registers a new user and sends success message")
    public void registerNewUser_WithValidChatIdAndUsername_SendSuccessRegistrationMessage() {
        TelegramUserState telegramUserState = new TelegramUserState(VALID_ID, VALID_EMAIL, false);
        telegramUserService.addUserState(VALID_CHAT_ID, telegramUserState);

        when(telegramUserInfoRepository.findByChatId(VALID_CHAT_ID))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(VALID_EMAIL))
                .thenReturn(Optional.of(createValidUser()));

        telegramUserService.registerNewUser(VALID_CHAT_ID, VALID_EMAIL);

        verify(eventPublisher)
                .publishEvent(new TelegramMessageEvent(VALID_CHAT_ID, REGISTRATION_SUCCESS));
    }

    @Test
    @DisplayName("getUserState() method works")
    void getUserState_ValidChatId_ValidTelegramUserState() {
        TelegramUserState expected = createValidTelegramUserState();
        telegramUserService.addUserState(VALID_CHAT_ID, expected);

        TelegramUserState actual = telegramUserService.getUserState(VALID_CHAT_ID);

        Assertions.assertEquals(expected, actual);
    }
}
