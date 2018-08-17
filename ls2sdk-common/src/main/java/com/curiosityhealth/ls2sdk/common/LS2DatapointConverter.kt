package com.curiosityhealth.ls2sdk.common

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.squareup.tape2.ObjectQueue
import org.researchsuite.researchsuiteextensions.encryption.RSEncryptor
import java.io.OutputStream
import java.security.GeneralSecurityException
import kotlin.reflect.KClass

public abstract class LS2DatapointConverter <T: LS2Datapoint> (val encryptor: RSEncryptor, val gson: Gson): ObjectQueue.Converter<LS2Datapoint> {

    abstract val datapointClass: KClass<T>

    @Throws(GeneralSecurityException::class, JsonParseException::class)
    override fun from(bytes: ByteArray): T {

        //decyrpt bytes
        val clearBytes = encryptor.decrypt(bytes, null)

        //convert bytes into JSON
        val jsonString = String(clearBytes)
        val datapoint = gson.fromJson<T>(jsonString, datapointClass.java)
        return datapoint
    }

    @Throws(GeneralSecurityException::class)
    override fun toStream(o: LS2Datapoint, bytes: OutputStream) {

        //convert datapoint to JSON string
        val jsonString = gson.toJson(o)

        //encrypt json string
        val cipherBytes = encryptor.encrypt(jsonString.toByteArray(), null)
        bytes.write(cipherBytes)
    }

}

open class LS2ConcreteDatapointConverter(
        encryptor: RSEncryptor,
        gson: Gson
): LS2DatapointConverter<LS2ConcreteDatapoint>(encryptor, gson) {
    override val datapointClass: KClass<LS2ConcreteDatapoint>
        get() = LS2ConcreteDatapoint::class

}