# QLMCP(Query Language Model Context Protocol)

> 하나의 Tool로 MCP 서비스를 사용하세요.

QLMCP는 자연어 요청만으로 여러 MCP(Model Context Protocol) 서버들을 지능적으로 연결해주는 Gateway 서비스입니다.  
복잡한 MCP 서버 설정 없이, 단 하나의 URL만 등록하면 모든 기능을 사용할 수 있습니다.

## ✨ 주요 기능

### 현재 지원하는 MCP 서버

- [korea_weather_mcp](https://github.com/ohhan777/korea_weather)
- [context7](https://github.com/upstash/context7)
- [naver-search-mcp](https://github.com/isnow890/naver-search-mcp)

### 핵심 기능

- `query`: 자연어로 요청 시 적절한 MCP Tool을 선택하여 처리합니다.

```
"창원의 맛집이 궁금해" -> "naver-search-mcp의 검색 기능 사용" -> "{result: {name: ..., address: ...}}"
```

## 🚀 시작하기

### Claude Desktop에서 사용하기

1. Claude Desktop의 설정 들어가기
2. 커넥터 탭 선택
3. 커스텀 커넥터 추가
4. 아래 정보 입력
    - 이름 : QLMCP
    - 원격 MCP 서버 URL : `https://mcp.qlmcp.com/sse`

## 🗺️ 로드맵

### v1.0.0(현재 버전)

- 기본 query 툴 사용 가능
- 3개 MCP 서버 통합
- SSE 프로토콜 지원

### 향후 계획

- 더 많은 MCP 서버 통합
- 사용자 맞춤형 툴 설정 기능
- 사용자 맞춤형 메모리 기능
- 웹 대시보드를 통한 툴 관리 기능

## 📝 라이선스

MIT License