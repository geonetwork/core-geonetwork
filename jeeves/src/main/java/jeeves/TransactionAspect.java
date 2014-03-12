package jeeves;

import jeeves.constants.Jeeves;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Declares the cut-points/places where transactions are needed in Geonetwork.  Each module that
 * needs transactions needs to define a class like this and add it as a bean in the spring configuration.
 * <p/>
 * Created by Jesse on 3/10/14.
 */
@Aspect
public class TransactionAspect {
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

    @Autowired
    ApplicationContext _applicationContext;

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

            result = action.doInTransaction(transaction);

        } catch (Throwable e) {
            if (exception[0] == null) {
                exception[0] = e;
            }
            rolledBack = true;
            doRollback(transactionManager, transaction);
        } finally {
            try {
                if (readOnly) {
                    doRollback(transactionManager, transaction);
                } else if (!rolledBack && (isNewTransaction || commitBehavior == CommitBehavior.ALWAYS_COMMIT)) {
                    transactionManager.commit(transaction);
                }
            } catch (Throwable t) {
                Log.error(Log.JEEVES, "ERROR committing transaction, will try to rollback", t);
                doRollback(transactionManager, transaction);
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

    private static void doRollback(PlatformTransactionManager transactionManager, TransactionStatus transaction) {
        try {

            transactionManager.rollback(transaction);
        } catch (Throwable t) {
            Log.error(Log.JEEVES, "ERROR rolling back transaction", t);
        }
    }

    @Around("execution(org.jdom.Element jeeves.server.dispatchers.ServiceInfo.execServices(..))")
    public Element aroundServiceManagerDispatch(final ProceedingJoinPoint joinPoint) throws Throwable {
        return runInTransaction("ServiceManager.dispatch", _applicationContext,
                TransactionRequirement.CREATE_ONLY_WHEN_NEEDED,
                CommitBehavior.ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS,
                false, new TransactionTask<Element>() {
            @Override
            public Element doInTransaction(TransactionStatus transaction) throws Throwable {
                return (Element) joinPoint.proceed();
            }
        });
    }
}
