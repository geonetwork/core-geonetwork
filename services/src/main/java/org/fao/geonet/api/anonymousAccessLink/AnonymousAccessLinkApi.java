package org.fao.geonet.api.anonymousAccessLink;

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

@RequestMapping(value = {
		"/{portal}/api/anonymousAccessLink"
})
@Controller("anonymousAccessLink")
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
	public AnonymousAccessLink createAnonymousAccessLink(@RequestBody AnonymousAccessLink anonymousAccessLink) {
		String uuid = anonymousAccessLink.getMetadataUuid();
		AnonymousAccessLink anonymousAccessLinkToCreate = new AnonymousAccessLink() //
				.setMetadataId(metadataUtils.findOneByUuid(uuid).getId()) //
				.setMetadataUuid(uuid) //
				.setHash(uuid + "_hash");
		anonymousAccessLinkRepository.save(anonymousAccessLinkToCreate);
		return anonymousAccessLinkToCreate;
	}

}
