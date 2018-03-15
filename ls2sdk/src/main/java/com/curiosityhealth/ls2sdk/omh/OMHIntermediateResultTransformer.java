package com.curiosityhealth.ls2sdk.omh;

import android.content.Context;

import org.researchsuite.rsrp.RSRPIntermediateResult;

/**
 * Created by jameskizer on 3/15/18.
 */

public interface OMHIntermediateResultTransformer {
    OMHDataPoint transform(Context context, RSRPIntermediateResult intermediateResult);
}
