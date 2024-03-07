package mate.academy.carsharing.telegram;

public record TelegramMessageEvent(Long chatId, String message) {
}
