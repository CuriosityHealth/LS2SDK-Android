package com.curiosityhealth.ls2sdk.core.rs

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.curiosityhealth.ls2sdk.core.LS2ParticipantAccountGeneratorCredentials
import com.curiosityhealth.ls2sdk.core.core.manager.LS2Manager
import com.curiosityhealth.ls2sdk.core.core.manager.exception.LS2ManagerHasCredentials
import org.researchsuite.rsuiteextensionscore.RSLoginStepLayout

class LS2ParticipantAccountGeneratorRequestingCredentialsStepLayout(context: Context): RSLoginStepLayout(context) {

    var manager: LS2Manager

    init {
        this.manager = LS2Manager.getInstance()!!
    }

    override fun loginButtonAction(identity: String?, password: String?, completion: ActionCompletion?) {

        val activity = this.context as Activity

        val triple = (this.step as? LS2ParticipantAccountGeneratorRequestingCredentialsStep)?.let { step ->
            (step.generatorID ?: identity)?.let { generatorID ->
                password?.let { Triple(step, generatorID, it) }
            }
        }

        if (triple == null) {

            setLoggedIn(false)

            val message = "Invalid configuration. Please contact support."
            activity.runOnUiThread(Runnable {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT)
                        .show()
                completion?.onCompletion(false)
            })

        }
        else {
            val step = triple.first
            val generatorID = triple.second
            val generatorPassword = triple.third

            val credentials = LS2ParticipantAccountGeneratorCredentials(generatorID, generatorPassword)

            this.isLoading = true
            this.manager.generateParticipantAccount(credentials) { e: Exception? ->

                if (e != null && e !is LS2ManagerHasCredentials) run {
                    setLoggedIn(false)
                    this.isLoading = false

                    activity.runOnUiThread {
                        Toast.makeText(getContext(), "Unable to create log in credentials. Please contact support.", Toast.LENGTH_SHORT)
                                .show()

                        completion?.onCompletion(false)
                    }



                }
                else {

                    this.manager.signInWithCredentials { innerE: Exception? ->

                        this.isLoading = false
                        if (innerE != null) {
                            setLoggedIn(false)
                            activity.runOnUiThread {
                                Toast.makeText(getContext(), "Invalid log in credentials. Please contact support.", Toast.LENGTH_SHORT)
                                        .show()

                                completion?.onCompletion(false)
                            }


                        } else {
                            setLoggedIn(true)
                            activity.runOnUiThread {
                                completion?.onCompletion(true)
                            }
                        }
                    }

                }


            }





        }



    }
}