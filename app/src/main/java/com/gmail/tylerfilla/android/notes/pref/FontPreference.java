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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FontPreference extends DialogPreference {
    
    private static final String[] FONT_DIRECTORIES = { "/data/fonts", "/system/font", "/system/fonts", };
    
    private FontListAdapter fontListAdapter;
    
    private String currentFontName;
    private String currentFontPath;
    
    private String persistentFontPath;
    
    private boolean cancelled;
    
    public FontPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.fontListAdapter = new FontListAdapter(this.getContext());
    }
    
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        
        // Select first item by default
        int beginningSelection = 0;
        
        // If a current font has been established, get its index
        if (this.currentFontPath != null) {
            beginningSelection = this.fontListAdapter.deviceFontPathList.indexOf(this.currentFontPath);
        }
        
        // Set up font list
        builder.setSingleChoiceItems(this.fontListAdapter, beginningSelection, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set current font name
                FontPreference.this.currentFontName = FontPreference.this.fontListAdapter.deviceFontNameList.get(which);
                
                // Set current font path
                FontPreference.this.currentFontPath = FontPreference.this.fontListAdapter.deviceFontPathList.get(which);
                
                // Clear cancelled flag
                FontPreference.this.cancelled = false;
                
                // Dismiss dialog
                dialog.dismiss();
            }
            
        });
        
        // Set cancel button
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Set cancelled flag
                FontPreference.this.cancelled = true;
            }
            
        });
        
        // Remove ok button
        builder.setPositiveButton(null, null);
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(!this.cancelled);
        
        // Determine whether to update or reset
        if (this.cancelled) {
            this.currentFontPath = this.persistentFontPath;
        } else {
            this.persistentFontPath = this.currentFontPath;
        }
        
        // Persist font path
        this.persistString(this.persistentFontPath);
        
        // Trigger update
        this.callChangeListener(this.persistentFontPath);
        this.notifyChanged();
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // Determine if the default should be used
        if (restorePersistedValue) {
            this.persistentFontPath = this.getPersistedString(this.persistentFontPath);
        } else {
            this.persistentFontPath = "";
        }
        
        // Persist font path
        this.persistString(this.persistentFontPath);
        
        // Set current font path to persistent font name
        this.currentFontPath = this.persistentFontPath;
        
        // Set current font name to that associated with font file
        int currentFontIndex = this.fontListAdapter.deviceFontPathList.indexOf(this.currentFontPath);
        if (currentFontIndex >= 0) {
            this.currentFontName = this.fontListAdapter.deviceFontNameList.get(currentFontIndex);
        }
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
        
        // Get font path from saved state
        this.persistentFontPath = savedState.getSavedFontPath();
        
        // Call through to super
        super.onRestoreInstanceState(savedState.getSuperState());
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        // Create a package for saved state
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        
        // Set font path in saved state
        savedState.setSavedFontPath(this.currentFontPath);
        
        return savedState;
    }
    
    @Override
    public CharSequence getSummary() {
        return String.format(super.getSummary().toString(), this.currentFontName == null ? "Unset" : this.currentFontName);
    }
    
    private static class FontListAdapter extends BaseAdapter {
        
        private Context context;
        
        private TTFReader ttfReader;
        
        private List<String> deviceFontNameList;
        private List<String> deviceFontPathList;
        
        private FontListAdapter(Context context) {
            this.context = context;
            
            this.ttfReader = new TTFReader();
            
            this.deviceFontNameList = new ArrayList<>();
            this.deviceFontPathList = new ArrayList<>();
            
            // Add all enumerated device font paths
            this.deviceFontPathList.addAll(this.enumerateDeviceFontPaths());
            
            // Read fonts and add all names
            for (String fontPath : this.deviceFontPathList) {
                // Try to get name from font file
                try {
                    this.ttfReader.parseForName(new File(fontPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                // Add font name to name list
                if (this.ttfReader.getFontName() != null) {
                    this.deviceFontNameList.add(this.ttfReader.getFontName());
                } else {
                    // Create name from font filename
                    String name = new File(fontPath).getName();
                    
                    // Remove file extension
                    if (name.contains(".")) {
                        name = name.substring(0, name.lastIndexOf("."));
                    }
                    
                    // Add 
                    this.deviceFontNameList.add(name);
                }
            }
        }
        
        @Override
        public int getCount() {
            return this.deviceFontPathList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return this.deviceFontPathList.get(position);
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
                view = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.select_dialog_singlechoice, parent, false);
            }
            
            // Get reference to main text view
            CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
            
            // Set text
            checkedTextView.setText(this.deviceFontNameList.get(position));
            
            // Set font
            if (this.deviceFontPathList.get(position) == null || this.deviceFontPathList.get(position).isEmpty()) {
                checkedTextView.setTypeface(Typeface.DEFAULT);
            } else {
                checkedTextView.setTypeface(Typeface.createFromFile(this.deviceFontPathList.get(position)));
            }
            
            return view;
        }
        
        private List<String> enumerateDeviceFontPaths() {
            List<String> deviceFontPathList = new ArrayList<>();
            
            // Iterate over font directories
            for (String fontDirectoryPath : FONT_DIRECTORIES) {
                File fontDirectory = new File(fontDirectoryPath);
                
                // Make sure font directory exists and is a directory
                if (!fontDirectory.exists() || !fontDirectory.isDirectory()) {
                    continue;
                }
                
                // Iterate over font files in font directory
                for (File fontFile : fontDirectory.listFiles()) {
                    if (fontFile.getName().toLowerCase().endsWith("ttf")) {
                        deviceFontPathList.add(fontFile.getAbsolutePath());
                    }
                }
            }
            
            return deviceFontPathList;
        }
        
        private static class TTFReader {
            
            private String fontName;
            
            private void parseForName(File fontFile) throws IOException {
                // Default font name to null
                this.fontName = null;
                
                // Wrap font file in a RandomAccessFile
                RandomAccessFile fontFileData = new RandomAccessFile(fontFile, "r");
                
                // Check version code
                int versionCode = fontFileData.readInt();
                if (versionCode != 0x74727565 && versionCode != 0x00010000) {
                    return;
                }
                
                // Get number of tables
                int numTables = fontFileData.readShort();
                
                // Skip remaining header
                fontFileData.skipBytes(6);
                
                // Iterate over tables
                for (int i = 0; i < numTables; i++) {
                    // Read tag
                    int tableTag = fontFileData.readInt();
                    
                    // Skip checksum
                    fontFileData.skipBytes(4);
                    
                    // Read offset and length
                    int tableOffset = fontFileData.readInt();
                    int tableLength = fontFileData.readInt();
                    
                    // If table contains name data
                    if (tableTag == 0x6E616D65) {
                        // Get table data
                        byte[] tableData = new byte[tableLength];
                        
                        // Seek by designated offset
                        fontFileData.seek(tableOffset);
                        
                        // Fill table data buffer
                        fontFileData.read(tableData);
                        
                        // Get number of table records
                        int numTableRecords = ((int) tableData[2] << 8) | (int) tableData[3];
                        
                        // Get string offset
                        int stringOffset = ((int) tableData[4] << 8) | (int) tableData[5];
                        
                        // Iterate over table records
                        for (int j = 0; j < numTableRecords; j++) {
                            // Offset of name ID
                            int nameIdOffset = j*12 + 6;
                            
                            // Name ID value
                            int nameIdValue = ((int) tableData[nameIdOffset + 6] << 8) | (int) tableData[nameIdOffset + 7];
                            
                            // Platform ID
                            int platformId = ((int) tableData[nameIdOffset] << 8) | (int) tableData[nameIdOffset + 1];
                            
                            // Name ID 4 in Mac encoding
                            if (nameIdValue == 4 && platformId == 1) {
                                // Name offset and length
                                int nameLength = ((int) tableData[nameIdOffset + 8] << 8) | (int) tableData[nameIdOffset + 9];
                                int nameOffset = ((int) tableData[nameIdOffset + 10] << 8) | (int) tableData[nameIdOffset + 11];
                                
                                // Add string offset to name offset
                                nameOffset += stringOffset;
                                
                                // Get font name
                                if (nameOffset >= 0 && nameOffset + nameLength < tableData.length) {
                                    this.fontName = new String(tableData, nameOffset, nameLength);
                                }
                                
                                return;
                            }
                        }
                    }
                }
            }
            
            private String getFontName() {
                return this.fontName;
            }
            
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
        
        private String savedFontPath;
        
        public SavedState(Parcelable superState) {
            super(superState);
            
            // Default to empty string
            this.savedFontPath = "";
        }
        
        public SavedState(Parcel source) {
            super(source);
            
            // Read font path from source
            this.savedFontPath = source.readString();
        }
        
        public String getSavedFontPath() {
            return this.savedFontPath;
        }
        
        public void setSavedFontPath(String savedFontPath) {
            this.savedFontPath = savedFontPath;
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            
            // Write font path to dest
            dest.writeString(this.savedFontPath);
        }
        
    }
    
}
