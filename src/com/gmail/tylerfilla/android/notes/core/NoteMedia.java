package com.gmail.tylerfilla.android.notes.core;

public class NoteMedia {
    
    private Type type;
    
    public NoteMedia() {
        this.type = Type.IMAGE;
    }
    
    public Type getType() {
        return this.type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public static enum Type {
        
        IMAGE, AUDIO, VIDEO,
        
    }
    
}
