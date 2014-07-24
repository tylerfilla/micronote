package com.gmail.tylerfilla.android.notes.core;


public class NoteMedia {
    
    private Note note;
    private String type;
    private String name;
    
    public NoteMedia() {
        this.note = null;
        this.type = null;
        this.name = null;
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
    }
    
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
}
