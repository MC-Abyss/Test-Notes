package com.example.testnotes.classes;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class ContextUtils extends ContextWrapper {
    public ContextUtils(Context base) {
        super(base);
    }

    public static ContextWrapper updateLocale(Context context, Locale localeToSwitchTo) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = localeToSwitchTo;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return new ContextUtils(context);
    }
}
