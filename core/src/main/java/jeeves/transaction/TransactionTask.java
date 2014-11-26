package jeeves.transaction;

import org.springframework.transaction.TransactionStatus;

/**
 * A task to run in a transaction as part of {@link TransactionManager}.
 *
 * Created by Jesse on 3/11/14.
 */
public interface TransactionTask<V> {
    /**
     * Execute task and return result.
     *
     * @param transaction an object to allow the task to interact with the transaction if needed.
     *
     * @return result of task
     * @throws Throwable
     */
    V doInTransaction(TransactionStatus transaction) throws Throwable;
}
