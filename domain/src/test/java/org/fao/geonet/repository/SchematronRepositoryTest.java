package org.fao.geonet.repository;

import org.fao.geonet.domain.Schematron;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test {@link org.fao.geonet.repository.SchematronRepository}
 * Created by Jesse on 1/21/14.
 */
public class SchematronRepositoryTest extends AbstractSpringDataTest {
    @Autowired
    private SchematronRepository _repo;
    private AtomicInteger _inc = new AtomicInteger();


    @Test
    public void testFindOne() throws Exception {
        final Schematron schematron = _repo.save(newSchematron(_inc));

        final Schematron found = _repo.findOne(schematron.getId());

        assertSameContents(schematron, found);
    }

    @Test
    public void testFindAllByIsoschema() throws Exception {
        final Schematron schematron1 = _repo.save(newSchematron(_inc));
        final Schematron schematron2 = _repo.save(newSchematron(_inc));
        final Schematron entity = newSchematron(_inc);
        entity.setSchemaName(schematron1.getSchemaName());
        final Schematron schematron3 = _repo.save(entity);

        final List<Schematron> allByIsoschema = _repo.findAllBySchemaName(schematron1.getSchemaName());

        assertEquals(2, allByIsoschema.size());

        for (Schematron schematron : allByIsoschema) {
            if (schematron.getId() == schematron2.getId()) {
                fail("schematron 2 should not have been found.  Schematron found are: "+allByIsoschema);
            } else if (schematron.getId() != schematron1.getId() && schematron.getId() != schematron3.getId()) {
                fail("schematron id was neither from schematron 1 or 2: "+allByIsoschema);
            }
        }
    }

    @Test
    public void testFindAllByFileAndSchemaName() throws Exception {
        final Schematron schematron1 = _repo.save(newSchematron(_inc));
        _repo.save(newSchematron(_inc));
        final Schematron entity = newSchematron(_inc);
        entity.setFile(schematron1.getFile());
        _repo.save(entity);

        final Schematron oneBySchemaAndFile = _repo.findOneByFileAndSchemaName(schematron1.getFile(), schematron1.getSchemaName());

        assertNotNull(oneBySchemaAndFile);
    }

    @Test (expected = DataIntegrityViolationException.class)
    public void testUniqueContraintSchemaNameAndFile() throws Exception {
        final Schematron schematron = _repo.saveAndFlush(newSchematron(_inc));
        Schematron illegalSchematron = new Schematron();
        illegalSchematron.setFile(schematron.getFile());
        illegalSchematron.setSchemaName(schematron.getSchemaName());
        _repo.saveAndFlush(illegalSchematron);
    }

    public static Schematron newSchematron(AtomicInteger inc) {
        int id = inc.incrementAndGet();

        final Schematron schematron = new Schematron();
        schematron.setFile("file"+id);
        schematron.setSchemaName("schema" + id);

        return schematron;
    }
}
