# QLMCP(Query Language Model Context Protocol)

> 여러 MCP 서버를 하나의 MCP Tool로 통합하는 Natural Language Gateway

QLMCP는 다양한 MCP(Model Context Protocol) 서버를 **단일 MCP Tool(`query`)**로 통합하여 제공하는 Gateway입니다.

하나의 MCP 서버 URL만 등록하면, 사용자와 모델은 자연어 요청만으로 필요한 MCP 기능을 자동으로 사용할 수 있습니다.

MCP Tool을 직접 선택하거나, 여러 MCP 서버를 일일이 등록할 필요가 없습니다.

## ✨ Why QLMCP?

오늘날 MCP 생태계의 도구 연결은 쉽지만 다음과 같은 불편함이 있습니다.

### ❌ 기존 MCP의 구조적 문제

- MCP 서버마다 별도로 등록해야 함
- 어떤 요청이 어떤 MCP Tool을 통해 사용해야 하는지 사용자가 직접 판단해야 함
- 대용량 텍스트를 불러오는 Tool 호출이 모델 컨텍스트를 급격히 소모시키는 문제

### ✅ QLMCP의 해결 방식

- 단일 MCP 서버 URL만 등록하면 전체 기능 사용 가능
- AI 기반 라우팅으로 적절한 MCP Tool 자동 호출
- 응답에 대해 AI를 통한 전처리를 통해 모델 컨텍스트 최소화

이를 통해 **MCP의 복잡성을 AI와 사용자에게서 제거**할 수 있습니다.

## 📝 주요 기능

### 1️⃣ 자연어 Tool 라우팅

사용자는 단순히 자연어로 요청하면 됩니다.

QLMCP는 내부적으로 여러 MCP 서버의 Tool들 중 요청에 가장 적절한 MCP Tool을 자동으로 선택합니다.

```
"내일 비 와?"
→ korea_weather_mcp.weather

"창원의 브런치 카페 찾아줘"
→ naver-search-mcp.search

"이 프로젝트 기록해줘"
→ memory.create
```

### 2️⃣ 단일 MCP Tool:`query`

모든 기능은 다음 형태로 호출됩니다.

```
{
  "tool": "query",
  "args": {
    "query": "내일 서울 날씨 알려줘"
  }
}
```

내부적으로 어떤 MCP 서버, Tool을 호출하든 사용자와 AI에게 노출되는 인터페이스는 단 하나입니다.

### 3️⃣ 지원하는 MCP 서버

- [korea_weather_mcp](https://github.com/ohhan777/korea_weather): 한국 기상 정보
- [context7](https://github.com/upstash/context7): 개발 문서
- [naver-search-mcp](https://github.com/isnow890/naver-search-mcp): 네이버 검색
- Memory: PARA 기반 개인화 Memory

### 4️⃣ 토큰 최적화

QLMCP는 모델이 MCP Tool을 직접 호출하는 대신, 서버가 MCP Tool의 결과를 필요한 부분만 AI 기반으로 추출하여 전달합니다.
이를 통해 모델 컨텍스트 과소비 방지, 대형 문서 기반 Tool 호출의 부하 감소, 추가적인 요약 및 필터링이 가능합니다.

## 🚀 시작하기

### Claude Desktop에서 사용하기

1. Claude Desktop의 설정 들어가기
2. 커넥터 탭 선택
3. 커스텀 커넥터 추가
4. 아래 정보 입력
   - 이름 : QLMCP
   - 원격 MCP 서버 URL : `https://mcp.qlmcp.com/mcp`

### 예시

```
User: "QLMCP를 통해 창원의 맛집을 검색해줘"
→ Claude: QLMCP("창원의 맛집을 검색해줘")
→ QLMCP : {"result": [...]}
→ Claude: "창원의 맛집은 ...이 있습니다."
```

## 📝 라이선스

MIT License

