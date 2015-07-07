package com.gmail.tylerfilla.android.notes.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.NoteEditor;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.io.NoteIO;

import java.io.File;
import java.io.IOException;

public class ActivityEdit extends AppCompatActivity {
    
    private static final int NOTE_TITLE_MAX_LENGTH = 20;
    
    private Note note;
    private File noteFile;
    
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
            
            // Create a new note file
            this.noteFile = new File(NoteIO.getNoteStoreDirectory(this), "_" + String.valueOf(System.currentTimeMillis()) + ".note");
            
            // Ensure the filename is unique
            while (this.noteFile.exists()) {
                this.noteFile = new File(this.noteFile.getParentFile(), "_" + this.noteFile.getName());
            }
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
            if (!isDescendant && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_import_enable", true)) {
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
                    prompt.setTitle("Read Failed");
                    prompt.setMessage("Unable to read note file at " + this.noteFile.getAbsolutePath() + " due to the following exception: " + e.getMessage());
                    
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
        
        // Update task description
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTaskDescription(new ActivityManager.TaskDescription(title, null, this.getTheme().obtainStyledAttributes(R.style.MicroNote_Theme, new int[] { R.attr.colorPrimary }).getColor(0, 0)));
        }
    }
    
    private void handlePromptImport(boolean doImport, boolean stop) {
        if (stop) {
            // Save preference
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("pref_import_enable", false).apply();
            
            // Notification dialog
            AlertDialog.Builder prompt = new AlertDialog.Builder(this);
            
            // Dialog title and message
            prompt.setTitle("Import Disabled");
            prompt.setMessage("You can re-enable this feature in the settings menu.");
            
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
    
    private void promptImport() {
        // Import prompt dialog
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        
        // Dialog title and message
        prompt.setTitle("Import Note");
        prompt.setMessage("Would you like to import a copy of this note?");
        
        // Stop checkbox
        final CheckBox promptStopCheckBox = new CheckBox(this);
        promptStopCheckBox.setText("Stop asking to import notes");
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
        prompt.setTitle("Rename");
        prompt.setMessage("Please enter a new title below.");
        
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
        promptInputTitleLayoutParams.setMargins(dpToPxInt(24), dpToPxInt(20), dpToPxInt(24), dpToPxInt(24));
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
    
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.getResources().getDisplayMetrics());
    }
    
    private int dpToPxInt(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, this.getResources().getDisplayMetrics());
    }
    
    public static class EditorFragment extends Fragment {
        
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
                
                // Pass note to editor
                this.noteEditor.setNote(((ActivityEdit) this.getActivity()).note);
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
                        if (this.noteEditor.getNote().getLastModified() == 0l) {
                            this.noteEditor.getNote().setTitle(contentDetaggedDewhited.substring(0, Math.min(contentDetaggedDewhited.length(), contentDetaggedDewhited.contains("\n") ? Math.min(NOTE_TITLE_MAX_LENGTH, contentDetaggedDewhited.indexOf('\n')) : NOTE_TITLE_MAX_LENGTH)));
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
            
            // Pause note editor
            this.noteEditor.onPause();
        }
        
    }
    
}
