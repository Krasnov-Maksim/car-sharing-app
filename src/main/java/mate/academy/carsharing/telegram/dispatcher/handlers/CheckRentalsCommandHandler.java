package mate.academy.carsharing.telegram.dispatcher.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mate.academy.carsharing.dto.rental.RentalResponseDto;
import mate.academy.carsharing.dto.rental.RentalSearchParametersDto;
import mate.academy.carsharing.model.TelegramUserInfo;
import mate.academy.carsharing.model.User;
import mate.academy.carsharing.repository.telegram.TelegramUserInfoRepository;
import mate.academy.carsharing.service.RentalService;
import mate.academy.carsharing.telegram.TelegramMessageEvent;
import mate.academy.carsharing.telegram.dispatcher.CommandHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CheckRentalsCommandHandler implements CommandHandler {
    private static final String COMMAND = "/checkRentals";
    private static final String CAN_T_FIND_YOR_ACCOUNT = "Can't find yor account in DB :(";
    private final RentalService rentalService;
    private final TelegramUserInfoRepository telegramUserInfoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void handleCommand(Long chatId, String command, String[] args) {
        Optional<TelegramUserInfo> optionalWithTelegramUserInfo =
                telegramUserInfoRepository.findByChatId(chatId);
        if (optionalWithTelegramUserInfo.isEmpty()) {
            eventPublisher.publishEvent(
                    new TelegramMessageEvent(chatId, CAN_T_FIND_YOR_ACCOUNT));
            return;
        }
        TelegramUserInfo telegramUserInfo = optionalWithTelegramUserInfo.get();
        User user = telegramUserInfo.getUser();

        String[] userIds = new String[]{user.getId().toString()};
        String[] isActive = new String[]{""};
        RentalSearchParametersDto rentalSearchParametersDto =
                new RentalSearchParametersDto(userIds, isActive);
        Pageable pageable = PageRequest.of(0, 20);
        List<RentalResponseDto> allRentals =
                rentalService.searchRentals(rentalSearchParametersDto, pageable);

        if (allRentals.isEmpty()) {
            eventPublisher.publishEvent(
                    new TelegramMessageEvent(chatId, "You don't have rentals"));
            return;
        }
        List<String> resultList = allRentals.stream()
                .map(dto -> {
                    try {
                        return objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(dto);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        eventPublisher.publishEvent(
                new TelegramMessageEvent(chatId, resultList.toString()));
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
