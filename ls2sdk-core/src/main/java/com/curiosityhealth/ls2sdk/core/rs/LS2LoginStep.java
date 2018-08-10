package com.curiosityhealth.ls2sdk.core.rs;

import android.text.InputType;

import org.researchsuite.rsuiteextensionscore.RSLoginStep;
import org.researchsuite.rsuiteextensionscore.RSLoginStepLayout;

/**
 * Created by jameskizer on 3/14/18.
 */

public class LS2LoginStep extends RSLoginStep {

    public LS2LoginStep(
            String identifier,
            String title,
            String text,
            String forgotPasswordButtonTitle
    ) {
//        super(
//                identifier,
//                title,
//                text,
//                LS2LoginStepLayout.class,
//                "Login",
//                forgotPasswordButtonTitle,
//                "Username",
//                InputType.TYPE_CLASS_TEXT,
//                true,
//                "Password",
//                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
//                true
//        );

        super(
                identifier,
                title,
                text,
                LS2LoginStepLayout.class
        );
    }

}
