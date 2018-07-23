package com.curiosityhealth.ls2sdk.rstb;

import com.curiosityhealth.ls2sdk.core.manager.LS2Provider;
import com.curiosityhealth.ls2sdk.rs.LS2LoginStep;
import com.curiosityhealth.ls2sdk.rs.LS2ParticipantAccountGenerationStep;
import com.google.gson.JsonObject;

import org.researchstack.backbone.step.Step;
import org.researchsuite.rstb.DefaultStepGenerators.RSTBBaseStepGenerator;
import org.researchsuite.rstb.DefaultStepGenerators.descriptors.RSTBStepDescriptor;
import org.researchsuite.rstb.RSTBTaskBuilderHelper;

import java.util.Arrays;

/**
 * Created by jameskizer on 3/29/18.
 */

public class LS2ParticipantAccountGenerationStepGenerator extends RSTBBaseStepGenerator {

    public LS2ParticipantAccountGenerationStepGenerator()
    {
        super();
        this.supportedTypes = Arrays.asList(
                "LS2ParticipantAccountGeneratorStep"
        );
    }

    @Override
    public Step generateStep(RSTBTaskBuilderHelper helper, String type, JsonObject jsonObject) {

        LS2ParticipantAccountGeneratorStepDescriptor stepDescriptor = helper.getGson().fromJson(jsonObject, LS2ParticipantAccountGeneratorStepDescriptor.class);

        if (!(helper.getStateHelper() instanceof LS2Provider)) {
            return null;
        }

        LS2Provider provider = (LS2Provider)helper.getStateHelper();

        LS2ParticipantAccountGenerationStep step = new LS2ParticipantAccountGenerationStep(
                stepDescriptor.identifier,
                stepDescriptor.title,
                stepDescriptor.text,
                stepDescriptor.buttonText,
                provider.getParticipantAccountGeneratorCredentials(helper.getContext())
        );

        step.setOptional(stepDescriptor.optional);

        return step;
    }

}
