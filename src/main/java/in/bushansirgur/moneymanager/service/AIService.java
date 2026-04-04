package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.AIIntent;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.IncomeEntity;
import in.bushansirgur.moneymanager.repository.ExpenseRepository;
import in.bushansirgur.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;



@Service
@RequiredArgsConstructor
public class AIService {

    private final ExpenseRepository expenseRepo;
    private final IncomeRepository incomeRepo;
    private final MarketService marketService;

    @Value("${openai.api.key}")
    private String apiKey;

    public String getFinancialInsights(String question) {

        AIIntent intent = detectIntent(question);

        List<ExpenseEntity> expenses = expenseRepo.findAll();
        List<IncomeEntity> incomes = incomeRepo.findAll();

        double totalExpense = expenses.stream()
                .mapToDouble(e -> e.getAmount().doubleValue())
                .sum();

        double totalIncome = incomes.stream()
                .mapToDouble(i -> i.getAmount().doubleValue())
                .sum();

        StringBuilder context = new StringBuilder();

        // 📊 USER DATA
        if (intent.isNeedsUserData()) {
            context.append("User Income: ₹").append(totalIncome).append("\n");
            context.append("User Expenses: ₹").append(totalExpense).append("\n\n");
        }

        // 📈 MARKET + NEWS DATA
        if (intent.isNeedsMarketData() || intent.isNeedsNewsData()) {

            String rawNews = marketService.getMarketNews();

            JSONArray arr = new JSONArray(rawNews);

            StringBuilder newsSummary = new StringBuilder();

            for (int i = 0; i < Math.min(5, arr.length()); i++) {
                JSONObject obj = arr.getJSONObject(i);
                newsSummary.append("- ")
                        .append(obj.getString("headline"))
                        .append("\n");
            }

            context.append("Latest Market News:\n")
                    .append(newsSummary)
                    .append("\n");
        }

        // 🧠 FINAL PROMPT
        String finalPrompt = """
You are a smart financial assistant.

Context:
%s

User Question:
%s

👉 Respond in structured format:
1. 🌍 Market Insight (if available)
2. 📊 User Insight (if available)
3. 💡 Advice

Keep it clean and readable.
""".formatted(context.toString(), question);

        return callOpenAI(finalPrompt);
    }

    // 🧠 Prompt Builder
    private String buildPrompt(String question, double income, double expense, List<ExpenseEntity> expenses) {

        StringBuilder categoryBreakdown = new StringBuilder();

        for (ExpenseEntity e : expenses) {
            categoryBreakdown.append(e.getCategory())
                    .append(": ₹")
                    .append(e.getAmount())
                    .append(", ");
        }

        return """
You are a smart AI financial advisor.

Analyze the user's income and expense data carefully.

User Income: ₹%f  
User Expenses: ₹%f  

Expense Breakdown:
%s  

User Question: %s  

👉 Respond in a clean, structured format EXACTLY like this:

1. 📊 Summary
- Give a quick 2-3 line overview of the user's financial situation

2. 📈 Key Insights
- Compare income vs expenses clearly
- Highlight major trends (increase/decrease)
- Mention important observations with numbers

3. ⚠️ Problems / Risks
- Identify overspending categories
- Mention financial risks if any

4. 💡 Recommendations
- Give practical, actionable steps
- Keep them realistic and specific

5. 🎯 Final Advice
- End with a short motivating conclusion

IMPORTANT:
- Use bullet points (• or -)
- Keep formatting clean and readable
- Use ₹ values wherever relevant
- DO NOT write in paragraph blocks
- DO NOT repeat same points
-IMPORTANT:
                 - Use line breaks after every point
                 - Each bullet must be on a new line
                 - Use "\\n" properly
                 - Format cleanly for UI display
""".formatted(income, expense, categoryBreakdown, question);
    }


    private AIIntent detectIntent(String question) {

        String prompt = """
    You are an AI planner.

    Decide what data is needed to answer the question.

    Available:
    - USER_DATA (income, expenses)
    - MARKET_DATA (stock trends)
    - NEWS_DATA (financial news)

    Return ONLY JSON:
    {
      "needsUserData": true/false,
      "needsMarketData": true/false,
      "needsNewsData": true/false
    }

    Question: %s
    """.formatted(question);

        String response = callOpenAI(prompt);

        JSONObject json = new JSONObject(response);

        AIIntent intent = new AIIntent();
        intent.setNeedsUserData(json.getBoolean("needsUserData"));
        intent.setNeedsMarketData(json.getBoolean("needsMarketData"));
        intent.setNeedsNewsData(json.getBoolean("needsNewsData"));

        return intent;
    }

    // 🚀 OpenAI API Call
    private String callOpenAI(String prompt) {

        try {
            URL url = new URL("https://openrouter.ai/api/v1/chat/completions");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            // 🔥 OpenRouter specific headers
            conn.setRequestProperty("HTTP-Referer", "http://localhost:8080");
            conn.setRequestProperty("X-Title", "MoneyManagerApp");
            conn.setDoOutput(true);

            // ✅ PROPER JSON BODY CREATION
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "meta-llama/llama-3-8b-instruct");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            messages.put(message);
            requestBody.put("messages", messages);

            String body = requestBody.toString();

            // 🔹 Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();

            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            // 🔥 DEBUG (keep for now)
            System.out.println("RAW RESPONSE: " + response.toString());

            // ✅ CORRECT PARSING
            JSONObject json = new JSONObject(response.toString());

            // 🔥 HANDLE ERROR FIRST
            if (json.has("error")) {
                return "⚠️ OpenRouter Error: " + json.getJSONObject("error").getString("message");
            }

            JSONArray choices = json.getJSONArray("choices");
            JSONObject msg = choices.getJSONObject(0).getJSONObject("message");

            return msg.getString("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error generating AI response";
        }
    }
}