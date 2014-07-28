package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;

public class NotesActivity extends ListActivity {
    
    private NoteKeeper noteKeeper;
    
    private ArrayList<Note> noteList;
    private NoteListAdapter noteListAdapter;
    
    private String searchQuery;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.noteList = new ArrayList<Note>();
        this.noteListAdapter = new NoteListAdapter();
        
        this.searchQuery = null;
        
        this.getActionBar().setCustomView(R.layout.actionbar_activity_notes);
        this.setContentView(R.layout.activity_notes);
        
        this.initListView();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        this.refresh();
    }
    
    private void refresh() {
        this.getListView().setVisibility(View.GONE);
        this.findViewById(R.id.activityNotesNoteListEmpty).setVisibility(View.GONE);
        this.findViewById(R.id.activityNotesSearchEmpty).setVisibility(View.GONE);
        this.findViewById(R.id.actionbarActivityNotesButtonSearch).setVisibility(View.GONE);
        
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
                    if (this.searchQuery == null) {
                        this.noteList.add(note);
                    } else {
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
                    }
                }
            }
            
            if (this.searchQuery == null) {
                this.getListView().setVisibility(View.VISIBLE);
            } else {
                if (this.noteList.isEmpty()) {
                    this.findViewById(R.id.activityNotesSearchEmpty).setVisibility(View.VISIBLE);
                } else {
                    this.getListView().setVisibility(View.VISIBLE);
                }
            }
        } else {
            this.findViewById(R.id.activityNotesNoteListEmpty).setVisibility(View.VISIBLE);
        }
        
        if (noteFiles.length >= 2 && this.searchQuery == null) {
            this.findViewById(R.id.actionbarActivityNotesButtonSearch).setVisibility(View.VISIBLE);
        }
        
        ((TextView) this.findViewById(R.id.activityNotesNoteListFooter)).setText(this.noteList
                .size() + " note" + (this.noteList.size() == 1 ? "" : "s"));
        
        this.noteListAdapter.notifyDataSetChanged();
    }
    
    private void initListView() {
        ListView listView = this.getListView();
        
        listView.setDividerHeight(2);
        listView.setAdapter(this.noteListAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new NoteListMultiChoiceModeListener(
                (NoteListAdapter) listView.getAdapter()));
        listView.setOnItemClickListener(new NoteListOnItemClickListener((NoteListAdapter) listView
                .getAdapter()));
        listView.setFooterDividersEnabled(false);
        listView.addFooterView(
                LayoutInflater.from(this).inflate(R.layout.activity_notes_note_list_footer, null),
                null, false);
    }
    
    public void buttonActionClicked(View view) {
        if ("settings".equals(view.getTag())) {
            Toast.makeText(this, "Settings not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("search".equals(view.getTag())) {
            this.enterSearchMode();
        } else if ("search_close".equals(view.getTag())) {
            this.exitSearchMode();
        } else if ("new".equals(view.getTag())) {
            this.enterNoteEditor(null);
        }
    }
    
    private void enterSearchMode() {
        AlertDialog.Builder queryPrompt = new AlertDialog.Builder(this);
        
        queryPrompt.setTitle("Search");
        queryPrompt.setMessage("Input search query:");
        
        final EditText queryPromptInput = new EditText(this);
        queryPromptInput.setMaxLines(1);
        queryPrompt.setView(queryPromptInput);
        
        queryPrompt.setNegativeButton("Cancel", null);
        queryPrompt.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                NotesActivity.this.searchQuery = queryPromptInput.getText().toString();
                NotesActivity.this.refresh();
                
                NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonSettings)
                        .setVisibility(View.GONE);
                NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonSearch)
                        .setVisibility(View.GONE);
                NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonNoteNew)
                        .setVisibility(View.GONE);
                
                NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonSearchClose)
                        .setVisibility(View.VISIBLE);
                
                ((TextView) NotesActivity.this.findViewById(R.id.actionbarActivityNotesTitle))
                        .setText("Search Results");
            }
            
        });
        
        queryPrompt.show();
    }
    
    private void exitSearchMode() {
        this.searchQuery = null;
        this.refresh();
        
        NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonSearchClose)
                .setVisibility(View.GONE);
        
        NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonSettings).setVisibility(
                View.VISIBLE);
        NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonSearch).setVisibility(
                View.VISIBLE);
        NotesActivity.this.findViewById(R.id.actionbarActivityNotesButtonNoteNew).setVisibility(
                View.VISIBLE);
        
        ((TextView) this.findViewById(R.id.actionbarActivityNotesTitle)).setText(this
                .getString(R.string.actionbar_activity_notes_title));
    }
    
    private void enterNoteEditor(File noteFile) {
        Intent noteEditIntent = new Intent("com.gmail.tylerfilla.android.notes.ACTION_EDIT_NOTE");
        
        if (noteFile != null) {
            noteEditIntent.putExtra("noteFilePath", noteFile.getAbsolutePath());
        }
        
        this.startActivity(noteEditIntent);
    }
    
    private class NoteListAdapter extends BaseAdapter {
        
        private final LayoutInflater layoutInflater;
        private final HashSet<Integer> selection;
        
        public NoteListAdapter() {
            this.layoutInflater = (LayoutInflater) NotesActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            View view = convertView != null ? convertView : layoutInflater.inflate(
                    R.layout.activity_notes_note_list_item, parent, false);
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
                        R.color.background_note_list_entry_selected));
            } else {
                view.setBackgroundColor(NotesActivity.this.getResources().getColor(
                        R.color.transparent));
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
            mode.getMenuInflater().inflate(R.menu.activity_notes_note_list_select_cab, menu);
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
