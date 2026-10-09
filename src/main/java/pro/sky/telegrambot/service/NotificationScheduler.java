package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationScheduler(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    /** Раз в минуту (в 00 секунд) рассылает напоминания, время которых совпадает с текущей минутой. */
    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findAllByDateTime(now);
        for (NotificationTask task : tasks) {
            SendResponse response = telegramBot.execute(
                    new SendMessage(task.getChatId(), "Напоминание: " + task.getMessage()));
            if (!response.isOk()) {
                logger.error("Не удалось отправить напоминание {}: {} {}",
                        task.getId(), response.errorCode(), response.description());
            }
        }
    }
}
