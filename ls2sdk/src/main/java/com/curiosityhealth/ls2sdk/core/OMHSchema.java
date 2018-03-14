package com.curiosityhealth.ls2sdk.core;

/**
 * Created by jameskizer on 2/2/17.
 */
public class OMHSchema {
    private String name;
    private String namespace;
    private String version;

    public OMHSchema(String name, String namespace, String version) {
        this.name = name;
        this.namespace = namespace;
        this.version = version;
    }


    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getVersion() {
        return version;
    }
}
