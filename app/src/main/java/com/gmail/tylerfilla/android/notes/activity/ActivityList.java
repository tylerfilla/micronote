package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.Note;
import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.io.NoteIO;
import com.gmail.tylerfilla.android.notes.util.NoteSearcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityList extends Activity {
    
    private static final int NOTE_PREVIEW_TITLE_MAX = 20;
    private static final int NOTE_PREVIEW_CONTENT_MAX = 50;
    
    private File noteFileDir;
    
    private RecyclerView list;
    private ActivityList.ListAdapter listAdapter;
    private RecyclerView.LayoutManager listLayoutManager;
    
    private NoteSearcher noteSearcher;
    private String noteSearcherQuery;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set content view
        this.setContentView(R.layout.activity_list);
        
        // Note file directory
        this.noteFileDir = new File(this.getFilesDir(), "notes");
        this.noteFileDir.mkdirs();
        
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
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh list
        this.refreshList();
    }
    
    private void refreshList() {
        // Get final reference to note preview list
        final List<ListAdapter.NotePreview> notePreviewList = this.listAdapter.getNotePreviewList();
        
        // Clear note preview list
        notePreviewList.clear();
        
        // List note files to searcher
        this.noteSearcher.setFileList(Arrays.asList(this.noteFileDir.listFiles()));
        
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
                    
                    // If there exists a note preview to remove
                    if (killMe != null) {
                        notePreviewList.remove(killMe);
                    }
                }
            }
            
        });
        
        // Execute search
        this.noteSearcher.search(this.noteSearcherQuery);
        
        // Notify adapter of change
        this.listAdapter.notifyDataSetChanged();
    }
    
    public static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        
        private Context context;
        
        private boolean selecting;
        
        private List<NotePreview> notePreviewList;
        private Set<Integer> noteSelectionSet;
        
        public ListAdapter(Context context) {
            this.context = context;
            
            this.selecting = false;
            
            this.notePreviewList = new ArrayList<>();
            this.noteSelectionSet = new HashSet<>();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ListAdapter.ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.activity_list_list_item, viewGroup, false));
        }
        
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int i) {
            // Get note preview info
            NotePreview notePreview = this.notePreviewList.get(i);
            
            // Set note preview info text
            viewHolder.getTextViewTitle().setText(notePreview.getTitle());
            viewHolder.getTextViewContentPreview().setText(notePreview.getContent());
            
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
                            
                            // If selection empty
                            if (ListAdapter.this.noteSelectionSet.isEmpty()) {
                                // Stop selecting
                                ListAdapter.this.selecting = false;
                            }
                        } else {
                            ListAdapter.this.noteSelectionSet.add(i);
                        }
                        
                        return;
                    }
                    
                    // TODO: Open note
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
                        
                        // Add to selection
                        ListAdapter.this.noteSelectionSet.add(i);
                        
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
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            
            private View view;
            private TextView textViewTitle;
            private TextView textViewContentPreview;
            
            public ViewHolder(View itemView) {
                super(itemView);
                
                this.view = itemView;
                this.textViewTitle = (TextView) itemView.findViewById(R.id.activityListListItemTitle);
                this.textViewContentPreview = (TextView) itemView.findViewById(R.id.activityListListItemContentPreview);
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
        
    }
    
}
