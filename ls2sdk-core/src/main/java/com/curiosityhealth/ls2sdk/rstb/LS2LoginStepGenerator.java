package com.curiosityhealth.ls2sdk.rstb;

import com.curiosityhealth.ls2sdk.rs.LS2LoginStep;
import com.google.gson.JsonObject;

import org.researchstack.backbone.step.Step;
import org.researchsuite.rstb.DefaultStepGenerators.RSTBBaseStepGenerator;
import org.researchsuite.rstb.DefaultStepGenerators.descriptors.RSTBStepDescriptor;
import org.researchsuite.rstb.RSTBTaskBuilderHelper;

import java.util.Arrays;

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
    public Step generateStep(RSTBTaskBuilderHelper helper, String type, JsonObject jsonObject) {

        RSTBStepDescriptor stepDescriptor = helper.getGson().fromJson(jsonObject, RSTBStepDescriptor.class);

        LS2LoginStep step = new LS2LoginStep(
                stepDescriptor.identifier,
                stepDescriptor.title,
                stepDescriptor.text,
                null);
        step.setOptional(stepDescriptor.optional);

        return step;
    }

}
