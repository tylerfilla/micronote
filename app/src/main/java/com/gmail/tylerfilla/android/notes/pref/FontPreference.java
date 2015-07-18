package com.gmail.tylerfilla.android.notes.pref;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.Arrays;
import java.util.List;

public class FontPreference extends DialogPreference {
    
    private String currentFontName;
    private String persistentFontName;
    
    public FontPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        
        // Create font list adapter
        final FontListAdapter fontListAdapter = new FontListAdapter();
        
        // Select first item by default
        int beginningSelection = 0;
        
        // If a current font has been established, get its index
        if (this.currentFontName != null) {
            for (int i = 0; i < fontListAdapter.getDeviceFontNames().size(); i++) {
                if (this.currentFontName.equals(fontListAdapter.getDeviceFontNames().get(i))) {
                    beginningSelection = i;
                    break;
                }
            }
        }
        
        // Set up font list
        builder.setSingleChoiceItems(new FontListAdapter(), beginningSelection, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FontPreference.this.currentFontName = fontListAdapter.getDeviceFontNames().get(which);
            }
            
        });
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // Determine if the default should be used
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
    
    private class FontListAdapter extends BaseAdapter {
        
        private List<String> deviceFontNames;
        
        private FontListAdapter() {
            this.deviceFontNames = this.enumerateDeviceFontNames();
        }
        
        @Override
        public int getCount() {
            return this.deviceFontNames.size();
        }
        
        @Override
        public Object getItem(int position) {
            return this.deviceFontNames.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return (long) position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Try to reuse convertView
            View view = convertView;
            
            // If view needs to be created
            if (view == null) {
                // Inflate standard single-choice list layout
                view = ((LayoutInflater) FontPreference.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.select_dialog_singlechoice, parent, false);
            }
            
            // Get reference to main text view
            CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
            
            // Get font associated with this list item
            String fontName = this.deviceFontNames.get(position);
            
            // Set text to font name
            checkedTextView.setText(fontName);
            
            // Set font
            checkedTextView.setTypeface(Typeface.create(fontName, Typeface.NORMAL));
            
            return view;
        }
        
        public List<String> getDeviceFontNames() {
            return this.deviceFontNames;
        }
        
        private List<String> enumerateDeviceFontNames() {
            // TODO: Read fonts on device
            return Arrays.asList("arial", "courier new", "times new roman");
        }
        
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
