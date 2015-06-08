package com.gmail.tylerfilla.android.notes.io;

import android.util.Log;

import com.gmail.tylerfilla.android.notes.Note;

import java.io.File;
import java.io.IOException;

public class NoteIO {
    
    public static void readNote(Note note, File noteFile) throws IOException {
        Log.d("NoteIO", "Need to read note");
    }
    
    public static void writeNote(Note note, File noteFile) throws IOException {
        Log.d("NoteIO", "Need to write note");
    }
    
}
