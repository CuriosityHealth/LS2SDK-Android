package com.curiosityhealth.ls2sdk.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

public interface JsonConvertible<T>:  JsonSerializer<T>,  JsonDeserializer<T> {

}

public val JsonElement.asJsonObjectOrNull: JsonObject?
    get() = if (this.isJsonObject) this.asJsonObject else null

public val JsonElement.asJsonPrimitiveOrNull: JsonPrimitive?
    get() = if (this.isJsonPrimitive) this.asJsonPrimitive else null

public val JsonPrimitive.asStringOrNull: String?
    get() = if (this.isString) this.asString else null

public fun JsonObject.getStringOrNull(memberName: String): String? {
    return if (this.has(memberName)) this.get(memberName).asJsonPrimitiveOrNull?.asStringOrNull else null
}

public fun JsonObject.getJsonObjectOrNull(memberName: String): JsonObject? {
    return if (this.has(memberName)) this.get(memberName).asJsonObjectOrNull else null
}

@Throws(JsonParseException::class)
public fun <T> JsonObject.getObjectOrNull(memberName: String, context: JsonDeserializationContext, typeOfT: Type): T? {
    val jsonObject = this.getJsonObjectOrNull(memberName)
    return jsonObject?.let {
        try {
            context.deserialize<T>(it, typeOfT)
        } catch (e: JsonParseException) {
            null
        }
    }
}



public data class LS2SchemaVersion(val major: Int, val minor: Int, val patch: Int) {

    companion object {
        fun fromString(versionString: String): LS2SchemaVersion? {
            val versionComponents: List<Int> = versionString.split('.').map { it.toIntOrNull() }.filterNotNull()
            if (versionComponents.count() != 3) return null
            return LS2SchemaVersion(versionComponents[0], versionComponents[1], versionComponents[2])
        }
    }

    val versionString: String
        get() = "$major.$minor.$patch"

}

public data class LS2Schema(val name: String, val version: LS2SchemaVersion, val namespace: String) {

    companion object {
        fun init(name: String, version: LS2SchemaVersion?, namespace: String): LS2Schema? {
            if (version != null) return LS2Schema(name, version, namespace)
            else return null
        }
    }

    public class JSONAdaptor : JsonConvertible<LS2Schema> {

        override public fun serialize(src: LS2Schema, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("name", src.name)
            jsonObject.addProperty("namespace", src.namespace)
            jsonObject.addProperty("version", src.version.versionString)
            return jsonObject
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LS2Schema {

            val schema: LS2Schema? = json.let {
                val jsonObject = json.asJsonObjectOrNull
                val name = jsonObject?.getStringOrNull("name")
                val namespace = jsonObject?.getStringOrNull("namespace")
                val version: LS2SchemaVersion? = jsonObject?.getStringOrNull("version")?.let { LS2SchemaVersion.fromString(it) }

                if (name != null && namespace != null && version != null) {
                    LS2Schema(name, version, namespace)
                }
                else null
            }

            return schema ?: { throw JsonParseException("Cannot decode LS2Schema") }()

        }
    }

}

public sealed class LS2AcquisitionProvenanceModality {

    class Sensed: LS2AcquisitionProvenanceModality()
    class SelfReported: LS2AcquisitionProvenanceModality()

    val toString: String
        get() = when(this) {
            is Sensed -> "sensed"
            is SelfReported -> "self-reported"
        }

    companion object {
        fun fromString(modality: String): LS2AcquisitionProvenanceModality? {
            return when(modality) {
                "sensed" -> Sensed()
                "self-reported" -> SelfReported()
                else -> null
            }
        }
    }

}

public data class LS2AcquisitionProvenance(val sourceName: String, val sourceCreationDateTime: Date, val modality: LS2AcquisitionProvenanceModality) {

    public class JSONAdaptor : JsonConvertible<LS2AcquisitionProvenance> {
        override fun serialize(src: LS2AcquisitionProvenance, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("source_name", src.sourceName)
            jsonObject.addProperty("source_creation_date_time", LS2Datapoint.dateFormatter.format(src.sourceCreationDateTime))
            jsonObject.addProperty("modality", src.modality.toString)
            return jsonObject
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LS2AcquisitionProvenance {

            val ap: LS2AcquisitionProvenance? = json.asJsonObjectOrNull?.let {
                val sourceName = it.getStringOrNull("source_name")
                val sourceCreationDateTime: Date? = it.getStringOrNull("source_creation_date_time")?.let { LS2Datapoint.parse(it) }
                val modality = it.getStringOrNull("modality")?.let { LS2AcquisitionProvenanceModality.fromString(it) }

                if (sourceName != null && sourceCreationDateTime != null && modality != null) {
                    LS2AcquisitionProvenance(sourceName, sourceCreationDateTime, modality)
                }
                else null

            }

            return ap ?: { throw JsonParseException("Cannot decode LS2AcquisitionProvenance") }()
        }

    }

