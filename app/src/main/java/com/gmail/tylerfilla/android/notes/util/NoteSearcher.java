package com.gmail.tylerfilla.android.notes.util;

import com.gmail.tylerfilla.android.notes.Note;

import java.io.File;
import java.util.List;

public class NoteSearcher {
    
    private List<File> fileList;
    private NoteSearchHandler noteSearchHandler;
    
    public NoteSearcher() {
        this.fileList = null;
        this.noteSearchHandler = null;
    }
    
    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }
    
    public void setNoteSearchHandler(NoteSearchHandler noteSearchHandler) {
        this.noteSearchHandler = noteSearchHandler;
    }
    
    public void search(String query) {
        // Some sanity checks
        if (this.fileList == null) {
            throw new IllegalStateException("No file list set");
        }
        if (this.noteSearchHandler == null) {
            throw new IllegalStateException("No request handler set");
        }
        if (query.isEmpty()) {
            return;
        }
        
        // Loop through files
        for (File noteFile : this.fileList) {
            boolean match = false;
            
            // Request the note
            Note note = this.noteSearchHandler.request(noteFile);
    
            // Search for query in title and content
            if (note.getTitle().toLowerCase().contains(query.toLowerCase())) {
                match = true;
            }
            if (note.getContent().toLowerCase().contains(query.toLowerCase())) {
                match = true;
            }
            
            // Report result
            this.noteSearchHandler.result(noteFile, match);
        }
    }
    
    public static interface NoteSearchHandler {
        
        public Note request(File noteFile);
        
        public void result(File noteFile, boolean match);
        
    }
    
}
