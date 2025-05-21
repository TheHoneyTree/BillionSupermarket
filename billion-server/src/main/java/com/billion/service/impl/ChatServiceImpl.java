package com.billion.service.impl;

import com.agentsflex.llm.chatglm.ChatglmLlm;
import com.agentsflex.llm.chatglm.ChatglmLlmConfig;
import com.billion.agent.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.billion.service.ChatService;
import java.util.regex.*;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatglmLlm chatglmLlm;
    private final DragonBroAgent dragonBroAgent;
    private final XuanDogAgent xuanDogAgent;
    private final HorseHeadAgent horseHeadAgent;
    private final ZeusAgent zeusAgent;

    public ChatServiceImpl() {
        ChatglmLlmConfig chatglmLlmConfig = new ChatglmLlmConfig();
        chatglmLlmConfig.setModel("glm-4-flash");
        chatglmLlmConfig.setApiKey("XXXXXXXXXXXXXX");
        this.chatglmLlm = new ChatglmLlm(chatglmLlmConfig);
        this.dragonBroAgent = new DragonBroAgent();
        this.xuanDogAgent = new XuanDogAgent();
        this.horseHeadAgent = new HorseHeadAgent();
        this.zeusAgent = new ZeusAgent();
    }

    @Override
    public String chat(String question) {
        return chatglmLlm.chat(question);
    }


    public int extractCode(String jsonResponse) {
        Pattern pattern = Pattern.compile("\"code\":\\s*(\\d+)");
        Matcher matcher = pattern.matcher(jsonResponse);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return -1;
        }
    }


    public String classify(String question) {
        String prompt1 = "如果用户的对话中出现了“我是龙”或者其他和龙有关的字眼，则切换为龙哥角色，输出序号为1的答案，写在markdown的json格式里面。\n" +
                "如果用户的对话中出现了“我是狗”之类或者其他与狗有关的字眼，则切换为炫狗角色，输出序号为2的答案，写在markdown的json格式里面。\n" +
                "如果用户的对话中出现了“我是明星”，“zeus”，“宙斯”，“宙活”之类或者是其他和明星有关的的字眼，则切换为宙斯角色，输出序号为3的答案，写在markdown的json格式里面。\n" +
                "如果用户的对话中出现了“马头”，“thethy”之类或者其他有关马的字眼，则切换为马头角色，输出序号为4的答案，写在markdown的json格式里面。\n" +
                "用户的输入为“"+         question   +    "”\n" +
                "示例输出：\n" +
                "```json\n" +
                "{\"role\": \"龙哥\", \"code\": 1}\n" +
                "```";
        String re1 = chat(prompt1);
        int code = extractCode(re1);
        String prompt2 = "";

        switch (code) {
            case 1:
                prompt2 = dragonBroAgent.getPrompt();
                break;
            case 2:
                prompt2 = xuanDogAgent.getPrompt();
                break;
            case 3:
                prompt2 = zeusAgent.getPrompt();
                break;
            case 4:
                prompt2 = horseHeadAgent.getPrompt();
                break;
        }

        prompt2 = prompt2 + question;
        String re2 = chat(prompt2);
        return re2;
    }
}