package com.gmail.tylerfilla.android.notes;

public class Note {
    
    private static final String TITLE_NOTE_BLANK = "Untitled Note";
    
    private String title;
    private String content;
    
    private boolean changed;
    
    public Note(String title, String content) {
        this.title   = title;
        this.content = content;
        
        this.changed = false;
    }
    
    public Note() {
        this.title   = Note.TITLE_NOTE_BLANK;
        this.content = "";
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
    
    public void setChanged(boolean changed) {
        this.changed = changed;
    }
    
}
