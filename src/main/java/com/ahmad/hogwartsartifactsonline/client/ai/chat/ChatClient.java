package com.ahmad.hogwartsartifactsonline.client.ai.chat;

import com.ahmad.hogwartsartifactsonline.client.ai.chat.dto.ChatRequest;
import com.ahmad.hogwartsartifactsonline.client.ai.chat.dto.ChatResponse;

public interface ChatClient {

    ChatResponse generate(ChatRequest chatRequest);
}
