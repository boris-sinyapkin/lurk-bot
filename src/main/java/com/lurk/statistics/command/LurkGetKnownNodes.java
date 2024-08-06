package com.lurk.statistics.command;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import com.lurk.statistics.LurkHttpClientWrapper;
import com.lurk.statistics.LurkNode;
import com.lurk.statistics.LurkNodeManager;
import com.lurk.statistics.LurkNodeStatus;
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

    /*
     * HTTP path of this endpoint on the remote side
     */
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

        StringBuilder messageText = new StringBuilder("🌿 *Nodes health status*\n\n");
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

        HttpResponse<String> httpResponse = null;
        HttpRequest httpRequest = LurkHttpClientWrapper.buildHttpGetRequest(nodeUri);

        try {
            log.info("Sending request to {}", nodeUri);
            httpResponse = httpClientWrapper.send(httpRequest);
        } catch (HttpTimeoutException e) {
            log.error("Request to {} is timed out", node);
            return new HealthcheckResult(node, "request is timed out");
        } catch (ConnectException e) {
            log.error("Error occurred while attempting to connect a socket to a remote address and port {}", node);
            return new HealthcheckResult(node, "network error occured or connection was refused remotely");
        } catch (IOException | InterruptedException e) {
            log.error("Exception thrown while sending request to {}", node, e);
            return new HealthcheckResult(node, (e.getMessage() != null) ? e.getMessage() : "unknown error occured");
        }

        log.info("Received response from {}: version={}, method={}, code={}, body_length={}", nodeUri,
                httpResponse.version(), httpResponse.request().method(), httpResponse.statusCode(),
                httpResponse.body().length());

        // Check that HTTP_OK status code is received
        if (httpResponse.statusCode() == 200) {
            // Parse JSON from received HTTP body
            return new HealthcheckResult(node, LurkNodeStatus.from(httpResponse.body()));
        }

        return new HealthcheckResult(node, "unexpected HTTP status code (%d)".formatted(httpResponse.statusCode()));
    }

    private class HealthcheckResult {

        final LurkNode node;
        final LurkNodeStatus nodeStatus;
        final String errorMessage;

        HealthcheckResult(LurkNode node, String failureMessage) {
            this.node = node;
            this.nodeStatus = null;
            this.errorMessage = failureMessage;
        }

        HealthcheckResult(LurkNode targetNode, LurkNodeStatus nodeStatus) {
            this.node = targetNode;
            this.nodeStatus = nodeStatus;
            this.errorMessage = null;
        }

        String asMarkdown() {
            if (errorMessage != null) {
                return "🔴 Node __%s__ is *unreachable*: %s".formatted(node, errorMessage);
            }

            StringBuilder str = new StringBuilder("🟢 Node __%s__ is *up and running*!".formatted(node));
            if (nodeStatus != null) {
                log.info("Node {} information: {}", node, nodeStatus);
                Duration nodeUptime = nodeStatus.getNodeUptime();
                str.append("\n        🕒 started at *%s*".formatted(nodeStatus.getNodeStartedUtcDate()));
                str.append("\n        🫀 alive *%d* hours *%d* min *%d* sec"
                        .formatted(nodeUptime.toHoursPart(), nodeUptime.toMinutesPart(), nodeUptime.toSecondsPart()));
            }
            return str.toString();
        }
    }

}
