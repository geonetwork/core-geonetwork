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

package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class AbstractOperationsAllowedTest extends AbstractSpringDataTest {

    @Autowired
    protected OperationAllowedRepository _opAllowRepo;
    protected Metadata _md1;
    protected Operation _viewOp;
    protected Group _allGroup;
    protected OperationAllowed _opAllowed1;
    protected Metadata _md2;
    protected Operation _downloadOp;
    protected Group _intranetGroup;
    protected OperationAllowed _opAllowed2;
    protected OperationAllowed _opAllowed3;
    protected OperationAllowed _opAllowed4;
    @Autowired
    MetadataRepository _mdRepo;
    @Autowired
    GroupRepository _groupRepo;
    @Autowired
    OperationRepository _opRepo;
    @PersistenceContext
    EntityManager _entityManager;

    @Before
    public void createEntities() {

        this._viewOp = _opRepo.save(ReservedOperation.view.getOperationEntity().setId(-1));
        this._downloadOp = _opRepo.save(ReservedOperation.download.getOperationEntity().setId(-1));

        this._allGroup = _groupRepo.save(ReservedGroup.all.getGroupEntityTemplate());
        this._intranetGroup = _groupRepo.save(ReservedGroup.intranet.getGroupEntityTemplate());

        Metadata newMd = newMetadata(1);
        this._md1 = _mdRepo.save(newMd);

        newMd = newMetadata(2);
        this._md2 = _mdRepo.save(newMd);

        this._opAllowed1 = _opAllowRepo.save(new OperationAllowed().setId(_md1, _allGroup, _viewOp));
        this._opAllowed2 = _opAllowRepo.save(new OperationAllowed().setId(_md2, _intranetGroup, _downloadOp));
        this._opAllowed3 = _opAllowRepo.save(new OperationAllowed().setId(_md1, _intranetGroup, _downloadOp));
        this._opAllowed4 = _opAllowRepo.save(new OperationAllowed().setId(_md1, _intranetGroup, _viewOp));

        flushAndClear();
    }

    private Metadata newMetadata(int id) {
        Metadata newMd = new Metadata();
        newMd.setUuid("uuid" + id).setData("data" + id);
        newMd.getDataInfo().setSchemaId("schemaId" + id);
        newMd.getSourceInfo().setOwner(id).setSourceId("source" + id);
        return newMd;
    }

    protected void flushAndClear() {
        _opRepo.flush();
        _mdRepo.flush();
        _opAllowRepo.flush();
        _groupRepo.flush();
        _entityManager.flush();
        _entityManager.clear();
    }

}
