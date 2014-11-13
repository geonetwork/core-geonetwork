package jeeves.transaction;

import org.springframework.transaction.TransactionStatus;

/**
 * A class that needs to perform an action after a transaction is rolled back.
 *
 * @author Jesse on 11/12/2014.
 */
public interface AfterRollbackTransactionListener {
    /**
     * Called just after a commit is rolled back.
     */
    void afterRollback(TransactionStatus transaction);
}
