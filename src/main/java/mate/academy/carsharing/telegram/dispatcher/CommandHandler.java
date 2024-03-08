package mate.academy.carsharing.telegram.dispatcher;

public interface CommandHandler {
    void handleCommand(Long chatId, String command, String[] args);

    String getCommand();
}
