/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.api.auditable;

import javax.servlet.ServletRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fao.geonet.auditable.BaseAuditableService;
import org.fao.geonet.auditable.model.RevisionInfo;
import org.fao.geonet.domain.auditable.AuditableEntity;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping(value = {
    "/{portal}/api/auditable"
})
@Tag(name = "auditable",
    description = "Entity auditable operations")
@RestController("auditable")
public class AuditableApi {

    // Auditable service beans
    private final Map<String, BaseAuditableService<? extends AuditableEntity>> factory = new HashMap<>();

    public AuditableApi(ListableBeanFactory beanFactory) {
        Collection<BaseAuditableService> auditableServiceBeans = beanFactory.getBeansOfType(BaseAuditableService.class).values();
        auditableServiceBeans.forEach(filter -> factory.put(filter.getEntityType().toLowerCase(), filter));
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get an entity history",
        description = "")
    @GetMapping(
        value = "/{entityType}/{entityIdentifier}"
    )
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entity history details.")
    })
    @PreAuthorize("hasAuthority('UserAdmin')")
    public List<RevisionInfo> getEntityHistory(
        @Parameter(
            description = "Entity type",
            required = true
        )
        @PathVariable
        String entityType,
        @Parameter(
            description = "Entity identifier",
            required = true
        )
        @PathVariable
        Integer entityIdentifier
    ) {
        BaseAuditableService<? extends AuditableEntity> service = factory.get(entityType);
        return service.getEntityHistory(entityIdentifier);
    }
}
