package com.gmail.tylerfilla.android.notes.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;

public class NoteKeeper {
    
    private static NoteKeeper instance = null;
    
    private File dirNotes;
    private File dirTemp;
    
    private NoteKeeper(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            this.dirNotes = new File(Environment.getExternalStorageDirectory(), "Notes");
            this.dirTemp = new File(context.getExternalCacheDir(), "Temp");
        } else {
            this.dirNotes = new File(context.getFilesDir(), "Notes");
            this.dirTemp = new File(context.getFilesDir(), "Temp");
        }
        
        this.dirNotes.mkdirs();
        this.dirTemp.mkdirs();
    }
    
    public static NoteKeeper getInstance(Context context) {
        if (instance == null) {
            instance = new NoteKeeper(context);
        }
        
        return instance;
    }
    
    public File[] listNoteFiles() {
        Collection<File> dirNotesContents = scanDirectory(this.dirNotes);
        ArrayList<File> noteFileList = new ArrayList<File>();
        
        for (File file : dirNotesContents) {
            if (file.isFile() && file.getName().endsWith(".note")) {
                noteFileList.add(file);
            }
        }
        
        return noteFileList.toArray(new File[noteFileList.size()]);
    }
    
    public void cleanup() {
        for (File tempFile : this.dirTemp.listFiles()) {
            tempFile.delete();
        }
    }
    
    public Note readNote(File noteFile) throws IOException {
        final Note note = new Note();
        note.setFile(noteFile);
        
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(noteFile, new DefaultHandler() {
                
                boolean infoTitle = false;
                boolean infoAuthor = false;
                boolean content = false;
                
                @Override
                public void startElement(String uri, String localName, String qName,
                        Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("note")) {
                        int versionAttributeIndex = attributes.getIndex("version");
                        if (versionAttributeIndex > -1) {
                            try {
                                note.setVersion(Integer.parseInt(attributes
                                        .getValue(versionAttributeIndex)));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (qName.equalsIgnoreCase("title")) {
                        infoTitle = true;
                    } else if (qName.equalsIgnoreCase("author")) {
                        infoAuthor = true;
                    } else if (qName.equalsIgnoreCase("content")) {
                        content = true;
                    }
                }
                
                @Override
                public void endElement(String uri, String localName, String qName)
                        throws SAXException {
                    if (qName.equalsIgnoreCase("title")) {
                        infoTitle = false;
                    } else if (qName.equalsIgnoreCase("author")) {
                        infoAuthor = false;
                    } else if (qName.equalsIgnoreCase("content")) {
                        content = false;
                    }
                }
                
                @Override
                public void characters(char ch[], int start, int length) throws SAXException {
                    String string = new String(ch, start, length);
                    
                    if (!string.trim().isEmpty()) {
                        if (infoTitle) {
                            note.setTitle(string);
                        } else if (infoAuthor) {
                            note.setAuthor(string);
                        } else if (content) {
                            note.setContent(new String(Base64.decode(string.getBytes(),
                                    Base64.DEFAULT)));
                        }
                    }
                }
                
            });
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        
        return note;
    }
    
    public void writeNote(Note note) throws IOException {
        
    }
    
    private static Collection<File> scanDirectory(File directory) {
        Collection<File> collection = new HashSet<File>();
        
        if (directory != null && directory.isDirectory()) {
            for (File child : directory.listFiles()) {
                collection.add(child);
                
                if (child.isDirectory()) {
                    collection.addAll(scanDirectory(child));
                }
            }
        }
        
        return collection;
    }
    
}
