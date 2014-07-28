package com.gmail.tylerfilla.android.notes.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
                    } else if (qName.equalsIgnoreCase("content")) {
                        content = true;
                    }
                }
                
                @Override
                public void endElement(String uri, String localName, String qName)
                        throws SAXException {
                    if (qName.equalsIgnoreCase("title")) {
                        infoTitle = false;
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
                        } else if (content) {
                            String currentContent = note.getContent() != null ? note.getContent()
                                    : "";
                            note.setContent(currentContent.concat(new String(Base64.decode(
                                    string.getBytes(), Base64.DEFAULT))));
                        }
                    }
                }
                
            });
        } catch (ParserConfigurationException e) {
            throw new NoteIOException(e);
        } catch (SAXException e) {
            throw new NoteIOException(e);
        }
        
        return note;
    }
    
    public void writeNote(Note note) throws IOException {
        if (note.getFile() == null) {
            note.setFile(new File(this.dirNotes, note.getTitle().toLowerCase()
                    .replaceAll("[^A-Za-z0-9]", "-").concat(".note")));
            while (note.getFile().exists()) {
                note.setFile(new File(note.getFile().getParentFile(), "_".concat(note.getFile()
                        .getName())));
            }
        }
        
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Transformer transformer = transformerFactory.newTransformer();
            
            Document document = documentBuilder.newDocument();
            
            Node tagNote = document.createElement("note");
            Attr tagNoteAttrVersion = document.createAttribute("version");
            tagNoteAttrVersion.setValue(String.valueOf(note.getVersion()));
            tagNote.getAttributes().setNamedItem(tagNoteAttrVersion);
            document.appendChild(tagNote);
            
            Node tagNoteInfo = document.createElement("info");
            tagNote.appendChild(tagNoteInfo);
            
            Node tagNoteInfoTitle = document.createElement("title");
            tagNoteInfoTitle.setTextContent(note.getTitle());
            tagNoteInfo.appendChild(tagNoteInfoTitle);
            
            Node tagNoteContent = document.createElement("content");
            tagNoteContent.setTextContent(Base64.encodeToString(note.getContent().getBytes(),
                    Base64.DEFAULT));
            tagNote.appendChild(tagNoteContent);
            
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            
            transformer.transform(new DOMSource(document), new StreamResult(note.getFile()));
        } catch (ParserConfigurationException e) {
            throw new NoteIOException(e);
        } catch (TransformerConfigurationException e) {
            throw new NoteIOException(e);
        } catch (TransformerException e) {
            throw new NoteIOException(e);
        }
    }
    
    public void deleteNote(Note note) throws IOException {
        File noteFile = note.getFile();
        
        if (noteFile != null) {
            noteFile.delete();
        }
        
        note.setFile(null);
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
    
    private static class NoteIOException extends IOException {
        
        private static final long serialVersionUID = 4869480158190405839L;
        
        public NoteIOException(Throwable cause) {
            super(cause);
        }
        
    }
    
}
