package com.curiosityhealth.ls2sdk.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.curiosityhealth.ls2sdk.common.*
import com.curiosityhealth.ls2sdk.common.LS2Datapoint.Companion.gson
import com.curiosityhealth.ls2sdk.database.LS2RealmDatapoint
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gson: Gson = {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LS2Schema::class.java, LS2Schema.JSONAdaptor())
            gsonBuilder.registerTypeAdapter(LS2AcquisitionProvenance::class.java, LS2AcquisitionProvenance.JSONAdaptor())
            gsonBuilder.registerTypeAdapter(LS2DatapointHeader::class.java, LS2DatapointHeader.JSONAdaptor())
            gsonBuilder.registerTypeAdapter(LS2ConcreteDatapoint::class.java, LS2ConcreteDatapoint.JSONAdaptor())
            gsonBuilder.registerTypeAdapter(LS2RealmDatapoint::class.java, LS2RealmDatapoint.JSONAdaptor())
            gsonBuilder.create()
        }()

        val header = LS2DatapointHeader(
                id = UUID.randomUUID(),
                schemaID = LS2Schema("test_datapoint", LS2SchemaVersion(1, 0, 0), "com.curiosityhealth"),
                acquisitionProvenance = LS2AcquisitionProvenance(LS2AcquisitionProvenance.defaultSourceName(this), Date(), LS2AcquisitionProvenanceModality.Sensed())
        )

        val body: Map<String, Any> = mapOf(Pair("testValue", 109.7))
        val datapoint = LS2Datapoint.createDatapoint(header, body)
        Log.d(TAG, datapoint.toString())

        val jsonString = datapoint.toJson(gson)
        Log.d(TAG, jsonString)

        val deserializedDatapoint = LS2Datapoint.fromJson<LS2ConcreteDatapoint>(jsonString, gson)

        Log.d(TAG, deserializedDatapoint.toString())

        val realmDatapoint = LS2RealmDatapoint.fromDatapoint(deserializedDatapoint)!!
        Log.d(TAG, realmDatapoint.toString())

        val realmJsonString = realmDatapoint.toJson(gson)
        Log.d(TAG, realmJsonString)

        val deserializedRealmDatapoint = LS2Datapoint.fromJson<LS2RealmDatapoint>(jsonString, gson)

        Log.d(TAG, deserializedRealmDatapoint.toString())
        

    }
}
