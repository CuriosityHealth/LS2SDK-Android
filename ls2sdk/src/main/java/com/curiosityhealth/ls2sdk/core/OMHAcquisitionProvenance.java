package com.curiosityhealth.ls2sdk.core;

import java.util.Date;

/**
 * Created by jameskizer on 2/2/17.
 */
public class OMHAcquisitionProvenance {

    public enum OMHAcquisitionProvenanceModality {
        SENSED,
        SELF_REPORTED
    }

    private String sourceName;
    private Date sourceCreationDate;
    private OMHAcquisitionProvenanceModality modality;

    public OMHAcquisitionProvenance(String sourceName, Date sourceCreationDate, OMHAcquisitionProvenanceModality modality) {
        this.sourceName = sourceName;
        this.sourceCreationDate = sourceCreationDate;
        this.modality = modality;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Date getSourceCreationDate() {
        return sourceCreationDate;
    }

    public OMHAcquisitionProvenanceModality getModality() {
        return modality;
    }

    public String getModalityString() {
        switch (this.modality) {
            case SENSED:
                return "sensed";
            case SELF_REPORTED:
                return "self-reported";
            default:
                return null;
        }
    }

}
