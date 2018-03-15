package com.curiosityhealth.ls2sdk.rs;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.Toast;

import com.curiosityhealth.ls2sdk.core.manager.LS2Manager;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchsuite.rsuiteextensionscore.RSLoginStepLayout;

/**
 * Created by jameskizer on 3/13/18.
 */

public class LS2LoginStepLayout extends RSLoginStepLayout {

    public LS2LoginStepLayout(Context context) {
        super(context);
    }

    public LS2LoginStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LS2LoginStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private LS2Manager ls2Manager;

    @Nullable
    public LS2Manager getLS2Manager() {
        return ls2Manager;
    }

    public void setLS2Manager(LS2Manager ls2Manager) {
        this.ls2Manager = ls2Manager;
    }

    @Override
    public void initialize(Step step, StepResult result) {
        super.initialize(step, result);
        this.passwordField.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        this.setLS2Manager(LS2Manager.getInstance());
    }

    @Override
    protected void loginButtonAction(String identity, String password, final ActionCompletion completion) {

        final Activity activity = (Activity)this.context;

        LS2Manager manager = this.getLS2Manager();
        if (manager != null) {
            manager.signIn(identity, password, new LS2Manager.Completion() {
                @Override
                public void onCompletion(Exception e) {
                    if (e == null) {
                        setLoggedIn(true);
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                completion.onCompletion(true);
                            }
                        });

                    }
                    else {
                        setLoggedIn(false);

                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getContext(), "Username / Password are not valid", Toast.LENGTH_SHORT)
                                        .show();

                                completion.onCompletion(false);
                            }
                        });



                    }
                }
            });
        }

//        OhmageOMHManager.getInstance().signIn(identity, password, new OhmageOMHManager.Completion() {
//            @Override
//            public void onCompletion(Exception e) {
//                if (e == null) {
//                    setLoggedIn(true);
//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//                            completion.onCompletion(true);
//                        }
//                    });
//
//                }
//                else {
//                    setLoggedIn(false);
//
//                    activity.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(getContext(), "Username / Password are not valid", Toast.LENGTH_SHORT)
//                                    .show();
//
//                            completion.onCompletion(false);
//                        }
//                    });
//
//
//
//                }
//            }
//        });

    }

    @Override
    protected void forgotPasswordButtonAction(String identity, final ActionCompletion completion) {

        final Activity activity = (Activity)this.context;

        setLoggedIn(false);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                completion.onCompletion(true);
            }
        });

    }
}
