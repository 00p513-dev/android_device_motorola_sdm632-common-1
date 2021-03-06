package com.moto.actions;

import java.lang.reflect.Method;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.os.Bundle;

import android.app.ActionBar;
import com.moto.actions.util.SeekBarPreference;
import com.moto.actions.R;

public class MiscPanel extends PreferenceActivity implements
	OnPreferenceChangeListener {

    public static final String KEY_LED_MAX = "misc_led_brightness";
    public static final String KEY_BREATH_TOGGLE = "switch_breath_toggle";

    private SeekBarPreference mLedMax;
    private SwitchPreference mBreathToggle;
    private SharedPreferences mPrefs;

    private String mLEDMaxBrightness;
    private boolean mBreathToggleEnabled;

    public static final String LED_MAXBRIGHTNESS_FILE = "/sys/class/leds/charging/max_brightness";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.misc_panel);

        addPreferencesFromResource(R.xml.misc_panel);

        mLedMax = (SeekBarPreference) findPreference(KEY_LED_MAX);
        mLedMax.setInitValue(mPrefs.getInt(KEY_LED_MAX, mLedMax.def));
	mLedMax.setOnPreferenceChangeListener(this);

        mBreathToggle = (SwitchPreference) findPreference(KEY_BREATH_TOGGLE);
        mBreathToggle.setChecked(mPrefs.getBoolean(MiscPanel.KEY_BREATH_TOGGLE, false));
        mBreathToggle.setOnPreferenceChangeListener(this);

	mLEDMaxBrightness = String.valueOf(mPrefs.getInt(KEY_LED_MAX, mLedMax.def));

    }

    private boolean isSupported(String file) {
        return UtilsKCAL.fileWritable(file);
    }

    public static void restore(Context context) {
       int storedMax = PreferenceManager.getDefaultSharedPreferences(context).getInt(MiscPanel.KEY_LED_MAX, 255);

       UtilsKCAL.writeValue(LED_MAXBRIGHTNESS_FILE, String.valueOf(storedMax));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if (preference == mLedMax) {
            float val = Float.parseFloat((String) newValue);
            mPrefs.edit().putInt(KEY_LED_MAX, (int) val).commit();
            UtilsKCAL.writeValue(LED_MAXBRIGHTNESS_FILE, String.valueOf((int) val));
	    return true;
        } else if (preference == mBreathToggle) {
            Boolean enabled = (Boolean) newValue;
            mPrefs.edit().putBoolean(KEY_BREATH_TOGGLE, enabled).commit();
            try {
                 @SuppressWarnings("rawtypes")
                 Class SystemProperties = Class.forName("android.os.SystemProperties");

                 Method set1 = SystemProperties.getMethod("set", new Class[] {String.class, String.class});
                 if (enabled == false) {
                      set1.invoke(SystemProperties, new Object[] {"vendor.light.breath-toggle", "on"});
                 } else {
                      set1.invoke(SystemProperties, new Object[] {"vendor.light.breath-toggle", "off"});
                 }
            } catch( IllegalArgumentException iAE ){
                 throw iAE;
            } catch( Exception e ){
                 e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
