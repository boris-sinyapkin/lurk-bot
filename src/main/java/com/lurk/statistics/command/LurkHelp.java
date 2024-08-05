package com.lurk.statistics.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.lurk.statistics.LurkUtils;

public class LurkHelp implements LurkCommand {

    public LurkHelp() {}

    @Override
    public SendMessage execute(long chatId) {
        String helpText = """
                Commands:
                    /help - view this information
                    /myproxies - list available proxies
                """;
        return LurkUtils.buildMessageWithText(chatId, helpText);
    }

    @Override
    public String path() {
        return "/help";
    }

    @Override
    public String name() {
        return "/help";
    }

}
