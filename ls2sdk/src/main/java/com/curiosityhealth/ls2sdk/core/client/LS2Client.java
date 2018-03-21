package com.curiosityhealth.ls2sdk.core.client;

import android.util.Log;

import com.curiosityhealth.ls2sdk.core.client.exception.*;
import com.curiosityhealth.ls2sdk.omh.OMHDataPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jameskizer on 3/14/18.
 */

public class LS2Client {

    final static String TAG = LS2Client.class.getSimpleName();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public class SignInResponse {
        private String authToken;

        public SignInResponse(String authToken) {
            this.authToken = authToken;
        }

        public String getAuthToken() {
            return authToken;
        }
    }

    public interface AuthCompletion {
        void onCompletion(SignInResponse response, Exception e);
    }

    public interface SignOutCompletion {
        void onCompletion(Boolean success, Exception e);
    }

    public interface PostSampleCompletion {
        void onCompletion(boolean success, Exception e);
    }

    private final static OkHttpClient client = new OkHttpClient();

    private String baseURL;

    public LS2Client(String baseURL) {
        this.baseURL = baseURL;
    }

    public void signIn(String username, String password, final AuthCompletion completion) {

        Map<String, String> bodyMap = new HashMap();
        bodyMap.put("username", username);
        bodyMap.put("password", password);
        JSONObject jsonBody = new JSONObject(bodyMap);
        RequestBody body = RequestBody.create(JSON, jsonBody.toString());

        Request request = new Request.Builder()
                .url(this.baseURL + "/auth/token")
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processAuthResponse(completion));
    }

    private Callback processAuthResponse(final AuthCompletion completion) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    completion.onCompletion(null, new LS2ClientUnreachable(e));
                }
                else {
                    completion.onCompletion(null, new LS2ClientOtherException(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {

                    String responseBody = "";
                    try {
                        responseBody = response.body().string();
                        JSONObject responseJson = new JSONObject(responseBody);

                        String authToken = responseJson.getString("token");

                        if (authToken != null) {
                            completion.onCompletion(new SignInResponse(authToken), null);
                            return;
                        }

                        else {
                            completion.onCompletion(null, new LS2ClientMalformedResponse(responseBody));
                            return;
                        }

                    } catch (JSONException e) {
//                        Log.e(TAG, "Fail to parse response from omh-sign-in endpoint:" + responseBody, e);
                        completion.onCompletion(null, new LS2ClientMalformedResponse(responseBody));
                        return;
                    }

                }
                else {

                    int responseCode = response.code();

                    if (responseCode == 502) {
                        completion.onCompletion(null, new LS2ClientBadGateway());
                        return;
                    }
                    else {
                        completion.onCompletion(null, new LS2ClientServerException());
                        return;
                    }

                }

            }
        };
    }

    public void signOut(String token, final SignOutCompletion completion) {

        RequestBody body = RequestBody.create(JSON, "{}");

        Request request = new Request.Builder()
                .url(this.baseURL + "/auth/token")
                .header("Authorization", "Token " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processSignOutResponse(completion));
    }

    private Callback processSignOutResponse(final SignOutCompletion completion) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    completion.onCompletion(null, new LS2ClientUnreachable(e));
                }
                else {
                    completion.onCompletion(null, new LS2ClientOtherException(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    completion.onCompletion(true, null);
                    return;
                }
                else {

                    int responseCode = response.code();

                    if (responseCode == 502) {
                        completion.onCompletion(null, new LS2ClientBadGateway());
                        return;
                    }
                    else {
                        completion.onCompletion(null, new LS2ClientServerException());
                        return;
                    }

                }

            }
        };
    }

    public boolean validateSample(OMHDataPoint sample) {
        boolean isValid = this.validateSampleJson(sample.toJson());
        return isValid;
    }

    public boolean validateSampleJson(JSONObject sampleJson) {
        try {
            String sampleJsonString = sampleJson.toString();
            Log.i(TAG, "validating json" + sampleJsonString);
            JSONObject recodedJson = new JSONObject(sampleJsonString);
            return true;
        } catch (JSONException ex) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void postSample(JSONObject sampleJson, String authToken, final PostSampleCompletion completion) {

        this.postJSONSample(sampleJson, authToken, completion);

    }

    public void postSample(String sampleString, String authToken, final PostSampleCompletion completion) {

        this.postStringSample(sampleString, authToken, completion);

    }

    private void postJSONSample(JSONObject sampleJson, String authToken, final PostSampleCompletion completion) {

        String jsonString = sampleJson.toString();
        RequestBody body = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url(this.baseURL + "/dataPoints")
                .header("Authorization", "Token " + authToken)
                .header("Accept", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processJSONResponse(completion));
    }

    private void postStringSample(String jsonString, String authToken, final PostSampleCompletion completion) {

        RequestBody body = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url(this.baseURL + "/dataPoints")
                .header("Authorization", "Token " + authToken)
                .header("Accept", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(this.processJSONResponse(completion));
    }

    private Callback processJSONResponse(final PostSampleCompletion completion) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e instanceof UnknownHostException) {
                    completion.onCompletion(false, new LS2ClientUnreachable(e));
                }
                else {
                    completion.onCompletion(false, new LS2ClientOtherException(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                int responseCode = response.code();

                if (response.isSuccessful() && responseCode == 201) {
                    completion.onCompletion(true, null);
                    return;
                }
                else if (responseCode == 400){
                    completion.onCompletion(false, new LS2ClientInvalidDataPoint());
                    return;
                }
                else if (responseCode == 401){
                    completion.onCompletion(false, new LS2ClientInvalidAuthToken());
                    return;
                }
                else if (responseCode == 409) {
                    completion.onCompletion(false, new LS2ClientDataPointConflict());
                    return;
                }
                else if (responseCode == 500) {
                    completion.onCompletion(false, new LS2ClientServerException());
                    return;
                }
                else if (responseCode == 502) {
                    completion.onCompletion(false, new LS2ClientBadGateway());
                    return;
                }
                else {
                    String responseBody = response.body().string();
                    completion.onCompletion(false, new LS2ClientMalformedResponse(responseBody));
                    return;
                }

            }
        };
    }

}
