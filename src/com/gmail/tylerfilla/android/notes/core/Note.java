package com.gmail.tylerfilla.android.notes.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Note {
    
    private String title;
    private String author;
    
    private Date dateCreated;
    private Date dateModified;
    
    private String content;
    private final ArrayList<NoteMedia> media;
    
    public Note() {
        this.title = null;
        this.author = null;
        
        this.dateCreated = null;
        this.dateModified = null;
        
        this.content = null;
        this.media = new ArrayList<NoteMedia>();
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public Date getDateCreated() {
        return this.dateCreated;
    }
    
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public Date getDateModified() {
        return this.dateModified;
    }
    
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }
    
    public String getContent() {
        return this.content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<NoteMedia> getMedia() {
        return this.media;
    }
    
    public void addMedia(NoteMedia media) {
        this.media.add(media);
    }
    
    public boolean removeMedia(NoteMedia media) {
        return this.media.remove(media);
    }
    
}
