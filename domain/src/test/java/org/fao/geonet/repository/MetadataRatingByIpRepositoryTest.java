package org.fao.geonet.repository;


import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MetadataRatingByIpRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRatingByIpRepository _repo;

    @Test
    public void testFindOne() {
        MetadataRatingByIp rating1 = newMetadataRatingByIp();
        rating1 = _repo.save(rating1);

        MetadataRatingByIp rating2 = newMetadataRatingByIp();
        rating2 = _repo.save(rating2);

        assertEquals(rating2, _repo.findOne(rating2.getId()));
        assertEquals(rating1, _repo.findOne(rating1.getId()));
    }

    @Test
    public void testAverageRating() {
        MetadataRatingByIp rating1 = _repo.save(newMetadataRatingByIp());
        MetadataRatingByIp rating2 = _repo.save(updateMetadataId(rating1, newMetadataRatingByIp()));
        MetadataRatingByIp rating3 = _repo.save(updateMetadataId(rating1, newMetadataRatingByIp()));
        MetadataRatingByIp rating4 = _repo.save(newMetadataRatingByIp());

        final double sum = (double) (rating1.getRating() + rating2.getRating() + rating3.getRating());
        final double average = sum / 3;
        assertEquals((int) average, _repo.averageRating(rating1.getId().getMetadataId()));
    }

    private MetadataRatingByIp updateMetadataId(MetadataRatingByIp source, MetadataRatingByIp toUpdate) {
        toUpdate.getId().setMetadataId(source.getId().getMetadataId());

        return toUpdate;
    }

    @Test
    public void testDeleteAllById_MetadataId() throws Exception {
        MetadataRatingByIp rating1 = _repo.save(newMetadataRatingByIp());
        MetadataRatingByIp rating2 = newMetadataRatingByIp();
        rating2.getId().setMetadataId(rating1.getId().getMetadataId());
        rating2 = _repo.save(rating2);
        MetadataRatingByIp rating3 = _repo.save(newMetadataRatingByIp());

        assertEquals(3, _repo.count());
        _repo.deleteAllById_MetadataId(rating1.getId().getMetadataId());
        assertEquals(1, _repo.count());
        final List<MetadataRatingByIp> all = _repo.findAll();
        assertEquals(1, all.size());
        assertEquals(rating3.getId(), all.get(0).getId());
    }

    @Test
    public void testSaveAndUpdate() {
        MetadataRatingByIp rating1 = newMetadataRatingByIp();
        rating1 = _repo.save(rating1);

        MetadataRatingByIp rating2 = new MetadataRatingByIp();
        final int newRating = rating1.getRating() * 100;
        rating2.setRating(newRating);
        rating2.setId(new MetadataRatingByIpId(rating1.getId().getMetadataId(), rating1.getId().getIpAddress()));

        MetadataRatingByIp saved = _repo.save(rating2);

        assertEquals(1, _repo.count());

        MetadataRatingByIp loaded = _repo.findAll().get(0);

        assertEquals(loaded.getRating(), newRating);
        assertEquals(rating1.getId(), loaded.getId());
        assertEquals(rating2.getId(), loaded.getId());
        assertEquals(saved.getId(), loaded.getId());
    }

    @Test
    public void testFindAllByMetadataId() {
        MetadataRatingByIp rating1 = newMetadataRatingByIp();
        rating1 = _repo.save(rating1);

        MetadataRatingByIp rating2 = newMetadataRatingByIp();
        rating2 = _repo.save(rating2);

        List<MetadataRatingByIp> metadataRatings = _repo.findAllByIdMetadataId(rating1.getId().getMetadataId());
        assertEquals(rating1, metadataRatings.get(0));

        metadataRatings = _repo.findAllByIdMetadataId(rating2.getId().getMetadataId());
        assertEquals(rating2, metadataRatings.get(0));
    }

    private MetadataRatingByIp newMetadataRatingByIp() {
        return newMetadataRatingByIp(_inc);
    }
    public static MetadataRatingByIp newMetadataRatingByIp(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        MetadataRatingByIp metadataRatingByIp = new MetadataRatingByIp();
        metadataRatingByIp.setRating(1);
        MetadataRatingByIpId id = new MetadataRatingByIpId();
        id.setIpAddress("ip" + val);
        id.setMetadataId(val);
        metadataRatingByIp.setId(id);

        return metadataRatingByIp;
    }

}
