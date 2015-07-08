package com.gmail.tylerfilla.android.notes.activity;

import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.io.NoteIO;
import com.gmail.tylerfilla.android.notes.util.NoteSearcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityList extends AppCompatActivity {
    
    private static final String STATE_KEY_ACTION_MODE_TYPE = "action_mode_select_shown";
    
    private static final int NOTE_PREVIEW_TITLE_MAX_LENGTH = 20;
    private static final int NOTE_PREVIEW_CONTENT_MAX_LENGTH = 50;
    
    private RecyclerView list;
    private ListAdapter listAdapter;
    private RecyclerView.LayoutManager listLayoutManager;
    
    private NoteSearcher noteSearcher;
    private String noteSearcherQuery;
    
    private View messageEmptyList;
    private View messageEmptySearch;
    
    private ActionMode actionMode;
    private EnumActionMode actionModeType;
    
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
        this.listAdapter = new ListAdapter(this);
        this.list.setAdapter(this.listAdapter);
        
        // Create and set layout manager
        this.listLayoutManager = new LinearLayoutManager(this);
        this.list.setLayoutManager(this.listLayoutManager);
        
        // Note searcher and query
        this.noteSearcher = new NoteSearcher();
        this.noteSearcherQuery = "";
        
        // Get references to empty messages
        this.messageEmptyList = this.findViewById(R.id.activityListMessageEmptyList);
        this.messageEmptySearch = this.findViewById(R.id.activityListMessageEmptySearch);
        
        // Action mode state
        this.actionMode = null;
        this.actionModeType = EnumActionMode.NONE;
        
        // List adapter note preview click listener
        this.listAdapter.setNotePreviewClickListener(new ListAdapter.NotePreviewClickListener() {
            
            @Override
            public void onNotePreviewClick(ListAdapter.NotePreview notePreview) {
                // Open note file
                ActivityList.this.openNoteFile(notePreview.getFile());
            }
            
        });
        
        // Configure toolbar
        this.setSupportActionBar((Toolbar) this.findViewById(R.id.activityListToolbar));
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
        
        // Activate action mode by type
        this.activateActionMode(EnumActionMode.fromTypeId(savedInstanceState.getInt(STATE_KEY_ACTION_MODE_TYPE, EnumActionMode.NONE.getTypeId())));
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save list adapter state
        this.listAdapter.saveState(outState);
        
        // Save action mode type
        outState.putInt(STATE_KEY_ACTION_MODE_TYPE, this.actionModeType.getTypeId());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu for actionbar buttons
        this.getMenuInflater().inflate(R.menu.activity_list, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Switch against item ID
        switch (item.getItemId()) {
        case R.id.activityListMenuItemSearch:
            // Begin search
            this.searchBegin();
            break;
        }
        
        return super.onOptionsItemSelected(item);
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
                        title = title.substring(0, Math.min(ActivityList.NOTE_PREVIEW_TITLE_MAX_LENGTH, title.length()));
                    }
                    if (content != null) {
                        // Strip HTML tags
                        content = Html.fromHtml(content).toString().replaceAll("\n+", " ");
                        
                        // Cut length to maximum
                        content = content.substring(0, Math.min(ActivityList.NOTE_PREVIEW_CONTENT_MAX_LENGTH, content.length()));
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
        
        // If list is empty
        if (notePreviewList.isEmpty()) {
            // Choose appropriate empty message
            if (this.actionModeType == EnumActionMode.NONE || this.actionModeType == EnumActionMode.SELECT) {
                this.messageEmptyList.setVisibility(View.VISIBLE);
                this.messageEmptySearch.setVisibility(View.GONE);
            } else if (this.actionModeType == EnumActionMode.SEARCH || this.actionModeType == EnumActionMode.SEARCH_SELECT) {
                this.messageEmptyList.setVisibility(View.GONE);
                this.messageEmptySearch.setVisibility(View.VISIBLE);
            }
        } else {
            this.messageEmptyList.setVisibility(View.GONE);
            this.messageEmptySearch.setVisibility(View.GONE);
        }
        
        // Notify adapter of change
        this.listAdapter.notifyDataSetChanged();
    }
    
    private void searchBegin() {
        // Upgrade action mode appropriately
        if (this.actionModeType == EnumActionMode.NONE) {
            // Upgrade to search action mode
            this.activateActionMode(EnumActionMode.SEARCH);
        }
        
        // Refresh list
        this.refreshList();
        
        // Animate in new button
        this.findViewById(R.id.activityListNewButton).setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                ActivityList.this.findViewById(R.id.activityListNewButton).setVisibility(View.GONE);
            }
            
        }, 500l);
    }
    
    private void searchEnd() {
        // Downgrade action mode appropriately
        if (this.actionModeType == EnumActionMode.SEARCH_SELECT) {
            // Downgrade to select action mode
            this.activateActionMode(EnumActionMode.SELECT);
        } else if (this.actionModeType == EnumActionMode.SEARCH) {
            // Downgrade to no action mode
            this.activateActionMode(EnumActionMode.NONE);
        }
        
        // Refresh list
        this.refreshList();
        
        // Animate in new button
        this.findViewById(R.id.activityListNewButton).setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                ActivityList.this.findViewById(R.id.activityListNewButton).setEnabled(true);
            }
            
        }, 500l);
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
        // Deletion confirmation prompt dialog
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        
        // Dialog title
        prompt.setTitle(R.string.dialog_activity_list_prompt_delete_note_files_title);
        
        // Dialog message
        if (noteFiles.size() == 1) {
            prompt.setMessage(R.string.dialog_activity_list_prompt_delete_note_files_message_single);
        } else if (noteFiles.size() > 1) {
            prompt.setMessage(String.format(this.getString(R.string.dialog_activity_list_prompt_delete_note_files_message_multiple), NumberFormat.getIntegerInstance().format(noteFiles.size())));
        }
        
        // Dialog buttons
        prompt.setNegativeButton(android.R.string.no, null);
        prompt.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                // Downgrade action mode appropriately
                if (ActivityList.this.actionModeType == EnumActionMode.SELECT) {
                    // Finish select action mode
                    ActivityList.this.activateActionMode(EnumActionMode.NONE);
                } else if (ActivityList.this.actionModeType == EnumActionMode.SEARCH_SELECT) {
                    // Drop to search action mode
                    ActivityList.this.activateActionMode(EnumActionMode.SEARCH);
                }
                
                // Iterate over and delete files
                for (File noteFile : noteFiles) {
                    noteFile.delete();
                }
                
                // Show a toast
                if (noteFiles.size() == 1) {
                    Toast.makeText(ActivityList.this, R.string.toast_activity_list_confirm_delete_note_files_single, Toast.LENGTH_SHORT).show();
                } else if (noteFiles.size() > 1) {
                    Toast.makeText(ActivityList.this, String.format(ActivityList.this.getString(R.string.toast_activity_list_confirm_delete_note_files_multiple), NumberFormat.getIntegerInstance().format(noteFiles.size())), Toast.LENGTH_SHORT).show();
                }
                
                // Refresh note list
                ActivityList.this.refreshList();
            }
            
        });
        
        // Show dialog
        prompt.show();
    }
    
    private void activateActionMode(EnumActionMode actionModeType) {
        // If an action mode is already active
        if (this.actionModeType != EnumActionMode.NONE) {
            // Finish action mode
            this.actionMode.finish();
            
            // Nullify action mode state
            this.actionMode = null;
            this.actionModeType = EnumActionMode.NONE;
        }
        
        // If an action mode is desired
        if (actionModeType != EnumActionMode.NONE) {
            // Get callback class
            Class actionModeCallbackClass = actionModeType.getCallbackClass();
            
            // Action mode callback class constructor
            Constructor actionModeCallbackClassConstructor = null;
            
            // Try to get constructor
            try {
                actionModeCallbackClassConstructor = actionModeCallbackClass.getConstructor(ActivityList.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            
            // If constructor retrieved
            if (actionModeCallbackClassConstructor != null) {
                // Action mode callback
                ActionMode.Callback actionModeCallback = null;
                
                // Try to instantiate callback class
                try {
                    actionModeCallback = (ActionMode.Callback) actionModeCallbackClassConstructor.newInstance(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                
                // If instantiation succeeded
                if (actionModeCallback != null) {
                    // Start action mode
                    this.actionMode = this.startSupportActionMode(actionModeCallback);
                    
                    // Save action mode type
                    this.actionModeType = actionModeType;
                }
            }
        } else {
            // Nullify action mode state
            this.actionMode = null;
            this.actionModeType = EnumActionMode.NONE;
        }
    }
    
    public static abstract class ActionModeCallback implements ActionMode.Callback {
        
        protected ActivityList activityList;
        
        protected ActionModeCallback(ActivityList activityList) {
            this.activityList = activityList;
        }
        
    }
    
    public static class ActionModeCallbackSelect extends ActionModeCallback {
        
        public ActionModeCallbackSelect(ActivityList activityList) {
            super(activityList);
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate selection action mode menu
            mode.getMenuInflater().inflate(R.menu.activity_list_action_mode_select, menu);
            
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
            case R.id.activityListMenuActionModeSelectItemDelete:
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
            
            // Nullify action mode state
            this.activityList.actionMode = null;
            this.activityList.actionModeType = EnumActionMode.NONE;
        }
        
        private void update(ActionMode mode, Menu menu) {
            // Update title
            int numSelected = this.activityList.listAdapter.getNoteSelectionSet().size();
            if (numSelected == 1) {
                mode.setTitle(R.string.activity_list_action_mode_select_title_single);
            } else if (numSelected > 1) {
                mode.setTitle(String.format(this.activityList.getString(R.string.activity_list_action_mode_select_title_multiple), NumberFormat.getIntegerInstance().format(numSelected)));
            }
            
            // Clear subtitle
            mode.setSubtitle("");
        }
        
    }
    
    public static class ActionModeCallbackSearch extends ActionModeCallback {
        
        public ActionModeCallbackSearch(ActivityList activityList) {
            super(activityList);
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate search action mode menu
            mode.getMenuInflater().inflate(R.menu.activity_list_action_mode_search, menu);
            
            // Get reference to search view
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.activityListMenuActionModeSearchSearchView));
            
            // Expand and focus search view immediately
            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.setIconifiedByDefault(false);
            searchView.requestFocusFromTouch();
            
            // Get reference to search view query input
            EditText searchViewQueryInput = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            
            // Set search view query input hint text color
            searchViewQueryInput.setHintTextColor(Color.GRAY);
            
            // Make search view query input not long-clickable
            searchViewQueryInput.setLongClickable(false);
            
            // Set search view query input text
            searchViewQueryInput.setText(this.activityList.noteSearcherQuery);
            
            // Add text changed listener to query input
            searchViewQueryInput.addTextChangedListener(new TextWatcher() {
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Set query
                    ActionModeCallbackSearch.this.activityList.noteSearcherQuery = s.toString();
                    
                    // Refresh note list
                    ActionModeCallbackSearch.this.activityList.refreshList();
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                }
                
            });
            
            return true;
        }
        
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }
        
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }
        
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Nullify action mode state
            this.activityList.actionMode = null;
            this.activityList.actionModeType = EnumActionMode.NONE;
            
            // End search
            this.activityList.searchEnd();
            
            // Hide soft keyboard from search view
            ((InputMethodManager) this.activityList.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(MenuItemCompat.getActionView(mode.getMenu().findItem(R.id.activityListMenuActionModeSearchSearchView)).getWindowToken(), 0);
        }
        
    }
    
    public static class ActionModeCallbackSearchSelect extends ActionModeCallback {
        
        public ActionModeCallbackSearchSelect(ActivityList activityList) {
            super(activityList);
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate selection action mode menu
            mode.getMenuInflater().inflate(R.menu.activity_list_action_mode_select, menu);
            
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
            case R.id.activityListMenuActionModeSelectItemDelete:
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
            
            // Nullify action mode state
            this.activityList.actionMode = null;
            this.activityList.actionModeType = EnumActionMode.NONE;
            
            // End search
            this.activityList.searchEnd();
        }
        
        private void update(ActionMode mode, Menu menu) {
            // Update title
            int numSelected = this.activityList.listAdapter.getNoteSelectionSet().size();
            if (numSelected == 1) {
                mode.setTitle(R.string.activity_list_action_mode_search_select_title_single);
            } else if (numSelected > 1) {
                mode.setTitle(String.format(this.activityList.getString(R.string.activity_list_action_mode_search_select_title_multiple), NumberFormat.getIntegerInstance().format(numSelected)));
            }
            
            // Update subtitle
            mode.setSubtitle(String.format(this.activityList.getString(R.string.activity_list_action_mode_search_select_subtitle), this.activityList.noteSearcherQuery));
        }
        
    }
    
    private enum EnumActionMode {
        
        NONE(0, null),
        SELECT(1, ActionModeCallbackSelect.class),
        SEARCH(2, ActionModeCallbackSearch.class),
        SEARCH_SELECT(3, ActionModeCallbackSearchSelect.class);
        
        private int typeId;
        private Class callbackClass;
        
        EnumActionMode(int typeId, Class callbackClass) {
            this.typeId = typeId;
            this.callbackClass = callbackClass;
        }
        
        public int getTypeId() {
            return this.typeId;
        }
        
        public Class getCallbackClass() {
            return this.callbackClass;
        }
        
        public static EnumActionMode fromTypeId(int typeId) {
            for (EnumActionMode actionModeType : values()) {
                if (actionModeType.typeId == typeId) {
                    return actionModeType;
                }
            }
            
            return NONE;
        }
        
        public static EnumActionMode fromCallbackClass(Class callbackClass) {
            for (EnumActionMode actionModeType : values()) {
                if (actionModeType.callbackClass == callbackClass) {
                    return actionModeType;
                }
            }
            
            return NONE;
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
                            viewHolder.setSelected(false);
                            
                            // Invalidate action mode
                            if (ListAdapter.this.activityList.actionMode != null) {
                                ListAdapter.this.activityList.actionMode.invalidate();
                            }
                            
                            // If selection empty
                            if (ListAdapter.this.noteSelectionSet.isEmpty()) {
                                // Stop selecting
                                ListAdapter.this.selecting = false;
                                
                                // Downgrade action mode appropriately
                                if (ListAdapter.this.activityList.actionModeType == EnumActionMode.SELECT) {
                                    // Finish select action mode
                                    ListAdapter.this.activityList.activateActionMode(EnumActionMode.NONE);
                                } else if (ListAdapter.this.activityList.actionModeType == EnumActionMode.SEARCH_SELECT) {
                                    // Drop to search action mode
                                    ListAdapter.this.activityList.activateActionMode(EnumActionMode.SEARCH);
                                }
                            }
                        } else {
                            // Add to selection
                            ListAdapter.this.noteSelectionSet.add(i);
                            viewHolder.setSelected(true);
                            
                            // Invalidate action mode
                            if (ListAdapter.this.activityList.actionMode != null) {
                                ListAdapter.this.activityList.actionMode.invalidate();
                            }
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
                        
                        // Upgrade action mode appropriately
                        if (ListAdapter.this.activityList.actionModeType == EnumActionMode.NONE) {
                            // Activate select action mode
                            ListAdapter.this.activityList.activateActionMode(EnumActionMode.SELECT);
                        } else if (ListAdapter.this.activityList.actionModeType == EnumActionMode.SEARCH) {
                            // If search query is empty
                            if (ListAdapter.this.activityList.noteSearcherQuery.isEmpty()) {
                                // Activate select action mode
                                ListAdapter.this.activityList.activateActionMode(EnumActionMode.SELECT);
                            } else {
                                // Activate search select action mode
                                ListAdapter.this.activityList.activateActionMode(EnumActionMode.SEARCH_SELECT);
                            }
                        }
                        
                        // Add to selection
                        ListAdapter.this.noteSelectionSet.add(i);
                        viewHolder.setSelected(true);
                        
                        // Invalidate action mode
                        if (ListAdapter.this.activityList.actionMode != null) {
                            ListAdapter.this.activityList.actionMode.invalidate();
                        }
                        
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
            
            private Drawable defaultBackground;
            private int defaultPadding;
            
            public ViewHolder(View itemView) {
                super(itemView);
                
                this.view = itemView;
                this.textViewTitle = (TextView) itemView.findViewById(R.id.activityListListItemTitle);
                this.textViewContentPreview = (TextView) itemView.findViewById(R.id.activityListListItemContentPreview);
                
                this.selected = false;
                
                this.defaultBackground = itemView.getBackground();
                this.defaultPadding = itemView.getPaddingLeft();
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
                    backgroundDrawable = this.defaultBackground;
                }
                
                // Set background and padding
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                    this.view.setBackgroundDrawable(backgroundDrawable);
                    this.view.setPadding(this.defaultPadding, this.defaultPadding, this.defaultPadding, this.defaultPadding);
                } else {
                    this.view.setBackground(backgroundDrawable);
                    this.view.setPadding(this.defaultPadding, this.defaultPadding, this.defaultPadding, this.defaultPadding);
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
