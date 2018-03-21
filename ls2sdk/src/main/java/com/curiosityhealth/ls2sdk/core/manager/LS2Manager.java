package com.curiosityhealth.ls2sdk.core.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.curiosityhealth.ls2sdk.core.client.LS2Client;
import com.curiosityhealth.ls2sdk.core.client.exception.LS2ClientDataPointConflict;
import com.curiosityhealth.ls2sdk.core.client.exception.LS2ClientInvalidDataPoint;
import com.curiosityhealth.ls2sdk.core.manager.exception.LS2ManagerAlreadySignIn;
import com.curiosityhealth.ls2sdk.core.manager.exception.LS2ManagerNotSignedIn;
import com.curiosityhealth.ls2sdk.omh.OMHDataPoint;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;

import org.researchsuite.rsuiteextensionscore.RSCredentialStore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by jameskizer on 3/14/18.
 */

public class LS2Manager {

    private static class DatapointConverter implements FileObjectQueue.Converter<String> {
        @Override
        public String from(byte[] bytes) throws IOException {
            return new String(bytes);
        }

        @Override
        public void toStream(String o, OutputStream bytes) throws IOException {
            Writer writer = new OutputStreamWriter(bytes);
            writer.append(o);
            writer.close();
        }
    }

    private static class QueueListener implements ObjectQueue.Listener<String> {

        public QueueListener(LS2Manager manager) {
            this.manager = manager;
        }

        private LS2Manager manager;
        //Queue Listener Methods
        @Override
        public void onAdd(ObjectQueue<String> queue, String entry) {
            manager.upload();
        }

        @Override
        public void onRemove(ObjectQueue<String> queue) {

        }
    }

    public interface Completion {
        void onCompletion(Exception e);
    }

    public interface Delegate {
        void onInvalidToken(LS2Manager manager);
        void onSignIn(LS2Manager manager);
        void onSignOut(LS2Manager manager);
    }

    final static String TAG = LS2Manager.class.getSimpleName();

    private static String AUTHENTICATION_TOKEN = "AuthenticationToken";

    private static LS2Manager manager = null;
    private static Object managerLock = new Object();

    private RSCredentialStore credentialStore;
    private String authToken;
    private Object credentialsLock;
    private boolean credentialStoreUnlocked;

    private Context context;

    private LS2Client client;

    private Object uploadLock;
    FileObjectQueue<String> datapointQueue;
    ObjectQueue.Listener<String> queueListener;
    boolean isUploading = false;

    private Delegate delegate;

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    @Nullable
    public static LS2Manager getInstance() {
        synchronized (managerLock) {
            return manager;
        }
    }

    public static void config(Context context, String baseURL, RSCredentialStore store, String queueStorageDirectory) {
        synchronized (managerLock) {
            if (manager == null) {
                manager = new LS2Manager(context, baseURL, store, queueStorageDirectory);
            }
        }
    }


    @Nullable
    private String getAuthToken() {
        //if local authToken is null, try to load
        if (this.authToken == null) {
            byte[] authTokenData = this.credentialStore.get(context, AUTHENTICATION_TOKEN);
            if (authTokenData != null) {
                String authToken = new String(authTokenData);
                if (authToken != null  && !authToken.isEmpty()) {
                    this.authToken = authToken;
                }
            }
        }

        return this.authToken;
    }

    private LS2Manager(Context context, String baseURL, RSCredentialStore store, String queueStorageDirectory) {

        this.context = context;
        this.client = new LS2Client(baseURL);

        this.credentialsLock = new Object();

        this.credentialStore = store;

        this.credentialStoreUnlocked = false;

        //load queue from disk
        this.uploadLock = new Object();

        File queueFile = new File(context.getFilesDir() + queueStorageDirectory);
        DatapointConverter converter = new DatapointConverter();
        try {
            this.datapointQueue = new FileObjectQueue<>(queueFile, converter);
        } catch(IOException e) {
            e.printStackTrace();
        }

        this.queueListener = new QueueListener(this);
        //this calls onAdd for each element on queue
        this.datapointQueue.setListener(this.queueListener);
//        this.datapointQueue.setListener(this);

        //try to upload any existing datapoints
//        this.upload();

    }

    public void setCredentialStoreUnlocked(boolean credentialStoreUnlocked) {
        this.credentialStoreUnlocked = credentialStoreUnlocked;
        if (credentialStoreUnlocked) {
            this.getAuthToken();
            this.upload();
        }
    }

    public boolean isSignedIn() {
        synchronized (this.credentialsLock) {
            return this.getAuthToken() != null && !this.getAuthToken().isEmpty();
        }
    }

    private void setCredentials(String authToken) {
        synchronized (this.credentialsLock) {
            this.authToken = authToken;
            byte[] authTokenData = authToken.getBytes();
            this.credentialStore.set(context, AUTHENTICATION_TOKEN, authTokenData);
        }
    }

