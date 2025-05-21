package com.billion.service;


import com.agentsflex.llm.chatglm.ChatglmLlm;
//import org.junit.jupiter.api.Test;
public interface ChatService {

  String chat(String question);

  String classify(String question);
}
