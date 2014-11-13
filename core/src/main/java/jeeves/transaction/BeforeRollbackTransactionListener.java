package jeeves.transaction;

import org.springframework.transaction.TransactionStatus;

/**
 * A class that needs to perform an action before a transaction is rolled back.
 *
 * @author Jesse on 11/12/2014.
 */
public interface BeforeRollbackTransactionListener {
    /**
     * Called just before a commit is rolled back.
     */
    void beforeRollback(TransactionStatus transaction);
}
