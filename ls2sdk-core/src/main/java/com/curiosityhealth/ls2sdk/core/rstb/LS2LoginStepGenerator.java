package com.curiosityhealth.ls2sdk.core.rstb;

import com.curiosityhealth.ls2sdk.core.rs.LS2LoginStep;
import com.google.gson.JsonObject;

import org.researchstack.backbone.step.Step;
import org.researchsuite.rstb.DefaultStepGenerators.RSTBBaseStepGenerator;
import org.researchsuite.rstb.DefaultStepGenerators.descriptors.RSTBStepDescriptor;
import org.researchsuite.rstb.RSTBTaskBuilderHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by jameskizer on 3/14/18.
 */

public class LS2LoginStepGenerator extends RSTBBaseStepGenerator {

    public LS2LoginStepGenerator()
    {
        super();
        this.supportedTypes = Arrays.asList(
                "LS2Login"
        );
    }

    @Override
    public List<Step> generateSteps(RSTBTaskBuilderHelper helper, String type, JsonObject jsonObject, String identifierPrefix) {

        RSTBStepDescriptor stepDescriptor = helper.getGson().fromJson(jsonObject, RSTBStepDescriptor.class);

        String identifier = this.combineIdentifiers(stepDescriptor.identifier, identifierPrefix);
        LS2LoginStep step = new LS2LoginStep(
                identifier,
                stepDescriptor.title,
                stepDescriptor.text,
                null);
        step.setOptional(stepDescriptor.optional);

        return Collections.singletonList((Step) step);
    }

}
