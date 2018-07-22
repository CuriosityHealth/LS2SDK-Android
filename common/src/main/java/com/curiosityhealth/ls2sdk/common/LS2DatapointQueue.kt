package com.curiosityhealth.ls2sdk.common

import com.curiosityhealth.ls2sdk.common.LS2Datapoint.Companion.gson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.squareup.tape.FileObjectQueue
import com.squareup.tape.ObjectQueue
import org.researchsuite.researchsuiteextensions.encryption.RSEncryptor
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.security.GeneralSecurityException


public class LS2DatapointQueue(filePath: String, encryptor: RSEncryptor): FileObjectQueue<LS2Datapoint>(File(filePath), DatapointConverter(encryptor)) {

//    var listener: QueueListener? = null

    companion object {
        @Throws(IOException::class)
        fun createQueue(filePath: String, encryptor: RSEncryptor): LS2DatapointQueue {
            return LS2DatapointQueue(filePath, encryptor)
        }
    }

    //NOTE: this accepts any type of LS2Datapoint, but will ALWAYS instantiate an LS2ConcreteDatapoint
    //possibly make this generic so that another type of datapoint can be constructed (e.g., LS2RealmDatapoint)
    private class DatapointConverter(val encryptor: RSEncryptor, val gson: Gson = LS2Datapoint.gson): FileObjectQueue.Converter<LS2Datapoint> {

        @Throws(GeneralSecurityException::class, JsonParseException::class)
        override fun from(bytes: ByteArray): LS2Datapoint {

            //decyrpt bytes
            val clearBytes = encryptor.decrypt(bytes, null)

            //convert bytes into JSON
            val jsonString = String(clearBytes)
            val datapoint = gson.fromJson<LS2ConcreteDatapoint>(jsonString, LS2ConcreteDatapoint::class.java)
            return datapoint
        }

        @Throws(GeneralSecurityException::class)
        override fun toStream(o: LS2Datapoint, bytes: OutputStream) {

            //convert datapoint to JSON string
            val jsonString = gson.toJson(o)

            //encrypt json string
            val cipherBytes = encryptor.encrypt(jsonString.toByteArray(), null)

            //write encrypted bytes
            val writer = OutputStreamWriter(bytes)
            writer.append(String(cipherBytes))
            writer.close()

        }

    }

//    public class QueueListener() : ObjectQueue.Listener<LS2Datapoint> {
//
//        override fun onRemove(queue: ObjectQueue<LS2Datapoint>) {
//            onRemove(queue)
//        }
//
//        override fun onAdd(queue: ObjectQueue<LS2Datapoint>, entry: LS2Datapoint) {
//            onAdd(queue, entry)
//        }
//
//    }

}