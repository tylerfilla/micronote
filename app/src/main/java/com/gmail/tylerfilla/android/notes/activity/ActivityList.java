package com.gmail.tylerfilla.android.notes.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.xml.sax.XMLReader;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.io.NoteIO;

public class ActivityList extends ListActivity {
    
    private ArrayList<File> noteFileList;
    
    private NoteFileListAdapter                 noteFileListAdapter;
    private NoteFileListOnItemClickListener     noteFileListOnItemClickListener;
    private NoteFileListMultiChoiceModeListener noteFileListMultiChoiceModeListener;
    
    private LinearLayout activityListMessageListEmpty;
    private LinearLayout activityListMessageSearchEmpty;
    private LinearLayout activityListMessageSearchOpen;
    
    private TextView activityListListFooter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        this.getActionBar().setCustomView(R.layout.activity_list_actionbar);
        this.setContentView(R.layout.activity_list);
        
        this.noteFileList = new ArrayList<>();
        
        this.noteFileListAdapter                 = new NoteFileListAdapter();
        this.noteFileListOnItemClickListener     = new NoteFileListOnItemClickListener();
        this.noteFileListMultiChoiceModeListener = new NoteFileListMultiChoiceModeListener();
    
        this.activityListMessageListEmpty   = (LinearLayout) this.findViewById(R.id.activityListMessageListEmpty);
        this.activityListMessageSearchEmpty = (LinearLayout) this.findViewById(R.id.activityListMessageSearchEmpty);
        this.activityListMessageSearchOpen  = (LinearLayout) this.findViewById(R.id.activityListMessageSearchOpen);
        
        ListView listView = this.getListView();
        
        // Add footer to display number of notes
        this.activityListListFooter = (TextView) this.getLayoutInflater().inflate(R.layout.activity_list_list_footer, null);
        listView.addFooterView(this.activityListListFooter, null, false);
        
        // Set adapter and listeners
        listView.setAdapter(this.noteFileListAdapter);
        listView.setOnItemClickListener(this.noteFileListOnItemClickListener);
        listView.setMultiChoiceModeListener(this.noteFileListMultiChoiceModeListener);
        
        // Set task description
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(this.getTitle().toString(), null, this.getResources().getColor(R.color.task_primary)));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Update list
        this.update();
    }
    
    private void update() {
        // Clear note file list
        this.noteFileList.clear();
        
        // Iterate over files within notes directory and populate noteFileList
        File[] files = new File(this.getFilesDir(), "notes").listFiles();
        if (files != null) {
            for (File file : files) {
                // Check if the file has the *.note extension
                if (file.getName().toLowerCase().endsWith(".note")) {
                    // Check the file content
                    boolean contentCheck = false;
                    try {
                        contentCheck = NoteIO.check(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
    
                    // If the file is truly a note file
                    if (contentCheck) {
                        this.noteFileList.add(file);
                    }
                }
            }
        }
        
        // Show empty list message if no files are found
        if (this.noteFileList.isEmpty()) {
            this.activityListMessageListEmpty.setVisibility(View.VISIBLE);
        } else {
            this.activityListMessageListEmpty.setVisibility(View.GONE);
        }
        
        // Set footer text to a note counter
        if (!this.noteFileList.isEmpty()) {
            this.activityListListFooter.setText(String.valueOf(this.noteFileList.size()) + " note" + (this.noteFileList.size() > 1 ? "s" : ""));
        }
        
        // Notify adapter of data change
        this.noteFileListAdapter.notifyDataSetChanged();
    }
    
    private void openSettings() {
        this.startActivity(new Intent(this, ActivitySettings.class));
    }
    
    private void openNoteFile(File noteFile) {
        Intent intentEdit = new Intent(this, ActivityEdit.class);
        
        int intentFlags = Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intentFlags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS;
        }
        
        intentEdit.setFlags(intentFlags);
        
        if (noteFile != null) {
            intentEdit.setData(Uri.fromFile(noteFile));
        } else {
            intentEdit.setData(null);
        }
        
        this.startActivity(intentEdit);
    }
    
    public void onActionButtonClick(View view) {
        if ("list_settings".equals(view.getTag())) {
            this.openSettings();
        } else if ("list_new".equals(view.getTag())) {
            this.openNoteFile(null);
        }
    }
    
    private class NoteFileListAdapter extends BaseAdapter {
        
        private final HashSet<Integer> selection;
        
        public NoteFileListAdapter() {
            this.selection = new HashSet<>();
        }
        
        @Override
        public int getCount() {
            return ActivityList.this.noteFileList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return ActivityList.this.noteFileList.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // The list item view to generate
            View view = convertView != null ? convertView : ActivityList.this.getLayoutInflater().inflate(R.layout.activity_list_list_item, parent, false);
            
            // Get reference to note file
            File noteFile = ActivityList.this.noteFileList.get(position);
            
            // Read note file
            Note note = null;
            try {
                note = NoteIO.read(noteFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (note != null) {
                // Store note data
                String title = note.getTitle();
                String content = note.getContent();
                
                // Title and preview subviews
                TextView textViewTitle = (TextView) view.findViewById(R.id.activityListListItemTitle);
                TextView textViewPreview = (TextView) view.findViewById(R.id.activityListListItemPreview);
                
                // Set title
                if (title == null || title.isEmpty()) {
                    textViewTitle.setText("No Title");
                } else {
                    textViewTitle.setText(title);
                }
                
                // Set content
                if (content == null || content.isEmpty()) {
                    textViewPreview.setText("No content");
                } else {
                    // Remove all HTML tags
                    String htmlStrippedContentPreview = Html.fromHtml(content, null, new Html.TagHandler() {
                        
                        @Override
                        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                            if (tag.equalsIgnoreCase("li") && opening) {
                                output.append(' ');
                            }
                        }
                        
                    }).toString().replace('\n', ' ').replaceAll("\\s+", " ").trim();
                    
                    // Shorten the preview content before applying it to the UI
                    htmlStrippedContentPreview = htmlStrippedContentPreview.substring(0, Math.min(50, htmlStrippedContentPreview.length()));
                    
                    // Perhaps the note's content was strictly HTML tags (odd, but possible)
                    if (htmlStrippedContentPreview.isEmpty()) {
                        textViewPreview.setText("No content");
                    } else {
                        textViewPreview.setText(htmlStrippedContentPreview);
                    }
                }
                
                // Set background based on selection state
                if (this.getSelected(position)) {
                    view.setBackgroundColor(ActivityList.this.getResources().getColor(R.color.background_activity_list_list_item_selected));
                } else {
                    view.setBackgroundColor(ActivityList.this.getResources().getColor(R.color.background_activity_list_list_item));
                }
                
                return view;
            } else {
                return null;
            }
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
    
    private class NoteFileListOnItemClickListener implements OnItemClickListener {
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ActivityList.this.openNoteFile(ActivityList.this.noteFileList.get(position));
        }
        
    }
    
    private class NoteFileListMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate CAB
            mode.getMenuInflater().inflate(R.menu.activity_list_list_select_cab, menu);
            
            return true;
        }
        
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }
        
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Clear selection
            ActivityList.this.noteFileListAdapter.clearSelection();
        }
        
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // Add to selection
            ActivityList.this.noteFileListAdapter.setSelected(position, checked);
            
            // Display number of selected notes
            mode.setTitle(ActivityList.this.noteFileListAdapter.getSelectionCount() + " note" + (ActivityList.this.noteFileListAdapter.getSelectionCount() == 1 ? "" : "s") + " selected");
        }
        
    }
    
}
