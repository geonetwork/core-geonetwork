package org.fao.geonet.repository;


import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Transactional
public class MetadataRatingByIpRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    MetadataRatingByIpRepository _repo;

    AtomicInteger _inc = new AtomicInteger();
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
    public void testFindAllByMetadataId() {
        MetadataRatingByIp rating1 = newMetadataRatingByIp();
        rating1 = _repo.save(rating1);

        MetadataRatingByIp rating2 = newMetadataRatingByIp();
        rating2 = _repo.save(rating2);

        List<MetadataRatingByIp> metadataRatings = _repo.findAllByMetadataId(rating1.getId().getMetadataId());
        assertEquals(rating1, metadataRatings.get(0));

        metadataRatings= _repo.findAllByMetadataId(rating2.getId().getMetadataId());
        assertEquals(rating2, metadataRatings.get(0));
    }

    private MetadataRatingByIp newMetadataRatingByIp() {
        int val = _inc.incrementAndGet();
        MetadataRatingByIp metadataRatingByIp = new MetadataRatingByIp();
        metadataRatingByIp.setRating(1);
        MetadataRatingByIpId id = new MetadataRatingByIpId();
        id.setIpAddress("ip"+val);
        id.setMetadataId(val);
        metadataRatingByIp.setId(id);

        return metadataRatingByIp;
    }

}
