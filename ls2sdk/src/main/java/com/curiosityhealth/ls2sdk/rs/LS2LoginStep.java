package com.curiosityhealth.ls2sdk.rs;

import org.researchsuite.rsuiteextensionscore.RSLoginStep;

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
        super(identifier, title, text, LS2LoginStepLayout.class);
        this.setForgotPasswordButtonTitle(forgotPasswordButtonTitle);
    }

}
