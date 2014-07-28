package com.gmail.tylerfilla.android.notes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.NoteKeeper;

public class NotesActivity extends ListActivity {
    
    private NoteKeeper noteKeeper;
    private ArrayList<Note> noteList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.noteKeeper = NoteKeeper.getInstance(this);
        
        this.noteList = new ArrayList<Note>();
        
        this.getActionBar().setCustomView(R.layout.actionbar_activity_notes);
        this.setContentView(R.layout.activity_notes);
        
        ListView listView = this.getListView();
        listView.setAdapter(new NoteListAdapter());
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new NoteListMultiChoiceModeListener(
                (NoteListAdapter) this.getListView().getAdapter()));
        listView.setDividerHeight(2);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        this.refreshNoteList();
    }
    
    private void refreshNoteList() {
        ImageButton buttonActionSearch = (ImageButton) this
                .findViewById(R.id.actionbarActivityButtonSearch);
        
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
                    this.noteList.add(note);
                }
            }
        }
        
        if (noteFiles.length >= 2) {
            buttonActionSearch.setVisibility(View.VISIBLE);
        } else {
            buttonActionSearch.setVisibility(View.GONE);
        }
        
        ((BaseAdapter) this.getListView().getAdapter()).notifyDataSetChanged();
    }
    
    public void buttonActionClicked(View view) {
        if ("settings".equals(view.getTag())) {
            Toast.makeText(this, "Settings not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("search".equals(view.getTag())) {
            Toast.makeText(this, "Search not yet implemented", Toast.LENGTH_SHORT).show();
        } else if ("new".equals(view.getTag())) {
            this.enterNoteEditor(null);
        }
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
        private final SparseBooleanArray selection;
        
        public NoteListAdapter() {
            this.layoutInflater = (LayoutInflater) NotesActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.selection = new SparseBooleanArray();
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
            return this.selection.get(position);
        }
        
        public void setSelected(int position, boolean selected) {
            this.selection.put(position, selected);
            this.notifyDataSetChanged();
        }
        
        public void clearSelection() {
            this.selection.clear();
            this.notifyDataSetChanged();
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
                mode.finish();
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
        }
        
    }
    
}
