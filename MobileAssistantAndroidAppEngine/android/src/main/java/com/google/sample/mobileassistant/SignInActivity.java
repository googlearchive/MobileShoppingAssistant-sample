/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.mobileassistant;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth
        .GoogleAccountCredential;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity that allows the user to select the account they want to use to sign
 * in. The class also implements integration with Google Play Services and
 * Google Accounts.
 */
public class SignInActivity extends Activity {

    /**
     * Tag used on log messages.
     */
    static final String TAG = SignInActivity.class.getSimpleName();

    /**
     * Name of the key for the shared preferences to access the current
     * * signed in account.
     */
    private static final String ACCOUNT_NAME_SETTING_NAME = "accountName";

    /**
     *  Constant for startActivityForResult flow.
     */
    private static final int REQUEST_ACCOUNT_PICKER = 1;

    /**
     *  Constant for startActivityForResult flow.
     */
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 2;

    /**
     *  Google Account credentials manager.
     */
    private static GoogleAccountCredential credential;

    /**
     *
     * @return the google account credential manager.
     */
    public static GoogleAccountCredential getCredential() {
        return credential;
    }

    /**
     * Called to sign out the user, so user can later on select a different
     * account.
     *
     * @param activity activity that initiated the sign out.
     */
    static void onSignOut(final Activity activity) {
        SharedPreferences settings = activity
                .getSharedPreferences("MobileAssistant", 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ACCOUNT_NAME_SETTING_NAME, "");

        editor.apply();
        credential.setSelectedAccountName("");

        Intent intent = new Intent(activity, SignInActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Initializes the activity content and then navigates to the MainActivity
     * if the user is already signed in or if the app is configured to not
     * require the sign in.
     * Otherwise it initiates starting the UI for the account selection and
     * a check for Google Play Services being up to date.
     */
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

        if (!Constants.SIGN_IN_REQUIRED) {
            // The app won't use authentication, just launch the main activity.
            startMainActivity();
            return;
        }

        if (!checkPlayServices()) {
            // Google Play Services are required, so don't proceed until they
            // are installed.
            return;
        }

        if (isSignedIn()) {
            startMainActivity();
        } else {
            startActivityForResult(credential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        }

    }

    /**
     * Handles the results from activities launched to select an account and to
     * install Google Play Services.
     */
    @Override
    protected final void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    checkPlayServices();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
            default:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras()
                            .getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        onSignedIn(accountName);
                        return;
                    }
                }
                // Signing in is required so display the dialog again
                startActivityForResult(credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
                break;
        }
    }

    /**
     * Retrieves the previously used account name from the application
     * preferences and checks if  the credential object can be set to this
     * account.
     * @return a boolean indicating if the user is signed in or not
     */
    private boolean isSignedIn() {
        credential = GoogleAccountCredential.usingAudience(this,
                Constants.AUDIENCE_ANDROID_CLIENT_ID);
        SharedPreferences settings = getSharedPreferences("MobileAssistant", 0);
        String accountName = settings
                .getString(ACCOUNT_NAME_SETTING_NAME, null);
        credential.setSelectedAccountName(accountName);
        return credential.getSelectedAccount() != null;
    }

    /**
     * Called when the user selected an account. The account name is stored in
     * the application preferences and set in the credential object.
     * @param accountName the account that the user selected.
     */
    private void onSignedIn(final String accountName) {
        SharedPreferences settings = getSharedPreferences("MobileAssistant", 0);

        credential.setSelectedAccountName(accountName);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(ACCOUNT_NAME_SETTING_NAME, accountName);
        editor.apply();

        startMainActivity();
    }

    /**
     * Registers the device with GCM if necessary, and then navigates to the
     * MainActivity.
     */
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected final void onResume() {
        super.onResume();
        if (Constants.SIGN_IN_REQUIRED) {
            // As per GooglePlayServices documentation, an application needs to
            // check from within onResume if Google Play Services is available.
            checkPlayServices();
        }
    }

    /**
     * Checks if Google Play Services are installed and if not it initializes
     * opening the dialog to allow user to install Google Play Services.
     * @return a boolean indicating if the Google Play Services are available.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        MainActivity.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
