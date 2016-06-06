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

package jeeves;

import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionalException;

import static jeeves.transaction.TransactionManager.CommitBehavior.ALWAYS_COMMIT;
import static jeeves.transaction.TransactionManager.CommitBehavior.ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_NEW;
import static jeeves.transaction.TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED;
import static jeeves.transaction.TransactionManager.TransactionRequirement.THROW_EXCEPTION_IF_NOT_PRESENT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test the basic functionality of the {@link jeeves.transaction.TransactionManager#runInTransaction(String,
 * org.springframework.context.ApplicationContext, jeeves.transaction.TransactionManager.TransactionRequirement,
 * jeeves.transaction.TransactionManager.CommitBehavior, boolean, TransactionTask)}
 * <p/>
 * Created by Jesse on 3/10/14.
 */
public class TransactionAspectDoRunInTransactionTest extends AbstractSpringDataTest {

    @PersistenceContext
    EntityManager _entityManager;
    @Autowired
    ApplicationContext _applicationContext;
    @Autowired
    PlatformTransactionManager _tm;

    @Test
    public void testRunInTransactionAlwaysCreateNewTransaction_AlwaysCommit() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                final String name = UUID.randomUUID().toString();
                try {
                    final TransactionInformation info = new TransactionInformation();
                    TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_NEW, ALWAYS_COMMIT,
                        false,
                        new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                info.set();
                                Setting setting = new Setting().setValue("value").setName(name).
                                    setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);

