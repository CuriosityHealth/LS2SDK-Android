package com.curiosityhealth.ls2sdk.core;

import java.io.Serializable;

/**
 * Created by jameskizer on 3/29/18.
 */

public class LS2ParticipantAccountGeneratorCredentials implements Serializable {

    private String generatorId;
    private String generatorPassword;

    public String getGeneratorId() {
        return generatorId;
    }

    public String getGeneratorPassword() {
        return generatorPassword;
    }

    public LS2ParticipantAccountGeneratorCredentials(String generatorId, String generatorPassword) {
        this.generatorId = generatorId;
        this.generatorPassword = generatorPassword;
    }

}
