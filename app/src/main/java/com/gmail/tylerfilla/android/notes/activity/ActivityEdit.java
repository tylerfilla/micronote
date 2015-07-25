package com.gmail.tylerfilla.android.notes.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.core.io.NoteIO;
import com.gmail.tylerfilla.android.notes.util.DimenUtil;
import com.gmail.tylerfilla.android.notes.widget.NoteEditor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivityEdit extends AppCompatActivity {
    
    private static final String STATE_KEY_COVER_VISIBILITY = "cover_visibility";
    
    private static final int NOTE_TITLE_MAX_LENGTH = 20;
    
    private Note note;
    private File noteFile;
    private boolean noteFileDeleted;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref, false);
        
        // Attempt to read note described by intent
        Uri noteFileUri = this.getIntent().getData();
        if (noteFileUri == null) {
            // Create a new note object
            this.note = new Note();
            
            // Set default title
            this.note.setTitle(this.getString(R.string.activity_edit_constant_default_note_title));
            
            // Create a new note file
            this.noteFile = new File(NoteIO.getNoteStoreDirectory(this), "_" + String.valueOf(System.currentTimeMillis()) + ".note");
            
            // Ensure the filename is unique
            while (this.noteFile.exists()) {
                this.noteFile = new File(this.noteFile.getParentFile(), "_" + this.noteFile.getName());
            }
            
            // This is a new note
            this.note.setChanged(false);
        } else {
            // Get the note file
            this.noteFile = new File(noteFileUri.getPath());
            
            // Check location of note file for import purposes
            boolean isDescendant = false;
            File parentFile = this.noteFile;
            while ((parentFile = parentFile.getParentFile()) != null) {
                if (this.getFilesDir().equals(parentFile)) {
                    isDescendant = true;
                }
            }
            
            // If the note file is unmanaged and importing is enabled
            if (!isDescendant && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_other_import_enable", true)) {
                this.promptImport();
            }
            
            // If note file exists
            if (this.noteFile.exists()) {
                // Try to read note file
                try {
                    this.note = NoteIO.read(this.noteFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    
                    // Notification dialog
                    AlertDialog.Builder prompt = new AlertDialog.Builder(this);
                    
                    // Dialog title and message
                    prompt.setTitle(R.string.dialog_activity_edit_alert_note_file_read_failed_title);
                    prompt.setMessage(String.format(this.getString(R.string.dialog_activity_edit_alert_note_file_read_failed_message), this.noteFile.getAbsolutePath(), e.getMessage()));
                    
                    // Dialog buttons
                    prompt.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityEdit.this.finish();
                        }
                        
                    });
                    
                    // Show dialog
                    prompt.show();
                }
            }
        }
        
        // Set content view
        this.setContentView(R.layout.activity_edit);
        
        // Configure toolbar
        this.setSupportActionBar((Toolbar) this.findViewById(R.id.activityEditToolbar));
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Update toolbar title
        this.getSupportActionBar().setTitle(this.note.getTitle());
        
        // Update task description title
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(this.note.getTitle(), null, this.getTheme().obtainStyledAttributes(R.style.MicroNote_Theme, new int[] { R.attr.colorPrimary }).getColor(0, 0)));
        }
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        // Restore cover visibility
        this.findViewById(R.id.activityEditCover).setVisibility(savedInstanceState.getBoolean(STATE_KEY_COVER_VISIBILITY, true) ? View.VISIBLE : View.GONE);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save cover visibility
        outState.putBoolean(STATE_KEY_COVER_VISIBILITY, this.findViewById(R.id.activityEditCover).getVisibility() == View.VISIBLE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu for actionbar buttons
        this.getMenuInflater().inflate(R.menu.activity_edit, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Switch against item ID
        switch (item.getItemId()) {
        case android.R.id.home:
            // Call back button press handler
            this.onBackPressed();
            break;
        case R.id.activityEditMenuItemRename:
            // Rename note
            this.promptRename();
            break;
        case R.id.activityEditMenuItemDelete:
            // Delete note
            this.promptDelete();
            break;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void finish() {
        // Choose and call through to appropriate finish method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        } else {
            super.finish();
        }
    }
    
    private void importNote() {
        File newNoteFile = new File(NoteIO.getNoteStoreDirectory(this), "_import_" + this.noteFile.getName());
        
        // Ensure the name is unique
        while (newNoteFile.exists()) {
            newNoteFile = new File(newNoteFile.getParentFile(), "_" + newNoteFile.getName());
        }
        
        // Copy note data to new file
        try {
            NoteIO.write(NoteIO.read(this.noteFile), newNoteFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Redirect all modifications to this new file
        this.noteFile = newNoteFile;
    }
    
    private void renameNote(String title) {
        // Set note title
        this.note.setTitle(title);
        
        // Update toolbar title
        this.getSupportActionBar().setTitle(this.note.getTitle());
        
        // Update task description title
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(this.note.getTitle(), null, this.getTheme().obtainStyledAttributes(R.style.MicroNote_Theme, new int[] { R.attr.colorPrimary }).getColor(0, 0)));
        }
    }
    
    private void deleteNote() {
        // Delete note file
        this.noteFile.delete();
        
        // Set deletion flag
        this.noteFileDeleted = true;
        
        // Finish normally
        this.finish();
    }
    
    private void handlePromptImport(boolean doImport, boolean stop) {
        if (stop) {
            // Save preference
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("pref_other_import_enable", false).apply();
            
            // Notification dialog
            AlertDialog.Builder prompt = new AlertDialog.Builder(this);
            
            // Dialog title and message
            prompt.setTitle(R.string.dialog_activity_edit_alert_note_file_import_disabled_title);
            prompt.setMessage(R.string.dialog_activity_edit_alert_note_file_import_disabled_message);
            
            // Dialog buttons
            prompt.setPositiveButton(android.R.string.ok, null);
            
            // Show dialog
            prompt.show();
        }
        
        // If prompt result was positive
        if (doImport) {
            this.importNote();
        }
    }
    
    private void handlePromptRename(String title) {
        // Rename note
        this.renameNote(title);
    }
    
    private void handlePromptDelete(boolean delete) {
        if (delete) {
            this.deleteNote();
        }
    }
    
    private void promptImport() {
        // Import prompt dialog
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        
        // Dialog title and message
        prompt.setTitle(R.string.dialog_activity_edit_prompt_note_file_import_title);
        prompt.setMessage(R.string.dialog_activity_edit_prompt_note_file_import_message);
        
        // Stop checkbox
        final CheckBox promptStopCheckBox = new CheckBox(this);
        promptStopCheckBox.setText(R.string.dialog_activity_edit_prompt_note_file_import_stop_text);
        prompt.setView(promptStopCheckBox);
        
        // Dialog buttons
        prompt.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ActivityEdit.this.handlePromptImport(false, promptStopCheckBox.isChecked());
            }
            
        });
        prompt.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ActivityEdit.this.handlePromptImport(true, promptStopCheckBox.isChecked());
            }
            
        });
        
        // Show dialog
        prompt.show();
    }
    
    private void promptRename() {
        // Rename prompt dialog
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        
        // Dialog title and message
        prompt.setTitle(R.string.dialog_activity_edit_prompt_rename_title);
        prompt.setMessage(R.string.dialog_activity_edit_prompt_rename_message);
        
        // Title textbox holder
        FrameLayout promptInputTitleHolder = new FrameLayout(this);
        prompt.setView(promptInputTitleHolder);
        
        // Title textbox
        final EditText promptInputTitle = new EditText(this);
        promptInputTitle.setMaxLines(1);
        promptInputTitle.setHint(this.note.getTitle());
        promptInputTitle.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        promptInputTitle.setFilters(new InputFilter[] { new InputFilter.LengthFilter(NOTE_TITLE_MAX_LENGTH) });
        promptInputTitleHolder.addView(promptInputTitle);
        
        // Title textbox layout parameters
        FrameLayout.LayoutParams promptInputTitleLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        promptInputTitleLayoutParams.setMargins(DimenUtil.dpToPxInt(this, 24), DimenUtil.dpToPxInt(this, 20), DimenUtil.dpToPxInt(this, 24), DimenUtil.dpToPxInt(this, 24));
        promptInputTitle.setLayoutParams(promptInputTitleLayoutParams);
        
        // Dialog buttons
        prompt.setNegativeButton(android.R.string.cancel, null);
        prompt.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = promptInputTitle.getText().toString();
                
                if (!title.isEmpty()) {
                    ActivityEdit.this.handlePromptRename(title);
                }
            }
            
        });
        
        // Show dialog
        prompt.show();
    }
    
    private void promptDelete() {
        // Deletion confirmation prompt dialog
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        
        // Dialog title and message
        prompt.setTitle(R.string.dialog_activity_edit_prompt_delete_note_file_title);
        prompt.setMessage(R.string.dialog_activity_edit_prompt_delete_note_file_message);
        
        // Dialog buttons
        prompt.setNegativeButton(android.R.string.no, null);
        prompt.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ActivityEdit.this.handlePromptDelete(true);
            }
            
        });
        
        // Show dialog
        prompt.show();
    }
    
    public static class FragmentEditor extends Fragment {
        
        private NoteEditor noteEditor;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Retain instance of this fragment
            this.setRetainInstance(true);
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // If note editor not already created
            if (this.noteEditor == null) {
                // Create note editor
                this.noteEditor = new NoteEditor(this.getActivity().getApplicationContext());
                
                // Load context
                this.loadContext();
                
                // Pass note to editor
                this.noteEditor.setNote(((ActivityEdit) this.getActivity()).note);
                
                // Set initialized listener
                this.noteEditor.setOnInitializedListener(new NoteEditor.OnInitializedListener() {
                    
                    @Override
                    public void onInitialized() {
                        // Get reference to edit activity cover
                        final View activityEditCover = FragmentEditor.this.getActivity().findViewById(R.id.activityEditCover);
                        
                        // Get fade duration
                        int fadeDuration = FragmentEditor.this.getActivity().getResources().getInteger(android.R.integer.config_mediumAnimTime);
                        
                        // Animate cover out
                        AlphaAnimation fade = new AlphaAnimation(1.0f, 0.0f);
                        fade.setDuration(fadeDuration);
                        fade.setFillAfter(true);
                        activityEditCover.startAnimation(fade);
                        
                        // Handler to sync with animation completion
                        new Handler().postDelayed(new Runnable() {
                            
                            @Override
                            public void run() {
                                // Hide cover
                                activityEditCover.setVisibility(View.GONE);
                                
                                // If note is new, simulate touch to naturally focus editor
                                if (FragmentEditor.this.noteEditor.getNote().getLastModified() == 0l) {
                                    // Calculate simulated touch point coordinates
                                    float x = (FragmentEditor.this.noteEditor.getLeft() + FragmentEditor.this.noteEditor.getRight())/2.0f;
                                    float y = (FragmentEditor.this.noteEditor.getTop() + FragmentEditor.this.noteEditor.getBottom())/2.0f;
                                    
                                    // Generate events
                                    MotionEvent eventDown = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0);
                                    MotionEvent eventUp = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0);
                                    
                                    // Dispatch events
                                    FragmentEditor.this.noteEditor.onTouchEvent(eventDown);
                                    FragmentEditor.this.noteEditor.onTouchEvent(eventUp);
                                    
                                    // Recycle events
                                    eventDown.recycle();
                                    eventUp.recycle();
                                }
                            }
                            
                        }, fadeDuration);
                    }
                    
                });
            }
            
            return this.noteEditor;
        }
        
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            
            // Remove note editor from its current parent
            ((ViewGroup) this.noteEditor.getParent()).removeView(this.noteEditor);
        }
        
        @Override
        public void onDetach() {
            super.onDetach();
            
            /* Some hacky stuff to remove focus from the editor */
            
            // Hide soft keyboard
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.noteEditor.getWindowToken(), 0);
            
            // Clear editor focus
            this.noteEditor.loadUrl("javascript:contentText.blur();");
        }
        
        @Override
        public void onResume() {
            super.onResume();
            
            // Resume note editor
            this.noteEditor.onResume();
        }
        
        @Override
        public void onPause() {
            super.onPause();
            
            // If activity is finishing
            if (this.getActivity().isFinishing()) {
                // Unload note editor
                this.noteEditor.unload();
                
                // If note file hasn't been deleted
                if (!((ActivityEdit) this.getActivity()).noteFileDeleted) {
                    // If changes occurred
                    if (this.noteEditor.getNote().getChanged()) {
                        // Get reference to content
                        String content = this.noteEditor.getNote().getContent();
                        
                        // Strip HTML tags
                        String contentDetagged = Html.fromHtml(content).toString();
                        
                        // Strip whitespace
                        String contentDetaggedDewhited = contentDetagged.replaceAll("\\s+", "").trim();
                        
                        // One of those weird edge cases
                        if (!contentDetaggedDewhited.isEmpty()) {
                            // Create title from first line for new notes
                            if (this.noteEditor.getNote().getLastModified() == 0l && this.noteEditor.getNote().getTitle().equals(this.getString(R.string.activity_edit_constant_default_note_title))) {
                                this.noteEditor.getNote().setTitle(contentDetagged.substring(0, Math.min(contentDetagged.length(), contentDetagged.contains("\n") ? Math.min(NOTE_TITLE_MAX_LENGTH, contentDetagged.indexOf('\n')) : NOTE_TITLE_MAX_LENGTH)));
                            }
                            
                            // Attempt to write note
                            try {
                                NoteIO.write(this.noteEditor.getNote(), ((ActivityEdit) this.getActivity()).noteFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            // Pause note editor
            this.noteEditor.onPause();
        }
        
        private void loadContext() {
            // Load configuration
            this.loadConfiguration();
            
            // Load res
            this.loadRes();
        }
        
        private void loadConfiguration() {
            // Get preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            
            // Get configuration
            NoteEditor.Configuration configuration = this.noteEditor.getConfiguration();
            
            // Modify configuration
            configuration.textColor = preferences.getInt("pref_style_notepad_text_color", 0);
            configuration.showNotepadLines = preferences.getBoolean("pref_style_notepad_show_lines", false);
            configuration.formatDate = NoteEditor.Configuration.EnumFormatDate.valueOf(preferences.getString("pref_timedate_format_date", null));
            configuration.formatTime = NoteEditor.Configuration.EnumFormatTime.valueOf(preferences.getString("pref_timedate_format_time", null));
            configuration.timestampScheme = NoteEditor.Configuration.EnumTimestampScheme.valueOf(preferences.getString("pref_timedate_scheme_note_timestamp", null));
            configuration.locale = Locale.getDefault();
            
            // Set configuration
            this.noteEditor.setConfiguration(configuration);
        }
        
        private void loadRes() {
            // Get res
            Map<String, Object> res = this.noteEditor.getRes();
            
            // Integers
            Map<String, Integer> integer = new HashMap<>();
            for (Field field : R.integer.class.getDeclaredFields()) {
                try {
                    integer.put(field.getName(), this.getActivity().getResources().getInteger(field.getInt(null)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            res.put("integer", integer);
            
            // Strings
            Map<String, String> string = new HashMap<>();
            for (Field field : R.string.class.getDeclaredFields()) {
                try {
                    string.put(field.getName(), this.getActivity().getResources().getString(field.getInt(null)).replaceAll("\\\"", "\\\\u0022"));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            res.put("string", string);
            
            // Set res
            this.noteEditor.setRes(res);
        }
        
    }
    
}
