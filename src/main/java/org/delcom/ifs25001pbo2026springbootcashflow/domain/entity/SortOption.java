package org.delcom.ifs25001pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/** Kriteria pengurutan transaksi; setiap opsi membawa comparator-nya sendiri. */
public enum SortOption {
    AMOUNT_ASC(Comparator.comparing(Transaction::getAmount)),
    AMOUNT_DESC(Comparator.comparing(Transaction::getAmount).reversed()),
    INCOME_FIRST(Comparator.comparing((Transaction t) -> !t.isIncome())),
    EXPENSE_FIRST(Comparator.comparing(Transaction::isIncome));

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
