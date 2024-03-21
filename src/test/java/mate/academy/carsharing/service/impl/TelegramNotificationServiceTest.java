package mate.academy.carsharing.service.impl;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import mate.academy.carsharing.model.TelegramUserInfo;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.telegram.TelegramUserInfoRepository;
import mate.academy.carsharing.telegram.TelegramMessageEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationServiceTest {
    private static final String VALID_EMAIL = "testemail@email.com";
    private static final String VALID_PASSWORD = "Password";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final Long VALID_ID = 1L;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private TelegramUserInfoRepository telegramUserInfoRepository;
    @InjectMocks
    private TelegramNotificationServiceImpl telegramNotificationService;

    private User createValidUser() {
        User user = new User();
        user.setId(VALID_ID);
        user.setEmail(VALID_EMAIL);
        user.setPassword(VALID_PASSWORD);
        user.setRoles(new HashSet<>());
        user.setLastName(VALID_LAST_NAME);
        user.setFirstName(VALID_FIRST_NAME);
        return user;
    }

    @Test
    @DisplayName("sendNotification() method sends message to a specific user")
    void sendNotification_WithValidUserId_SendNotificationToSpecificUser() {
        User validUser = createValidUser();
        TelegramUserInfo userInfo = new TelegramUserInfo();
        userInfo.setChatId(123L);
        userInfo.setUser(validUser);
        String message = "Test message";
        when(telegramUserInfoRepository.findByUserId(validUser.getId()))
                .thenReturn(Optional.of(userInfo));

        telegramNotificationService.sendNotification(validUser.getId(), message);

        verify(eventPublisher)
                .publishEvent(new TelegramMessageEvent(userInfo.getChatId(), message));
    }

    @Test
    @DisplayName("sendNotification() method does nothing if user not found")
    void sendNotification_WithNotValidUserId_DoNotSendMessage() {
        Long userId = 1L;
        String message = "Test message";
        when(telegramUserInfoRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        telegramNotificationService.sendNotification(userId, message);

        verify(eventPublisher, never())
                .publishEvent(Mockito.any(TelegramMessageEvent.class));
    }

    @Test
    @DisplayName("sendGlobalNotification() method sends messages to all users")
    void sendGlobalNotification_SendMessageToAllUsers() {
        User validUser = createValidUser();
        TelegramUserInfo userInfo01 = new TelegramUserInfo();
        userInfo01.setId(1L);
        userInfo01.setChatId(123L);
        userInfo01.setUser(validUser);
        TelegramUserInfo userInfo02 = new TelegramUserInfo();
        userInfo02.setId(1L);
        userInfo02.setChatId(456L);
        userInfo02.setUser(createValidUser());
        String message = "Global message";

        List<TelegramUserInfo> userList = List.of(userInfo01, userInfo02);
        Mockito.when(telegramUserInfoRepository.findAll()).thenReturn(userList);

        telegramNotificationService.sendGlobalNotification(message);

        userList.forEach(user -> Mockito.verify(eventPublisher)
                .publishEvent(new TelegramMessageEvent(user.getChatId(), message)));
    }

    @Test
    @DisplayName("sendGlobalNotification() method does nothing if users list is empty")
    void sendGlobalNotification_NoUsers() {
        String message = "Global message";
        Mockito.when(telegramUserInfoRepository.findAll())
                .thenReturn(List.of());

        telegramNotificationService.sendGlobalNotification(message);

        Mockito.verify(eventPublisher, Mockito.never())
                .publishEvent(Mockito.any(TelegramMessageEvent.class));
    }
}
