package jeeves.transaction;

import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Declares the cut-points/places where transactions are needed in Geonetwork.  Each module that
 * needs transactions needs to define a class like this and add it as a bean in the spring configuration.
 * <p/>
 * Created by Jesse on 3/10/14.
 */
public class TransactionManager {
    public static enum TransactionRequirement {
        CREATE_ONLY_WHEN_NEEDED(TransactionDefinition.PROPAGATION_REQUIRED),
        THROW_EXCEPTION_IF_NOT_PRESENT(TransactionDefinition.PROPAGATION_MANDATORY),
        CREATE_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        private final int propagationId;

        TransactionRequirement(int propagation) {
            this.propagationId = propagation;
        }
    }

    public static enum CommitBehavior {
        ALWAYS_COMMIT, ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS
    }

    public static <V> V runInTransaction(String name,
                                         ApplicationContext context,
                                         TransactionRequirement transactionRequirement,
                                         CommitBehavior commitBehavior,
                                         boolean readOnly,
                                         final TransactionTask<V> action) {
        final PlatformTransactionManager transactionManager = context.getBean(PlatformTransactionManager.class);
        final Throwable[] exception = new Throwable[1];
        TransactionStatus transaction = null;
        boolean isNewTransaction = false;
        boolean rolledBack = false;
        V result = null;
        try {
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition(transactionRequirement.propagationId);
            definition.setName(name);
            definition.setReadOnly(readOnly);
            transaction = transactionManager.getTransaction(definition);
            isNewTransaction = transaction.isNewTransaction();

            if (isNewTransaction) {
                Collection<NewTransactionListener> listeners = context.getBeansOfType(NewTransactionListener.class).values();
                for (NewTransactionListener listener : listeners) {
                    listener.newTransaction(transaction);
                }
            }
            
            result = action.doInTransaction(transaction);

        } catch (Throwable e) {
            if (exception[0] == null) {
                exception[0] = e;
            }
            rolledBack = true;
            doRollback(context, transactionManager, transaction);
        } finally {
            try {
                if (readOnly) {
                    doRollback(context, transactionManager, transaction);
                } else if (!rolledBack && (isNewTransaction || commitBehavior == CommitBehavior.ALWAYS_COMMIT)) {
                    doCommit(context, transactionManager, transaction);
                }
            } catch (Throwable t) {
                Log.error(Log.JEEVES, "ERROR committing transaction, will try to rollback", t);
                doRollback(context, transactionManager, transaction);
            }
        }

        if (exception[0] != null) {
            if (exception[0] instanceof RuntimeException) {
                throw (RuntimeException) exception[0];
            } else if (exception[0] instanceof Error) {
                throw (Error) exception[0];
            } else {
                throw new RuntimeException(exception[0]);
            }
        }
        return result;
    }

    protected static void doCommit(ApplicationContext context, PlatformTransactionManager transactionManager, TransactionStatus transaction) {

        for (BeforeCommitTransactionListener listener : context.getBeansOfType(BeforeCommitTransactionListener.class).values()) {
            listener.beforeCommit(transaction);
        }

        transactionManager.commit(transaction);

        for (AfterCommitTransactionListener listener : context.getBeansOfType(AfterCommitTransactionListener.class).values()) {
            listener.afterCommit(transaction);
        }
    }

    private static void doRollback(ApplicationContext context,
                                   PlatformTransactionManager transactionManager,
                                   @Nullable TransactionStatus transaction) {
        try {
            if (transaction != null && !transaction.isCompleted()) {

                try {
                    Collection<BeforeRollbackTransactionListener> listeners = context.getBeansOfType
                            (BeforeRollbackTransactionListener.class).values();
                    for (BeforeRollbackTransactionListener listener : listeners) {
                        listener.beforeRollback(transaction);
                    }
                } finally {
                    transactionManager.rollback(transaction);
                    Collection<AfterRollbackTransactionListener> listeners = context.getBeansOfType(
                            AfterRollbackTransactionListener.class).values();
                    for (AfterRollbackTransactionListener listener : listeners) {
                        listener.afterRollback(transaction);
                    }
                }
			} 
			//what if the transaction is completed?
			//maybe then we shouldn't be here
        } catch (Throwable t) {
            Log.error(Log.JEEVES, "ERROR rolling back transaction", t);
        }
    }
}
