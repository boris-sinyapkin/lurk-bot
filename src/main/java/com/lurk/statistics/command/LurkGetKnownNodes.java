package com.lurk.statistics.command;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import com.lurk.statistics.LurkHttpClientWrapper;
import com.lurk.statistics.LurkNode;
import com.lurk.statistics.LurkNodeManager;
import com.lurk.statistics.LurkUtils;
import com.lurk.statistics.LurkUtils.MessageParseMode;

/*
 * This command returns the list of known nodes user is eligible for.
 * Description of particular node contains the following information:
 *  -   IP:PORT (address)
 *  -   Location
 *  -   Data rate
 */
public class LurkGetKnownNodes implements LurkCommand {

    private static final Logger log = LoggerFactory.getLogger(LurkGetKnownNodes.class);

    private final LurkNodeManager nodeManager;
    private final LurkHttpClientWrapper httpClientWrapper;

    public LurkGetKnownNodes(LurkHttpClientWrapper httpClientWrapper, LurkNodeManager nodeManager) {
        this.nodeManager = nodeManager;
        this.httpClientWrapper = httpClientWrapper;
    }

    @Override
    public String path() {
        return "/healthcheck";
    }

    @Override
    public String name() {
        return "/myproxies";
    }

    @Override
    public SendMessage execute(long chatId) {
        // Retrieve eligible nodes for input chat_id.
        Set<LurkNode> visibleNodes = nodeManager.getVisibleNodes(chatId);
        log.debug("There's {} nodes visible for chat_id={}", visibleNodes.size(), chatId);

        // Bail out if chat-id doesn't have any nodes.
        if (visibleNodes.isEmpty()) {
            return LurkUtils.buildMessageWithText(chatId, "There's no visible nodes available for you");
        }

        StringBuilder messageText = new StringBuilder("*__Nodes health status__*:\n\n");
        // Iterate over visible nodes, request their health status
        // and construct response message.
        visibleNodes.forEach(node -> {
            HealthcheckResult result = doHealthcheck(node);
            messageText.append(result.asMarkdown() + "\n\n");
        });

        return LurkUtils.buildMessageWithText(chatId, messageText.toString(), MessageParseMode.MARKDOWN);
    }

    private HealthcheckResult doHealthcheck(LurkNode node) {
        URI nodeUri = node.getHttpUri(path());
        HealthcheckResult result = new HealthcheckResult(node);

        HttpResponse<String> httpResponse;
        HttpRequest httpRequest = LurkHttpClientWrapper.buildHttpGetRequest(nodeUri);

        try {
            log.debug("Sending request to {}", nodeUri);
            httpResponse = httpClientWrapper.send(httpRequest);
            result.setHttpStatusCode(httpResponse.statusCode());
        } catch (ConnectException e) {
            log.error("Error occurred while attempting to connect a socket to a remote address and port {}", node);
            result.setErrorMessage("network error occured or connection was refused remotely");
        } catch (IOException | InterruptedException e) {
            log.error("Exception thrown while sending request to {}", node, e);
            if (e.getMessage() != null) {
                result.setErrorMessage(e.getMessage());
            }
        }

        return result;
    }

    private class HealthcheckResult {
        final LurkNode targetNode;

        Optional<Integer> httpStatusCode;
        Optional<String> errorMessage;

        HealthcheckResult(LurkNode targetNode) {
            this.targetNode = targetNode;
            this.errorMessage = Optional.empty();
            this.httpStatusCode = Optional.empty();
        }

        void setHttpStatusCode(Integer code) {
            httpStatusCode = Optional.of(code);
        }

        void setErrorMessage(String msg) {
            errorMessage = Optional.of(msg);
        }

        String asMarkdown() {
            StringBuilder str = new StringBuilder();
            if (httpStatusCode.isPresent()) {
                int code = httpStatusCode.get();
                switch (code) {
                    case 200:
                        str.append("🟢 *%s* is *up and running*!".formatted(targetNode.toString()));
                        break;

                    default:
                        str.append("🟡 *%s* responded with unexpected HTTP code (%d)".formatted(targetNode.toString(),
                                code));
                        break;
                }
            } else if (errorMessage.isPresent()) {
                str.append(
                        "🔴 *%s* is *unreachable*: %s".formatted(targetNode.toString(), errorMessage.get()));
            } else {
                str.append(
                        "🔴 *%s* is in unknown state".formatted(targetNode.toString()));
            }
            return str.toString();
        }
    }

}
