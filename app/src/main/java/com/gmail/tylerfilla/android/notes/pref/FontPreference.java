package com.gmail.tylerfilla.android.notes.pref;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class FontPreference extends DialogPreference {
    
    private String currentFontName;
    private String persistentFontName;
    
    public FontPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            this.persistentFontName = this.getPersistedString(this.persistentFontName);
        } else {
            this.persistentFontName = (String) defaultValue;
        }
        
        // Persist font name
        this.persistString(this.persistentFontName);
        
        this.currentFontName = this.persistentFontName;
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // If state cannot be handled here
        if (state == null || !(state instanceof FontPreference.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        
        // Cast to usable saved state package
        FontPreference.SavedState savedState = (FontPreference.SavedState) state;
        
        // Get font name from saved state
        this.persistentFontName = savedState.getSavedFontName();
        
        // Call through to super
        super.onRestoreInstanceState(savedState.getSuperState());
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        // Create a package for saved state
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        
        // Set font name in saved state
        savedState.setSavedFontName(this.currentFontName);
        
        return savedState;
    }
    
    @Override
    public CharSequence getSummary() {
        // Format summary text
        return String.format(super.getSummary().toString(), this.currentFontName);
    }
    
    private static class SavedState extends BaseSavedState {
        
        public static final Creator<FontPreference.SavedState> CREATOR = new Creator<FontPreference.SavedState>() {
            
            public FontPreference.SavedState createFromParcel(Parcel in) {
                return new FontPreference.SavedState(in);
            }
            
            public FontPreference.SavedState[] newArray(int size) {
                return new FontPreference.SavedState[size];
            }
            
        };
        
        private String savedFontName;
        
        public SavedState(Parcelable superState) {
            super(superState);
            
            // Default to empty string
            this.savedFontName = "";
        }
        
        public SavedState(Parcel source) {
            super(source);
            
            // Read font name from source
            this.savedFontName = source.readString();
        }
        
        public String getSavedFontName() {
            return this.savedFontName;
        }
        
        public void setSavedFontName(String savedFontName) {
            this.savedFontName = savedFontName;
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            
            // Write font name to dest
            dest.writeString(this.savedFontName);
        }
        
    }
    
}
