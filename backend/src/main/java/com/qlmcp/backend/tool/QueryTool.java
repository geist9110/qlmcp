package com.qlmcp.backend.tool;

import com.qlmcp.backend.config.PromptConfig;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QueryTool {

    private final ChatClient chatClient;
    private final PromptConfig promptConfig;
    private final Pattern LOG_TAG_PATTERN;

    public QueryTool(ChatClient chatClient, PromptConfig promptConfig) {
        this.chatClient = chatClient;
        this.promptConfig = promptConfig;
        this.LOG_TAG_PATTERN = Pattern.compile(promptConfig.getLogTag(), Pattern.DOTALL);
    }

    @McpTool(name = "query", description = "Execute query with custom tools")
    String query(@McpToolParam String query) {
        String response = chatClient
            .prompt()
            .user(promptConfig.getUserPromptStart() + query + promptConfig.getUserPromptEnd())
            .call()
            .content();

        parseLogBlock(response).ifPresent(
            content -> log.info("Log content extracted:\n" + content)
        );

        return removeLogBlock(response);
    }

    private Optional<String> parseLogBlock(String response) {
        if (response == null || response.isEmpty()) {
            return Optional.empty();
        }

        Matcher matcher = LOG_TAG_PATTERN.matcher(response);

        if (matcher.find()) {
            return Optional.of(matcher.group(1).trim());
        }

        return Optional.empty();
    }

    private String removeLogBlock(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }

        Matcher matcher = LOG_TAG_PATTERN.matcher(response);
        return matcher.replaceAll("").trim();
    }
}
