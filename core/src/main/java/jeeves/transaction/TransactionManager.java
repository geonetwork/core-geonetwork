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

import javax.annotation.Nullable;
import javax.persistence.RollbackException;

/**
 * Declares the cut-points/places where transactions are needed in Geonetwork.  Each module that
 * needs transactions needs to define a class like this and add it as a bean in the spring
 * configuration.
 * <p/>
 * Created by Jesse on 3/10/14.
 */
public class TransactionManager {

    /**
     * Used to run a {@link TransactionTask} with appropriate commit, rollback and notifications behaviour.
     *
     * @param name Transaction name
     * @param context Application context
     * @param transactionRequirement Approach used for execution
     * @param commitBehavior Approach used for commit
     * @param readOnly Force rollback behavior
     * @param action transaction being executed
     * @param <V> result type
     * @return result of transaction
     */
    public static <V> V runInTransaction(String name,
                                         ApplicationContext context,
                                         TransactionRequirement transactionRequirement,
                                         CommitBehavior commitBehavior,
                                         boolean readOnly,
                                         final TransactionTask<V> action) {
        final PlatformTransactionManager transactionManager = context.getBean(PlatformTransactionManager.class);
        TransactionStatus status = null;
        boolean isNewTransaction = false;
        boolean isRolledBack = false;
        boolean isCommitted = false;
        try {
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition(transactionRequirement.propagationId);
            definition.setName(name);
            definition.setReadOnly(readOnly);
            status = transactionManager.getTransaction(definition);
            isNewTransaction = status.isNewTransaction();

            if (isNewTransaction) {
                try {
                    fireNewTransaction(context, transactionManager, status);
                }
                catch(Throwable t) {
                    Log.warning(Log.JEEVES, "New transaction:", t);
                    // warning as we continue with action below
                }
            }

            return action.doInTransaction(status);

        } catch (Throwable exception) {
            Log.error(Log.JEEVES, "Error occurred within a transaction", exception);
            isRolledBack = rollbackIfNotRolledBack(context, transactionManager, status, isRolledBack);
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else if (exception instanceof Error) {
                throw (Error) exception;
            } else {
                throw new RuntimeException("Run in transaction '"+action+"': "+exception.getLocalizedMessage(), exception);
            }
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
                    isRolledBack = rollbackIfNotRolledBack(context, transactionManager, status, isRolledBack);
                } else {
                    if (readOnly) {
                        isRolledBack = rollbackIfNotRolledBack(context, transactionManager, status, isRolledBack);
                    } else if (!isRolledBack && (isNewTransaction || commitBehavior == CommitBehavior.ALWAYS_COMMIT)) {
                        doCommit(context, transactionManager, status);
                        isCommitted = true;
                    }
                }
            } catch (TransactionSystemException e) {
                if (!(e.getOriginalException() instanceof RollbackException)) {
                    Log.error(Log.JEEVES, "Exception completing transaction rollback, will try to rollback", e);
                } else {
                    Log.debug(Log.JEEVES, "Exception completing transaction, will try to rollback", e);
                }
                isRolledBack = rollbackIfNotRolledBack(context, transactionManager, status, isRolledBack);

            } catch (Throwable t) {
                Log.error(Log.JEEVES, "Unexpected problem completing transaction, will try to rollback", t);
                isRolledBack = rollbackIfNotRolledBack(context, transactionManager, status, isRolledBack);
            }
            Log.debug(
                Log.JEEVES,
                "Run in transaction completed:" +
                    (isRolledBack?" rolled back":"") +
                    (isCommitted?" committed":"")
            );
            if( isNewTransaction || commitBehavior == CommitBehavior.ALWAYS_COMMIT ) {
                // this transaction was our responsibility, double checking with committed or rolledback successfully
                if (!isRolledBack && !isCommitted) {
                    Log.warning(Log.JEEVES, "Run in transaction did not complete cleanly, transaction not committed or rolledback");
                }
                if (isRolledBack && isCommitted) {
                    Log.warning(Log.JEEVES, "Run in transaction did not complete cleanly, transaction both committed and rolledback");
                }
            }
        }

    }

    private static Boolean rollbackIfNotRolledBack(ApplicationContext context, PlatformTransactionManager transactionManager, TransactionStatus status, Boolean isRolledBack) {
        if (!isRolledBack) {
            doRollback(context, transactionManager, status);
        }
        return true;
    }

    /**
     * Safely perform {@link PlatformTransactionManager#commit(TransactionStatus)}.
     *
     * This method is responsible for safely logging any notification failures as warnings, rather than failing outright.
     *
     * @param context context used to obtain listeners to notify
     * @param transactionManager manager responsible for execution
     * @param status transaction status
     */
    protected static void doCommit(ApplicationContext context, PlatformTransactionManager transactionManager, TransactionStatus status) {
        try {
            fireBeforeCommit(context, transactionManager, status);
        }
        catch( Throwable t ){
            Log.warning(Log.JEEVES, "Commit transaction - before:", t);
        }

        if ( status == null || status.isCompleted()){
            // not calling return here to preserve previous logic
            // we can check if the following log messages are seen before taking defensive action
            Log.debug( Log.JEEVES,"transactionManager.commit called unexpectedly when transaction is already completed ");
        }

        try {
            transactionManager.commit(status);
        }
        finally {
            try {
                fireAfterCommit(context, transactionManager, status);
                if(! status.isCompleted() ) {
                    Log.warning( Log.JEEVES, "transactionManager.commit failed to complete");
                }
            }
            catch( Throwable t) {
                Log.warning(Log.JEEVES, "Commit transaction - after:", t);
            }
        }
    }

    /**
     * Notify context AfterCommitTransactionListener instances of transaction status after commit.
     * <p>
     * Transaction commit listeners can check {@link TransactionStatus#isCompleted()} {@code true} if commit was successful,
     * or {@code false} if the commmit failed and the code is in an inconsistent state.
     * </p>
     * @param context context used to obtain listeners
     * @param transactionManager manager responsible for execution
     * @param status transaction status
     * @throws Throwable
     */
    private static void fireAfterCommit(ApplicationContext context,
                                        PlatformTransactionManager transactionManager,
                                        @Nullable TransactionStatus status) throws Throwable {
        Throwable afterCommitFailure = null;

        Collection<AfterCommitTransactionListener> listeners = context.getBeansOfType
            (AfterCommitTransactionListener.class).values();
        for (AfterCommitTransactionListener listener : listeners) {
            try {
                listener.afterCommit(status);
            }
            catch (Throwable t){
                Log.debug(Log.JEEVES, "Listener "+listener.toString()+" newTransaction callback failed: "+ t);
                afterCommitFailure = t;
            }
        }
        if (afterCommitFailure != null){
            throw afterCommitFailure;
        }
    }

    /**
     * Notify context BeforeCommitTransactionListener instances of transaction status change.
     *
     * @param context context used to obtain listeners
     * @param transactionManager manager responsible for execution
     * @param status transaction status
     * @throws Throwable
     */
    private static void fireBeforeCommit(ApplicationContext context,
                                         PlatformTransactionManager transactionManager,
                                         @Nullable TransactionStatus status) throws Throwable {
        Throwable beforeCommitFailure = null;

        Collection<BeforeCommitTransactionListener> listeners = context.getBeansOfType
            (BeforeCommitTransactionListener.class).values();
        for (BeforeCommitTransactionListener listener : listeners) {
            try {
                listener.beforeCommit(status);
            }
            catch (Throwable t){
                Log.debug(Log.JEEVES, "Listener "+listener.toString()+" newTransaction callback failed: "+ t);
                beforeCommitFailure = t;
            }
        }
        if (beforeCommitFailure != null){
            throw beforeCommitFailure;
        }
    }

    /**
     * Notify context NewTransactionListener instances of transaction status change.
     *
     * @param context context used to obtain listeners
     * @param transactionManager manager responsible for execution
     * @param status transaction status
     * @throws Throwable
     */
    private static void fireNewTransaction(ApplicationContext context,
                                           PlatformTransactionManager transactionManager,
                                           @Nullable TransactionStatus status) throws Throwable {
        Throwable newTransactionFailure = null;

        Collection<NewTransactionListener> listeners = context.getBeansOfType
            (NewTransactionListener.class).values();
        for (NewTransactionListener listener : listeners) {
            try {
                listener.newTransaction(status);
            }
            catch (Throwable t){
                Log.debug(Log.JEEVES, "Listener "+listener.toString()+" newTransaction callback failed: "+ t);
                newTransactionFailure = t;
            }
        }
        if (newTransactionFailure != null){
            throw newTransactionFailure;
        }
    }

    /**
     * Notify context AfterRollbackTransactionListener instances of transaction status change.
     * <p>
     * Transaction rollback listeners can check {@link TransactionStatus#isCompleted()} {@code true} if rollback was successful,
     * or {@code false} if the rollback failed and the code is in an inconsistent state.
     * </p>
     *
     * @param context context used to obtain listeners
     * @param transactionManager manager responsible for execution
     * @param status transaction status
     * @throws Throwable
     */
    private static void fireAfterRollback(ApplicationContext context,
                                          PlatformTransactionManager transactionManager,
                                          @Nullable TransactionStatus status) throws Throwable {
        Throwable afterRollbackFailure = null;

        Collection<AfterRollbackTransactionListener> listeners = context.getBeansOfType
            (AfterRollbackTransactionListener.class).values();
        for (AfterRollbackTransactionListener listener : listeners) {
            try {
                listener.afterRollback(status);
            }
            catch (Throwable t){
                Log.debug(Log.JEEVES, "Listener "+listener.toString()+" afterRollback callback failed: "+ t);
                afterRollbackFailure = t;
            }
        }
        if (afterRollbackFailure != null){
            throw afterRollbackFailure;
        }
    }

    /**
     * Notify context BeforeRollbackTransactionListener instances of transaction status change.
     *
     * @param context context used to obtain listeners
     * @param transactionManager manager responsible for execution
     * @param status transaction status
     * @throws Throwable
     */
    private static void fireBeforeRollback(ApplicationContext context,
                                           PlatformTransactionManager transactionManager,
                                           @Nullable TransactionStatus status) throws Throwable {
        Throwable beforeRollbackFailure = null;

        Collection<BeforeRollbackTransactionListener> listeners = context.getBeansOfType
            (BeforeRollbackTransactionListener.class).values();
        for (BeforeRollbackTransactionListener listener : listeners) {
            try {
                listener.beforeRollback(status);
            }
            catch (Throwable t){
                Log.debug(Log.JEEVES, "Listener "+listener.toString()+" beforeRollback callback failed: "+ t);
                beforeRollbackFailure = t;
            }
        }
        if (beforeRollbackFailure != null){
            throw beforeRollbackFailure;
        }
    }

    /**
     * Safely perform {@link PlatformTransactionManager#rollback(TransactionStatus)}.
     *
     * This method is responsible for safely logging any notification failures as warnings, rather than failing outright.
     *
     * @param context context used to obtain listeners to notify
     * @param transactionManager manager responsible for execution
     * @param status transaction status
     */
    private static void doRollback(ApplicationContext context,
                                   PlatformTransactionManager transactionManager,
                                   @Nullable TransactionStatus status) {
        if ( status == null || status.isCompleted()){
            return; // nothing to do transaction already completed
        }
        try {
            fireBeforeRollback( context, transactionManager, status);
        }
        catch (Throwable t) {
            Log.warning(Log.JEEVES, "Rolling back transaction - before:", t);
        }

        try {
            transactionManager.rollback(status);
        }
        catch (Throwable t) {
            Log.error(Log.JEEVES, "ERROR rolling back transaction", t);
        }
        finally {
            try {
                fireAfterRollback(context, transactionManager, status);
                if(! status.isCompleted() ) {
                    Log.warning( Log.JEEVES, "transactionManager.rollback incomplete");
                }
            }
            catch (Throwable t) {
                Log.warning(Log.JEEVES, "Rolling back transaction - after:", t);
            }
        }
    }

    /**
     * Approach used for transaction execution.
     */
    public static enum TransactionRequirement {
        /** Support a current transaction; create a new one if none exists. */
        CREATE_ONLY_WHEN_NEEDED(TransactionDefinition.PROPAGATION_REQUIRED),

        /** Support a current transaction; throw an exception if no current transaction exists. */
        THROW_EXCEPTION_IF_NOT_PRESENT(TransactionDefinition.PROPAGATION_MANDATORY),

        /** Create a new transaction, suspending the current transaction if one exists. */
        CREATE_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        /** Propagation id defined by {@link org.springframework.transaction.TransactionDefinition} */
        private final int propagationId;

        TransactionRequirement(int propagation) {
            this.propagationId = propagation;
        }
    }

    /** Approach used for transaction commit */
    public static enum CommitBehavior {
        ALWAYS_COMMIT,
        ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS
    }
}
