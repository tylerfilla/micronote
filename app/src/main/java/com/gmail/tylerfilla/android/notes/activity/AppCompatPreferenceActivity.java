package com.gmail.tylerfilla.android.notes.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class AppCompatPreferenceActivity extends PreferenceActivity {
    
    private AppCompatDelegate appCompatDelegate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getAppCompatDelegate().installViewFactory();
        this.getAppCompatDelegate().onCreate(savedInstanceState);
        
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        this.getAppCompatDelegate().onDestroy();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        this.getAppCompatDelegate().onStop();
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
        this.getAppCompatDelegate().onPostCreate(savedInstanceState);
    }
    
    @Override
    protected void onPostResume() {
        super.onPostResume();
        
        this.getAppCompatDelegate().onPostResume();
    }
    
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        this.getAppCompatDelegate().addContentView(view, params);
    }
    
    @Override
    public void setContentView(int layoutResID) {
        this.getAppCompatDelegate().setContentView(layoutResID);
    }
    
    @Override
    public void setContentView(View view) {
        this.getAppCompatDelegate().setContentView(view);
    }
    
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        this.getAppCompatDelegate().setContentView(view, params);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        this.getAppCompatDelegate().onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        
        this.getAppCompatDelegate().setTitle(title);
    }
    
    @Override
    public MenuInflater getMenuInflater() {
        return this.getAppCompatDelegate().getMenuInflater();
    }
    
    @Override
    public void invalidateOptionsMenu() {
        this.getAppCompatDelegate().invalidateOptionsMenu();
    }
    
    public ActionBar getSupportActionBar() {
        return this.getAppCompatDelegate().getSupportActionBar();
    }
    
    public void setSupportActionBar(Toolbar toolbar) {
        this.getAppCompatDelegate().setSupportActionBar(toolbar);
    }
    
    private AppCompatDelegate getAppCompatDelegate() {
        return this.appCompatDelegate == null ? this.appCompatDelegate = AppCompatDelegate.create(this, null) : this.appCompatDelegate;
    }
    
}
