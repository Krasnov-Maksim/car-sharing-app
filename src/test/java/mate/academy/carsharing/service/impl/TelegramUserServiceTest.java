package mate.academy.carsharing.service.impl;

import static mate.academy.carsharing.util.TestUtils.NOT_VALID_EMAIL;
import static mate.academy.carsharing.util.TestUtils.REGISTRATION_SUCCESS;
import static mate.academy.carsharing.util.TestUtils.VALID_CHAT_ID;
import static mate.academy.carsharing.util.TestUtils.VALID_EMAIL;
import static mate.academy.carsharing.util.TestUtils.VALID_ID;
import static mate.academy.carsharing.util.TestUtils.createValidTelegramUserState;
import static mate.academy.carsharing.util.TestUtils.createValidUser;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import mate.academy.carsharing.repository.telegram.TelegramUserInfoRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.telegram.TelegramMessageEvent;
import mate.academy.carsharing.telegram.TelegramUserState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class TelegramUserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TelegramUserInfoRepository telegramUserInfoRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private TelegramUserServiceImpl telegramUserService;

    @Test
    @DisplayName("registerNewUser() method with invalid 'email' sends error message")
    public void registerNewUser_WithValidChatIdAndInvalidEmail_SendErrorMessage() {
        telegramUserService.registerNewUser(VALID_CHAT_ID, NOT_VALID_EMAIL);

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
