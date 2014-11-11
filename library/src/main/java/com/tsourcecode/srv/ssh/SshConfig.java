package com.tsourcecode.srv.ssh;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SshConfig {
    private boolean allowPassword = true;
    private boolean startOnBoot = true;
    private String password = "42";
    private Context context;

    private final static String PREF_ALLOW_PASSWORD = "allow_password";
    private final static String PREF_PASSWORD = "password";
    private final static String PREF_BOOT = "boot";

    public SshConfig(Context context) {
        this.context = context.getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains(PREF_ALLOW_PASSWORD)){
            allowPassword = prefs.getBoolean(PREF_ALLOW_PASSWORD, true);
        }

        if (prefs.contains(PREF_PASSWORD)){
            password = prefs.getString(PREF_PASSWORD, "42");
        }


        if (prefs.contains(PREF_BOOT)){
            startOnBoot = prefs.getBoolean(PREF_BOOT, true);
        }
    }

    public boolean isAllowPassword() {
        return allowPassword;
    }

    public void setAllowPassword(boolean allowPassword) {
        this.allowPassword = allowPassword;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(PREF_ALLOW_PASSWORD, allowPassword);
        editor.apply();
    }

    public boolean startOnBoot() {
        return startOnBoot;
    }

    public void setStartOnBoot(boolean startOnBoot) {
        this.startOnBoot = startOnBoot;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(PREF_BOOT, startOnBoot);
        editor.apply();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREF_PASSWORD, password);
        editor.apply();
    }
}
