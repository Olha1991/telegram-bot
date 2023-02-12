package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Pattern PATTERN = Pattern.compile("([0-9\\:\\:\\s]{16})\\s([\\W+]+)");
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    private final NotificationTaskRepository notificationTaskRepository;
    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService notificationTaskService,
                                      NotificationTaskRepository notificationTaskRepository){
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {

        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                String text = update.message().text();
                Long chatId = update.message().chat().id();

                if ("/start".equals(text)) {
                    sendMessage(chatId,
                            "Welcome to chat! Send the scheduled meeting as in the example:\n**01.01.2022 20:00 Сделать домашнюю работу**");

                } else {
                    Matcher matcher = PATTERN.matcher(text);
                    LocalDateTime dateAndTime;
                    if(matcher.find() && (dateAndTime = parse(matcher.group(1)))!=null) {
                        String message = matcher.group(2);
                        notificationTaskService.create(chatId, message, dateAndTime);
                        sendMessage(chatId, "Success!");
                    }else{
                        sendMessage(chatId, "Incorrect data entry.");
                    }
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(Long chatId,String text, ParseMode parseMode){
        SendMessage sendMessage = new SendMessage(
                chatId, text);
        sendMessage.parseMode( parseMode);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if(!sendResponse.isOk()){
            LOGGER.error(sendResponse.toString());
        }
    }

    private void sendMessage(Long chatId,String text){
        sendMessage(chatId, text, ParseMode.MarkdownV2);
    }

    @Nullable
    private LocalDateTime parse(String dateAndTime) {
        try{
            return LocalDateTime.parse(dateAndTime, DATE_TIME_FORMATTER);
        }catch (DateTimeParseException e){
            return null;
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run(){
        Collection<NotificationTask> currentTasks =
                notificationTaskRepository.findAllTasksByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        currentTasks.forEach(task -> sendMessage(task.getUserId(), task.getMessage()));
    }

}
