/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package jeeves.transaction;

import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collection;

import jakarta.annotation.Nullable;
import jakarta.persistence.RollbackException;

/**
 * Declares the cut-points/places where transactions are needed in Geonetwork.  Each module that
 * needs transactions needs to define a class like this and add it as a bean in the spring
 * configuration.
 * <p/>
 * Created by Jesse on 3/10/14.
 */
public class TransactionManager {

    public static ThreadLocal<TransactionStatus> transactionInitiatedByJeeves = new ThreadLocal();

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
            transactionInitiatedByJeeves.set(transaction);
            result = action.doInTransaction(transaction);



        } catch (Throwable e) {
            Log.error(Log.JEEVES, "Error occurred within a transaction", e);
            if (exception[0] == null) {
                exception[0] = e;
            }
            rolledBack = true;
            doRollback(context, transactionManager, transaction);
        } finally {
            try {
                // GlobalExceptionManager stores the exception in a request attribute, to be processed to rollback the transaction
                // as otherwise GlobalExceptionManager processes the exception and the code doesn't enter in the catch block
                // to rollback the transaction
                Throwable requestException = null;
                try {
                    requestException = (Throwable) RequestContextHolder.currentRequestAttributes().getAttribute("exception", RequestAttributes.SCOPE_REQUEST);
                } catch (IllegalStateException ex) {
                    // Ignore: transaction non-related to a web request
                }

                if (requestException != null) {
                    doRollback(context, transactionManager, transaction);
                } else {
                    if (readOnly) {
                        doRollback(context, transactionManager, transaction);
                    } else if (!rolledBack && (isNewTransaction || commitBehavior == CommitBehavior.ALWAYS_COMMIT)) {
                        doCommit(context, transactionManager, transaction);
                    }
                }
            } catch (TransactionSystemException e) {
                if (!(e.getOriginalException() instanceof RollbackException)) {
                    Log.error(Log.JEEVES, "ERROR committing transaction, will try to rollback", e);
                    doRollback(context, transactionManager, transaction);
                } else {
                    Log.debug(Log.JEEVES, "ERROR committing transaction, will try to rollback", e);
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
}
