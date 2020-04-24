//=============================================================================
//===	Copyright (C) 2001-2019 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.url;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkStatus;
import org.fao.geonet.domain.Link_;
import org.fao.geonet.domain.MetadataLink;
import org.fao.geonet.domain.MetadataLink_;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.LinkAwareSchemaPlugin;
import org.fao.geonet.kernel.schema.LinkPatternStreamer.ILinkBuilder;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.repository.LinkRepository;
import org.fao.geonet.repository.LinkStatusRepository;
import org.fao.geonet.repository.MetadataLinkRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static java.util.Objects.isNull;

public class UrlAnalyzer {

    @Autowired
    protected SchemaManager schemaManager;

    @Autowired
    protected MetadataRepository metadataRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected UrlChecker urlChecker;

    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected LinkStatusRepository linkStatusRepository;

    @Autowired
    protected MetadataLinkRepository metadataLinkRepository;

    public void processMetadata(Element element, AbstractMetadata md) throws org.jdom.JDOMException {
        SchemaPlugin schemaPlugin = schemaManager.getSchema(md.getDataInfo().getSchemaId()).getSchemaPlugin();
        if (schemaPlugin instanceof LinkAwareSchemaPlugin) {

            metadataLinkRepository
                    .findAll(metadatalinksTargetting(md))
                    .stream()
                    .forEach(metadatalink -> {
                        metadatalink.getLink().getRecords().remove(metadatalink);
                    });
            entityManager.flush();
            ((LinkAwareSchemaPlugin) schemaPlugin).createLinkStreamer(new ILinkBuilder<Link, AbstractMetadata>() {

                @Override
                public Link found(String url) {
                    Link link = linkRepository.findOneByUrl(url);
                    if (link != null) {
                        return link;
                    } else {
                        link = new Link();
                        link.setUrl(url);
                        linkRepository.save(link);
                        return link;
                    }
                }

                @Override
                public void persist(Link link, AbstractMetadata metadata) {
                    MetadataLink metadataLink = new MetadataLink();
                    metadataLink.setMetadataId(new Integer(metadata.getId()));
                    metadataLink.setMetadataUuid(metadata.getUuid());
                    metadataLink.setLink(link);
                    link.getRecords().add(metadataLink);
                    linkRepository.save(link);
                }
            }).processAllRawText(element, md);
            entityManager.flush();
        }
    }

    public void purgeMetataLink(Link link) {
        metadataLinkRepository
                .findAll(metadatalinksTargetting(link))
                .stream()
                .filter(metadatalink -> isReferencingAnUnknownMetadata((MetadataLink)metadatalink))
                .forEach(metadataLinkRepository::delete);
        entityManager.flush();
    }

    public void deleteAll() {
        metadataLinkRepository.deleteAllInBatch();
        linkStatusRepository.deleteAllInBatch();
        linkRepository.deleteAllInBatch();
        entityManager.clear();
    }

    public void testLink(Link link) {
        LinkStatus linkStatus = urlChecker.getUrlStatus(link.getUrl());
        link.addStatus(linkStatus);
        linkRepository.save(link);
    }

    private Specification<MetadataLink> metadatalinksTargetting(Link link) {
        return new Specification<MetadataLink>() {
            @Override
            public Predicate toPredicate(Root<MetadataLink> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(MetadataLink_.link).get(Link_.id), link.getId());
            }
        };
    }

    private Specification<MetadataLink> metadatalinksTargetting(AbstractMetadata md) {
        return new Specification<MetadataLink>() {
            @Override
            public Predicate toPredicate(Root<MetadataLink> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.get(MetadataLink_.metadataId), md.getId());
            }
        };
    }

    private boolean isReferencingAnUnknownMetadata(MetadataLink metadatalink) {
        return isNull(metadataRepository.findOne(metadatalink.getMetadataId()));
    }


}
