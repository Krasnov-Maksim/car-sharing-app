package mate.academy.carsharing.service;

import mate.academy.carsharing.telegram.TelegramUserState;

public interface TelegramUserService {

    void registerNewUser(Long chatId, String username);

    TelegramUserState getUserState(Long chatId);

    void addUserState(Long chatId, TelegramUserState state);
}
