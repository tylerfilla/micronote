package com.gmail.tylerfilla.android.notes.core;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

public class NoteKeeper {
    
    private static NoteKeeper instance = null;
    
    private final Context context;
    
    private File dirNotes;
    private File dirTemp;
    
    private NoteKeeper(Context context) {
        this.context = context;
        
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            this.dirNotes = new File(Environment.getExternalStorageDirectory(), "Notes");
            this.dirTemp = new File(context.getExternalCacheDir(), "Temp");
        } else {
            this.dirNotes = new File(context.getFilesDir(), "Notes");
            this.dirTemp = new File(context.getFilesDir(), "Temp");
        }
        
        this.dirNotes.mkdirs();
        this.dirTemp.mkdirs();
    }
    
    public static NoteKeeper getInstance(Context context) {
        if (instance == null) {
            instance = new NoteKeeper(context);
        }
        
        return instance;
    }
    
    public File[] listNoteFiles() {
        return null;
    }
    
    public Note readNote(File noteFile) throws IOException {
        return null;
    }
    
    public void writeNote(Note note) throws IOException {
    }
    
    public void clearTempFiles() {
        for (File tempFile : this.dirTemp.listFiles()) {
            tempFile.delete();
        }
    }
    
}
