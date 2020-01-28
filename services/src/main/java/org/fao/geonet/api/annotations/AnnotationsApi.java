package org.fao.geonet.api.annotations;


import org.fao.geonet.domain.AnnotationEntity;
import org.fao.geonet.repository.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.UUID.randomUUID;

@RestController
@RequestMapping("/api/annotations")
public class AnnotationsApi {

    @Autowired
    private AnnotationRepository annotationRepository;

    @PreAuthorize("hasRole('Administrator')")
    @GetMapping
    public ResponseEntity<?> findAll() {
        return new ResponseEntity<>(annotationRepository.findAll(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('Administrator')")
    @GetMapping(path ={"/{uuid}"})
    public ResponseEntity<?> getOne(@PathVariable("uuid") String uuid) {
        if (annotationRepository.exists(uuid)) {
           return new ResponseEntity<>(annotationRepository.findByUUID(uuid), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('Administrator')")
    @PutMapping
    public ResponseEntity<?> create(@RequestBody AnnotationEntity annotationEntity) {
        if (annotationEntity.getUuid() == null) {
            annotationEntity.setUuid(randomUUID().toString());
        }
        AnnotationEntity created = annotationRepository.save(annotationEntity);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('Administrator')")
    @PutMapping(path ={"/{uuid}"})
    public ResponseEntity<?> update(@PathVariable("uuid") String uuid, @RequestBody AnnotationEntity annotationEntity) {
        if (annotationRepository.exists(uuid)) {
            AnnotationEntity annotationEntityToUpdate = annotationRepository.findByUUID(uuid);
            annotationEntity.setUuid(uuid);
            annotationEntity.setId(annotationEntityToUpdate.getId());
            return new ResponseEntity<>(annotationRepository.save(annotationEntity), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
