package org.fao.geonet.repository;

import org.fao.geonet.domain.*;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class AbstractOperationsAllowedTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRepository _mdRepo;
    @Autowired
    GroupRepository _groupRepo;
    @Autowired
    OperationRepository _opRepo;
    @Autowired
    protected OperationAllowedRepository _opAllowRepo;
    @PersistenceContext
    EntityManager _entityManager;

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
        Metadata newMd = new Metadata().setUuid("uuid" + id).setData("data" + id);
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