/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import com.google.common.collect.Iterables;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Generic processor for processing items in batches within transactions.
  * <p>
 * Each batch runs in its own {@code PROPAGATION_REQUIRES_NEW} transaction. Because of this,
 * integration tests exercising code that uses this class cannot rely on the test framework's
 * usual transaction-per-test rollback (e.g. Spring's {@code @Transactional} test rollback):
 * the nested per-batch transactions commit independently of the outer test transaction. Such
 * tests should run with {@code @Transactional(propagation = Propagation.NOT_SUPPORTED)} and
 * clean up any created/modified data explicitly (e.g. in an {@code @After} method).
 *
 * @param <T> The type of the items to process.
 */
public class BatchTransactionalProcessor<T> {

    private final String transactionName;
    private final ApplicationContext applicationContext;
    private int batchSize = 100;

    /**
     * Constructor.
     *
     * @param transactionName    The name of the transaction.
     * @param applicationContext The application context.
     */
    public BatchTransactionalProcessor(String transactionName, ApplicationContext applicationContext) {
        this.transactionName = transactionName;
        this.applicationContext = applicationContext;
    }

    /**
     * Sets the batch size.
     *
     * @param batchSize The batch size. Must be greater than 0.
     * @return This processor.
     */
    public BatchTransactionalProcessor<T> setBatchSize(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Process items in batches.
     *
     * @param items         The items to process.
     * @param itemProcessor The processor for each item.
     * @throws Exception If an error occurs during processing.
     */
    public void process(Iterable<T> items, BatchItemProcessor<T> itemProcessor) throws Exception {
        Iterable<List<T>> batches = Iterables.partition(items, batchSize);

        for (final List<T> batch : batches) {
            TransactionManager.runInTransaction(transactionName, applicationContext,
                TransactionManager.TransactionRequirement.CREATE_NEW,
                TransactionManager.CommitBehavior.ALWAYS_COMMIT, false, transactionStatus -> {
                    for (T item : batch) {
                        itemProcessor.process(item);
                    }
                    return null;
                });
        }
    }
}
