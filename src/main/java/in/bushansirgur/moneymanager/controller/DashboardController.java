package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.service.DashboardService;
import in.bushansirgur.moneymanager.service.ExpenseService;
import in.bushansirgur.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final IncomeService incomeService;   // ✅ ADD
    private final ExpenseService expenseService; // ✅ ADD

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/all-data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        Map<String, Object> data = new HashMap<>();

        data.put("incomes", incomeService.getAllIncomesForCurrentUser());
        data.put("expenses", expenseService.getAllExpensesForCurrentUser());

        return ResponseEntity.ok(data);
    }
}
