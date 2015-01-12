package jeeves.transaction;

import org.springframework.transaction.TransactionStatus;

/**
 * A class that needs to perform an action before a transaction commits.
 *
 * @author Jesse on 11/12/2014.
 */
public interface BeforeCommitTransactionListener {
    /**
     * Called just before a commit occurs.
     */
    void beforeCommit(TransactionStatus transaction);
}
