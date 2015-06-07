package com.gmail.tylerfilla.android.notes.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.xml.sax.XMLReader;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.NoteKeeper;
import com.gmail.tylerfilla.android.notes.R;

public class ActivityList extends ListActivity {
    
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
        
        this.getActionBar().setCustomView(R.layout.activity_list_actionbar_list);
        this.setContentView(R.layout.activity_list);
        
        ListView listView = this.getListView();
        listView.addFooterView(
                this.getLayoutInflater().inflate(R.layout.activity_list_list_footer, null), null,
                false);
        listView.setAdapter(this.noteListAdapter);
        listView.setMultiChoiceModeListener(new NoteListMultiChoiceModeListener(
                this.noteListAdapter));
        listView.setOnItemClickListener(new NoteListOnItemClickListener(this.noteListAdapter));
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(this.getTitle().toString(), null, this.getResources().getColor(R.color.task_primary)));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        this.refresh();
    }
    
    private void refresh() {
        TextView activityNotesListFooter = (TextView) this
                .findViewById(R.id.activityNotesListFooter);
        LinearLayout activityNotesMessageListEmpty = (LinearLayout) this
                .findViewById(R.id.activityNotesMessageListEmpty);
        LinearLayout activityNotesMessageSearchOpen = (LinearLayout) this
                .findViewById(R.id.activityNotesMessageSearchOpen);
        LinearLayout activityNotesMessageSearchEmpty = (LinearLayout) this
                .findViewById(R.id.activityNotesMessageSearchEmpty);
        
        activityNotesListFooter.setVisibility(View.GONE);
        activityNotesMessageListEmpty.setVisibility(View.GONE);
        activityNotesMessageSearchOpen.setVisibility(View.GONE);
        activityNotesMessageSearchEmpty.setVisibility(View.GONE);
        
        this.noteList.clear();
        
        File[] noteFiles = this.noteKeeper.listFiles();
        
        int numNotesAdded = 0;
        int numNotesTotal = 0;
        
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
                        if (this.filterSearchNote(note)) {
                            this.noteList.add(note);
                            numNotesAdded++;
                        }
                    } else {
                        this.noteList.add(note);
                        numNotesAdded++;
                    }
                    numNotesTotal++;
                }
            }
        }
        
        if (this.searchMode) {
            if (this.searchQuery.isEmpty()) {
                activityNotesMessageSearchOpen.setVisibility(View.VISIBLE);
            } else if (numNotesAdded == 0) {
                activityNotesMessageSearchEmpty.setVisibility(View.VISIBLE);
            } else {
                activityNotesListFooter.setVisibility(View.VISIBLE);
            }
        } else {
            if (numNotesAdded == 0) {
                activityNotesMessageListEmpty.setVisibility(View.VISIBLE);
            } else {
                activityNotesListFooter.setVisibility(View.VISIBLE);
            }
        }
        
        if (numNotesAdded == numNotesTotal) {
            activityNotesListFooter.setText(numNotesAdded + " note"
                    + (numNotesAdded == 1 ? "" : "s"));
        } else {
            activityNotesListFooter.setText(numNotesAdded + " of " + numNotesTotal + " note"
                    + (numNotesTotal == 1 ? "" : "s"));
        }
        
        this.noteListAdapter.notifyDataSetChanged();
    }
    
    public void onActionButtonClick(View view) {
        if ("list_settings".equals(view.getTag())) {
            this.openSettings();
        } else if ("list_search".equals(view.getTag())) {
            this.toggleSearchMode();
        } else if ("list_new".equals(view.getTag())) {
            this.openNote(null);
        } else if ("search_close".equals(view.getTag())) {
            this.toggleSearchMode();
        }
    }
    
    private void openSettings() {
        this.startActivity(new Intent(this, ActivitySettings.class));
    }
    
    private void openNote(File noteFile) {
        Intent intentEdit = new Intent(this, ActivityEdit.class);
        intentEdit.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS);
        
        if (noteFile != null) {
            intentEdit.setData(Uri.fromFile(noteFile));
        }
        
        this.startActivity(intentEdit);
    }
    
    private void toggleSearchMode() {
        EditText searchBubble = (EditText) this.findViewById(R.id.activityNotesSearchBubble);
        
        if (this.searchMode) {
            this.searchMode = false;
            this.searchQuery = "";
            
            this.getActionBar().setCustomView(R.layout.activity_list_actionbar_list);
            
            searchBubble.setVisibility(View.GONE);
            searchBubble.removeTextChangedListener(this.searchBubbleTextChangedListener);
            this.getListView().requestFocus();
            ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(searchBubble.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            this.searchMode = true;
            
            this.getActionBar().setCustomView(R.layout.activity_list_actionbar_search);
            
            this.searchBubbleTextChangedListener = new TextWatcher() {
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ActivityList.this.searchQuery = s.toString();
                    ActivityList.this.refresh();
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
    
    private boolean filterSearchNote(Note note) {
        String noteContentText = Html.fromHtml(note.getContent()).toString().toLowerCase();
        
        boolean result = !this.searchQuery.isEmpty();
        
        for (String queryWord : this.searchQuery.toLowerCase().split(" ")) {
            result = result && noteContentText.contains(queryWord);
        }
        
        return result;
    }
    
    private class NoteListAdapter extends BaseAdapter {
        
        private final HashSet<Integer> selection;
        
        public NoteListAdapter() {
            this.selection = new HashSet<Integer>();
        }
        
        @Override
        public int getCount() {
            return ActivityList.this.noteList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return ActivityList.this.noteList.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView != null ? convertView : ActivityList.this.getLayoutInflater()
                    .inflate(R.layout.activity_list_list_item, parent, false);
            Note note = ActivityList.this.noteList.get(position);
            
            TextView title = (TextView) view.findViewById(R.id.activityNotesListItemTitle);
            TextView contentPreview = (TextView) view
                    .findViewById(R.id.activityNotesListItemContentPreview);
            
            if (note != null) {
                if (note.getTitle() != null && !note.getTitle().isEmpty()) {
                    title.setText(note.getTitle());
                } else {
                    title.setText("No Title");
                }
                
                if (note.getContent() == null) {
                    contentPreview.setText("No content");
                } else {
                    String content = Html.fromHtml(note.getContent(), null, new Html.TagHandler() {
                        
                        @Override
                        public void handleTag(boolean opening, String tag, Editable output,
                                XMLReader xmlReader) {
                            if (tag.equalsIgnoreCase("li") && opening) {
                                output.append(' ');
                            }
                        }
                        
                    }).toString().replace('\n', ' ').replaceAll("\\s+", " ").trim();
                    
                    if (content.isEmpty()) {
                        contentPreview.setText("No content");
                    } else {
                        contentPreview.setText(content);
                    }
                }
            }
            
            if (this.getSelected(position)) {
                view.setBackgroundColor(ActivityList.this.getResources().getColor(
                        R.color.background_activity_list_list_item_selected));
            } else {
                view.setBackgroundColor(ActivityList.this.getResources().getColor(
                        R.color.background_activity_list_list_item));
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
                ActivityList.this.openNote(note.getFile());
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
            mode.getMenuInflater().inflate(R.menu.activity_list_list_select_cab, menu);
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
                            ActivityList.this.noteKeeper.deleteNote(note);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        
                        deletedNotes.add(note);
                    }
                }
                
                for (Note note : deletedNotes) {
                    ActivityList.this.noteList.remove(note);
                }
                
                this.noteListAdapter.notifyDataSetChanged();
                mode.finish();
                
                ActivityList.this.refresh();
                
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
