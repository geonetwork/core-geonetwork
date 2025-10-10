package org.fao.geonet.api.anonymousAccessLink;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AnonymousAccessLinkMapper {

    public AnonymousAccessLinkDto toDto(AnonymousAccessLink entity) {
        return new AnonymousAccessLinkDto()
                .setMetadataId(entity.getMetadataId())
                .setMetadataUuid(entity.getMetadataUuid())
                .setHash(entity.getHash());
    }

    public AnonymousAccessLink toEntity(AnonymousAccessLinkDto dto) {
        return new AnonymousAccessLink()
                .setMetadataId(dto.getMetadataId())
                .setMetadataUuid(dto.getMetadataUuid())
                .setHash(dto.getHash());
    }

    public List<AnonymousAccessLinkDto> toDtoList(List<AnonymousAccessLink> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AnonymousAccessLink> toEntityList(List<AnonymousAccessLinkDto> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
