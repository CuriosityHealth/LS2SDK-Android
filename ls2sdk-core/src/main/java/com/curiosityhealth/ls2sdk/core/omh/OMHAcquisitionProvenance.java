package com.curiosityhealth.ls2sdk.core.omh;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.researchstack.backbone.utils.LogExt;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jameskizer on 2/2/17.
 */
public class OMHAcquisitionProvenance {

    public enum OMHAcquisitionProvenanceModality {
        SENSED,
        SELF_REPORTED
    }

    public static String getOSString() {
        return String.format("Android %s", Build.VERSION.RELEASE);
    }

    public static String getDeviceString() {
        return String.format("%s %s", Build.MANUFACTURER, Build.MODEL);
    }

    public static String getApplicationName(Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    public static String defaultSourceName(Context context) {
        PackageManager manager = context.getPackageManager();

        String appName = getApplicationName(context);
        String OSString = getOSString();
        String deviceString = getDeviceString();

        try
        {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            String appVersion = info.versionName;
            String bundle = info.packageName;
            int appBuild = info.versionCode;

            return String.format("%s/%s (%s; build:%d; %s; %s)", appName, appVersion, bundle, appBuild, OSString, deviceString);
        }
        catch(PackageManager.NameNotFoundException e)
        {
            LogExt.e(OMHAcquisitionProvenance.class, "Could not find package version info");
            return String.format("%s (%s; %s)", appName, OSString, deviceString);
        }

    }

    private String sourceName;
    private Date sourceCreationDate;
    private OMHAcquisitionProvenanceModality modality;

    public OMHAcquisitionProvenance(String sourceName, Date sourceCreationDate, OMHAcquisitionProvenanceModality modality) {
        this.sourceName = sourceName;
        this.sourceCreationDate = sourceCreationDate;
        this.modality = modality;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Date getSourceCreationDate() {
        return sourceCreationDate;
    }

    public OMHAcquisitionProvenanceModality getModality() {
        return modality;
    }

    public String getModalityString() {
        switch (this.modality) {
            case SENSED:
                return "sensed";
            case SELF_REPORTED:
                return "self-reported";
            default:
                return null;
        }
    }

}
