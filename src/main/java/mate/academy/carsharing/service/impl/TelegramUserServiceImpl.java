package mate.academy.carsharing.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.model.TelegramUserInfo;
import mate.academy.carsharing.repository.telegram.TelegramUserInfoRepository;
import mate.academy.carsharing.repository.user.UserRepository;
import mate.academy.carsharing.service.TelegramUserService;
import mate.academy.carsharing.telegram.TelegramMessageEvent;
import mate.academy.carsharing.telegram.TelegramUserState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelegramUserServiceImpl implements TelegramUserService {
    private static final String REGISTRATION_SUCCESS = """
            Registration success. I know next commands: '/checkRentals'
            """;
    private static final String YOU_ALREADY_REGISTERED = "You are already registered";
    private static final String NOT_VALID_EMAIL = "Not valid email, please send again";
    private final TelegramUserInfoRepository telegramUserInfoRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<Long, TelegramUserState> userStates = new HashMap<>();

    @Override
    public void registerNewUser(Long chatId, String username) {
        if (!isEmail(username) || userRepository.findByEmail(username).isEmpty()) {
            eventPublisher.publishEvent(
                    new TelegramMessageEvent(chatId, NOT_VALID_EMAIL));
            return;
        }
        Optional<TelegramUserInfo> optionalWithTelegramUserInfo =
                telegramUserInfoRepository.findByChatId(chatId);
        if (optionalWithTelegramUserInfo.isPresent()) {
            String userFirstName = optionalWithTelegramUserInfo.get().getUser().getFirstName();
            userStates.put(chatId, new TelegramUserState(chatId, username, false));
            eventPublisher.publishEvent(new TelegramMessageEvent(chatId,
                    "Hi, " + userFirstName + " print your command! " /*+ YOU_ALREADY_REGISTERED*/));
            return;
        }
        userStates.put(chatId, new TelegramUserState(chatId, username, false));
        TelegramUserInfo telegramUserInfo = new TelegramUserInfo();
        telegramUserInfo.setChatId(chatId);
        telegramUserInfo.setUser(userRepository.findByEmail(username).get());
        telegramUserInfoRepository.save(telegramUserInfo);
        eventPublisher.publishEvent(new TelegramMessageEvent(chatId, REGISTRATION_SUCCESS));
    }

    @Override
    public TelegramUserState getUserState(Long chatId) {
        return userStates.get(chatId);
    }

    @Override
    public void addUserState(Long chatId, TelegramUserState state) {
        userStates.put(chatId, state);
    }

    private boolean isEmail(String email) {
        Pattern p = Pattern.compile("\\b[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,4}\\b",
                Pattern.CASE_INSENSITIVE);
        return p.matcher(email).find();
    }
}
