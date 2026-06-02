package com.anuththara.jobhunttracker.coverletter.anthropic.dto;

import java.util.List;

public record AnthropicResponse(
        List<ContentBlock> content
) {
    public record ContentBlock(String type, String text) {}
}