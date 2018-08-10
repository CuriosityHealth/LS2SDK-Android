package com.curiosityhealth.ls2sdk.core.omh;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jameskizer on 2/2/17.
 */
public abstract class OMHDataPointBuilder extends OMHDataPoint {

    public abstract String getDataPointID();
    public abstract Date getCreationDateTime();
    public abstract OMHSchema getSchema();

    @Nullable
    public abstract OMHAcquisitionProvenance getAcquisitionProvenance();

    public abstract JSONObject getBody();

    public JSONObject getSchemaJson() {
        OMHSchema schema = this.getSchema();

        Map<String, Object> map = new HashMap<>();

        map.put("name", schema.getName());
        map.put("namespace", schema.getNamespace());
        map.put("version", schema.getVersion());

        return new JSONObject(map);
    }

    public JSONObject getAcquisitionProvenanceJson() {

        OMHAcquisitionProvenance acquisitionProvenance = this.getAcquisitionProvenance();

        if (acquisitionProvenance != null) {
            Map<String, Object> map = new HashMap<>();

            String sourceName = acquisitionProvenance.getSourceName();
            if (sourceName != null) {
                map.put("source_name", sourceName);
            }

            String sourceCreationDateTime = stringFromDate(acquisitionProvenance.getSourceCreationDate());
            if (sourceCreationDateTime != null) {
                map.put("source_creation_date_time", sourceCreationDateTime);
            }

            String modality = acquisitionProvenance.getModalityString();
            if (modality != null) {
                map.put("modality", modality);
            }

            return new JSONObject(map);
        }
        else {
            return null;
        }
    }

    public JSONObject getHeader() {

        Map<String, Object> map = new HashMap<>();
        map.put("id", this.getDataPointID());
        map.put("creation_date_time", stringFromDate(this.getCreationDateTime()));
        map.put("schema_id", this.getSchemaJson());

        JSONObject acquisitionProvenance = this.getAcquisitionProvenanceJson();
        if (acquisitionProvenance != null) {
            map.put("acquisition_provenance", acquisitionProvenance);
        }

        return new JSONObject(map);
    }

    @Override
    public JSONObject toJson() {

        Map<String, Object> map = new HashMap<>();
        map.put("header", this.getHeader());
        map.put("body", this.getBody());

        return new JSONObject(map);

    }
}
