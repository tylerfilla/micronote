package com.gmail.tylerfilla.android.notes;

public class Note {
    
    private String content;
    private String title;
    private long lastModified;
    
    private boolean changed;
    private String previousContent;
    private String previousTitle;
    
    public Note(String content, String title) {
        this.content = content;
        this.title = title;
        this.lastModified = 0l;
        
        this.changed = false;
        this.previousContent = content;
        this.previousTitle = title;
    }
    
    public Note() {
        this("", "");
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
        
        if (this.previousContent != null && this.previousContent.equals(content)) {
            this.changed = false;
        }
        
        this.content = content;
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
        
        if (this.previousTitle != null && this.previousTitle.equals(title)) {
            this.changed = false;
        }
        
        this.title = title;
    }
    
    public long getLastModified() {
        return this.lastModified;
    }
    
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    public boolean getChanged() {
        return this.changed;
    }
    
    public void setChanged(boolean changed) {
        if (!changed) {
            this.previousContent = this.content;
            this.previousTitle = this.title;
        }
        
        this.changed = changed;
    }
    
}
