package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final Pattern NOTIFICATION_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final String WELCOME_TEXT =
            "Привет! Я бот-напоминалка.\n"
                    + "Отправь мне сообщение в формате:\n"
                    + "01.01.2022 20:00 Сделать домашнюю работу\n"
                    + "и в указанное время я пришлю тебе напоминание.";

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository repository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            logger.info("Processing update: {}", update);
            if (update.message() == null || update.message().text() == null) {
                continue;
            }
            String text = update.message().text();
            Long chatId = update.message().chat().id();

            if ("/start".equals(text)) {
                telegramBot.execute(new SendMessage(chatId, WELCOME_TEXT));
                continue;
            }

            Matcher matcher = NOTIFICATION_PATTERN.matcher(text);
            if (matcher.matches()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
                    NotificationTask task = new NotificationTask();
                    task.setChatId(chatId);
                    task.setMessage(matcher.group(3));
                    task.setDateTime(dateTime);
                    repository.save(task);
                    telegramBot.execute(new SendMessage(chatId, "Напоминание сохранено!"));
                } catch (DateTimeParseException e) {
                    telegramBot.execute(new SendMessage(chatId,
                            "Некорректная дата или время. Формат: дд.ММ.гггг чч:мм текст"));
                }
            } else {
                telegramBot.execute(new SendMessage(chatId,
                        "Не понял. Формат: 01.01.2022 20:00 Сделать домашнюю работу"));
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
