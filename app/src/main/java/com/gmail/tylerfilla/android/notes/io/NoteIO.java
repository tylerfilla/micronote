package com.gmail.tylerfilla.android.notes.io;

import com.gmail.tylerfilla.android.notes.Note;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class NoteIO {
    
    public static Note read(File noteFile) throws IOException {
        Note note            = new Note();
        Properties noteProps = new Properties();
        
        noteProps.load(new FileInputStream(noteFile));
        
        if (noteProps.containsKey("title")) {
            note.setTitle(noteProps.getProperty("title"));
        }
        if (noteProps.containsKey("content")) {
            note.setContent(noteProps.getProperty("content"));
        }
        
        return note;
    }
    
    public static void write(Note note, File noteFile) throws IOException {
        Properties noteProps = new Properties();
        
        noteProps.setProperty("title", note.getTitle());
        noteProps.setProperty("content", note.getContent());
        
        noteProps.store(new FileOutputStream(noteFile), "\u00b5Note Note File");
    }
    
}
