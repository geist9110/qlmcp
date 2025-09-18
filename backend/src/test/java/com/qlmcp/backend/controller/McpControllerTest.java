package com.qlmcp.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlmcp.backend.dto.JsonRpcRequest.McpRequest;
import com.qlmcp.backend.dto.JsonRpcResponse;
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
    @DisplayName("MCP Initialize 요청 처리")
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

        Mockito.when(mcpService.initialize(1))
            .thenReturn(objectMapper.readValue(expectResponseBody, JsonRpcResponse.class));

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

    @Test
    @DisplayName("MCP Notification 요청 처리")
    void handleMcp_notificationsInitialized() throws Exception {
        // given
        String requestBody = """
            {
              "jsonrpc": "2.0",
              "method": "notifications/initialized"
            }
            """;

        // when & then
        mockMvc.perform(post("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isAccepted())
            .andDo(document("mcp-notifications-initialized",
                requestFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("method").description("호출 메서드명 (예: 'notificationsInitialized')")
                )
            ));
    }

    @Test
    @DisplayName("MCP Error 응답 처리")
    void handleMcp_error() throws Exception {
        // given
        String expectResponseBody = """
            {
              "jsonrpc": "2.0",
              "id": 2,
              "error": {
                "code": -32601,
                "message": "Method not found"
              }
            }
            """;

        String requestBody = """
            {
              "jsonrpc": "2.0",
              "id": 2,
              "method": "unknownMethod",
              "params": {}
            }
            """;

        // when & then
        mockMvc.perform(post("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isInternalServerError())
            .andDo(document("mcp-error",
                requestFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("method").description("호출 메서드명 (예: 'unknownMethod')"),
                    fieldWithPath("params").description("메서드 매개변수 (비어있을 수 있음)")
                ),
                responseFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("error.code").description("에러 코드"),
                    fieldWithPath("error.message").description("에러 메시지")
                )
            ));
    }

    @Test
    @DisplayName("MCP Tools List 요청 처리")
    void handleMcp_toolsList() throws Exception {
        // given
        String expectResponseBody = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "result": {
                "tools": [
                  {
                    "name": "get_weather",
                    "title": "Weather Information Provider",
                    "description": "Get current weather information for a location",
                    "inputSchema": {
                      "type": "object",
                      "properties": {
                        "location": {
                          "type": "string",
                          "description": "City name or zip code"
                        }
                      },
                      "required": ["location"]
                    }
                  }
                ],
                "nextCursor": "next-page-cursor"
              }
            }
            """;

        Mockito.when(mcpService.toolList(1))
            .thenReturn(objectMapper.readValue(expectResponseBody, JsonRpcResponse.class));

        String requestBody = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "method": "tools/list",
              "params": {
                "cursor": "optional-cursor-value"
              }
            }
            """;

        // when & then
        mockMvc.perform(post("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andDo(document("mcp-tools-list",
                requestFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("method").description("호출 메서드명 (예: 'tools/list')"),
                    fieldWithPath("params.cursor").description("페이지네이션을 위한 커서 (Optional)")
                ),
                responseFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("result.tools[].name").description("툴 이름"),
                    fieldWithPath("result.tools[].title").description("툴 표시 이름"),
                    fieldWithPath("result.tools[].description").description("툴 설명"),
                    fieldWithPath("result.tools[].inputSchema.type").description(
                        "툴 입력 스키마 타입 (예: 'object')"),
                    fieldWithPath(
                        "result.tools[].inputSchema.properties.location.type").description(
                        "입력 파라미터 타입 (예: 'string')"),
                    fieldWithPath(
                        "result.tools[].inputSchema.properties.location.description").description(
                        "입력 파라미터 설명"),
                    fieldWithPath("result.tools[].inputSchema.required[]").description(
                        "필수 입력 파라미터 목록"),
                    fieldWithPath("result.nextCursor").description("다음 페이지를 위한 커서 (Optional)")
                )
            ));
    }

    @Test
    @DisplayName("MCP Tools Call 요청 처리")
    void handleMcp_toolsCall() throws Exception {
        // given
        String expectResponseBody = """
                {
                  "jsonrpc": "2.0",
                  "id": 2,
                  "result": {
                    "content": [
                      {
                        "type": "text",
                        "text": "Current weather in New York:\\nTemperature: 72°F\\nConditions: Partly cloudy"
                      }
                    ],
                    "isError": false
                  }
                }
            """;

        Mockito.when(mcpService.callTools(any(McpRequest.class)))
            .thenReturn(objectMapper.readValue(expectResponseBody, JsonRpcResponse.class));

        String requestBody = """
                {
                  "jsonrpc": "2.0",
                  "id": 2,
                  "method": "tools/call",
                  "params": {
                    "name": "get_weather",
                    "arguments": {
                      "location": "New York"
                    }
                  }
                }
            """;

        // when & then
        mockMvc.perform(post("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andDo(document("mcp-tools-call",
                requestFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("method").description("호출 메서드명 (예: 'tools/call')"),
                    fieldWithPath("params.name").description("호출할 툴 이름"),
                    fieldWithPath("params.arguments.location").description("툴에 전달할 인자")
                ),
                responseFields(
                    fieldWithPath("jsonrpc").description("JSON-RPC 프로토콜 버전 (예: '2.0')"),
                    fieldWithPath("id").description("요청 식별자"),
                    fieldWithPath("result.content[].type").description("응답 콘텐츠 타입 (예: 'text')"),
                    fieldWithPath("result.content[].text").description("응답 텍스트 내용"),
                    fieldWithPath("result.isError").description("툴 실행 오류 여부")
                )));
    }
}