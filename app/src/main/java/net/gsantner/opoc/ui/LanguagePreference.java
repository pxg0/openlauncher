/*
 * ------------------------------------------------------------------------------
 * Gregor Santner <gsantner.net> wrote this. You can do whatever you want
 * with it. If we meet some day, and you think it is worth it, you can buy me a
 * coke in return. Provided as is without any kind of warranty. Do not blame or
 * sue me if something goes wrong. No attribution required.    - Gregor Santner
 *
 * License: Creative Commons Zero (CC0 1.0)
 *  http://creativecommons.org/publicdomain/zero/1.0/
 * ----------------------------------------------------------------------------
 */

/*
 * A ListPreference that displays a list of available languages
 * Requires:
 *     The BuildConfig field "APPLICATION_LANGUAGES" which is a array of all available languages
 *     opoc/ContextUtils
 * BuildConfig field can be defined by using the method below

buildConfigField("String[]", "APPLICATION_LANGUAGES", '{' + getUsedAndroidLanguages().collect {"\"${it}\""}.join(",")  + '}')

@SuppressWarnings(["UnnecessaryQualifiedReference", "SpellCheckingInspection", "GroovyUnusedDeclaration"])
static String[] getUsedAndroidLanguages() {
    Set<String> langs = new HashSet<>()
    new File('.').eachFileRecurse(groovy.io.FileType.DIRECTORIES) {
        final foldername = it.name
        if (foldername.startsWith('values-') && !it.canonicalPath.contains("build" + File.separator + "intermediates")) {
            new File(it.toString()).eachFileRecurse(groovy.io.FileType.FILES) {
                if (it.name.toLowerCase().endsWith(".xml") && it.getCanonicalFile().getText('UTF-8').contains("<string")) {
                    langs.add(foldername.replace("values-", ""))
                }
            }
        }
    }
    return langs.toArray(new String[langs.size()])
}

 * Summary: Change language of this app. Restart app for changes to take effect

 * Define element in Preferences-XML:
    <net.gsantner.opoc.ui.LanguagePreference
        android:icon="@drawable/ic_language_black_24dp"
        android:key="@string/pref_key__language"
        android:summary="@string/pref_desc__language"
        android:title="@string/pref_title__language"/>
 */
package net.gsantner.opoc.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

import net.gsantner.opoc.util.ContextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A {@link android.preference.ListPreference} that displays a list of languages to select from
 */
@SuppressWarnings({"unused", "SpellCheckingInspection", "WeakerAccess"})
public class LanguagePreference extends ListPreference {
    private static final String SYSTEM_LANGUAGE_CODE = "";

    // The language of res/values/ -> (usually English)
    public String _systemLanguageName = "★System★";
    public String _defaultLanguageCode = "en";

    public LanguagePreference(Context context) {
        super(context);
        loadLangs(context, null);
    }

    public LanguagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadLangs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LanguagePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadLangs(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LanguagePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        loadLangs(context, attrs);
    }

    @Override
    public boolean callChangeListener(Object newValue) {
        if (newValue instanceof String) {
            // Does not apply to existing UI, use recreate()
            new ContextUtils(getContext()).setAppLanguage((String) newValue);
        }
        return super.callChangeListener(newValue);
    }


    private void loadLangs(Context context) {
        loadLangs(context, null);
    }

    private void loadLangs(Context context, @Nullable AttributeSet attrs) {
        setDefaultValue(SYSTEM_LANGUAGE_CODE);

        // Fetch readable details
        ContextUtils contextUtils = new ContextUtils(context);
        List<String> languages = new ArrayList<>();
        Object bcof = contextUtils.getBuildConfigValue("APPLICATION_LANGUAGES");
        if (bcof instanceof String[]) {
            for (String langId : (String[]) bcof) {
                Locale locale = contextUtils.getLocaleByAndroidCode(langId);
                languages.add(summarizeLocale(locale) + ";" + langId);
            }
        }

        // Sort languages naturally
        Collections.sort(languages);

        // Show in UI
        String[] entries = new String[languages.size() + 2];
        String[] entryval = new String[languages.size() + 2];
        for (int i = 0; i < languages.size(); i++) {
            entries[i + 2] = languages.get(i).split(";")[0];
            entryval[i + 2] = languages.get(i).split(";")[1];
        }
        entryval[0] = SYSTEM_LANGUAGE_CODE;
        entries[0] = _systemLanguageName + "\n[" + summarizeLocale(context.getResources().getConfiguration().locale) + "]";
        entryval[1] = _defaultLanguageCode;
        entries[1] = summarizeLocale(contextUtils.getLocaleByAndroidCode(_defaultLanguageCode));

        setEntries(entries);
        setEntryValues(entryval);
    }

    // Concat english and localized language name
    // Append country if country specific (e.g. Portuguese Brazil)
    private String summarizeLocale(Locale locale) {
        String country = locale.getDisplayCountry(locale);
        String language = locale.getDisplayLanguage(locale);
        return locale.getDisplayLanguage(Locale.ENGLISH)
                + " (" + language.substring(0, 1).toUpperCase(Locale.getDefault()) + language.substring(1)
                + ((!country.isEmpty() && !country.toLowerCase(Locale.getDefault()).equals(language.toLowerCase(Locale.getDefault()))) ? (", " + country) : "")
                + ")";
    }

    // Add current language to summary
    @Override
    public CharSequence getSummary() {
        Locale locale = new ContextUtils(getContext()).getLocaleByAndroidCode(getValue());
        return super.getSummary() + "\n\n" + summarizeLocale(locale);
    }

    public String getSystemLanguageName() {
        return _systemLanguageName;
    }

    public void setSystemLanguageName(String systemLanguageName) {
        _systemLanguageName = systemLanguageName;
        loadLangs(getContext());
    }

    public String getDefaultLanguageCode() {
        return _defaultLanguageCode;
    }

    public void setDefaultLanguageCode(String defaultLanguageCode) {
        _defaultLanguageCode = defaultLanguageCode;
        loadLangs(getContext());
    }
}
