package com.qlmcp.backend.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeminiApiResponse {

    private final List<Candidate> candidates;

    @Getter
    @AllArgsConstructor
    public static class Candidate {

        private final Content content;
        private final String finishReason;
        private final long index;

        @Getter
        @AllArgsConstructor
        public static class Content {

            private final List<Part> parts;
            private final String role;

            @JsonTypeInfo(use = Id.DEDUCTION)
            @JsonSubTypes({
                @JsonSubTypes.Type(value = TextPart.class, name = "text"),
                @JsonSubTypes.Type(value = FunctionCallPart.class, name = "functionCall")
            })
            @Getter
            public static class Part {

            }

            @Getter
            @AllArgsConstructor
            public static class TextPart extends Part {

                private final String text;
            }

            @Getter
            @AllArgsConstructor
            public static class FunctionCallPart extends Part {

                private final Map<String, Object> functionCall;
            }
        }

    }
}
