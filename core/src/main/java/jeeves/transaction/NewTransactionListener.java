package jeeves.transaction;

import org.springframework.transaction.TransactionStatus;

/**
 * A class that needs to perform an action when a new transaction is started.
 *
 * @author Jesse on 11/12/2014.
 */
public interface NewTransactionListener {
    /**
     * Called when a new transaction is created.
     */
    void newTransaction(TransactionStatus transaction);
}
