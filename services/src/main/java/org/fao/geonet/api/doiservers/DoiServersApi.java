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

package org.fao.geonet.api.doiservers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.doiservers.model.AnonymousDoiServer;
import org.fao.geonet.api.doiservers.model.DoiServerDto;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.DoiServer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.DoiServerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping(value = {
    "/{portal}/api/doiservers"
})
@Tag(name = "doiservers",
    description = "DOI servers related operations")
@RestController("doiservers")
public class DoiServersApi {
    private static final String API_PARAM_DOISERVER_IDENTIFIER = "DOI server identifier";

    private static final String API_PARAM_DOISERVER_DETAILS = "DOI server details";

    public static final String MSG_DOISERVER_WITH_ID_NOT_FOUND = "DOI server with id '%s' not found.";


    private final DoiServerRepository doiServerRepository;

    private final IMetadataUtils metadataUtils;

    DoiServersApi(final DoiServerRepository doiServerRepository, final IMetadataUtils metadataUtils) {
        this.doiServerRepository = doiServerRepository;
        this.metadataUtils = metadataUtils;

    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get DOI servers"
    )
    @GetMapping(
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of all DOI servers."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    List<AnonymousDoiServer> getDoiServers() {
        List<DoiServer> doiServers = doiServerRepository.findAll();
        List<AnonymousDoiServer> list = new ArrayList<>(doiServers.size());
        doiServers.stream().forEach(e -> list.add(new AnonymousDoiServer(DoiServerDto.from(e))));
        return list;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get DOI servers that can be used with a metadata"
    )
    @GetMapping(value = "metadata/{metadataId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    public
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of all DOI servers where a metadata can be published."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    List<AnonymousDoiServer> getDoiServers(
        @Parameter(description = "Metadata UUID",
            required = true,
            example = "")
        @PathVariable Integer metadataId) {

        List<DoiServer> doiServers = doiServerRepository.findAll();
        List<AnonymousDoiServer> list = new ArrayList<>(doiServers.size());

        AbstractMetadata metadata = metadataUtils.findOne(metadataId);
        Integer groupOwner = metadata.getSourceInfo().getGroupOwner();

        // Find servers related to the metadata groups owner
        List<DoiServer> doiServersForMetadata = doiServers.stream().filter(
            s -> s.getPublicationGroups().stream().anyMatch(g -> g.getId() == groupOwner)).collect(Collectors.toList());

        if (doiServersForMetadata.isEmpty()) {
            // If no servers related to the metadata groups owner,
            // find the servers that are not related to any metadata group
            doiServersForMetadata = doiServers.stream()
                .filter(s -> s.getPublicationGroups().isEmpty())
                .collect(Collectors.toList());
        }

        doiServersForMetadata.forEach(s -> {
            DoiServerDto doiServerDto = DoiServerDto.from(s);
            list.add(new AnonymousDoiServer(doiServerDto));
        });

        return list;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get a DOI Server"
    )
    @GetMapping(value = "/{doiServerId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_EDITOR)
    })
    public AnonymousDoiServer getDoiServer(
        @Parameter(description = API_PARAM_DOISERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable String doiServerId
    ) throws ResourceNotFoundException {
        Optional<DoiServer> doiServerOpt = doiServerRepository.findOneById(Integer.parseInt(doiServerId));
        if (doiServerOpt.isEmpty()) {
            throw new ResourceNotFoundException(String.format(
                MSG_DOISERVER_WITH_ID_NOT_FOUND,
                doiServerId
            ));
        } else {
            return new AnonymousDoiServer(DoiServerDto.from(doiServerOpt.get()));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Add a DOI server",
        description = "Return the id of the newly created DOI server."
    )
    @PutMapping(
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "DOI server created."),
        @ApiResponse(responseCode = "400", description = "Bad parameters."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Integer> addDoiServer(
        @Parameter(
            description = API_PARAM_DOISERVER_DETAILS,
            required = true
        )
        @RequestBody
        DoiServerDto doiServerDto
    ) {
        Optional<DoiServer> existingDoiServerOpt = doiServerRepository.findOneById(doiServerDto.getId());
        if (existingDoiServerOpt.isPresent()) {
            throw new IllegalArgumentException(String.format(
                "DOI server with id '%d' already exists.",
                doiServerDto.getId()
            ));
        } else {
            DoiServer doiServer = doiServerDto.asDoiServer();
            doiServerRepository.save(doiServer);

            return new ResponseEntity<>(doiServer.getId(), HttpStatus.CREATED);
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a DOI server"
    )
    @PutMapping(
        value = "/{doiServerId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "DOI server updated."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDoiServer(
        @Parameter(description = API_PARAM_DOISERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable Integer doiServerId,
        @Parameter(description = API_PARAM_DOISERVER_DETAILS,
            required = true)
        @RequestBody
        DoiServerDto doiServerDto
    ) throws ResourceNotFoundException {
        Optional<DoiServer> existingMapserverOpt = doiServerRepository.findOneById(doiServerId);
        if (existingMapserverOpt.isPresent()) {
            DoiServer doiServer = doiServerDto.asDoiServer();

            doiServerRepository.update(doiServerId, entity -> {
                entity.setName(doiServer.getName());
                entity.setDescription(doiServer.getDescription());
                entity.setUrl(doiServer.getUrl());
                entity.setPublicUrl(doiServer.getPublicUrl());
                entity.setLandingPageTemplate(doiServer.getLandingPageTemplate());
                entity.setPattern(doiServer.getPattern());
                entity.setPrefix(doiServer.getPrefix());
                entity.setPublicationGroups(doiServer.getPublicationGroups());
            });
        } else {
            throw new ResourceNotFoundException(String.format(
                MSG_DOISERVER_WITH_ID_NOT_FOUND,
                doiServerId
            ));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Remove a DOI server"
    )
    @DeleteMapping(
        value = "/{doiServerId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "DOI server removed."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMapserver(
        @Parameter(description = API_PARAM_DOISERVER_IDENTIFIER,
            required = true
        )
        @PathVariable Integer doiServerId
    ) throws ResourceNotFoundException {
        Optional<DoiServer> existingMapserverOpt = doiServerRepository.findOneById(doiServerId);
        if (existingMapserverOpt.isPresent()) {
            doiServerRepository.delete(existingMapserverOpt.get());
        } else {
            throw new ResourceNotFoundException(String.format(
                MSG_DOISERVER_WITH_ID_NOT_FOUND,
                doiServerId
            ));
        }
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Update a DOI server authentication"
    )
    @PostMapping(
        value = "/{doiServerId}/auth",
        produces = {
            MediaType.APPLICATION_JSON_VALUE
        })
    @PreAuthorize("hasAuthority('Administrator')")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "DOI server updated."),
        @ApiResponse(responseCode = "404", description = ApiParams.API_RESPONSE_RESOURCE_NOT_FOUND),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_ONLY_ADMIN)
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDoiServerAuth(
        @Parameter(
            description = API_PARAM_DOISERVER_IDENTIFIER,
            required = true,
            example = "")
        @PathVariable Integer doiServerId,
        @Parameter(
            description = "User name",
            required = true)
        @RequestParam
        String username,
        @Parameter(
            description = "Password",
            required = true)
        @RequestParam
        String password
    ) throws ResourceNotFoundException {
        Optional<DoiServer> existingMapserverOpt = doiServerRepository.findOneById(doiServerId);
        if (existingMapserverOpt.isPresent()) {
            doiServerRepository.update(doiServerId, entity -> {
                entity.setUsername(username);
                entity.setPassword(password);
            });
        } else {
            throw new ResourceNotFoundException(String.format(
                MSG_DOISERVER_WITH_ID_NOT_FOUND,
                doiServerId
            ));
        }
    }
}
