package com.qlmcp.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlmcp.backend.dto.McpRequest;
import com.qlmcp.backend.dto.McpResponse;
import com.qlmcp.backend.service.McpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(McpController.class)
@AutoConfigureRestDocs
class McpControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private McpService mcpService;

    @Test
    @DisplayName("MCP Initialize 요청 처리 - 성공")
    void handleMcp_initialize() throws Exception {
        // given
        String expectResponseBody = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "protocolVersion": "2025-06-18",
                "capabilities": {
                  "logging": {},
                  "prompts": {
                    "listChanged": true
                  },
                  "resources": {
                    "subscribe": true,
                    "listChanged": true
                  },
                  "tools": {
                    "listChanged": true
                  }
                },
                "serverInfo": {
                  "name": "ExampleServer",
                  "title": "Example Server Display Name",
                  "version": "1.0.0"
                },
                "instructions": "Optional instructions for the client"
              }
            }
            """;

        Mockito.when(mcpService.createResponse(any(McpRequest.class)))
            .thenReturn(objectMapper
                .readValue(expectResponseBody, McpResponse.class));

        String requestBody = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "method": "initialize",
              "params": {
                "protocolVersion": "2025-06-18",
                "capabilities": {
                  "roots": {
                    "listChanged": true
                  },
                  "sampling": {},
                  "elicitation": {}
                },
                "clientInfo": {
                  "name": "ExampleClient",
                  "title": "Example Client Display Name",
                  "version": "1.0.0"
                }
              }
            }
            """;

        // when & then
        mockMvc.perform(post("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andDo(document("mcp-initialize",
                requestFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("method").description("호출 메서드명 (예: 'initialize')"),
                    fieldWithPath("params.protocolVersion").description(
                        "프로토콜 버전 (예: '2025-06-18')"),
                    fieldWithPath("params.capabilities.roots.listChanged").description(
                        "roots 기능의 listChanged 지원 여부"),
                    fieldWithPath("params.capabilities.sampling").description("샘플링 기능 (비어있을 수 있음)"),
                    fieldWithPath("params.capabilities.elicitation").description(
                        "elicitation 기능 (비어있을 수 있음)"),
                    fieldWithPath("params.clientInfo.name").description("클라이언트 이름"),
                    fieldWithPath("params.clientInfo.title").description("클라이언트 표시 이름"),
                    fieldWithPath("params.clientInfo.version").description("클라이언트 버전")
                ),
                responseFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("result.protocolVersion").description(
                        "프로토콜 버전 (예: '2025-06-18')"),
                    fieldWithPath("result.capabilities.logging").description("로깅 기능 (비어있을 수 있음)"),
                    fieldWithPath("result.capabilities.prompts.listChanged").description(
                        "prompts 기능의 listChanged 지원 여부"),
                    fieldWithPath("result.capabilities.resources.subscribe").description(
                        "resources 기능의 subscribe 지원 여부"),
                    fieldWithPath("result.capabilities.resources.listChanged").description(
                        "resources 기능의 listChanged 지원 여부"),
                    fieldWithPath("result.capabilities.tools.listChanged").description(
                        "tools 기능의 listChanged 지원 여부"),
                    fieldWithPath("result.serverInfo.name").description("서버 이름"),
                    fieldWithPath("result.serverInfo.title").description("서버 표시 이름"),
                    fieldWithPath("result.serverInfo.version").description("서버 버전"),
                    fieldWithPath("result.instructions").description("클라이언트용 안내문 (Optional)")
                )
            ));
    }
}