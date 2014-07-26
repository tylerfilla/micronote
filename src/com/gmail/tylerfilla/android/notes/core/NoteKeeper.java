package com.gmail.tylerfilla.android.notes.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.util.Log;

public class NoteKeeper {
    
    private static String dirNotes;
    private static String dirTemp;
    
    private NoteKeeper() {
    }
    
    public static void setDirectories(String paramDirNotes, String paramDirTemp) {
        dirNotes = paramDirNotes;
        dirTemp = paramDirTemp;
    }
    
    public static File[] listNoteFiles() {
        ArrayList<File> noteFiles = new ArrayList<File>();
        
        for (File noteFile : new File(dirNotes).listFiles()) {
            if (checkNoteFile(noteFile)) {
                noteFiles.add(noteFile);
            }
        }
        
        return noteFiles.toArray(new File[noteFiles.size()]);
    }
    
    public static boolean checkNoteFile(File noteFile) {
        boolean check = false;
        
        if (noteFile.isFile() && noteFile.getName().endsWith(".note")) {
            try {
                ZipFile zip = new ZipFile(noteFile);
                
                ZipEntry mediaDirEntry = zip.getEntry("media");
                ZipEntry infoEntry = zip.getEntry("info");
                ZipEntry contentEntry = zip.getEntry("content");
                
                check = mediaDirEntry != null && infoEntry != null && contentEntry != null
                        && mediaDirEntry.isDirectory() && !infoEntry.isDirectory()
                        && !contentEntry.isDirectory();
                
                zip.close();
            } catch (IOException e) {
                check = false;
            }
        }
        
        return check;
    }
    
    public static Note readNoteFile(File noteFile) throws IOException {
        if (!checkNoteFile(noteFile)) {
            return null;
        }
        
        ZipFile zip = new ZipFile(noteFile);
        
        ZipEntry infoEntry = zip.getEntry("info");
        ZipEntry contentEntry = zip.getEntry("content");
        
        Note note = new Note();
        
        note.setFile(noteFile);
        
        Enumeration<? extends ZipEntry> enumeration = zip.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry entry = enumeration.nextElement();
            
            if (entry.getName().startsWith("media/") && entry.getName().length() > 6) {
                String filename = entry.getName().substring(6);
                
                InputStream input = null;
                OutputStream output = null;
                
                try {
                    File file = new File(dirTemp, filename);
                    
                    if (file.exists()) {
                        if (!file.delete()) {
                            continue;
                        }
                    }
                    
                    input = zip.getInputStream(entry);
                    output = new FileOutputStream(file);
                    
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while ((count = input.read(buffer, 0, buffer.length)) > 0) {
                        output.write(buffer, 0, count);
                    }
                    
                    NoteMedia media = new NoteMedia();
                    
                    media.setNote(note);
                    media.setFile(file);
                    
                    note.addMedia(media);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                }
            }
        }
        
        BufferedReader infoFileReader = new BufferedReader(new InputStreamReader(
                zip.getInputStream(infoEntry)));
        
        String infoFileLine = null;
        while ((infoFileLine = infoFileReader.readLine()) != null) {
            String infoFileLineTrim = infoFileLine.trim();
            if (infoFileLineTrim.isEmpty() || infoFileLineTrim.startsWith("#")) {
                continue;
            }
            
            String key = "";
            String value = "";
            
            int stage = 0;
            
            for (int ci = 0; ci < infoFileLine.length(); ci++) {
                char c = infoFileLine.charAt(ci);
                
                if (c == '=' && value.isEmpty()) {
                    stage = 1;
                    continue;
                }
                
                switch (stage) {
                case 0:
                    key += c;
                    break;
                case 1:
                    value += c;
                    break;
                }
            }
            
            if (key.equals("title")) {
                note.setTitle(value);
            } else if (key.equals("author")) {
                note.setAuthor(value);
            } else if (key.equals("time-created")) {
                note.setDateCreated(new Date(Long.parseLong(value)));
            } else if (key.equals("time-modified")) {
                note.setDateModified(new Date(Long.parseLong(value)));
            }
        }
        
        infoFileReader.close();
        
        InputStreamReader contentFileReader = new InputStreamReader(
                zip.getInputStream(contentEntry));
        
        StringBuilder contentBuilder = new StringBuilder();
        
        char[] buffer = new char[256];
        int count = 0;
        while ((count = contentFileReader.read(buffer, 0, buffer.length)) > 0) {
            contentBuilder.append(buffer, 0, count);
        }
        
        note.setContent(contentBuilder.toString());
        
        contentFileReader.close();
        
        zip.close();
        
        return note;
    }
    
    public static void writeNote(Note note) throws IOException {
        Log.d("", "write note");
    }
    
    public static Note createBlankNote() {
        Note note = new Note();
        note.setTitle("Untitled Note");
        
        return note;
    }
    
    public static void clearTempFiles() {
        for (File tempFile : new File(dirTemp).listFiles()) {
            tempFile.delete();
        }
    }
    
}
