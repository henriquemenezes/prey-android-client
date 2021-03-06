/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.prey.PreyConfig;
import com.prey.PreyEmail;
import com.prey.PreyLogger;
import com.prey.PreyPermission;
import com.prey.PreyStatus;
import com.prey.backwardcompatibility.FroyoSupport;
import com.prey.R;
import com.prey.preferences.ChangePinPreferences;
import com.prey.services.PreyDisablePowerOptionsService;

public class PreyConfigurationActivity extends PreferenceActivity {

    public void onBackPressed(){
        Intent intent = null;

        intent = new Intent(getApplication(), CheckPasswordHtmlActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());
        preyConfig.setAccountVerified();
        addPreferencesFromResource(R.xml.preferences);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            try {
                startActivity(intent);
            } catch (Exception e) {
            }
            finish();

        }

        PreyConfig preyConfig = PreyConfig.getPreyConfig(getApplicationContext());

        CheckBoxPreference pDisablePower=(CheckBoxPreference)findPreference("PREFS_DISABLE_POWER");
        try {
            if (preyConfig.isMarshmallowOrAbove()) {
                pDisablePower.setSummary(R.string.preferences_disable_power_options_summary);
            } else {
                pDisablePower.setSummary(R.string.preferences_disable_power_options_summary_old);
            }

            PreyLogger.d("SDK_INT:"+android.os.Build.VERSION.SDK_INT);

            if(android.os.Build.VERSION.SDK_INT >= PreyConfig.VERSION_CODES_P){

                pDisablePower.setEnabled(true);
                pDisablePower.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {



                        AlertDialog alertDialog = new AlertDialog.Builder(PreyConfigurationActivity.this).create();
                        alertDialog.setTitle(R.string.preferences_disable_power_alert_android9_title);
                        alertDialog.setMessage(getString(R.string.preferences_disable_power_alert_android9_message));


                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        alertDialog.show();


                        return false;
                    }
                });
                pDisablePower.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        CheckBoxPreference pDisablePower=(CheckBoxPreference)preference;
                        pDisablePower.setChecked(false);
                        return false;
                    }
                });
                pDisablePower.setChecked(false);
                stopService(new Intent(this, PreyDisablePowerOptionsService.class));
            }
        }catch(Exception e){
        }

        Preference p = findPreference("PREFS_ADMIN_DEVICE");
        try {
            if (preyConfig.isFroyoOrAbove()) {

                if (FroyoSupport.getInstance(getApplicationContext()).isAdminActive()) {
                    p.setTitle(R.string.preferences_admin_enabled_title);
                    p.setSummary(R.string.preferences_admin_enabled_summary);
                } else {
                    p.setTitle(R.string.preferences_admin_disabled_title);
                    p.setSummary(R.string.preferences_admin_disabled_summary);
                }
            } else
                p.setEnabled(false);
        }catch(Exception e){
        }

        boolean checkBiometricSupport=  PreyPermission.checkBiometricSupport(this);
        if(!checkBiometricSupport){
            try {
                CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference("PREFS_BIOMETRIC");
                PreferenceCategory mCategory = (PreferenceCategory) findPreference("PREFS_CAT_PREFS2");
                mCategory.removePreference(mCheckBoxPref);
            } catch (Exception e) {
            }
        }

        p = findPreference("PREFS_ABOUT");
        p.setSummary("Version " + preyConfig.getPreyVersion() + " - Prey Inc.");

        Preference pGo = findPreference("PREFS_GOTO_WEB_CONTROL_PANEL");
        pGo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {

                String url = PreyConfig.getPreyConfig(getApplicationContext()).getPreyPanelUrl();
                PreyLogger.d("url control:" + url);
                Intent internetIntent = new Intent(Intent.ACTION_VIEW);
                internetIntent.setData(Uri.parse(url));
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                }
                return false;
            }
        });


        /*
        Preference pSMS= findPreference("PREFS_SMS");
        pSMS.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(getApplicationContext(), PreyConfigurationSMSActivity.class);
                startActivity(intent);

                return false;
            }
        });*/

        CheckBoxPreference pBlockAppUninstall=(CheckBoxPreference)findPreference(PreyConfig.PREFS_BLOCK_APP_UNINSTALL);

        try {
            if ("".equals(preyConfig.getPinNumber())) {
                //pSMS.setEnabled(false);
                pDisablePower.setEnabled(false);
                PreyConfig.getPreyConfig(getApplicationContext()).setDisablePowerOptions(false);
                pDisablePower.setChecked(false);
                pBlockAppUninstall.setEnabled(false);
                PreyConfig.getPreyConfig(getApplicationContext()).setBlockAppUninstall(false);
                pBlockAppUninstall.setChecked(false);
            }else{
                //pSMS.setEnabled(true);
                pDisablePower.setEnabled(true);
                pBlockAppUninstall.setEnabled(true);
            }
        } catch (Exception e) {
        }
        PreyStatus.getInstance().setPreyConfigurationActivityResume(false);



        ChangePinPreferences changePin=(ChangePinPreferences)findPreference("PREFS_CHANGE_PIN");
        String pin=PreyConfig.getPreyConfig(this).getPinNumber();
        if("".equals(pin)) {
            changePin.setPositiveButtonText(R.string.preference_pin_save);
        }else{
            changePin.setPositiveButtonText(R.string.preference_pin_remove);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


}

