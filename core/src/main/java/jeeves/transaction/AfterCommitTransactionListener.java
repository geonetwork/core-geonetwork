package jeeves.transaction;

import org.springframework.transaction.TransactionStatus;

/**
 * A class that needs to perform an action after a transaction commits.
 *
 * @author Jesse on 11/12/2014.
 */
public interface AfterCommitTransactionListener {
    /**
     * Called just before a commit occurs.
     */
    void afterCommit(TransactionStatus transaction);
}
