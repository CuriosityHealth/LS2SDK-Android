package com.curiosityhealth.ls2sdk.core.core.manager;

import android.content.Context;

import com.curiosityhealth.ls2sdk.core.LS2ParticipantAccountGeneratorCredentials;

/**
 * Created by jameskizer on 3/29/18.
 */

public interface LS2Provider {
    public LS2ParticipantAccountGeneratorCredentials getParticipantAccountGeneratorCredentials(Context context);
}
