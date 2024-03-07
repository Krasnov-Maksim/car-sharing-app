package mate.academy.carsharing.service.impl;

import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.service.TelegramUserService;
import mate.academy.carsharing.telegram.TelegramUserState;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelegramUserServiceImpl implements TelegramUserService {

    @Override
    public void registerNewUser(Long chatId, String username) {
    }

    @Override
    public TelegramUserState getUserState(Long chatId) {
        // FIXME:
        return new TelegramUserState(100L, "email", true);
    }

    @Override
    public void addUserState(Long chatId, TelegramUserState state) {
    }
}
