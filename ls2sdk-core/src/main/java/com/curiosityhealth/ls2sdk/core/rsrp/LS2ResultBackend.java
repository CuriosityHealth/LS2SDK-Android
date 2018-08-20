package com.curiosityhealth.ls2sdk.core.rsrp;

import android.content.Context;
import android.support.annotation.Nullable;
//import android.util.Log;

import com.curiosityhealth.ls2sdk.core.core.client.LS2Client;
import com.curiosityhealth.ls2sdk.core.core.manager.LS2Manager;
import com.curiosityhealth.ls2sdk.core.omh.OMHDataPoint;
import com.curiosityhealth.ls2sdk.core.omh.OMHDefaultTransformer;
import com.curiosityhealth.ls2sdk.core.omh.OMHIntermediateResultTransformer;

import org.researchsuite.rsrp.RSRPBackEnd;
import org.researchsuite.rsrp.RSRPIntermediateResult;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.util.Preconditions.checkState;

/**
 * Created by jameskizer on 3/15/18.
 */

public class LS2ResultBackend implements RSRPBackEnd {

    final static String TAG = LS2ResultBackend.class.getSimpleName();

    private static LS2ResultBackend backEnd= null;
    private static Object backEndLock = new Object();

    private LS2Manager manager;
    private List<OMHIntermediateResultTransformer> transformerList;
    @Nullable
    public static synchronized LS2ResultBackend getInstance() {
        synchronized (backEndLock) {
            return backEnd;
        }
    }

    public static void config(LS2Manager manager) {
        synchronized (backEndLock) {
            if (backEnd == null) {
                List<OMHIntermediateResultTransformer> transformerList = new ArrayList<>();
                transformerList.add(new OMHDefaultTransformer());
                backEnd = new LS2ResultBackend(manager, transformerList);
            }
        }
    }

    public static void config(LS2Manager manager, List<OMHIntermediateResultTransformer> transformerList) {
        synchronized (backEndLock) {
            if (backEnd == null) {
                backEnd = new LS2ResultBackend(manager, transformerList);
            }
        }
    }

    private LS2ResultBackend(LS2Manager manager, List<OMHIntermediateResultTransformer> transformerList) {
        this.manager = manager;
        this.transformerList = transformerList;
    }

    @Override
    public void add(Context context, RSRPIntermediateResult intermediateResult) {

        for (OMHIntermediateResultTransformer transformer : transformerList) {
            OMHDataPoint datapoint = transformer.transform(context, intermediateResult);
            if (datapoint != null) {
                this.manager.addDatapoint(datapoint, new LS2Manager.Completion() {
                    @Override
                    public void onCompletion(Exception e) {
                        if (e != null) {
//                            Log.e(TAG, "Got an exception trying to post datapoint", e);
                        }

                    }
                });
            }
        }
//        OMHDataPoint datapoint = ORBEIntermediateResultTransformerService.getInstance().transform(context, intermediateResult);
//
//        if (datapoint != null) {
//            OhmageOMHManager.getInstance().addDatapoint(datapoint, new OhmageOMHManager.Completion() {
//                @Override
//                public void onCompletion(Exception e) {
//
//                    //
//
//                }
//            });
//        }

    }

}
