/*
 * =============================================================================
 * === Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * === United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * === and United Nations Environment Programme (UNEP)
 * ===
 * === This program is free software; you can redistribute it and/or modify
 * === it under the terms of the GNU General Public License as published by
 * === the Free Software Foundation; either version 2 of the License, or (at
 * === your option) any later version.
 * ===
 * === This program is distributed in the hope that it will be useful, but
 * === WITHOUT ANY WARRANTY; without even the implied warranty of
 * === MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * === General Public License for more details.
 * ===
 * === You should have received a copy of the GNU General Public License
 * === along with this program; if not, write to the Free Software
 * === Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * === Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * === Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */

package org.fao.geonet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.fao.geonet.domain.converter.BooleanToYNConverter;
import org.hibernate.annotations.Type;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Persistent state of an asynchronous resource upload.
 *
 * <p>Execution resources such as the worker future and active input stream are
 * not stored in this entity. They remain local to the application process
 * executing the upload.
 */
@Entity
@Access(AccessType.PROPERTY)
@JsonPropertyOrder(alphabetic = true)
@Table(
    name = ResourceUploadTask.TABLE_NAME,
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_rut_claim",
            columnNames = "claimKey"
        )
    },
    indexes = {
        @Index(
            name = "ix_rut_md_owner_date",
            columnList = "metadataUuid,ownerUserId,submittedDateTime"
        ),
        @Index(
            name = "ix_rut_worker_status",
            columnList = "workerId,status"
        ),
        @Index(
            name = "ix_rut_status_heartbeat",
            columnList = "status,lastHeartbeatDateTime"
        ),
        @Index(
            name = "ix_rut_ended",
            columnList = "endedDateTime"
        )
    }
)
public class ResourceUploadTask extends GeonetEntity {
    /**
     * Database table used to persist asynchronous upload tasks.
     */
    public static final String TABLE_NAME = "ResourceUploadTasks";

    private static final String RELEASED_CLAIM_PREFIX = "task:";
    private static final String ACTIVE_CLAIM_PREFIX = "upload:";

    private String id;
    private String metadataUuid;
    private Integer ownerUserId;
    private String sourceUrl;
    private MetadataResourceVisibility visibility;
    private Boolean approved;
    private ResourceUploadTaskStatus status;
    private String filename;
    private long bytesTransferred;
    private long totalBytes;
    private Date submittedDateTime;
    private Date startedDateTime;
    private Date endedDateTime;
    private Date lastHeartbeatDateTime;
    private String workerId;
    private String claimKey;
    private String error;

    /**
     * Constructor required by JPA.
     */
    protected ResourceUploadTask() {
    }

    /**
     * Creates a pending upload task with a unique identifier, initial heartbeat,
     * zero transferred bytes, and an unclaimed task-specific claim key.
     *
     * @param metadataUuid UUID of the metadata record receiving the resource
     * @param ownerUserId identifier of the user submitting the upload
     * @param sourceUrl remote URL from which the resource will be downloaded
     * @param visibility visibility assigned to the stored resource
     * @param approved whether the approved metadata version should be used
     * @param workerId identifier of the application process executing the task
     * @return a newly initialized pending upload task
     */
    public static ResourceUploadTask create(
        String metadataUuid,
        Integer ownerUserId,
        String sourceUrl,
        MetadataResourceVisibility visibility,
        boolean approved,
        String workerId
    ) {
        String id = UUID.randomUUID().toString();
        Date now = new Date();

        return new ResourceUploadTask()
            .setId(id)
            .setMetadataUuid(metadataUuid)
            .setOwnerUserId(ownerUserId)
            .setSourceUrl(sourceUrl)
            .setVisibility(visibility)
            .setApproved(approved)
            .setStatus(ResourceUploadTaskStatus.PENDING)
            .setBytesTransferred(0)
            .setTotalBytes(-1)
            .setSubmittedDateTime(now)
            .setLastHeartbeatDateTime(now)
            .setWorkerId(workerId)
            .setClaimKey(releasedClaimKey(id));
    }

    /**
     * Returns the unique non-active claim key used before filename resolution and
     * after a task reaches a terminal state.
     *
     * <p>A unique released key allows the database uniqueness constraint to
     * continue enforcing active claims without preventing terminal tasks from
     * retaining their history.
     *
     * @param taskId upload task identifier
     * @return unique non-active claim key for the task
     */
    public static String releasedClaimKey(String taskId) {
        return RELEASED_CLAIM_PREFIX + taskId;
    }

