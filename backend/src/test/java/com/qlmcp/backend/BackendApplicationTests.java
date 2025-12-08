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

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private ObjectProvider<List<SyncToolSpecification>> tools;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("[SUCCESS] contextLoads - no input -> load spring application context")
    void contextLoads_NoInput_LoadSpringApplicationContext() {
        String appName = environment.getProperty("spring.application.name");
        assertEquals("backend", appName);
    }

    @Test
    @DisplayName("[SUCCESS] externalTool - no input -> load only external tool")
    void externalTool_NoInput_LoadOnlyExternalTool() {
        List<SyncToolSpecification> serverTools = tools.stream().flatMap(List::stream).toList();
        assertAll(() -> assertNotNull(serverTools), () -> assertEquals(1, serverTools.size()));
    }
}
