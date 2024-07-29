package com.lurk.statistics;

import java.util.Properties;
import uk.org.webcompere.lightweightconfig.ConfigLoader;

public final class LurkConfiguration {

    private static final Properties properties;

    public static final String databaseUrl;
    public static final String databaseUsername;
    public static final String databasePassword;
    public static final String telegramBotToken;

    static {
        properties = ConfigLoader.loadPropertiesFromResource("application.properties");
        databaseUrl = properties.getProperty("lurkbot.database.url");
        databaseUsername = properties.getProperty("lurkbot.database.username");
        databasePassword = properties.getProperty("lurkbot.database.password");
        telegramBotToken = properties.getProperty("lurkbot.telegram-bot-token");
    }
}
