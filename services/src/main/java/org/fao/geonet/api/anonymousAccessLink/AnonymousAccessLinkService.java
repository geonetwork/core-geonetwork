
package org.fao.geonet.api.anonymousAccessLink;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnonymousAccessLinkService {

    @Autowired
    private IMetadataUtils metadataUtils;

    @Autowired
    private AnonymousAccessLinkRepository anonymousAccessLinkRepository;

    @Autowired
    private AnonymousAccessLinkMapper mapper;

    public AnonymousAccessLinkDto createAnonymousAccessLink(AnonymousAccessLinkDto anonymousAccessLinkDto) {
        String uuid = anonymousAccessLinkDto.getMetadataUuid();
        AnonymousAccessLink anonymousAccessLinkToCreate = new AnonymousAccessLink()
                .setMetadataId(metadataUtils.findOneByUuid(uuid).getId())
                .setMetadataUuid(uuid)
                .setHash(AnonymousAccessLink.getRandomHash());
        anonymousAccessLinkRepository.save(anonymousAccessLinkToCreate);
        return mapper.toDto(anonymousAccessLinkToCreate);
    }

    public List<AnonymousAccessLinkDto> getAllAnonymousAccessLinks() {
        return mapper.toDtoList(anonymousAccessLinkRepository.findAll());
    }

    public void deleteAnonymousAccessLink(AnonymousAccessLinkDto anonymousAccessLinkDto) {
        AnonymousAccessLink linkToDelete = anonymousAccessLinkRepository
                .findOneByMetadataUuid(anonymousAccessLinkDto.getMetadataUuid());
        if (linkToDelete != null) {
            anonymousAccessLinkRepository.delete(linkToDelete);
        }
    }

}
