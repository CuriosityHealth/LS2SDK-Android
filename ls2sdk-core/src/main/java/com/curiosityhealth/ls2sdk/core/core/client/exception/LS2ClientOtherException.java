package com.curiosityhealth.ls2sdk.core.core.client.exception;

/**
 * Created by jameskizer on 2/5/17.
 */
public class LS2ClientOtherException extends LS2ClientException {

    private Exception underlyingError;

    public LS2ClientOtherException(Exception underlyingError) {
        this.underlyingError = underlyingError;
    }

    public Exception getUnderlyingError() {
        return underlyingError;
    }

}
