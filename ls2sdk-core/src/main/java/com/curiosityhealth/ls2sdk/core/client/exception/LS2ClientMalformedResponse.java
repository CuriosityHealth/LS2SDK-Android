package com.curiosityhealth.ls2sdk.core.client.exception;

import org.json.JSONObject;

/**
 * Created by jameskizer on 2/5/17.
 */
public class LS2ClientMalformedResponse extends LS2ClientException {

    private String responseBody;

    public LS2ClientMalformedResponse(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
