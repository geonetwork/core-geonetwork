package org.fao.geonet.kernel.url;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.LinkStatusRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import static org.junit.Assert.*;

public class LinkPresenterTest extends AbstractCoreIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    protected LinkStatusRepository linkStatusRepository;

    private LinkStatus okAtTheEnd;
    private LinkStatus statusKo;
    private LinkStatus oldStausOk;
    private LinkStatus koInTheMiddle;

    @Test
    public void errorThenUnknownThenOk() {
        Link linkOk = new Link().setUrl("a i am ok, but ko in the middle");
        Link linkKo = new Link().setUrl("i am ko");
        Link linkProbable = new Link().setUrl("i am probable");
        linkRepository.save(linkOk);
        linkRepository.save(linkKo);
        linkRepository.save(linkProbable);
        entityManager.flush();

        createLinkStatus();
        linkOk.addStatus(oldStausOk);
        linkKo.addStatus(statusKo);
        linkRepository.save(linkOk);
        linkRepository.save(linkKo);
        entityManager.flush();

        List<Link> allLink = linkRepository.getLinks();
        assertEquals(3, allLink.size());
        assertTrue(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(1).getLinkStatus().isEmpty());
        assertFalse(allLink.get(2).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());

        linkOk.addStatus(koInTheMiddle);
        linkRepository.save(linkOk);
        entityManager.flush();

        allLink = linkRepository.getLinks();
        assertEquals(3, allLink.size());
        assertTrue(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertFalse(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[1].isFailing());
        assertTrue(allLink.get(1).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(2).getLinkStatus().isEmpty());

        linkOk.addStatus(okAtTheEnd);
        linkRepository.save(linkOk);
        entityManager.flush();

        allLink = linkRepository.getLinks();
        assertEquals(3, allLink.size());
        assertTrue(allLink.get(0).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(1).getLinkStatus().isEmpty());
        assertFalse(allLink.get(2).getLinkStatus().toArray(new LinkStatus[]{})[0].isFailing());
        assertTrue(allLink.get(2).getLinkStatus().toArray(new LinkStatus[]{})[1].isFailing());
        assertFalse(allLink.get(2).getLinkStatus().toArray(new LinkStatus[]{})[2].isFailing());
    }

    private void createLinkStatus() {
        ISODate yesterday = new ISODate();
        yesterday.setDateAndTime("1980-06-03T01:02:03");

        ISODate today = new ISODate();
        today.setDateAndTime("2020-06-03T01:02:03");

        ISODate tomorrow = new ISODate();
        tomorrow.setDateAndTime("2025-06-03T01:02:03");

        oldStausOk = new LinkStatus()
                .setFailing(false)
                .setStatusValue("200")
                .setStatusInfo("OK")
                .setCheckDate(yesterday);

        koInTheMiddle = new LinkStatus()
                .setFailing(true)
                .setStatusValue("400")
                .setStatusInfo("KO")
                .setCheckDate(today);

        okAtTheEnd = new LinkStatus()
                .setFailing(false)
                .setStatusValue("200")
                .setStatusInfo("OK")
                .setCheckDate(tomorrow);

        statusKo = new LinkStatus()
                .setFailing(true)
                .setStatusValue("400")
                .setStatusInfo("KO")
                .setCheckDate(today);


    }
}
