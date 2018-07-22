package com.curiosityhealth.ls2sdk

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.curiosityhealth.ls2sdk.common.*
import com.google.gson.GsonBuilder
import java.util.*

class MainActivity: AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(LS2Schema::class.java, LS2Schema.JSONAdaptor())
        gsonBuilder.registerTypeAdapter(LS2AcquisitionProvenance::class.java, LS2AcquisitionProvenance.JSONAdaptor())
        gsonBuilder.registerTypeAdapter(LS2DatapointHeader::class.java, LS2DatapointHeader.JSONAdaptor())
        gsonBuilder.registerTypeAdapter(LS2ConcreteDatapoint::class.java, LS2ConcreteDatapoint.JSONAdaptor())
        val gson = gsonBuilder.create()

        val header = LS2DatapointHeader(
                id = UUID.randomUUID(),
                schemaID = LS2Schema("test_datapoint", LS2SchemaVersion(1, 0, 0), "com.curiosityhealth"),
                acquisitionProvenance = LS2AcquisitionProvenance(LS2AcquisitionProvenance.defaultSourceName(this), Date(), LS2AcquisitionProvenanceModality.Sensed())
        )

        val body: Map<String, Any> = mapOf(Pair("testValue", 109.7))
        val datapoint = LS2ConcreteDatapoint(header, body)
        Log.d(TAG, datapoint.toString())

        val jsonString = gson.toJson(datapoint)
        Log.d(TAG, jsonString)

        val deserializedDatapoint = gson.fromJson<LS2ConcreteDatapoint>(jsonString, LS2ConcreteDatapoint::class.java)

        Log.d(TAG, deserializedDatapoint.toString())



    }

}