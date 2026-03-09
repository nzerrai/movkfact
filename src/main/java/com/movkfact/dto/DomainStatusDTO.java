package com.movkfact.dto;

/**
 * DTO representing aggregated status flags for a domain or dataset.
 * - downloaded: at least one DOWNLOADED activity exists
 * - modified:   dataset.version > 0 (at least one edit since creation)
 * - viewed:     at least one VIEWED activity exists
 */
public class DomainStatusDTO {

    private boolean downloaded;
    private boolean modified;
    private boolean viewed;

    public DomainStatusDTO() {}

    public DomainStatusDTO(boolean downloaded, boolean modified, boolean viewed) {
        this.downloaded = downloaded;
        this.modified = modified;
        this.viewed = viewed;
    }

    public boolean isDownloaded() { return downloaded; }
    public void setDownloaded(boolean downloaded) { this.downloaded = downloaded; }

    public boolean isModified() { return modified; }
    public void setModified(boolean modified) { this.modified = modified; }

    public boolean isViewed() { return viewed; }
    public void setViewed(boolean viewed) { this.viewed = viewed; }
}
