package com.qlmcp.backend;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private ObjectProvider<List<SyncToolSpecification>> tools;

    @Autowired
    ObjectProvider<List<McpSyncClient>> syncMcpClients;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Spring Application Context가 정상적으로 로드되는지 테스트")
    void contextLoads() {
        String appName = environment.getProperty("spring.application.name");
        assertEquals("backend", appName);
    }

    @Test
    @DisplayName("SyncTool 전체를 불렀을 때 서버 툴만 불러오는지 테스트")
    void internalToolTest() {
        List<SyncToolSpecification> serverTools = tools.stream().flatMap(List::stream).toList();
        assertAll(() -> assertNotNull(serverTools), () -> assertEquals(1, serverTools.size()));
    }
}