    private void clearCredentials() {

        //clear queue as well
        this.clearDatapointQueue();

        synchronized (this.credentialsLock) {
            this.authToken = null;
            this.credentialStore.remove(context, AUTHENTICATION_TOKEN);
        }

    }

    private void clearDatapointQueue() {
        while(this.datapointQueue.size() > 0) {
            this.datapointQueue.remove();
        }
    }

    public void checkTokenIsValid() {

    }

    //Sign In
    public void signIn(String username, String password, final Completion completion) {

//        if (this.isSignedIn()) {
//            completion.onCompletion(new LS2ManagerAlreadySignIn());
//            return;
//        }

        assert(this.credentialStoreUnlocked);

        this.client.signIn(username, password, new LS2Client.AuthCompletion() {
            @Override
            public void onCompletion(LS2Client.SignInResponse response, Exception e) {
                if (e != null) {
                    completion.onCompletion(e);
                    return;
                }

                if (response != null) {
                    setCredentials(response.getAuthToken());
                }

                if (LS2Manager.this.delegate != null) {
                    LS2Manager.this.delegate.onSignIn(LS2Manager.this);
                }

                completion.onCompletion(null);
                return;
            }
        });

    }

    public void signOut(final Completion completion) {

        if (this.isSignedIn()) {
            this.client.signOut(this.getAuthToken(), new LS2Client.SignOutCompletion() {
                @Override
                public void onCompletion(Boolean success, Exception e) {
                    LS2Manager.this.clearDatapointQueue();
                    LS2Manager.this.clearCredentials();
                    if (LS2Manager.this.delegate != null) {
                        LS2Manager.this.delegate.onSignOut(LS2Manager.this);
                    }
                    completion.onCompletion(null);
                }
            });
        }
        else {
            this.clearDatapointQueue();
            this.clearCredentials();
            if (LS2Manager.this.delegate != null) {
                LS2Manager.this.delegate.onSignOut(LS2Manager.this);
            }
            completion.onCompletion(null);
        }
    }


    public void addDatapoint(final OMHDataPoint datapoint, final Completion completion) {

        if (!this.isSignedIn()) {
            completion.onCompletion(new LS2ManagerNotSignedIn());
            return;
        }

        if (!this.client.validateSample(datapoint)) {
            Log.w(TAG, "Dropping datapoint, it looks like it's invalid: " + datapoint.toJson().toString());
//            Log.w(TAG, datapoint);
            completion.onCompletion(new LS2ClientInvalidDataPoint());
            return;
        }

        //add datapoint
        //this should notify the listener, which should start the upload
        String datapointString = datapoint.toJson().toString();
        this.datapointQueue.add(datapointString);

    }


    private void tryToUpload() {

        assert(this.isSignedIn());
        assert(this.credentialStoreUnlocked);

        synchronized (this.uploadLock) {

            if (this.isUploading) {
                return;
            }

            if (this.datapointQueue.size() < 1) {
                return;
            }

            this.isUploading = true;

            String datapointString = this.datapointQueue.peek();

            assert(datapointString != null && !datapointString.isEmpty());

            String localAuthToken;
            synchronized (this.credentialsLock) {
                localAuthToken = this.getAuthToken();
            }

            assert(localAuthToken != null && !localAuthToken.isEmpty());

            this.client.postSample(datapointString, localAuthToken, new LS2Client.PostSampleCompletion() {
                @Override
                public void onCompletion(boolean success, Exception e) {

//                    OhmageOMHManager.this.isUploading = false;

                    if (success) {
                        Log.w(TAG, "Datapoint successfully uploaded");
                        LS2Manager.this.datapointQueue.remove();

                        LS2Manager.this.isUploading = false;
                        LS2Manager.this.upload();
                        return;
                    }

                    Log.e(TAG, "Got an exception trying to upload datapoint", e);

                    if (e instanceof LS2ClientDataPointConflict ||
                            e instanceof LS2ClientInvalidDataPoint){

                        LS2Manager.this.datapointQueue.remove();
                        LS2Manager.this.isUploading = false;
                        LS2Manager.this.upload();
                        return;
                    }

                    else {
                        LS2Manager.this.isUploading = false;
                        return;
                    }

                }
            });

        }
    }

    private void upload() {


        if (!this.credentialStoreUnlocked || !this.isSignedIn()) { return; }

        //start async task here

        class UploadTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {

                LS2Manager.this.tryToUpload();

                return null;

            }
        }

        new UploadTask().execute();

    }

//    @Override
//    public void onAdd(ObjectQueue<String> queue, String entry) {
//        manager.upload();
//    }
//
//    @Override
//    public void onRemove(ObjectQueue<String> queue) {
//
//    }


}
