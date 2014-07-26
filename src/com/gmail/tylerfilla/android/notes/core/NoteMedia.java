package com.gmail.tylerfilla.android.notes.core;

import java.io.File;

public class NoteMedia {
    
    private Note note;
    private File file;
    
    public NoteMedia() {
        this.note = null;
        this.file = null;
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
    }
    
    public File getFile() {
        return this.file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
}
