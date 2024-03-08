package mate.academy.carsharing.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.model.TelegramUserInfo;
import mate.academy.carsharing.repository.telegram.TelegramUserInfoRepository;
import mate.academy.carsharing.service.NotificationService;
import mate.academy.carsharing.telegram.TelegramMessageEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelegramNotificationServiceImpl implements NotificationService {
    private final ApplicationEventPublisher eventPublisher;
    private final TelegramUserInfoRepository telegramUserInfoRepository;

    @Override
    public void sendNotification(Long id, String message) {
        Optional<TelegramUserInfo> byUserId = telegramUserInfoRepository.findByUserId(id);
        if (byUserId.isPresent()) {
            TelegramUserInfo userInfo = byUserId.get();
            eventPublisher.publishEvent(
                    new TelegramMessageEvent(userInfo.getChatId(), message));
        }
    }

    @Override
    public void sendGlobalNotification(String message) {
        List<TelegramUserInfo> listOfUserInfo = telegramUserInfoRepository.findAll();
        if (!listOfUserInfo.isEmpty()) {
            listOfUserInfo.stream()
                    .map(userInfo -> new TelegramMessageEvent(userInfo.getChatId(), message))
                    .forEach(eventPublisher::publishEvent);
        }
    }
}
