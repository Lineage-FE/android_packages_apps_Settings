/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.notification;

import static android.provider.Settings.Secure.NOTIFICATION_BUBBLES;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedSwitchPreference;

import androidx.preference.Preference;

public class BubblePreferenceController extends NotificationPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String TAG = "BubblePrefContr";
    private static final String KEY = "bubble";
    private static final int SYSTEM_WIDE_ON = 1;
    private static final int SYSTEM_WIDE_OFF = 0;

    public BubblePreferenceController(Context context, NotificationBackend backend) {
        super(context, backend);
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public boolean isAvailable() {
        if (!super.isAvailable()) {
            return false;
        }
        if (mAppRow == null && mChannel == null) {
            return false;
        }
        if (Settings.Secure.getInt(mContext.getContentResolver(),
                NOTIFICATION_BUBBLES, SYSTEM_WIDE_ON) == SYSTEM_WIDE_OFF) {
            return false;
        }
        if (mChannel != null) {
            if (isDefaultChannel()) {
                return true;
            } else {
                return mAppRow == null ? false : mAppRow.allowBubbles;
            }
        }
        return true;
    }

    public void updateState(Preference preference) {
        if (mAppRow != null) {
            RestrictedSwitchPreference pref = (RestrictedSwitchPreference) preference;
            pref.setDisabledByAdmin(mAdmin);
            if (mChannel != null) {
                pref.setChecked(mChannel.canBubble());
                pref.setEnabled(isChannelConfigurable() && !pref.isDisabledByAdmin());
            } else {
                pref.setChecked(mAppRow.allowBubbles);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean value = (Boolean) newValue;
        if (mChannel != null) {
            mChannel.setAllowBubbles(value);
            saveChannel();
        } else if (mAppRow != null){
            mAppRow.allowBubbles = value;
            mBackend.setAllowBubbles(mAppRow.pkg, mAppRow.uid, value);
        }
        return true;
    }

}
