package com.gmail.tylerfilla.android.notes.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Note {
    
    private File file;
    
    private int version;
    
    private String title;
    private String author;
    
    private Date dateCreated;
    private Date dateModified;
    
    private String content;
    private final ArrayList<NoteMedia> media;
    
    private boolean changed;
    
    public Note() {
        this.file = null;
        
        this.version = 0;
        
        this.title = null;
        this.author = null;
        
        this.dateCreated = null;
        this.dateModified = null;
        
        this.content = null;
        this.media = new ArrayList<NoteMedia>();
        
        this.changed = false;
    }
    
    public File getFile() {
        return this.file;
    }
    
    public void setFile(File file) {
        if (this.file != null) {
            this.changed = !this.file.equals(file);
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
            this.changed = this.version != version;
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
            this.changed = !this.title.equals(title);
        } else if (title != null) {
            this.changed = true;
        }
        
        this.title = title;
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public void setAuthor(String author) {
        if (this.author != null) {
            this.changed = !this.author.equals(author);
        } else if (author != null) {
            this.changed = true;
        }
        
        this.author = author;
    }
    
    public Date getDateCreated() {
        return this.dateCreated;
    }
    
    void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public Date getDateModified() {
        return this.dateModified;
    }
    
    void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }
    
    public String getContent() {
        return this.content;
    }
    
    public void setContent(String content) {
        if (this.content != null) {
            this.changed = !this.content.equals(content);
        } else if (content != null) {
            this.changed = true;
        }
        
        this.content = content;
    }
    
    public List<NoteMedia> getMedia() {
        return this.media;
    }
    
    public void addMedia(NoteMedia media) {
        this.changed = true;
        this.media.add(media);
    }
    
    public boolean removeMedia(NoteMedia media) {
        this.changed = true;
        return this.media.remove(media);
    }
    
    public boolean getChanged() {
        return this.changed;
    }
    
    public void clearChanged() {
        this.changed = false;
    }
    
}
