package com.qlmcp.backend;

import org.springframework.ai.mcp.client.common.autoconfigure.McpToolCallbackAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    // McpToolCallbackAutoConfiguration은 외부 MCP 서버의 Tool을 다 가져와서 Tool로 등록을 시킴
    // 따라서 프로젝트 내부에서 정의한 Tool만 외부에서 사용할 Tool로 등록하기 위해 제외
    McpToolCallbackAutoConfiguration.class
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
