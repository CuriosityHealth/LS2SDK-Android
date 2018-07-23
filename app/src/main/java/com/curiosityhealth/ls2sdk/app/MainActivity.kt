package com.curiosityhealth.ls2sdk.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.curiosityhealth.ls2sdk.common.*
import com.curiosityhealth.ls2sdk.database.LS2DatabaseManager
import com.curiosityhealth.ls2sdk.database.LS2RealmDatapoint
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmConfiguration
import org.researchsuite.researchsuiteextensions.common.RSKeyValueStore
import org.researchsuite.researchsuiteextensions.encryption.RSEncryptionManager
import org.researchsuite.researchsuiteextensions.encryption.RSEncryptedJavaObjectConverter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.util.*
//import kotlin.experimental.and

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName

        private val hexArray = "0123456789ABCDEF".toCharArray()
        fun bytesToHex(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                assert(j < 64)
                val v = (bytes[j].toInt() and 0xFF)
                val shiftedV = v ushr 4
                assert(shiftedV < 16)
                hexChars[j * 2] = hexArray[shiftedV]
                assert(v.toInt() and 0x0F < 16)
                hexChars[j * 2 + 1] = hexArray[v.toInt() and 0x0F]
            }
            return String(hexChars)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gson: Gson = {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LS2Schema::class.java, LS2Schema.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2AcquisitionProvenance::class.java, LS2AcquisitionProvenance.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2DatapointHeader::class.java, LS2DatapointHeader.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2ConcreteDatapoint::class.java, LS2ConcreteDatapoint.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2RealmDatapoint::class.java, LS2RealmDatapoint.JSONAdapter())
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

//        val jsonString = datapoint.toJson(gson)
//        Log.d(TAG, jsonString)
//
//        val deserializedDatapoint = LS2Datapoint.fromJson<LS2ConcreteDatapoint>(jsonString, gson)
//
//        Log.d(TAG, deserializedDatapoint.toString())
//
//        val realmDatapoint = LS2RealmDatapoint.fromDatapoint(deserializedDatapoint)!!
//        Log.d(TAG, realmDatapoint.toString())
//
//        val realmJsonString = realmDatapoint.toJson(gson)
//        Log.d(TAG, realmJsonString)
//
//        val deserializedRealmDatapoint = LS2Datapoint.fromJson<LS2RealmDatapoint>(jsonString, gson)
//
//        Log.d(TAG, deserializedRealmDatapoint.toString())


//        val kvs = RSKeyValueStore(
//                filePath = this.filesDir.absolutePath + "/kvs/map.ser"
//        )

//        kvs.clear()

        val encryptionManager = RSEncryptionManager(
                "ls2sdk.app.masterKey",
                this,
                "ls2sdk.app.prefsFile"
        )

        val encryptor = encryptionManager.getAEADEncryptor("ls2sdk.kvs")

        val ekvs = RSKeyValueStore(
                filePath = this.filesDir.absolutePath + "/ekvs/map.ser",
                objectConverter = RSEncryptedJavaObjectConverter(encryptor)
        )

//        ekvs.clear()

//        val key = "ls2DatabaseKey"
//
//        val databaseKey: ByteArray = {
//            if (kvs.has(key) && ekvs.has(key)) {
//                assert(kvs.get(key) == ekvs.get(key))
//                ekvs.get(key)!! as ByteArray
//            }
//            else {
//                val generatedKey = RSEncryptionManager.generateKeyMaterial(64)
//                kvs.set(key, generatedKey)
//                ekvs.set(key, generatedKey)
//                generatedKey
//            }
//        }()
//
//        Log.d(TAG, MainActivity.bytesToHex(databaseKey))
//
//        assert(databaseKey.size == 64)
//
//        Realm.init(this)
//
//        val realmDir = File(this.filesDir, "realmDir")
//
//        val config = RealmConfiguration.Builder()
//                .directory(realmDir)
//                .name("ls2.realm")
//                .encryptionKey(databaseKey)
//                .build()
//
//        Realm.setDefaultConfiguration(config)

//        databaseStorageDirectory: String,
//        databaseFileName: String,
//        encrypted: Boolean,
//        queueStorageDirectory: String,
//        queueEncryptor: RSEncryptor,
//        val credentialStore: RSKeyValueStore
        val databaseManager = LS2DatabaseManager(
                context = this,
                databaseStorageDirectory = "ls2Database",
                databaseFileName = "ls2.realm",
                encrypted = true,
                queueStorageDirectory = "ls2Database.queue",
                queueEncryptor = encryptionManager.getAEADEncryptor("ls2db.queue"),
                credentialStore = ekvs
        )


        databaseManager.addDatapoint(datapoint)

        val realm = LS2DatabaseManager.realm
        val datapoints = realm.where(LS2RealmDatapoint::class.java)
        datapoints.findAll().forEach {
            Log.d(TAG, it.toString())
        }

    }
}
