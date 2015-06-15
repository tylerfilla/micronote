package com.gmail.tylerfilla.android.notes.io;

import com.gmail.tylerfilla.android.notes.Note;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class NoteIO {
    
    private static String fileIdentificationComment = "MICRONOTE_DOCUMENT_FILE";
    
    public static boolean check(File file) throws IOException {
        if (file == null) {
            return false;
        }
        
        boolean isNoteFile = false;
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        if (line != null) {
            isNoteFile = line.endsWith(NoteIO.fileIdentificationComment);
        }
        reader.close();
        
        return isNoteFile;
    }
    
    public static Note read(File noteFile) throws IOException {
        if (noteFile == null) {
            return null;
        }
        
        Note note = new Note();
        Properties noteProps = new Properties();
        
        noteProps.load(new FileInputStream(noteFile));
        
        if (noteProps.containsKey("content")) {
            note.setContent(noteProps.getProperty("content"));
        }
        if (noteProps.containsKey("title")) {
            note.setTitle(noteProps.getProperty("title"));
        }
        note.setLastModified(noteFile.lastModified());
        
        note.setChanged(false);
        
        return note;
    }
    
    public static void write(Note note, File noteFile) throws IOException {
        if (note == null || noteFile == null) {
            return;
        }
        
        Properties noteProps = new Properties();
        
        noteProps.setProperty("content", note.getContent());
        noteProps.setProperty("title", note.getTitle());
        
        noteProps.store(new FileOutputStream(noteFile), NoteIO.fileIdentificationComment);
    }
    
}
