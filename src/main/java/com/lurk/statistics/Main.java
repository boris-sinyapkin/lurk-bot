package com.lurk.statistics;

import java.io.IOException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigProviderResolver;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LurkConfiguration config = getConfig();
        try (TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication()) {
            application.registerBot(config.telegramBotToken(), new LurkBot(config));
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Exception occured while bot was running", e);
        }
    }

    private static LurkConfiguration getConfig() throws IOException {
        URL url = Main.class.getResource("/application.properties");
        if (url == null) {
            throw new IOException("application.properties not found");
        }
        SmallRyeConfig config = new SmallRyeConfigProviderResolver().getBuilder()
                .withMapping(LurkConfiguration.class)
                .withSources(new PropertiesConfigSource(url))
                .build();
        return config.getConfigMapping(LurkConfiguration.class);
    }
}