package org.delcom.ifs25001pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25001pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25001pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25001pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Tampilan web Vaadin untuk catatan keuangan. */
@Route("")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span("- Data transaksi belum tersedia!");
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceLabel = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        add(new H1("Cashflow"), buildSummary(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    // ---------- Ringkasan ----------

    private HorizontalLayout buildSummary() {
        HorizontalLayout summary = new HorizontalLayout(
                card("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8"),
                card("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A"),
                card("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626"),
                card("Saldo", balanceLabel, "#FEF3C7", "#D97706"));
        summary.setWidthFull();
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div card(String label, Span value, String background, String accent) {
        Span caption = new Span(label);
        caption.getStyle().set("display", "block").set("font-size", "0.875rem").set("color", accent);
        value.getStyle().set("display", "block").set("font-size", "1.5rem")
                .set("font-weight", "700").set("color", accent);
        Div card = new Div(caption, value);
        card.getStyle().set("flex", "1").set("min-width", "0").set("background", background)
                .set("border-left", "6px solid " + accent).set("border-radius", "12px")
                .set("padding", "1rem").set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(useCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatMoney(useCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(useCase.getTotalExpense()));
        balanceLabel.setText(presenter.formatMoney(useCase.getBalance()));
    }

    // ---------- Grid ----------

    private void configureGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getId));

        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setKey("description")
                .setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah").setKey("amount")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getAmount));

        grid.addColumn(t -> presenter.formatType(t.getType())).setHeader("Tipe").setKey("type")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(presenter::formatCreatedAt).setHeader("Waktu").setKey("createdAt")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::deleteButton).setHeader("Tindakan").setKey("action")
                .setAutoWidth(true).setFlexGrow(0).setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    private Button deleteButton(Transaction t) {
        Button b = new Button("Hapus", VaadinIcon.TRASH.create(), e -> openDeleteDialog(t));
        b.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return b;
    }

    // ---------- Toolbar ----------

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        Button search = new Button("Cari", VaadinIcon.SEARCH.create(), e -> search());
        Button reload = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refreshAll());
        Button income = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openAddDialog(TransactionType.INCOME));
        income.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        Button expense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openAddDialog(TransactionType.EXPENSE));
        expense.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, search, reload, income, expense);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    // ---------- Dialog ----------

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;
        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setWidthFull();

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setWidth("450px");
        dialog.add(new VerticalLayout(description, amount));

        Button save = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            try {
                BigDecimal value = new BigDecimal(amount.getValue().trim());
                Transaction created = income
                        ? useCase.addIncome(description.getValue(), value)
                        : useCase.addExpense(description.getValue(), value);
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (NumberFormatException ex) {
                showError(presenter.showInvalidAmount());
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Batal", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openDeleteDialog(Transaction t) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Menghapus Transaksi");
        dialog.add(new Paragraph("Yakin ingin menghapus transaksi \"" + t.getDescription()
                + "\" (ID " + t.getId() + ")?"));

        Button confirm = new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            deleteTransaction(t);
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Batal", e -> dialog.close());
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    // ---------- Aksi ----------

    private void deleteTransaction(Transaction t) {
        try {
            useCase.removeTransaction(t.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
    }

    private void search() {
        String keyword = searchField.getValue();
        if (keyword == null || keyword.isBlank()) {
            refreshAll();
            return;
        }
        emptyState.setText("- Transaksi tidak ditemukan!");
        refreshGrid(useCase.searchTransactions(keyword), "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void refreshAll() {
        emptyState.setText("- Data transaksi belum tersedia!");
        searchField.clear();
        refreshGrid(useCase.getAllTransactions(), "Daftar Transaksi");
    }

    private void refreshGrid(List<Transaction> items, String title) {
        grid.setItems(items);
        updateSummary();
        statusLabel.setText(items.isEmpty()
                ? title + " — " + emptyState.getText().replaceFirst("^- ", "")
                : title);
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 4000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
