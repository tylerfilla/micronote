package com.gmail.tylerfilla.android.notes.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.os.Environment;
import android.webkit.MimeTypeMap;

public class NoteKeeper {
    
    private static final File notesDirectory = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath().concat("/Notes"));
    
    private NoteKeeper() {
    }
    
    public static File[] listNoteFiles() throws IOException {
        ArrayList<File> noteFiles = new ArrayList<File>();
        
        for (File noteFile : notesDirectory.listFiles()) {
            if (checkNoteFile(noteFile)) {
                noteFiles.add(noteFile);
            }
        }
        
        return noteFiles.toArray(new File[noteFiles.size()]);
    }
    
    public static boolean checkNoteFile(File noteFile) throws IOException {
        boolean check = false;
        
        if (noteFile.isFile() && noteFile.getName().endsWith(".zip")) {
            ZipFile zip = new ZipFile(noteFile);
            
            ZipEntry mediaDirEntry = zip.getEntry("media");
            ZipEntry infoEntry = zip.getEntry("info");
            ZipEntry contentEntry = zip.getEntry("content");
            
            check = mediaDirEntry != null && infoEntry != null && contentEntry != null
                    && mediaDirEntry.isDirectory() && !infoEntry.isDirectory()
                    && !contentEntry.isDirectory();
            
            zip.close();
        }
        
        return check;
    }
    
    public static Note readNoteFile(File noteFile) throws IOException {
        if (!checkNoteFile(noteFile)) {
            return null;
        }
        
        Note note = new Note();
        
        File mediaDirFile = new File(noteFile, "media");
        File infoFile = new File(noteFile, "info");
        File contentFile = new File(noteFile, "content");
        
        for (File mediaFile : mediaDirFile.listFiles()) {
            NoteMedia media = new NoteMedia();
            
            String mediaFileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(mediaFile.getName()));
            
            if (mediaFileMimeType.startsWith("image/")) {
                media.setType(NoteMedia.Type.IMAGE);
            } else if (mediaFileMimeType.startsWith("audio/")) {
                media.setType(NoteMedia.Type.AUDIO);
            } else if (mediaFileMimeType.startsWith("video/")) {
                media.setType(NoteMedia.Type.VIDEO);
            }
        }
        
        BufferedReader infoFileReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(infoFile)));
        
        String infoFileLine = null;
        while ((infoFileLine = infoFileReader.readLine()) != null) {
            
        }
        
        infoFileReader.close();
        
        return note;
    }
    
    public static void writeNoteFile(Note note) {
    }
    
}
