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
        final Throwable[] exception = new Throwable[1];
        TransactionStatus status = null;
        boolean isNewTransaction = false;
        boolean isRolledBack = false;
        boolean isCommitted = false;
        V result = null;
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

            result = action.doInTransaction(status);

        } catch (Throwable e) {
            Log.error(Log.JEEVES, "Error occurred within a transaction", e);
            if (exception[0] == null) {
                exception[0] = e;
            }

            try {
                doRollback(context, transactionManager, status);
            }
            finally {
                isRolledBack = true;
            }
        } finally {
            try {
                if (readOnly && !isRolledBack) {
                    try {
                        doRollback(context, transactionManager, status);
                    }
                    finally {
                        isRolledBack = true;
                    }
                } else if (!isRolledBack && (isNewTransaction || commitBehavior == CommitBehavior.ALWAYS_COMMIT)) {
                    try {
                        doCommit(context, transactionManager, status);
                    }
                    finally {
                        isCommitted = true;
                    }
                }
            } catch (TransactionSystemException e) {
                if (!(e.getOriginalException() instanceof RollbackException)) {
                    Log.error(Log.JEEVES, "ERROR committing transaction, will try to rollback", e);
                    if (!isRolledBack) {
                        try {
                            doRollback(context, transactionManager, status);
                        }
                        finally {
                            isRolledBack = true;
                        }
                    }
                } else {
                    Log.debug(Log.JEEVES, "ERROR committing transaction, will try to rollback", e);
                    if (!isRolledBack) {
                        try {
                            doRollback(context, transactionManager, status);
                        }
                        finally {
                            isRolledBack = true;
                        }
                    }
                }
            } catch (Throwable t) {
                Log.error(Log.JEEVES, "ERROR committing transaction, will try to rollback", t);
                if (!isRolledBack) {
                    try {
                        doRollback(context, transactionManager, status);
                    }
                    finally {
                        isRolledBack = true;
                    }
                }
            }
            Log.debug(
                Log.JEEVES,
                "Run in transaction completed:" +
                    (isRolledBack?" rolled back":"") +
                    (isCommitted?" committed":"")
            );
            if( isRolledBack == false && isCommitted == false ){
                Log.warning(  Log.JEEVES, "Run in transaction did not complete cleanly, transaction not committed or rolledback");
            }
        }

        if (exception[0] != null) {
            if (exception[0] instanceof RuntimeException) {
                throw (RuntimeException) exception[0];
            } else if (exception[0] instanceof Error) {
                throw (Error) exception[0];
            } else {
                throw new RuntimeException("Run in transaction '"+action+"': "+exception[0].getLocalizedMessage(), exception[0]);
            }
        }
        return result;
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
            }
            catch( Throwable t) {
                Log.warning(Log.JEEVES, "Commit transaction - after:", t);
            }
        }
    }

    /**
     * Notify context AfterCommitTransactionListener instances of transaction status after commit.
     *
     * @param context context to obtains listeners from
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
