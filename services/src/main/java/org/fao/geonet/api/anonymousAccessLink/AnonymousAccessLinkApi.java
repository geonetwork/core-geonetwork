package org.fao.geonet.api.anonymousAccessLink;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.AnonymousAccessLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequestMapping(value = {
		"/{portal}/api/anonymousAccessLink"
})
@Controller("anonymousAccessLink")
@Tag(name = "anonymous access links", description = "'permalinks to not published mds'")
public class AnonymousAccessLinkApi {

	@Autowired
	private IMetadataUtils metadataUtils;

	@Autowired
	private AnonymousAccessLinkRepository anonymousAccessLinkRepository;

	@RequestMapping(
			produces = MediaType.APPLICATION_JSON_VALUE,
			method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	@PreAuthorize("hasRole('Administrator')")
	@ResponseBody
	@io.swagger.v3.oas.annotations.Operation(
			summary = "Create anonymous access link",
			description = "")
	public AnonymousAccessLink createAnonymousAccessLink(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "at least {\"metadataUuid\":\"...\"}")
			@RequestBody AnonymousAccessLink anonymousAccessLink) {
		String uuid = anonymousAccessLink.getMetadataUuid();
		AnonymousAccessLink anonymousAccessLinkToCreate = new AnonymousAccessLink() //
				.setMetadataId(metadataUtils.findOneByUuid(uuid).getId()) //
				.setMetadataUuid(uuid) //
				.setHash(uuid + "_hash");
		anonymousAccessLinkRepository.save(anonymousAccessLinkToCreate);
		return anonymousAccessLinkToCreate;
	}

	@RequestMapping(
			produces = MediaType.APPLICATION_JSON_VALUE,
			method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@PreAuthorize("hasRole('Administrator')")
	@ResponseBody
	@io.swagger.v3.oas.annotations.Operation(
			summary = "List all anonymous access links",
			description = "")
	public List<AnonymousAccessLink> getAnonymousAccessLinks() {
		return anonymousAccessLinkRepository.findAll();
	}

	@RequestMapping(
			method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
	@PreAuthorize("hasRole('Administrator')")
	@ResponseBody
	@io.swagger.v3.oas.annotations.Operation(
			summary = "Delete an anonymous access link",
			description = "")
	public void deleteAccessLinks(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "at least {\"hash\":\"...\"}")
		  	@RequestBody AnonymousAccessLink anonymousAccessLink) {
		anonymousAccessLinkRepository.delete(anonymousAccessLinkRepository.findOneByHash(anonymousAccessLink.getHash()));
	}
}
