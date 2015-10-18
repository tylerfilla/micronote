package io.microdev.note.core;

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
            // Request the note
            Note note = this.noteSearchHandler.request(noteFile);
            
            // Match query within title and content
            resultMap.put(noteFile, this.match(note.getTitle() + " " + note.getContent(), query));
        }
        
        // Report results
        for (File noteFile : resultMap.keySet()) {
            this.noteSearchHandler.result(noteFile, resultMap.get(noteFile));
        }
    }
    
    private boolean match(String content, String query) {
        // Match all with empty queries
        if (query.isEmpty()) {
            return true;
        }
        
        // Match flag
        boolean match = true;
        
        // Iterate over words in query
        for (String queryWord : query.split("\\s")) {
            // Test for word
            match = match && content.toLowerCase().contains(queryWord.toLowerCase());
        }
        
        return match;
    }
    
    public interface NoteSearchHandler {
        
        Note request(File noteFile);
        
        void result(File noteFile, boolean match);
        
    }
    
}
