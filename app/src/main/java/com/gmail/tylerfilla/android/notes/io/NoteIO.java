package com.gmail.tylerfilla.android.notes.io;

import android.content.Context;

import com.gmail.tylerfilla.android.notes.Note;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class NoteIO {
    
    private static String FILE_IDENTIFICATION_COMMENT = "MICRONOTE_DOCUMENT_FILE";
    
    public static File getNoteStoreDirectory(Context context) {
        // Create reference to directory
        File noteStoreDirectory = new File(context.getFilesDir(), "notes");
        
        // Ensure directory exists
        noteStoreDirectory.mkdirs();
        
        return noteStoreDirectory;
    }
    
    public static boolean check(File file) throws IOException {
        // Sanity check
        if (file == null) {
            return false;
        }
        
        // Self explanatory
        boolean isNoteFile = false;
        
        // Check first line of file against ID comment
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        if (line != null) {
            isNoteFile = line.endsWith(FILE_IDENTIFICATION_COMMENT);
        }
        reader.close();
        
        return isNoteFile;
    }
    
    public static Note read(File noteFile) throws IOException {
        // Sanity check
        if (noteFile == null) {
            return null;
        }
        
        // Load note file as Java properties file
        Properties noteProps = new Properties();
        noteProps.load(new FileInputStream(noteFile));
        
        // Note object to contain read data
        Note note = new Note();
        
        // Read data from file
        if (noteProps.containsKey("content")) {
            note.setContent(noteProps.getProperty("content"));
        }
        if (noteProps.containsKey("title")) {
            note.setTitle(noteProps.getProperty("title"));
        }
        note.setLastModified(noteFile.lastModified());
        
        // Clear changed flag
        note.setChanged(false);
        
        return note;
    }
    
    public static void write(Note note, File noteFile) throws IOException {
        // Sanity check
        if (note == null || noteFile == null) {
            return;
        }
        
        // Properties object to hold note data
        Properties noteProps = new Properties();
        
        // Set data as properties
        noteProps.setProperty("content", note.getContent());
        noteProps.setProperty("title", note.getTitle());
        
        // Store note data as Java properties file
        noteProps.store(new FileOutputStream(noteFile), FILE_IDENTIFICATION_COMMENT);
    }
    
}
