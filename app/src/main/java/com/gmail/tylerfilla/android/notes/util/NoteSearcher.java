package com.gmail.tylerfilla.android.notes.util;

import com.gmail.tylerfilla.android.notes.Note;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteSearcher {
    
    private List<File> fileList;
    private NoteSearcher.NoteSearchHandler noteSearchHandler;
    
    public NoteSearcher() {
        this.fileList = null;
        this.noteSearchHandler = null;
    }
    
    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }
    
    public void setNoteSearchHandler(NoteSearcher.NoteSearchHandler noteSearchHandler) {
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
        
        Map<File, Boolean> resultMap = new HashMap<>();
        
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
            
            // Match all with empty queries
            match = match || query.isEmpty();
            
            // Save result
            resultMap.put(noteFile, match);
        }
        
        // Report results
        for (File noteFile : resultMap.keySet()) {
            this.noteSearchHandler.result(noteFile, resultMap.get(noteFile));
        }
    }
    
    public static interface NoteSearchHandler {
        
        public Note request(File noteFile);
        
        public void result(File noteFile, boolean match);
        
    }
    
}
