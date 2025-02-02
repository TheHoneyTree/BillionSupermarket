package com.billion.test;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ChatTest {
    private final String Key = "a797b5aecc9f74f2ff9b469a042c4489.hp2PxyLSshEJ9QcB";
    //测试是否可以连接
    @Test
    public void test() {
        ClientV4 client = new ClientV4.Builder(Key).build();
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "原神是不是抄袭游戏？");
        messages.add(chatMessage);
//        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
//                .model(Constants.ModelChatGLM4)
                .model("glm-4-air")
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        try {
            System.out.println("model output:" + invokeModelApiResp.getData().getChoices().get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
