package com.gmail.tylerfilla.android.notes.core;

import java.io.File;

public class NoteMedia {
    
    private Note note;
    private File tempFile;
    
    public NoteMedia() {
        this.note = null;
        this.tempFile = null;
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
    }
    
    public File getTempFile() {
        return this.tempFile;
    }
    
    void setTempFile(File tempFile) {
        this.tempFile = tempFile;
    }
    
}
