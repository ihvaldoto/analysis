package ru.student.dataanalysis.service;

import ru.student.dataanalysis.model.Account;
import ru.student.dataanalysis.model.Expense;

public interface AccountService {

    /**
     * Создаёт счёт вместе с его первой тратой в одной транзакции.
     * Трата логически не может существовать без счёта, поэтому обе операции
     * должны быть атомарны: если трата некорректна, счёт тоже не создаётся.
     *
     * @param account счёт для создания
     * @param expense первая трата по этому счёту
     * @return сохранённый счёт
     */
    Account createAccountWithExpense(Account account, Expense expense);
}
