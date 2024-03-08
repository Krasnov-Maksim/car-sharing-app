package mate.academy.carsharing.telegram.dispatcher.handlers;

import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.telegram.TelegramMessageEvent;
import mate.academy.carsharing.telegram.dispatcher.CommandHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StartCommandHandler implements CommandHandler {
    private static final String COMMAND = "/start";

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handleCommand(Long chatId, String command, String[] args) {
        String welcomeMessage = "Hello, i`m bot! I will help you with Car Sharing App "
                + "Please send me your email!";
        eventPublisher.publishEvent(new TelegramMessageEvent(chatId, welcomeMessage));
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
