package mate.academy.carsharing.telegram;

public record TelegramUserState(
        Long chatId,
        String email,
        boolean awaitingEmail) {
}
