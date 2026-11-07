package org.delcom.ifs25001pbo2026springbootcashflow;

import org.delcom.ifs25001pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25001pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppTest {
    @Autowired ITransactionRepository repository;
    @Autowired FinanceUseCase useCase;
    @Autowired FinancePresenter presenter;

    @Test
    void beansAreWired() {
        assertNotNull(repository);
        assertNotNull(useCase);
        assertNotNull(presenter);
    }

    @Test
    void balanceFollowsIncomeAndExpense() throws Exception {
        BigDecimal before = useCase.getBalance();
        useCase.addIncome("Gaji", new BigDecimal("1000"));
        useCase.addExpense("Makan", new BigDecimal("250"));
        assertEquals(0, useCase.getBalance().subtract(before).compareTo(new BigDecimal("750")));
    }
}
