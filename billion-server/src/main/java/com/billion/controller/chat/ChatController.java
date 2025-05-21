package com.billion.controller.chat;

import com.billion.dto.QuestionDTO;
import com.billion.dto.AnswerDTO;
import com.billion.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.billion.service.ChatService;

@RestController
@RequestMapping("/api/qna")
@Api(tags = "Question and Answer API")
@Slf4j
public class ChatController  {



    @Autowired
    private ChatService chatService;
    /**
     * Get answer for a question
     *
     * @param questionDTO contains the question string
     * @return response containing the answer
     */
    @PostMapping
    @ApiOperation("Get answer for a question")
    public Result<AnswerDTO> getAnswer(@RequestBody QuestionDTO questionDTO) {
        log.info("Received question: {}", questionDTO.getQuestion());

        // Process the question (in this simple example, we just echo it back)
        String answer = chatService.classify(questionDTO.getQuestion());
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setAnswer(answer);

        return Result.success(answerDTO);
    }
}