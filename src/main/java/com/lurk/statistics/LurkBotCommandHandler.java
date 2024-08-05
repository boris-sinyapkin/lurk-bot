package com.lurk.statistics;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import com.lurk.statistics.command.LurkCommand;
import com.lurk.statistics.command.LurkGetKnownNodes;
import com.lurk.statistics.command.LurkHelp;
import com.lurk.statistics.database.LurkDatabaseHelper;

/*
 * Handles incoming Telegram commands. 
 */
public class LurkBotCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(LurkBot.class);

    private final LurkHttpClientWrapper httpClientWrapper;
    private final LurkNodeManager nodeManager;
    private final Set<LurkCommand> commands;

    public LurkBotCommandHandler(LurkDatabaseHelper databaseHelper) {
        nodeManager = new LurkNodeManager(databaseHelper);
        httpClientWrapper = new LurkHttpClientWrapper();
        commands = Set.of(
                new LurkHelp(),
                new LurkGetKnownNodes(httpClientWrapper, nodeManager));
    }

    public SendMessage handle(String name, long chatId) {
        LurkCommand command = findCommandByName(name);
        if (command != null) {
            log.info("Executing command {} from chat_id={}", name, chatId);
            return command.execute(chatId);
        }

        log.error("Unknown command '{}' sent from chat_id={}", name, chatId);
        return LurkUtils.buildMessageWithText(chatId,
                "Unknown command '%s'. Try /help to see the list of available commands",
                name);
    }

    private LurkCommand findCommandByName(String name) {
        for (LurkCommand command : commands) {
            if (command.name().equals(name)) {
                return command;
            }
        }
        return null;
    }

}