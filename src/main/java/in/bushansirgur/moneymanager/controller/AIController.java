package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.AIRequest;
import in.bushansirgur.moneymanager.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/insights")
    public String getInsights(@RequestBody AIRequest request) {
        System.out.println("AI HIT: " + request.getQuestion());

        return aiService.getFinancialInsights(request.getQuestion());
    }
}