package com.curiosityhealth.ls2sdk.core.rstb;

import com.curiosityhealth.ls2sdk.core.LS2ParticipantAccountGeneratorCredentials;
import com.curiosityhealth.ls2sdk.core.core.manager.LS2Provider;
import com.curiosityhealth.ls2sdk.core.rs.LS2LoginStep;
import com.curiosityhealth.ls2sdk.core.rs.LS2ParticipantAccountGenerationStep;
import com.google.gson.JsonObject;

import org.researchstack.backbone.step.Step;
import org.researchsuite.rstb.DefaultStepGenerators.RSTBBaseStepGenerator;
import org.researchsuite.rstb.DefaultStepGenerators.descriptors.RSTBStepDescriptor;
import org.researchsuite.rstb.RSTBTaskBuilderHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public List<Step> generateSteps(RSTBTaskBuilderHelper helper, String type, JsonObject jsonObject, String identifierPrefix) {

        LS2ParticipantAccountGeneratorStepDescriptor stepDescriptor = helper.getGson().fromJson(jsonObject, LS2ParticipantAccountGeneratorStepDescriptor.class);

        LS2ParticipantAccountGeneratorCredentials credentials = (LS2ParticipantAccountGeneratorCredentials)helper.getStateHelper().get(helper.getContext(), "ls2ParticipantAccountCredentials");

        if (credentials == null){
            return null;
        }

//        if (!(helper.getStateHelper() instanceof LS2Provider)) {
//            return null;
//        }
//
//        LS2Provider provider = (LS2Provider)helper.getStateHelper();

        String identifier = this.combineIdentifiers(stepDescriptor.identifier, identifierPrefix);
        LS2ParticipantAccountGenerationStep step = new LS2ParticipantAccountGenerationStep(
                identifier,
                stepDescriptor.title,
                stepDescriptor.text,
                stepDescriptor.buttonText,
                credentials
        );

        step.setOptional(stepDescriptor.optional);

        return Collections.singletonList((Step) step);
    }

}