                                _entityManager.persist(setting);
                                _entityManager.flush();
                                return null;
                            }
                        });

                    info.assertNotSameTransaction(transaction);
                    info.assertIsNew();
                    info.assertIsCompleted();
                    info.assertIsNotRollbackOnly();
                    assertTrue(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
                assertTrue(_entityManager.getReference(Setting.class, name) != null);
            }
        });
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final String name = UUID.randomUUID().toString();
                final TransactionInformation info = new TransactionInformation();
                TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_NEW, ALWAYS_COMMIT,
                    false,
                    new TransactionTask<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                            info.set();
                            Setting setting = new Setting().setValue("value").setName(name).
                                setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                            _entityManager.persist(setting);
                            _entityManager.flush();
                            return null;
                        }
                    });

                assertTrue(_entityManager.getReference(Setting.class, name) != null);
                info.assertIsCompleted();
                info.assertIsNotRollbackOnly();
            }
        });
    }

    @Test
    public void testRunInTransactionAlwaysCreateNewTransaction_CommitOnlyWhenNew() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                final String name = UUID.randomUUID().toString();
                try {
                    final TransactionInformation info = new TransactionInformation();

                    TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_NEW,
                        ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS,
                        false,
                        new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                info.set();
                                Setting setting = new Setting().setValue("value").setName(name).
                                    setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);

                                _entityManager.persist(setting);
                                _entityManager.flush();
                                return null;
                            }
                        });

                    info.assertNotSameTransaction(transaction);
                    info.assertIsNew();
                    info.assertIsCompleted();
                    info.assertIsNotRollbackOnly();

                    assertTrue(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
                assertTrue(_entityManager.getReference(Setting.class, name) != null);
            }
        });
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final String name = UUID.randomUUID().toString();
                final TransactionInformation info = new TransactionInformation();

                TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_NEW,
                    ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS, false,
                    new TransactionTask<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                            info.set();
                            Setting setting = new Setting().setValue("value").setName(name).
                                setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                            _entityManager.persist(setting);
                            _entityManager.flush();
                            return null;
                        }
                    });

                info.assertIsNew();
                info.assertIsCompleted();
                info.assertIsNotRollbackOnly();
                assertTrue(_entityManager.getReference(Setting.class, name) != null);
            }
        });
    }

    @Test
    public void testRunInTransactionAlwaysCreateNewTransaction_ReadOnly() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                try {
                    final TransactionInformation info = new TransactionInformation();
                    final String name = UUID.randomUUID().toString();
                    TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_NEW, ALWAYS_COMMIT,
                        true,
                        new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                info.set();
                                Setting setting = new Setting().setValue("value").setName(name).
                                    setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                                _entityManager.persist(setting);
                                _entityManager.flush();
                                return null;
                            }
                        });

                    info.assertNotSameTransaction(transaction);
                    info.assertIsNew();
                    assertFalse(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
            }
        });
    }

    @Test
    public void testRunInTransaction_ReuseExistingTransactionIfPossible_AlwaysCommit() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                final String name = UUID.randomUUID().toString();
                try {
                    final TransactionInformation info = new TransactionInformation();

                    TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_ONLY_WHEN_NEEDED,
                        ALWAYS_COMMIT, false,
                        new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                info.set();
                                Setting setting = new Setting().setValue("value").setName(name).
                                    setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                                _entityManager.persist(setting);
                                _entityManager.flush();
                                return null;
                            }
                        });

                    info.assertIsSameTransaction(transaction);
                    info.assertNotNew();
                    info.assertIsCompleted();
                    info.assertIsNotRollbackOnly();

                    assertTrue(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
                assertTrue(_entityManager.getReference(Setting.class, name) != null);
            }
        });
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final String name = UUID.randomUUID().toString();
                final TransactionInformation info = new TransactionInformation();

                TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_ONLY_WHEN_NEEDED,
                    ALWAYS_COMMIT, false,
                    new TransactionTask<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                            info.set();
                            Setting setting = new Setting().setValue("value").setName(name).
                                setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                            _entityManager.persist(setting);
                            _entityManager.flush();
                            return null;
                        }
                    });

                info.assertIsNew();
                info.assertIsCompleted();
                info.assertIsNotRollbackOnly();
                assertTrue(_entityManager.getReference(Setting.class, name) != null);
            }
        });
    }

    @Test
    public void testRunInTransaction_ReuseExistingTransactionIfPossible_CommitOnNewTransaction() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                final String name = UUID.randomUUID().toString();
                try {
                    final TransactionInformation info = new TransactionInformation();
                    TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_ONLY_WHEN_NEEDED,
                        ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS, false,
                        new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                info.set();
                                Setting setting = new Setting().setValue("value").setName(name).
                                    setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                                _entityManager.persist(setting);
                                _entityManager.flush();
                                return null;
                            }
                        });

                    info.assertIsSameTransaction(transaction);
                    info.assertNotNew();
                    info.assertIsNotCompleted();
                    info.assertIsNotRollbackOnly();
                    assertTrue(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
                assertFalse(_entityManager.getReference(Setting.class, name) != null);
            }
        });
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final String name = UUID.randomUUID().toString();
                final TransactionInformation info = new TransactionInformation();

                TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext, CREATE_ONLY_WHEN_NEEDED,
                    ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS, false,
                    new TransactionTask<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                            info.set();
                            Setting setting = new Setting().setValue("value").setName(name).
                                setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                            _entityManager.persist(setting);
                            _entityManager.flush();
                            return null;
                        }
                    });

                info.assertIsCompleted();
                info.assertIsNotRollbackOnly();
                assertTrue(_entityManager.getReference(Setting.class, name) != null);
            }
        });
    }

    @Test
    public void testRunInTransaction_ReuseExistingTransactionIfPossible_RollbackOnError() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                final String name = UUID.randomUUID().toString();
                try {
                    final TransactionInformation info = new TransactionInformation();
                    try {
                        TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext,
                            CREATE_ONLY_WHEN_NEEDED,
                            ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS, false,
                            new TransactionTask<Object>() {
                                @Override
                                public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                    info.set();
                                    Setting setting = new Setting().setValue("value").setName(name).
                                        setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                                    _entityManager.persist(setting);
                                    _entityManager.flush();
                                    return null;
                                }
                            });
                    } catch (Exception e) {
                        info.assertIsCompleted();
                        info.assertIsRollbackOnly();
                    }
                    assertFalse(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
            }
        });
    }

    @Test(expected = IllegalTransactionStateException.class)
    public void testRunInTransactionThrowsExceptionWhenRequireTransactionAndTransactionIsMissing() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final String name = UUID.randomUUID().toString();
                TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext,
                    THROW_EXCEPTION_IF_NOT_PRESENT,
                    ALWAYS_COMMIT, false,
                    new TransactionTask<Object>() {
                        @Override
                        public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                            Setting setting = new Setting().setValue("value").setName(name).
                                setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                            _entityManager.persist(setting);
                            _entityManager.flush();
                            return null;
                        }
                    });
            }
        });
    }

    @Test
    public void testRunInTransaction_RequireTransactionExisting_AlwaysCommit() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                final String name = UUID.randomUUID().toString();
                try {
                    final TransactionInformation info = new TransactionInformation();
                    TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext,
                        THROW_EXCEPTION_IF_NOT_PRESENT,
                        ALWAYS_COMMIT, false,
                        new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                info.set();
                                Setting setting = new Setting().setValue("value").setName(name).
                                    setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                                _entityManager.persist(setting);
                                _entityManager.flush();
                                return null;
                            }
                        });

                    info.assertIsSameTransaction(transaction);
                    info.assertNotNew();
                    info.assertIsCompleted();
                    info.assertIsNotRollbackOnly();
                    assertTrue(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
                // ensure transaction was committed
                assertTrue(_entityManager.getReference(Setting.class, name) != null);
            }
        });

    }

    @Test
    public void testRunInTransaction_RequireExistingTransaction_OnlyCommitOnNewTransaction() throws Exception {
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                final TransactionStatus transaction = _tm.getTransaction(new DefaultTransactionDefinition());
                final String name = UUID.randomUUID().toString();
                final TransactionInformation info = new TransactionInformation();
                try {
                    TransactionManager.runInTransaction("testRunInTransactionSimpleWrite", _applicationContext,
                        THROW_EXCEPTION_IF_NOT_PRESENT,
                        ONLY_COMMIT_NEWLY_CREATED_TRANSACTIONS, false,
                        new TransactionTask<Object>() {
                            @Override
                            public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                                info.set();
                                Setting setting = new Setting().setValue("value").setName(name).
                                    setDataType(SettingDataType.STRING).setInternal(false).setPosition(1);
                                _entityManager.persist(setting);
                                _entityManager.flush();
                                return null;
                            }
                        });

                    info.assertIsSameTransaction(transaction);
                    info.assertNotNew();
                    info.assertIsNotCompleted();
                    info.assertIsNotRollbackOnly();
                    assertTrue(_entityManager.getReference(Setting.class, name) != null);
                } finally {
                    _tm.rollback(transaction);
                }
                // ensure transaction was committed
                assertFalse(_entityManager.getReference(Setting.class, name) != null);
                info.assertIsCompleted();
                info.assertIsRollbackOnly();
            }
        });

    }

    private class TransactionInformation {
        boolean isNew;
        TransactionStatus transaction;

        private void set() {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
            try {
                transaction = _tm.getTransaction(def);
                this.isNew = transaction.isNewTransaction();
            } catch (TransactionalException t) {
                isNew = false;
            }
        }

        public void assertIsSameTransaction(TransactionStatus transaction) {
            assertSame(transaction, this.transaction);
        }

        public void assertIsNew() {
            assertTrue(isNew);
        }

        public void assertNotSameTransaction(TransactionStatus transaction) {
            assertNotSame(transaction, this.transaction);
        }

        public void assertNotNew() {
            assertFalse(isNew);
        }

        public void assertIsRollbackOnly() {
            assertTrue(transaction.isRollbackOnly());
        }

        public void assertIsNotRollbackOnly() {
            assertTrue(transaction.isRollbackOnly());
        }

        public void assertIsCompleted() {
            assertTrue(transaction.isCompleted());
        }

        public void assertIsNotCompleted() {
            assertFalse(transaction.isCompleted());
        }
    }
}
