package com.gmail.tylerfilla.android.notes.activity;

import android.animation.AnimatorInflater;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.io.NoteIO;
import com.gmail.tylerfilla.android.notes.util.NoteSearcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityList extends AppCompatActivity {
    
    private static final String STATE_KEY_SELECTION_ACTION_MODE_SHOWN = "action_mode_select_shown";
    
    private static final int NOTE_PREVIEW_TITLE_MAX = 20;
    private static final int NOTE_PREVIEW_CONTENT_MAX = 50;
    
    private RecyclerView list;
    private ActivityList.ListAdapter listAdapter;
    private RecyclerView.LayoutManager listLayoutManager;
    
    private NoteSearcher noteSearcher;
    private String noteSearcherQuery;
    
    private View messageListEmpty;
    
    private ActionMode actionModeSelect;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        
        // Set content view
        this.setContentView(R.layout.activity_list);
        
        // Get reference to list
        this.list = (RecyclerView) this.findViewById(R.id.activityListList);
        
        // Create and set adapter
        this.listAdapter = new ActivityList.ListAdapter(this);
        this.list.setAdapter(this.listAdapter);
        
        // Create and set layout manager
        this.listLayoutManager = new LinearLayoutManager(this);
        this.list.setLayoutManager(this.listLayoutManager);
        
        // Note searcher
        this.noteSearcher = new NoteSearcher();
        this.noteSearcherQuery = "";
        
        // Messages
        this.messageListEmpty = this.findViewById(R.id.activityListMessageListEmpty);
        
        // Note preview click listener
        this.listAdapter.setNotePreviewClickListener(new ListAdapter.NotePreviewClickListener() {
            
            @Override
            public void onNotePreviewClick(ListAdapter.NotePreview notePreview) {
                // Open note file
                ActivityList.this.openNoteFile(notePreview.getFile());
            }
            
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh list
        this.refreshList();
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        // Restore list adapter state
        this.listAdapter.restoreState(savedInstanceState);
        
        // Restore selection action mode state
        if (savedInstanceState.getBoolean(STATE_KEY_SELECTION_ACTION_MODE_SHOWN)) {
            this.actionModeSelect = this.startSupportActionMode(new SelectionActionModeCallback(this));
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save list adapter state
        this.listAdapter.saveState(outState);
        
        // Save selection action mode state
        outState.putBoolean(STATE_KEY_SELECTION_ACTION_MODE_SHOWN, this.actionModeSelect != null);
    }
    
    public void onButtonClick(View view) {
        // Switch against button ID
        switch (view.getId()) {
        case R.id.activityListNewButton:
            // Open editor with no note file
            this.openNoteFile(null);
            break;
        }
    }
    
    private void refreshList() {
        // Get final reference to note preview list
        final List<ListAdapter.NotePreview> notePreviewList = this.listAdapter.getNotePreviewList();
        
        // Clear note preview list
        notePreviewList.clear();
        
        // List note files to searcher
        this.noteSearcher.setFileList(Arrays.asList(NoteIO.getNoteStoreDirectory(this).listFiles()));
        
        // Set handler for searcher
        this.noteSearcher.setNoteSearchHandler(new NoteSearcher.NoteSearchHandler() {
            
            @Override
            public Note request(File noteFile) {
                Note note = null;
                
                // Read note file
                try {
                    note = NoteIO.read(noteFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                // Create preview for note
                if (note != null) {
                    String title = note.getTitle();
                    String content = note.getContent();
                    
                    if (title != null) {
                        // Cut length to maximum
                        title = title.substring(0, Math.min(ActivityList.NOTE_PREVIEW_TITLE_MAX, title.length()));
                    }
                    if (content != null) {
                        // Strip HTML tags
                        content = Html.fromHtml(content).toString();
                        
                        // Cut length to maximum
                        content = content.substring(0, Math.min(ActivityList.NOTE_PREVIEW_CONTENT_MAX, content.length()));
                    }
                    
                    // Add to note preview list
                    notePreviewList.add(new ListAdapter.NotePreview(noteFile, title, content));
                }
                
                return note;
            }
            
            @Override
            public void result(File noteFile, boolean match) {
                // If file failed match
                if (!match) {
                    // Note preview to remove
                    ListAdapter.NotePreview killMe = null;
                    
                    // Iterate over note previews
                    for (ListAdapter.NotePreview notePreview : notePreviewList) {
                        // If note preview corresponds with failed file
                        if (notePreview.getFile().equals(noteFile)) {
                            // Mark for removal
                            killMe = notePreview;
                            break;
                        }
                    }
                    
                    // If there exists a note preview to remove, remove it
                    if (killMe != null) {
                        notePreviewList.remove(killMe);
                    }
                }
            }
            
        });
        
        // Execute search
        this.noteSearcher.search(this.noteSearcherQuery);
        
        // Show empty message if no note previews are visible
        if (notePreviewList.isEmpty()) {
            this.messageListEmpty.setVisibility(View.VISIBLE);
        } else {
            this.messageListEmpty.setVisibility(View.GONE);
        }
        
        // Notify adapter of change
        this.listAdapter.notifyDataSetChanged();
    }
    
    private void openNoteFile(File noteFile) {
        // Intent to edit activity
        Intent intentEdit = new Intent(this, ActivityEdit.class);
        
        // Set intent flags
        int intentFlags = Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intentFlags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS;
        }
        intentEdit.setFlags(intentFlags);
        
        // Set URI from file
        intentEdit.setData(noteFile == null ? null : Uri.fromFile(noteFile));
        
        // Start activity
        this.startActivity(intentEdit);
    }
    
    private void promptDeleteNoteFiles(final Collection<File> noteFiles) {
        AlertDialog.Builder promptConfirmDeleteBuilder = new AlertDialog.Builder(this);
        
        // Title
        promptConfirmDeleteBuilder.setTitle("Confirm Deletion");
        
        // Message
        if (noteFiles.size() == 1) {
            promptConfirmDeleteBuilder.setMessage("Are you sure you want to delete this note?");
        } else if (noteFiles.size() > 1) {
            promptConfirmDeleteBuilder.setMessage("Are you sure you want to delete these " + noteFiles.size() + " notes?");
        }
        
        // Buttons and positive handler
        promptConfirmDeleteBuilder.setNegativeButton("No", null);
        promptConfirmDeleteBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // End selection action mode
                if (ActivityList.this.actionModeSelect != null) {
                    ActivityList.this.actionModeSelect.finish();
                }
                
                // Iterate over and delete files
                for (File noteFile : noteFiles) {
                    noteFile.delete();
                }
                
                // Show a toast
                Toast.makeText(ActivityList.this, "Deleted " + noteFiles.size() + " note" + (noteFiles.size() == 1 ? "" : "s"), Toast.LENGTH_SHORT).show();
                
                // Refresh note list
                ActivityList.this.refreshList();
            }
            
        });
        
        // Show the dialog
        promptConfirmDeleteBuilder.show();
    }
    
    public static class SelectionActionModeCallback implements ActionMode.Callback {
        
        private ActivityList activityList;
        
        public SelectionActionModeCallback(ActivityList activityList) {
            this.activityList = activityList;
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate selection action mode menu
            mode.getMenuInflater().inflate(R.menu.action_mode_select_activity_list, menu);
            
            // Update
            this.update(mode, menu);
            
            return true;
        }
        
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            this.update(mode, menu);
            
            return true;
        }
        
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Switch against menu item ID
            switch (item.getItemId()) {
            case R.id.activityListActionModeSelectionDelete:
                Set<File> noteFiles = new HashSet<>();
                for (int selectionIndex : this.activityList.listAdapter.getNoteSelectionSet()) {
                    noteFiles.add(this.activityList.listAdapter.getNotePreviewList().get(selectionIndex).getFile());
                }
                this.activityList.promptDeleteNoteFiles(noteFiles);
                break;
            }
            
            return true;
        }
        
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Clear selection and update adapter
            this.activityList.listAdapter.selecting = false;
            this.activityList.listAdapter.getNoteSelectionSet().clear();
            this.activityList.listAdapter.notifyDataSetChanged();
            
            // Suicide by garbage collector
            this.activityList.actionModeSelect = null;
        }
        
        private void update(ActionMode mode, Menu menu) {
            // Update title
            mode.setTitle(this.activityList.listAdapter.getNoteSelectionSet().size() + " note" + (this.activityList.listAdapter.getNoteSelectionSet().size() == 1 ? "" : "s"));
        }
        
    }
    
    public static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        
        private static final String STATE_KEY_SELECTING = "list_adapter_selecting";
        private static final String STATE_KEY_NOTE_SELECTION_SET = "list_adapter_note_selection_set";
        
        private ActivityList activityList;
        
        private boolean selecting;
        
        private List<NotePreview> notePreviewList;
        private Set<Integer> noteSelectionSet;
        
        private NotePreviewClickListener notePreviewClickListener;
        
        public ListAdapter(ActivityList activityList) {
            this.activityList = activityList;
            
            this.selecting = false;
            
            this.notePreviewList = new ArrayList<>();
            this.noteSelectionSet = new HashSet<>();
            
            this.notePreviewClickListener = null;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(this.activityList).inflate(R.layout.activity_list_list_item, viewGroup, false);
            
            // Use state animations if supported
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setStateListAnimator(AnimatorInflater.loadStateListAnimator(this.activityList, R.anim.state_list_activity_list_list_item));
            }
            
            return new ListAdapter.ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            // Get note preview info
            final NotePreview notePreview = this.notePreviewList.get(i);
            
            // Set note preview info text
            viewHolder.getTextViewTitle().setText(notePreview.getTitle());
            viewHolder.getTextViewContentPreview().setText(notePreview.getContent());
            
            // Set selection
            viewHolder.setSelected(this.noteSelectionSet.contains(i));
            
            // Handle clicks
            viewHolder.getView().setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    // If selecting
                    if (ListAdapter.this.selecting) {
                        // Toggle selection
                        if (ListAdapter.this.noteSelectionSet.contains(i)) {
                            // Remove from selection
                            ListAdapter.this.noteSelectionSet.remove(i);
                            ListAdapter.this.activityList.actionModeSelect.invalidate();
                            viewHolder.setSelected(false);
                            
                            // If selection empty
                            if (ListAdapter.this.noteSelectionSet.isEmpty()) {
                                // Stop selecting
                                ListAdapter.this.selecting = false;
                                
                                // Finish select action mode
                                ListAdapter.this.activityList.actionModeSelect.finish();
                            }
                        } else {
                            // Add to selection
                            ListAdapter.this.noteSelectionSet.add(i);
                            ListAdapter.this.activityList.actionModeSelect.invalidate();
                            viewHolder.setSelected(true);
                        }
                        
                        return;
                    }
                    
                    // Invoke click listener
                    ListAdapter.this.notePreviewClickListener.onNotePreviewClick(notePreview);
                }
                
            });
            
            // Handle long clicks
            viewHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    // If not selecting
                    if (!ListAdapter.this.selecting) {
                        // Begin selecting
                        ListAdapter.this.selecting = true;
                        
                        // Start select action mode
                        ListAdapter.this.activityList.actionModeSelect = ListAdapter.this.activityList.startSupportActionMode(new SelectionActionModeCallback(ListAdapter.this.activityList));
                        
                        // Add to selection
                        ListAdapter.this.noteSelectionSet.add(i);
                        ListAdapter.this.activityList.actionModeSelect.invalidate();
                        viewHolder.setSelected(true);
                        
                        return true;
                    }
                    
                    return false;
                }
                
            });
        }
        
        @Override
        public int getItemCount() {
            return this.notePreviewList.size();
        }
        
        public List<NotePreview> getNotePreviewList() {
            return this.notePreviewList;
        }
        
        public Set<Integer> getNoteSelectionSet() {
            return this.noteSelectionSet;
        }
        
        public void saveState(Bundle outState) {
            // Save selecting state
            outState.putBoolean(STATE_KEY_SELECTING, this.selecting);
            
            // Save note selection set
            outState.putIntegerArrayList(STATE_KEY_NOTE_SELECTION_SET, new ArrayList<>(this.noteSelectionSet));
        }
        
        public void restoreState(Bundle inState) {
            // Restore selecting state
            this.selecting = inState.getBoolean(STATE_KEY_SELECTING);
            
            // Restore note selection set
            List<Integer> noteSelectionList = inState.getIntegerArrayList(STATE_KEY_NOTE_SELECTION_SET);
            if (noteSelectionList != null) {
                this.noteSelectionSet = new HashSet<>(noteSelectionList);
            }
        }
        
        public void setNotePreviewClickListener(NotePreviewClickListener notePreviewClickListener) {
            this.notePreviewClickListener = notePreviewClickListener;
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            
            private View view;
            private TextView textViewTitle;
            private TextView textViewContentPreview;
            
            private boolean selected;
            
            private Drawable backgroundDrawableDefault;
            
            public ViewHolder(View itemView) {
                super(itemView);
                
                this.view = itemView;
                this.textViewTitle = (TextView) itemView.findViewById(R.id.activityListListItemTitle);
                this.textViewContentPreview = (TextView) itemView.findViewById(R.id.activityListListItemContentPreview);
                
                this.selected = false;
                
                this.backgroundDrawableDefault = itemView.getBackground();
            }
            
            public View getView() {
                return this.view;
            }
            
            public TextView getTextViewTitle() {
                return this.textViewTitle;
            }
            
            public TextView getTextViewContentPreview() {
                return this.textViewContentPreview;
            }
            
            public boolean getSelected() {
                return this.selected;
            }
            
            public void setSelected(boolean selected) {
                this.selected = selected;
                
                // Set selected
                this.view.setSelected(selected);
                
                // Choose appropriate background
                Drawable backgroundDrawable;
                if (selected) {
                    StateListDrawable stateListDrawable = new StateListDrawable();
                    stateListDrawable.addState(new int[] { android.R.attr.state_selected, }, new ColorDrawable(this.view.getContext().getResources().getColor(R.color.activity_list_list_item_selected)));
                    stateListDrawable.addState(StateSet.WILD_CARD, null);
                    backgroundDrawable = stateListDrawable;
                } else {
                    backgroundDrawable = this.backgroundDrawableDefault;
                }
                
                // Set background
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    // noinspection deprecation
                    this.view.setBackgroundDrawable(backgroundDrawable);
                } else {
                    this.view.setBackground(backgroundDrawable);
                }
            }
            
        }
        
        public static class NotePreview {
            
            private File file;
            private String title;
            private String content;
            
            public NotePreview(File file, String title, String content) {
                this.file = file;
                this.title = title;
                this.content = content;
            }
            
            public File getFile() {
                return this.file;
            }
            
            public String getTitle() {
                return this.title;
            }
            
            public String getContent() {
                return this.content;
            }
            
        }
        
        public interface NotePreviewClickListener {
            
            void onNotePreviewClick(NotePreview notePreview);
            
        }
        
    }
    
}
