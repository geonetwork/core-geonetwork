package org.fao.geonet.api.annotations;


import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
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

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

import static java.util.UUID.randomUUID;

@RestController
@RequestMapping("/api/annotations")
public class AnnotationsApi {

    @Autowired
    protected AnnotationRepository annotationRepository;

    protected IDateFactory dateFactory = new DateFactory();

    @PreAuthorize("hasRole('Administrator')")
    @GetMapping
    public ResponseEntity<?> findAll() {
        return new ResponseEntity<>(annotationRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(path ={"/{uuid}"})
    public ResponseEntity<?> getOne(@PathVariable("uuid") String uuid, HttpServletRequest request) {
        if (annotationRepository.exists(uuid)) {
            AnnotationEntity annotationEntity = annotationRepository.findByUUID(uuid);
            if (annotationEntity.getMetadataUuid() != null) {
                try {
                    ApiUtils.canViewRecord(annotationEntity.getMetadataUuid(), request);
                } catch (Exception e) {
                    throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW);
                }
            }
            Date todayNoon = dateFactory.getTodayNoon();
            if (annotationEntity.getLastRead() == null || todayNoon.compareTo(annotationEntity.getLastRead()) != 0) {
                annotationEntity.setLastRead(todayNoon);
                annotationEntity = annotationRepository.save(annotationEntity);
            }
            return new ResponseEntity<>(annotationEntity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping
    public ResponseEntity<?> create(@RequestBody AnnotationEntity annotationEntity, HttpServletRequest request) {
        if (annotationEntity.getMetadataUuid() != null) {
            try {
                ApiUtils.canEditRecord(annotationEntity.getMetadataUuid(), request);
            } catch (Exception e) {
                throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT);
            }
        }
        if (annotationEntity.getUuid() == null) {
            annotationEntity.setUuid(randomUUID().toString());
        }
        if (annotationRepository.exists(annotationEntity.getUuid())) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(
                            "duplicate_uuid",
                             String.format("annotation with uuid: $s already exists.", annotationEntity.getUuid())));
        }
        annotationEntity.setLastWrite(dateFactory.getTodayNoon());
        AnnotationEntity created = annotationRepository.save(annotationEntity);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping(path ={"/{uuid}"})
    public ResponseEntity<?> update(@PathVariable("uuid") String uuid, @RequestBody AnnotationEntity annotationEntity, HttpServletRequest request) {
        if (annotationRepository.exists(uuid)) {
            AnnotationEntity annotationEntityToUpdate = annotationRepository.findByUUID(uuid);
            if (annotationEntityToUpdate.getMetadataUuid() != null) {
                try {
                    ApiUtils.canEditRecord(annotationEntityToUpdate.getMetadataUuid(), request);
                } catch (Exception e) {
                    throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT);
                }
            }

            if (annotationEntity.getMetadataUuid() != null) {
                try {
                    ApiUtils.canEditRecord(annotationEntity.getMetadataUuid(), request);
                } catch (Exception e) {
                    throw new NotAllowedException(ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT);
                }
            }
            annotationEntityToUpdate.setGeometry(annotationEntity.getGeometry());
            annotationEntityToUpdate.setMetadataUuid(annotationEntity.getMetadataUuid());
            annotationEntityToUpdate.setLastWrite(dateFactory.getTodayNoon());
            return new ResponseEntity<>(annotationRepository.save(annotationEntityToUpdate), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    class ErrorResponse {
        ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        private String error;
        private String message;
    }

    interface IDateFactory {
        Date getTodayNoon();
    }

    class DateFactory implements IDateFactory {

        @Override
        public Date getTodayNoon() {
            Date todayNoon = Calendar.getInstance().getTime();
            todayNoon.setHours(12);
            todayNoon.setMinutes(0);
            todayNoon.setSeconds(0);
            return todayNoon;
        }
    }
}
