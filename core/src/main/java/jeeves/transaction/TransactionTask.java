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
