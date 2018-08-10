package com.curiosityhealth.ls2sdk.core.rs;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.curiosityhealth.ls2sdk.core.LS2ParticipantAccountGeneratorCredentials;
import com.curiosityhealth.ls2sdk.core.R;
import com.curiosityhealth.ls2sdk.core.core.manager.LS2Manager;
import com.curiosityhealth.ls2sdk.core.core.manager.exception.LS2ManagerHasCredentials;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.layout.StepLayout;

/**
 * Created by jameskizer on 3/29/18.
 */

public class LS2ParticipantAccountGenerationStepLayout extends RelativeLayout implements StepLayout {

    private LS2ParticipantAccountGenerationStep step;
    private StepResult<Boolean> result;
    private StepCallbacks callbacks;
    protected Context context;
    private AppCompatButton mLogInButton;
    private AppCompatTextView mTitle;
    private AppCompatTextView mText;
    private LS2Manager ls2Manager;

    private boolean loggedIn;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @Nullable
    public LS2Manager getLS2Manager() {
        return ls2Manager;
    }

    public void setLS2Manager(LS2Manager ls2Manager) {
        this.ls2Manager = ls2Manager;
    }

    public LS2ParticipantAccountGenerationStepLayout(Context context) {
        super(context);
        this.context = context;
    }

    public LS2ParticipantAccountGenerationStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public LS2ParticipantAccountGenerationStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void initialize(Step step, StepResult result) {
        this.step = (LS2ParticipantAccountGenerationStep)step;
        this.result = result == null ? new StepResult<>(step) : result;
        View layout = LayoutInflater.from(this.getContext()).inflate(R.layout.participant_account_generator_layout, this, true);
        this.mLogInButton = (AppCompatButton)this.findViewById(org.researchsuite.rsuiteextensionscore.R.id.log_in_button);
        if(this.step.getButtonText() != null) {
            this.mLogInButton.setText(this.step.getButtonText());
        }

        this.mLogInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LS2ParticipantAccountGenerationStepLayout.this.logInTapped();
            }
        });
        this.mTitle = (AppCompatTextView)this.findViewById(org.researchsuite.rsuiteextensionscore.R.id.title);
        if(this.step.getTitle() != null) {
            this.mTitle.setText(this.step.getTitle());
            this.mTitle.setVisibility(View.VISIBLE);
        } else {
            this.mTitle.setVisibility(View.GONE);
        }

        this.mText = (AppCompatTextView)this.findViewById(org.researchsuite.rsuiteextensionscore.R.id.text);
        if(this.step.getText() != null) {
            this.mText.setText(this.step.getText());
            this.mText.setVisibility(View.VISIBLE);
        } else {
            this.mText.setVisibility(View.GONE);
        }

        this.setLS2Manager(LS2Manager.getInstance());
    }


    private void logInTapped() {

        final Activity activity = (Activity)this.context;
        final LS2Manager manager = this.getLS2Manager();
        LS2ParticipantAccountGeneratorCredentials credentials = this.step.getParticipantAccountGeneratorCredentials();
        if (manager != null && credentials != null) {

            manager.generateParticipantAccount(credentials, new LS2Manager.Completion() {
                @Override
                public void onCompletion(Exception e) {

                    if (e != null && !(e instanceof LS2ManagerHasCredentials)) {
                        setLoggedIn(false);

                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getContext(), "Unable to create log in credentials. Please contact support.", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });



                    }
                    else {

                        manager.signInWithCredentials(new LS2Manager.Completion() {
                            @Override
                            public void onCompletion(Exception e) {

                                if (e != null) {

                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getContext(), "Invalid log in credentials. Please contact support.", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });

                                }
                                else {
                                    setLoggedIn(true);
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            moveForward();
                                        }
                                    });
                                }

                            }
                        });



                    }

                }
            });
        }
        else {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getContext(), "Invalid configuration. Please contact support.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }

    public Parcelable onSaveInstanceState() {
        this.callbacks.onSaveStep(0, this.step, this.getResult());
        return super.onSaveInstanceState();
    }

    public void moveForward() {
        this.callbacks.onSaveStep(1, this.step, this.getResult());
    }

    public View getLayout() {
        return this;
    }

    public boolean isBackEventConsumed() {
        this.callbacks.onSaveStep(-1, this.step, this.getResult());
        return false;
    }

    public void setCallbacks(StepCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public StepResult getResult() {
        result.setResult(new Boolean(this.loggedIn));
        return result;
    }
}
