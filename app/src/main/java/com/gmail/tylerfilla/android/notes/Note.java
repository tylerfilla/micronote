package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.util.List;

public class Note {
    
    private static final String TITLE_NOTE_BLANK = "Untitled Note";
    
    private File file;
    
    private int version;
    private String title;
    
    private String content;
    
    private boolean changed;
    
    Note() {
        this.file = null;
        
        this.version = 0;
        this.title = "";
        
        this.content = "";
        
        this.changed = false;
    }
    
    public static Note blank() {
        Note note = new Note();
        note.setVersion(1);
        note.setTitle(TITLE_NOTE_BLANK);
        note.clearChanged();
        
        return note;
    }
    
    public File getFile() {
        return this.file;
    }
    
    public void setFile(File file) {
        if (this.file != null) {
            this.changed = this.changed || !this.file.equals(file);
        } else if (file != null) {
            this.changed = true;
        }
        
        this.file = file;
    }
    
    public int getVersion() {
        return this.version;
    }
    
    public void setVersion(int version) {
        if (this.version != 0) {
            this.changed = this.changed || this.version != version;
        } else if (version != 0) {
            this.changed = true;
        }
        
        this.version = version;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        if (this.title != null) {
            this.changed = this.changed || !this.title.equals(title);
        } else if (title != null) {
            this.changed = true;
        }
        
        this.title = title;
    }
    
    public String getContent() {
        return this.content;
    }
    
    public void setContent(String content) {
        if (this.content != null) {
            this.changed = this.changed || !this.content.equals(content);
        } else if (content != null) {
            this.changed = true;
        }
        
        this.content = content;
    }
    
    public boolean getChanged() {
        return this.changed;
    }
    
    public void clearChanged() {
        this.changed = false;
    }
    
}