    companion object {

        fun getOSString(): String {
            return String.format("Android %s", Build.VERSION.RELEASE)
        }

        fun getDeviceString(): String {
            return String.format("%s %s", Build.MANUFACTURER, Build.MODEL)
        }

        fun getApplicationName(context: Context): String {
            return context.applicationInfo.loadLabel(context.packageManager).toString()
        }

        fun defaultSourceName(context: Context): String {
            val manager = context.packageManager

            val appName = getApplicationName(context)
            val OSString = getOSString()
            val deviceString = getDeviceString()

            try {
                val info = manager.getPackageInfo(context.packageName, 0)

                val appVersion = info.versionName
                val bundle = info.packageName
                val appBuild = {
//                    if (Build.VERSION.SDK_INT < 28) info.versionCode
//                    else info.longVersionCode
                    info.versionCode
                }()

                return String.format("%s/%s (%s; build:%d; %s; %s)", appName, appVersion, bundle, appBuild, OSString, deviceString)

            } catch (e: PackageManager.NameNotFoundException) {

                return String.format("%s (%s; %s)", appName, OSString, deviceString)
            }

        }
    }

}

public data class LS2DatapointHeader(val id: UUID, val schemaID: LS2Schema, val acquisitionProvenance: LS2AcquisitionProvenance, val metadata: Map<String, Any>? = null) {

    public class JSONAdaptor : JsonConvertible<LS2DatapointHeader> {
        override fun serialize(src: LS2DatapointHeader, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", src.id.toString())
            jsonObject.add("schema_id", context.serialize(src.schemaID))
            jsonObject.add("acquisition_provenance", context.serialize(src.acquisitionProvenance))
            src.metadata?.let { jsonObject.add("metadata", context.serialize(it)) }
            return jsonObject
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LS2DatapointHeader {

            val header: LS2DatapointHeader? = json.asJsonObjectOrNull?.let {
                val id = it.getStringOrNull("id")?.let { UUID.fromString(it) }
                val schemaID: LS2Schema? = it.getObjectOrNull("schema_id", context, LS2Schema::class.java)
                val ap: LS2AcquisitionProvenance? = it.getObjectOrNull("acquisition_provenance", context, LS2AcquisitionProvenance::class.java)
                val metadata: Map<String, Any>? = it.getObjectOrNull("metadata", context, Map::class.java)

                if (id != null && schemaID != null && ap != null) {
                    LS2DatapointHeader(id, schemaID, ap, metadata)
                }
                else null

            }

            return header ?: { throw JsonParseException("Cannot decode LS2DatapointHeader") }()

        }

    }

}

public interface LS2Datapoint {

    val header: LS2DatapointHeader
    val body: Map<String, Any>

    companion object {

        val gson: Gson = {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(LS2Schema::class.java, LS2Schema.JSONAdaptor())
            gsonBuilder.registerTypeAdapter(LS2AcquisitionProvenance::class.java, LS2AcquisitionProvenance.JSONAdaptor())
            gsonBuilder.registerTypeAdapter(LS2DatapointHeader::class.java, LS2DatapointHeader.JSONAdaptor())
            gsonBuilder.registerTypeAdapter(LS2ConcreteDatapoint::class.java, LS2ConcreteDatapoint.JSONAdaptor())
            gsonBuilder.create()
        }()

        val dateFormatter = {
            val tz = TimeZone.getTimeZone("UTC")
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
            df.timeZone = tz
            df
        }()

        fun format(date: Date): String {
            return dateFormatter.format(date)
        }

        fun parse(dateString: String): Date? {
            try {
                return dateFormatter.parse(dateString)
            } catch (e: ParseException) {
                e.printStackTrace()
                return null
            }

        }
    }
}

public interface LS2DatapointBuilder {
    fun createDatapoint(header: LS2DatapointHeader, body: Map<String, Any>): LS2Datapoint
}

public interface LS2DatapointEncoder<T> {
    fun toDatapoint(src: T): LS2Datapoint
}

public interface LS2DatapointDecoder<T> {
    fun fromDatapoint(datapoint: LS2Datapoint): T?
}

public interface LS2DatapointConvertible<T>: LS2DatapointEncoder<T>, LS2DatapointDecoder<T> {}

public data class LS2ConcreteDatapoint(
        override val header: LS2DatapointHeader,
        override val body: Map<String, Any>
): LS2Datapoint {

    companion object: LS2DatapointBuilder, LS2DatapointConvertible<LS2ConcreteDatapoint> {

        override fun createDatapoint(header: LS2DatapointHeader, body: Map<String, Any>): LS2ConcreteDatapoint {
            return LS2ConcreteDatapoint(header, body)
        }

        override fun toDatapoint(src: LS2ConcreteDatapoint): LS2Datapoint {
            return src
        }

        override fun fromDatapoint(datapoint: LS2Datapoint): LS2ConcreteDatapoint? {
            return createDatapoint(datapoint.header, datapoint.body)
        }

    }

    public class JSONAdaptor : JsonConvertible<LS2ConcreteDatapoint> {
        override fun serialize(src: LS2ConcreteDatapoint, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.add("header", context.serialize(src.header))
            jsonObject.add("body", context.serialize(src.body))
            return jsonObject
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LS2ConcreteDatapoint {

            val datapoint: LS2ConcreteDatapoint? = json.asJsonObjectOrNull?.let {

                val header: LS2DatapointHeader? = it.getObjectOrNull("header", context, LS2DatapointHeader::class.java)
                val body: Map<String, Any>? = it.getObjectOrNull("body", context, Map::class.java)

                if (header != null && body != null) {
                    LS2ConcreteDatapoint(header, body)
                }
                else null

            }

            return datapoint ?: { throw JsonParseException("Cannot decode JSONAdaptor") }()

        }

    }

}