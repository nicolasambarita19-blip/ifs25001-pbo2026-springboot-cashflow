package org.delcom.ifs25001pbo2026springbootcashflow.usecase;

import org.delcom.ifs25001pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.util.List;

/** Logika bisnis catatan keuangan. Tanpa I/O dan tanpa komponen UI. */
public class FinanceUseCase {
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    public Transaction addIncome(String description, BigDecimal amount) throws ValidationException {
        validate(description, amount);
        return repository.saveIncome(description.trim(), amount);
    }

    public Transaction addExpense(String description, BigDecimal amount) throws ValidationException {
        validate(description, amount);
        return repository.saveExpense(description.trim(), amount);
    }

    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Pencarian berdasarkan keterangan (case-insensitive); tidak mengubah data. */
    public List<Transaction> searchTransactions(String keyword) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findBy(t -> t.getDescription().toLowerCase().contains(key));
    }

    public List<Transaction> sortTransactions(SortOption option) {
        return repository.findAll().stream().sorted(option.comparator()).toList();
    }

    public BigDecimal getTotalIncome() {
        return sum(true);
    }

    public BigDecimal getTotalExpense() {
        return sum(false);
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    private BigDecimal sum(boolean income) {
        return repository.findAll().stream()
                .filter(t -> t.isIncome() == income)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validate(String description, BigDecimal amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
    }
}