    /**
     * Returns the deterministic distributed claim key for a metadata UUID and
     * resolved filename.
     *
     * <p>The value is a SHA-256 digest of the length-delimited UUID and filename.
     * The database uniqueness constraint on this value prevents concurrent
     * uploads of the same filename for the same metadata record across
     * application nodes.
     *
     * @param metadataUuid UUID of the metadata record
     * @param filename resolved resource filename
     * @return deterministic active claim key
     * @throws IllegalStateException if SHA-256 is unavailable
     */
    public static String activeClaimKey(
        String metadataUuid,
        String filename
    ) {
        String value =
            metadataUuid.length() + ":" + metadataUuid
                + filename.length() + ":" + filename;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder encoded = new StringBuilder(hash.length * 2);
            for (byte valueByte : hash) {
                encoded.append(
                    Character.forDigit((valueByte >> 4) & 0xf, 16)
                );
                encoded.append(
                    Character.forDigit(valueByte & 0xf, 16)
                );
            }

            return ACTIVE_CLAIM_PREFIX + encoded;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "SHA-256 is not available.",
                e
            );
        }
    }

    @Id
    public String getId() {
        return id;
    }

    public ResourceUploadTask setId(String id) {
        this.id = id;
        return this;
    }

    @Column(nullable = false)
    public String getMetadataUuid() {
        return metadataUuid;
    }

    public ResourceUploadTask setMetadataUuid(String metadataUuid) {
        this.metadataUuid = metadataUuid;
        return this;
    }

    @JsonIgnore
    @Column(nullable = false)
    public Integer getOwnerUserId() {
        return ownerUserId;
    }

    public ResourceUploadTask setOwnerUserId(Integer ownerUserId) {
        this.ownerUserId = ownerUserId;
        return this;
    }

    @JsonProperty("url")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(nullable = false)
    public String getSourceUrl() {
        return sourceUrl;
    }

    public ResourceUploadTask setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MetadataResourceVisibility getVisibility() {
        return visibility;
    }

    public ResourceUploadTask setVisibility(
        MetadataResourceVisibility visibility
    ) {
        this.visibility = visibility;
        return this;
    }

    @Convert(converter = BooleanToYNConverter.class)
    @Column(nullable = false)
    public Boolean getApproved() {
        return approved;
    }

    public ResourceUploadTask setApproved(Boolean approved) {
        this.approved = approved;
        return this;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ResourceUploadTaskStatus getStatus() {
        return status;
    }

    public ResourceUploadTask setStatus(ResourceUploadTaskStatus status) {
        this.status = status;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public ResourceUploadTask setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    @Column(nullable = false)
    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public ResourceUploadTask setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
        return this;
    }

    @Column(nullable = false)
    public long getTotalBytes() {
        return totalBytes;
    }

    public ResourceUploadTask setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
        return this;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getSubmittedDateTime() {
        return submittedDateTime;
    }

    public ResourceUploadTask setSubmittedDateTime(
        Date submittedDateTime
    ) {
        this.submittedDateTime = submittedDateTime;
        return this;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartedDateTime() {
        return startedDateTime;
    }

    public ResourceUploadTask setStartedDateTime(Date startedDateTime) {
        this.startedDateTime = startedDateTime;
        return this;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndedDateTime() {
        return endedDateTime;
    }

    public ResourceUploadTask setEndedDateTime(Date endedDateTime) {
        this.endedDateTime = endedDateTime;
        return this;
    }

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastHeartbeatDateTime() {
        return lastHeartbeatDateTime;
    }

    public ResourceUploadTask setLastHeartbeatDateTime(
        Date lastHeartbeatDateTime
    ) {
        this.lastHeartbeatDateTime = lastHeartbeatDateTime;
        return this;
    }

    @JsonIgnore
    @Column(nullable = false)
    public String getWorkerId() {
        return workerId;
    }

    public ResourceUploadTask setWorkerId(String workerId) {
        this.workerId = workerId;
        return this;
    }

    @JsonIgnore
    @Column(length = 80, nullable = false)
    public String getClaimKey() {
        return claimKey;
    }

    public ResourceUploadTask setClaimKey(String claimKey) {
        this.claimKey = claimKey;
        return this;
    }

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String getError() {
        return error;
    }

    public ResourceUploadTask setError(String error) {
        this.error = error;
        return this;
    }

    /**
     * Returns whether the task has reached a terminal state.
     *
     * @return {@code true} for completed, failed, or cancelled tasks
     */
    @JsonIgnore
    @Transient
    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }
}
