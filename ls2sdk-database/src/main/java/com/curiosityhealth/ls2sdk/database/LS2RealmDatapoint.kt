package com.curiosityhealth.ls2sdk.database

import com.curiosityhealth.ls2sdk.common.*
import com.curiosityhealth.ls2sdk.common.LS2Datapoint.Companion.gson
import com.google.gson.*
import io.realm.RealmObject
import io.realm.annotations.Ignore
import org.researchsuite.researchsuiteextensions.common.JsonConvertible
import org.researchsuite.researchsuiteextensions.common.asJsonObjectOrNull
import org.researchsuite.researchsuiteextensions.common.getObjectOrNull
import java.lang.reflect.Type
import java.util.*

open public class LS2RealmDatapoint: RealmObject(), LS2Datapoint, LS2DatapointEncodable {

    override fun toDatapoint(builder: LS2DatapointBuilder): LS2Datapoint {
        return LS2RealmDatapoint.toDatapoint(this, builder)
    }

    companion object: LS2DatapointBuilder, LS2DatapointConvertible<LS2RealmDatapoint> {

        val gson: Gson = {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LS2Schema::class.java, LS2Schema.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2AcquisitionProvenance::class.java, LS2AcquisitionProvenance.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2DatapointHeader::class.java, LS2DatapointHeader.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2ConcreteDatapoint::class.java, LS2ConcreteDatapoint.JSONAdapter())
            gsonBuilder.registerTypeAdapter(LS2RealmDatapoint::class.java, LS2RealmDatapoint.JSONAdapter())
            gsonBuilder.create()
        }()

        override fun createDatapoint(header: LS2DatapointHeader, body: Map<String, Any>): LS2RealmDatapoint {
            val datapoint = LS2RealmDatapoint()
            datapoint.configureHeader(header)
            datapoint.configureBody(body)
            return datapoint
        }

        override fun toDatapoint(src: LS2RealmDatapoint, builder: LS2DatapointBuilder): LS2Datapoint {
            return builder.createDatapoint(src.header, src.body)
        }

        override fun fromDatapoint(datapoint: LS2Datapoint): LS2RealmDatapoint? {
            return createDatapoint(datapoint.header, datapoint.body)
        }


        fun generateHeader(datapoint: LS2RealmDatapoint): LS2DatapointHeader? {
            val id = UUID.fromString(datapoint.idString)
            val schemaVersion = LS2SchemaVersion(datapoint.schemaVersionMajor, datapoint.schemaVersionMinor, datapoint.schemaVersionPatch)
            val schema = LS2Schema(datapoint.schemaName, schemaVersion, datapoint.schemaNamespace)
            val ap = LS2AcquisitionProvenanceModality.fromString(datapoint.apModalityString)?.let {
                LS2AcquisitionProvenance(datapoint.apSourceName, datapoint.apSourceCreationDateTime, it)
            }

            val metadata = datapoint.metadataJSONString?.let { gson.fromJson<Map<String, Any>>(it, Map::class.java) }

            return ap?.let { LS2DatapointHeader(id, schema, it, metadata) }
        }

        fun generateBody(datapoint: LS2RealmDatapoint): Map<String, Any>? {
            return gson.fromJson<Map<String, Any>>(datapoint.bodyJSONString, Map::class.java)
        }

//        fun createDatapoint(header: LS2DatapointHeader, body: Map<String, Any>): LS2RealmDatapoint {
//            val datapoint = LS2RealmDatapoint()
//            datapoint.configureHeader(header)
//            datapoint.configureBody()
//            return datapoint
//        }
    }

    @Ignore
    var _header: LS2DatapointHeader? = null
    override val header: LS2DatapointHeader
        get() = {
            if (this._header == null) {
                this._header = LS2RealmDatapoint.generateHeader(this)
            }
            this._header!!
        }()

    @Ignore
    var _body: Map<String, Any>? = null
    override val body: Map<String, Any>
        get() = {
            if (this._body == null) {
                this._body = LS2RealmDatapoint.generateBody(this)
            }
            this._body!!
        }()

    var idString: String = ""
    var schemaNamespace: String = ""
    var schemaName: String = ""
    var schemaVersionMajor: Int = 0
    var schemaVersionMinor: Int = 0
    var schemaVersionPatch: Int = 0
    var apSourceName: String = ""
    var apSourceCreationDateTime: Date = Date(1)
    var apModalityString: String = ""
    var metadataJSONString: String? = null

    var bodyJSONString: String = ""

    private fun configureHeader(header: LS2DatapointHeader) {

        this.idString = header.id.toString()
        this.schemaNamespace = header.schemaID.namespace
        this.schemaName = header.schemaID.name
        this.schemaVersionMajor = header.schemaID.version.major
        this.schemaVersionMinor = header.schemaID.version.minor
        this.schemaVersionPatch = header.schemaID.version.patch
        this.apSourceName = header.acquisitionProvenance.sourceName
        this.apSourceCreationDateTime = header.acquisitionProvenance.sourceCreationDateTime
        this.apModalityString = header.acquisitionProvenance.modality.toString

        this.metadataJSONString = header.metadata?.let {
            gson.toJson(it)
        }

        this._header = header

    }

    private fun configureBody(body: Map<String, Any>) {
        this.bodyJSONString = gson.toJson(body)
        this._body = body
    }


    public class JSONAdapter : JsonConvertible<LS2RealmDatapoint> {

        override fun serialize(src: LS2RealmDatapoint, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.add("header", context.serialize(src.header))
            jsonObject.add("body", context.serialize(src.body))
            return jsonObject
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LS2RealmDatapoint {

            val datapoint: LS2RealmDatapoint? = json.asJsonObjectOrNull?.let {

                val header: LS2DatapointHeader? = it.getObjectOrNull("header", context, LS2DatapointHeader::class.java)
                val body: Map<String, Any>? = it.getObjectOrNull("body", context, Map::class.java)

                if (header != null && body != null) {
                    LS2RealmDatapoint.createDatapoint(header, body)
                }
                else null

            }

            return datapoint ?: { throw JsonParseException("Cannot decode JSONAdapter") }()

        }

    }

}