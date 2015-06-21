package com.gmail.tylerfilla.android.notes.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.tylerfilla.android.notes.R;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ActivityList extends Activity {
    
    private RecyclerView list;
    private ActivityList.ListAdapter listAdapter;
    private RecyclerView.LayoutManager listLayoutManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
    }
    
    public static class ListAdapter extends RecyclerView.Adapter<ActivityList.ListAdapter.ViewHolder> {
        
        private Context context;
        
        private Set<File> noteFileSet;
        
        public ListAdapter(Context context) {
            this.context = context;
            
            this.noteFileSet = new HashSet<>();
        }
        
        @Override
        public ActivityList.ListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return null;
        }
        
        @Override
        public void onBindViewHolder(ActivityList.ListAdapter.ViewHolder viewHolder, int i) {
        }
        
        @Override
        public int getItemCount() {
            return 0;
        }
        
        public Set<File> getNoteFileSet() {
            return this.noteFileSet;
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            
            private TextView textViewTitle;
            private TextView textViewContentPreview;
            
            public ViewHolder(View itemView) {
                super(itemView);
                
                this.textViewTitle = null;
                this.textViewContentPreview = null;
            }
            
            public TextView getTextViewTitle() {
                return this.textViewTitle;
            }
            
            public void setTextViewTitle(TextView textViewTitle) {
                this.textViewTitle = textViewTitle;
            }
            
            public TextView getTextViewContentPreview() {
                return this.textViewContentPreview;
            }
            
            public void setTextViewContentPreview(TextView textViewContentPreview) {
                this.textViewContentPreview = textViewContentPreview;
            }
            
        }
        
    }
    
}
