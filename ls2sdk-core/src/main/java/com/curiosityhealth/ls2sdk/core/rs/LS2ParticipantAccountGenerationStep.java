package com.curiosityhealth.ls2sdk.core.rs;

import com.curiosityhealth.ls2sdk.core.LS2ParticipantAccountGeneratorCredentials;

import org.researchstack.backbone.step.Step;

/**
 * Created by jameskizer on 3/29/18.
 */

public class LS2ParticipantAccountGenerationStep extends Step {

    private String buttonText;
    private LS2ParticipantAccountGeneratorCredentials participantAccountGeneratorCredentials;

    public String getButtonText() {
        return this.buttonText;
    }

    public LS2ParticipantAccountGeneratorCredentials getParticipantAccountGeneratorCredentials() {
        return participantAccountGeneratorCredentials;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    @Override
    public Class getStepLayoutClass() {
        return LS2ParticipantAccountGenerationStepLayout.class;
    }

    public LS2ParticipantAccountGenerationStep(
            String identifier,
            String title,
            String text,
            String buttonText,
            LS2ParticipantAccountGeneratorCredentials participantAccountGeneratorCredentials
    ) {
        super(identifier, title);
        this.setText(text);
        this.buttonText = buttonText;
        this.participantAccountGeneratorCredentials = participantAccountGeneratorCredentials;
    }


}
