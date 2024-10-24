package com.billion.controller.user;

import com.billion.entity.Category;
import com.billion.result.Result;
import com.billion.service.CategoryService;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("userChatController")
@RequestMapping("/user/chat")
@Api(tags = "C端-聊天接口")
public class ChatController {
    private final String Key = "a797b5aecc9f74f2ff9b469a042c4489.hp2PxyLSshEJ9QcB";

    /**
     * 进行非流式的聊天
     * @param message
     * @return
     */
    @GetMapping("/not_stream")
    @ApiOperation("非流式聊天")
    public String chat_stream(String message) {
        ClientV4 client = new ClientV4.Builder(Key).build();
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), message);
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
        return invokeModelApiResp.getData().getChoices().get(0).toString();
    }

}
