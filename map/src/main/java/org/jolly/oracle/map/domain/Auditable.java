package org.jolly.oracle.map.domain;

public interface Auditable {
    Audit getAudit();
    void setAudit(Audit audit);
}
