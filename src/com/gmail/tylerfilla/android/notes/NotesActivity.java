package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;

public class NotesActivity extends ListActivity {
    
    private NoteKeeper noteKeeper;
    
    private ArrayList<Note> noteList;
    private NoteListAdapter noteListAdapter;
    
    private boolean searchMode;
    private String searchQuery;
    private TextWatcher searchBubbleTextChangedListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.noteList = new ArrayList<Note>();
        this.noteListAdapter = new NoteListAdapter();
        
        this.searchMode = false;
        this.searchQuery = "";
        this.searchBubbleTextChangedListener = null;
        
        this.getActionBar().setCustomView(R.layout.activity_notes_actionbar_list);
        this.setContentView(R.layout.activity_notes);
        
        ListView listView = this.getListView();
        listView.setAdapter(this.noteListAdapter);
        listView.setMultiChoiceModeListener(new NoteListMultiChoiceModeListener(
                (NoteListAdapter) listView.getAdapter()));
        listView.setOnItemClickListener(new NoteListOnItemClickListener((NoteListAdapter) listView
                .getAdapter()));
        listView.addFooterView(
                this.getLayoutInflater().inflate(R.layout.activity_notes_list_footer, null), null,
                false);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        this.refresh();
    }
    
    private void refresh() {
        this.findViewById(R.id.activityNotesNoteListEmpty).setVisibility(View.GONE);
        this.findViewById(R.id.activityNotesSearchEmpty).setVisibility(View.GONE);
        this.findViewById(R.id.activityNotesNoteListFooter).setVisibility(View.GONE);
        
        this.noteList.clear();
        
        File[] noteFiles = this.noteKeeper.listNoteFiles();
        
        if (noteFiles.length > 0) {
            for (File noteFile : noteFiles) {
                Note note = null;
                
                try {
                    note = this.noteKeeper.readNote(noteFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                if (note != null) {
                    if (this.searchMode) {
                        String noteContent = note.getContent();
                        if (noteContent != null) {
                            noteContent = Html.fromHtml(noteContent).toString().toLowerCase();
                            boolean match = true;
                            
                            for (String queryWord : this.searchQuery.toLowerCase().split(" ")) {
                                match = match && noteContent.contains(queryWord);
                            }
                            
                            if (match) {
                                this.noteList.add(note);
                            }
                        }
                    } else {
                        this.noteList.add(note);
                    }
                }
            }
        } else if (!this.searchMode) {
            this.findViewById(R.id.activityNotesNoteListEmpty).setVisibility(View.VISIBLE);
        }
        
        if (this.noteList.size() > 0) {
            ((TextView) this.findViewById(R.id.activityNotesNoteListFooter)).setText(this.noteList
                    .size() + " note" + (this.noteList.size() == 1 ? "" : "s"));
            this.findViewById(R.id.activityNotesNoteListFooter).setVisibility(View.VISIBLE);
        } else if (this.searchMode) {
            this.findViewById(R.id.activityNotesSearchEmpty).setVisibility(View.VISIBLE);
        }
        
        this.noteListAdapter.notifyDataSetChanged();
    }
    
    public void buttonActionClicked(View view) {
        if ("list_settings".equals(view.getTag())) {
            this.enterSettings();
        } else if ("list_search".equals(view.getTag())) {
            this.toggleSearchMode();
        } else if ("list_new".equals(view.getTag())) {
            this.enterNoteEditor(null);
        } else if ("search_close".equals(view.getTag())) {
            this.toggleSearchMode();
        }
    }
    
    private void enterSettings() {
        this.startActivity(new Intent(this, SettingsActivity.class));
    }
    
    private void toggleSearchMode() {
        EditText searchBubble = (EditText) this.findViewById(R.id.activityNotesSearchBubble);
        
        if (this.searchMode) {
            this.searchMode = false;
            this.searchQuery = "";
            this.getActionBar().setCustomView(R.layout.activity_notes_actionbar_list);
            
            searchBubble.setVisibility(View.GONE);
            searchBubble.removeTextChangedListener(this.searchBubbleTextChangedListener);
            this.getListView().requestFocus();
            ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(searchBubble.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            this.searchMode = true;
            this.getActionBar().setCustomView(R.layout.activity_notes_actionbar_search);
            
            this.searchBubbleTextChangedListener = new TextWatcher() {
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    NotesActivity.this.searchQuery = s.toString();
                    NotesActivity.this.refresh();
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                }
                
            };
            
            searchBubble.setVisibility(View.VISIBLE);
            searchBubble.setText("");
            searchBubble.addTextChangedListener(this.searchBubbleTextChangedListener);
            searchBubble.requestFocus();
            ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(searchBubble, InputMethodManager.SHOW_IMPLICIT);
        }
        
        this.refresh();
    }
    
    private void enterNoteEditor(File noteFile) {
        Intent noteEditIntent = new Intent(this, NoteEditActivity.class);
        
        if (noteFile != null) {
            noteEditIntent.putExtra("file", noteFile.getAbsolutePath());
        }
        
        this.startActivity(noteEditIntent);
    }
    
    private class NoteListAdapter extends BaseAdapter {
        
        private final HashSet<Integer> selection;
        
        public NoteListAdapter() {
            this.selection = new HashSet<Integer>();
        }
        
        @Override
        public int getCount() {
            return NotesActivity.this.noteList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return NotesActivity.this.noteList.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView : NotesActivity.this.getLayoutInflater()
                    .inflate(R.layout.activity_notes_list_item, parent, false);
            Note note = NotesActivity.this.noteList.get(position);
            
            TextView title = (TextView) view.findViewById(R.id.activityNotesNoteListItemTitle);
            TextView contentPreview = (TextView) view
                    .findViewById(R.id.activityNotesNoteListItemContentPreview);
            
            if (note != null) {
                if (note.getTitle() != null) {
                    title.setText(note.getTitle());
                }
                if (note.getContent() != null) {
                    contentPreview.setText(Html.fromHtml(note.getContent()));
                }
            }
            
            if (this.getSelected(position)) {
                view.setBackgroundColor(NotesActivity.this.getResources().getColor(
                        R.color.background_note_list_item_selected));
            } else {
                view.setBackgroundColor(NotesActivity.this.getResources().getColor(
                        android.R.color.transparent));
            }
            
            return view;
        }
        
        public boolean getSelected(int position) {
            return this.selection.contains(position);
        }
        
        public void setSelected(int position, boolean selected) {
            if (selected) {
                this.selection.add(position);
                this.notifyDataSetChanged();
            } else {
                this.selection.remove(position);
                this.notifyDataSetChanged();
            }
        }
        
        public int getSelectionCount() {
            return this.selection.size();
        }
        
        public void clearSelection() {
            this.selection.clear();
            this.notifyDataSetChanged();
        }
        
    }
    
    private class NoteListOnItemClickListener implements OnItemClickListener {
        
        private final NoteListAdapter noteListAdapter;
        
        public NoteListOnItemClickListener(NoteListAdapter noteListAdapter) {
            this.noteListAdapter = noteListAdapter;
        }
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Note note = (Note) this.noteListAdapter.getItem(position);
            
            if (note.getFile() != null) {
                NotesActivity.this.enterNoteEditor(note.getFile());
            }
        }
        
    }
    
    private class NoteListMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        
        private final NoteListAdapter noteListAdapter;
        
        public NoteListMultiChoiceModeListener(NoteListAdapter noteListAdapter) {
            this.noteListAdapter = noteListAdapter;
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.activity_notes_list_select_cab, menu);
            return true;
        }
        
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.activityNotesNoteListSelectCABDelete:
                HashSet<Note> deletedNotes = new HashSet<Note>();
                
                for (int i = 0; i < this.noteListAdapter.getCount(); i++) {
                    if (this.noteListAdapter.getSelected(i)) {
                        Note note = (Note) this.noteListAdapter.getItem(i);
                        
                        try {
                            NotesActivity.this.noteKeeper.deleteNote(note);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        
                        deletedNotes.add(note);
                    }
                }
                
                for (Note note : deletedNotes) {
                    NotesActivity.this.noteList.remove(note);
                }
                
                this.noteListAdapter.notifyDataSetChanged();
                mode.finish();
                
                NotesActivity.this.refresh();
                
                break;
            }
            return true;
        }
        
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            this.noteListAdapter.clearSelection();
        }
        
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {
            this.noteListAdapter.setSelected(position, checked);
            mode.setTitle(this.noteListAdapter.getSelectionCount() + " note"
                    + (this.noteListAdapter.getSelectionCount() == 1 ? "" : "s") + " selected");
        }
        
    }
    
}
