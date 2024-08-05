package com.lurk.statistics.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import com.lurk.statistics.LurkUtils;

/*
 * This command initiates conversation with bot and usually sent as
 * a first message by the user. 
 */
public class LurkStart implements LurkCommand {

    @Override
    public SendMessage execute(long chatId) {
        String startText = """
                Greetings!

                See /help for more information about available commands.
                """;
        return LurkUtils.buildMessageWithText(chatId, startText);
    }

    @Override
    public String path() {
        return "/start";
    }

    @Override
    public String name() {
        return "/start";
    }

}
