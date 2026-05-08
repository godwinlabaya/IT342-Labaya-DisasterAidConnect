package edu.cit.labaya.disasteraidconnect.aidrequest;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "aid_requests_images")
public class AidRequestImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "aid_request_id", columnDefinition = "uuid")
    private UUID aidRequestId;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "uploaded_at", updatable = false,
            columnDefinition = "timestamp with time zone default now()")
    private OffsetDateTime uploadedAt;

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId()                                  { return id; }
    public void setId(UUID id)                           { this.id = id; }

    public UUID getAidRequestId()                        { return aidRequestId; }
    public void setAidRequestId(UUID aidRequestId)       { this.aidRequestId = aidRequestId; }

    public String getImageUrl()                          { return imageUrl; }
    public void   setImageUrl(String imageUrl)           { this.imageUrl = imageUrl; }

    public OffsetDateTime getUploadedAt()                { return uploadedAt; }
    public void           setUploadedAt(OffsetDateTime t){ this.uploadedAt = t; }
}