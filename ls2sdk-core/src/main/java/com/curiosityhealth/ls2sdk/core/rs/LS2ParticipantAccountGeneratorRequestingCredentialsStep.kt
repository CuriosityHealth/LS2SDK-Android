package com.curiosityhealth.ls2sdk.core.rs

import android.text.InputType
import org.researchsuite.rsuiteextensionscore.RSLoginStep

open class LS2ParticipantAccountGeneratorRequestingCredentialsStep @JvmOverloads constructor(
        identifier: String,
        title: String? = null,
        text: String? = null,
        buttonText: String? = null,
        val generatorID: String? = null,
        identityFieldName: String? = null,
        passwordFieldName: String? = null
): RSLoginStep(
        identifier = identifier,
        title = title ?: "Log in",
        text = text ?: "Please log in",
        logInLayoutClass = LS2ParticipantAccountGeneratorRequestingCredentialsStepLayout::class.java,
        loginButtonTitle = buttonText ?: "Create Account",
        identityFieldName = identityFieldName ?: "Study ID",
        showIdentityField = (generatorID == null),
        passwordFieldName = passwordFieldName ?: "Study Passphrase",
        passwordFieldInputType = InputType.TYPE_CLASS_TEXT
) {

}