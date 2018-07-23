package com.curiosityhealth.ls2sdk.core.client.exception;

/**
 * Created by jameskizer on 2/5/17.
 */
public class LS2ClientCredentialsFailure extends LS2ClientException {

    private String description;

    public LS2ClientCredentialsFailure(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
