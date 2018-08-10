package com.curiosityhealth.ls2sdk.core.rstb

import com.curiosityhealth.ls2sdk.core.rs.LS2ParticipantAccountGeneratorRequestingCredentialsStep
import com.google.gson.JsonObject
import org.researchstack.backbone.step.Step
import org.researchsuite.rstb.DefaultStepGenerators.RSTBBaseStepGenerator
import org.researchsuite.rstb.RSTBTaskBuilderHelper

class LS2ParticipantAccountGeneratorRequestingCredentialsStepGenerator: RSTBBaseStepGenerator() {

    init {
        this.supportedTypes = listOf("LS2ParticipantAccountGeneratorStepRequestingCredentials")
    }

    override fun generateSteps(
            helper: RSTBTaskBuilderHelper,
            type: String,
            jsonObject: JsonObject,
            identifierPrefix: String
    ): MutableList<Step>? {

        val descriptor =
                helper.gson.fromJson<LS2ParticipantAccountGeneratorRequestingCredentialsStepDescriptor>(
                        jsonObject,
                        LS2ParticipantAccountGeneratorRequestingCredentialsStepDescriptor::class.java
                )

//        val ls2ManagerKey = descriptor.ls2ManagerKey ?: "ls2Manager"

        val identifier = this.combineIdentifiers(descriptor.identifier, identifierPrefix)

        val generatorIDKey = descriptor.generatorIDKey
        if (generatorIDKey != null) {

            return (helper.stateHelper.get(helper.context, generatorIDKey)as? String)?.let {
                val step = LS2ParticipantAccountGeneratorRequestingCredentialsStep(
                        identifier = identifier,
                        title = descriptor.title,
                        text = descriptor.text,
                        generatorID = it,
                        passwordFieldName = descriptor.passwordFieldName
                )

                step.isOptional = false
                listOf(step).toMutableList()
            }
        }
        else {
            val step = LS2ParticipantAccountGeneratorRequestingCredentialsStep(
                    identifier = identifier,
                    title = descriptor.title,
                    text = descriptor.text,
                    identityFieldName = descriptor.identityFieldName,
                    passwordFieldName = descriptor.passwordFieldName
            )

            step.isOptional = false
            return listOf(step).toMutableList()
        }
    }
}