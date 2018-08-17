package com.curiosityhealth.ls2sdk.database

import com.curiosityhealth.ls2sdk.common.LS2ConcreteDatapoint
import com.curiosityhealth.ls2sdk.common.LS2DatapointConverter
import com.google.gson.Gson
import org.researchsuite.researchsuiteextensions.encryption.RSEncryptor
import kotlin.reflect.KClass

open class LS2RealmDatapointConverter(
        encryptor: RSEncryptor,
        gson: Gson
): LS2DatapointConverter<LS2RealmDatapoint>(encryptor, gson) {
    override val datapointClass: KClass<LS2RealmDatapoint>
        get() = LS2RealmDatapoint::class

}