package mate.academy.carsharing.service;

public interface NotificationService {
    void sendNotification(Long id, String message);

    void sendGlobalNotification(String message);
}
