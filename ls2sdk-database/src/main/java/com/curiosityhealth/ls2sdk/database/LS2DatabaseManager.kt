package com.curiosityhealth.ls2sdk.database

import android.content.Context
import android.util.Log
import com.curiosityhealth.ls2sdk.common.LS2Datapoint
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.squareup.tape2.ObjectQueue
import com.squareup.tape2.QueueFile
//import com.squareup.tape2.ObjectQueue
//import com.squareup.tape.ObjectQueue
//import com.squareup.tape.QueueFile
import io.realm.Realm
import io.realm.RealmConfiguration
import org.researchsuite.researchsuiteextensions.common.RSKeyValueStore
import org.researchsuite.researchsuiteextensions.encryption.RSEncryptionManager
import org.researchsuite.researchsuiteextensions.encryption.RSEncryptor
import java.io.File
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.util.*


class LS2DatabaseManager(
        context: Context,
        databaseStorageDirectory: String,
        databaseFileName: String,
        encrypted: Boolean,
        queueStorageDirectory: String,
        queueEncryptor: RSEncryptor,
        val credentialStore: RSKeyValueStore
) {

    companion object {
        val DATABASE_ENCRYPTION_KEY = "ls2_database_key"
        val DATABASE_FILE_UUID = "ls2_file_uuid"
        val TAG = LS2DatabaseManager::class.java.simpleName
        val realm: Realm
            get() = Realm.getDefaultInstance()

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

    val datapointQueue: ObjectQueue<LS2Datapoint> = {
        val directory = File(context.filesDir, queueStorageDirectory)
        directory.mkdirs()
        val queueFile = QueueFile.Builder(File(directory, "ls2db.queue")).build()

        val datapointConverter = LS2RealmDatapointConverter(queueEncryptor, LS2RealmDatapoint.gson)
        val datapointQueue = ObjectQueue.create(queueFile, datapointConverter)
        datapointQueue
    }()

    val realmConfig: RealmConfiguration = {

        Realm.init(context)

        val configBuilder = RealmConfiguration.Builder()

        val fileUUID: UUID  = {

            (this.credentialStore.get(LS2DatabaseManager.DATABASE_FILE_UUID) as? UUID) ?: {
                val uuid = UUID.randomUUID()
                this.credentialStore.set(LS2DatabaseManager.DATABASE_FILE_UUID, uuid)
                uuid
            }()

        }()

        val relativeDatabasePath = databaseStorageDirectory + "/$fileUUID"
        val realmDir = File(context.filesDir, relativeDatabasePath)
        configBuilder.directory(realmDir)
        configBuilder.name(databaseFileName)

        if (encrypted) {
            val databaseEncyptionKey: ByteArray = {

                if (this.credentialStore.has(LS2DatabaseManager.DATABASE_ENCRYPTION_KEY)) {
                    this.credentialStore.get(LS2DatabaseManager.DATABASE_ENCRYPTION_KEY)!! as ByteArray
                }
                else {
                    val generatedKey = RSEncryptionManager.generateKeyMaterial(64)
                    this.credentialStore.set(LS2DatabaseManager.DATABASE_ENCRYPTION_KEY, generatedKey)
                    generatedKey
                }
            }()

            Log.d(TAG, LS2DatabaseManager.bytesToHex(databaseEncyptionKey))

            configBuilder.encryptionKey(databaseEncyptionKey)
        }

        configBuilder.build()
    }()


    init {
//        this.realmFilePath = this.realmConfig.path
        Realm.setDefaultConfiguration(this.realmConfig)
        this.trySync()
    }

    fun addDatapoint(datapoint: LS2Datapoint) {
        this.datapointQueue.add(datapoint)
        this.trySync()
    }

    fun trySync() {

        val datapoints = this.datapointQueue.map { it as LS2RealmDatapoint }.toSet()
        val realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            it.copyToRealm(datapoints)
        }

        Log.d(TAG, this.datapointQueue.toString())
        this.datapointQueue.remove(datapoints.size)
        Log.d(TAG, this.datapointQueue.toString())

    }

    fun deleteRealm() {

        this.datapointQueue.clear()

//        val realm = Realm.deleteRealm(this.realmConfig)

        val realm = Realm.getDefaultInstance()

        realm.deleteAll()
        realm.close()

        //now, delete the file
        Realm.deleteRealm(this.realmConfig)

        //also need to delete file uuid and key

    }


}