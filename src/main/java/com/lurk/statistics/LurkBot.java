package com.lurk.statistics;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import java.io.IOException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.lurk.statistics.database.LurkDatabaseHelper;

public class LurkBot implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(LurkBot.class);

    private final TelegramClient telegramClient;
    private final LurkBotCommandHandler commandHandler;
    private final LurkDatabaseHelper databaseHelper;

    public LurkBot(LurkConfiguration config) {
        telegramClient = new OkHttpTelegramClient(config.telegramBotToken());
        databaseHelper = new LurkDatabaseHelper(config.database().url(), config.database().username(),
                config.database().password());
        commandHandler = new LurkBotCommandHandler(databaseHelper);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            onMessageReceived(update.getMessage());
        }
    }

    private void onMessageReceived(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            long chatId = message.getChatId();
            SendMessage response = commandHandler.handle(text, chatId);
            try {
                telegramClient.execute(response);
            } catch (TelegramApiException e) {
                log.error("Exception thrown while sending response to chatId={}", chatId, e);
            }
        }
    }
}
