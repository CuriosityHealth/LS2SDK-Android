package com.curiosityhealth.ls2sdk.core.omh;

import android.content.Context;

import org.researchsuite.rsrp.RSRPIntermediateResult;

/**
 * Created by jameskizer on 3/15/18.
 */

public class OMHDefaultTransformer implements OMHIntermediateResultTransformer {

    @Override
    public OMHDataPoint transform(Context context, RSRPIntermediateResult intermediateResult) {
        return null;
    }
}
