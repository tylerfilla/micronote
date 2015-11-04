package io.microdev.note.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import io.microdev.note.R;
import io.microdev.note.util.DimenUtil;

public class NoteEditor extends EditText {
    
    private Note note;
    
    private UpdateThread updateThread;
    private boolean updated;
    
    private long timeLastChange;
    
    private Paint paintDecorLine;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.note = null;
        
        this.updateThread = new UpdateThread();
        this.updated = true;
        
        this.timeLastChange = System.currentTimeMillis();
        
        this.paintDecorLine = new Paint();
        
        TypedArray array = context.obtainStyledAttributes(attrs, new int[] { R.attr.decorLineColor, R.attr.decorLineSize, });
        
        this.paintDecorLine.setColor(array.getColor(0, Color.BLACK));
        this.paintDecorLine.setStrokeWidth(array.getDimension(1, DimenUtil.dpToPx(context, 1.0f)));
        
        array.recycle();
        
        // Programmatic attribute defaults
        this.setBackgroundColor(0);
        this.setGravity(Gravity.TOP);
        
        // Implement TextWatcher for note updating
        this.addTextChangedListener(new TextWatcher() {
            
            private int previousLineCount;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear updated flag
                NoteEditor.this.updated = false;
                
                // Set last change time
                NoteEditor.this.timeLastChange = System.currentTimeMillis();
                
                // Request layout if line count changed
                if (this.previousLineCount != NoteEditor.this.getLineCount()) {
                    this.previousLineCount = NoteEditor.this.getLineCount();
                    
                    NoteEditor.this.requestLayout();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
            
        });
        
        // Start update thread
        this.updateThread.start();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw notepad lines
        for (int i = 0; i < Math.max(this.getHeight()/this.getLineHeight(), this.getLineCount()); i++) {
            float lineY = DimenUtil.dpToPx(this.getContext(), 1.0f) + (float) (i*this.getLineHeight() + this.getBaseline());
            canvas.drawLine(this.getPaddingLeft(), lineY, (float) (this.getWidth() - this.getPaddingRight()), lineY, this.paintDecorLine);
        }
        
        super.onDraw(canvas);
    }
    
    public Note getNote() {
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
        
        // Parse HTML from note content
        Spanned text = Html.fromHtml(note.getContent());
        
        // Remove trailing newlines
        if (text.length() >= 1) {
            while (text.charAt(text.length() - 1) == '\n') {
                text = (Spanned) text.subSequence(0, text.length() - 1);
            }
        }
        
        // Set text
        this.setText(text);
    }
    
    private void updateNote() {
        // Generate HTML and update note content
        this.note.setContent(Html.toHtml(SpannedString.valueOf(this.getText().toString())));
        
        // Set updated flag
        this.updated = true;
    }
    
    private class UpdateThread extends Thread {
        
        private static final long NOTE_UPDATE_IDLE_TRIGGER = 350l;
        private static final long THREAD_LOOP_ITERATION_DELAY = 50l;
        
        @Override
        public void run() {
            while (true) {
                // Update if necessary
                if (!NoteEditor.this.updated && System.currentTimeMillis() - NoteEditor.this.timeLastChange >= NOTE_UPDATE_IDLE_TRIGGER) {
                    NoteEditor.this.updateNote();
                }
                
                // Wait for iteration delay
                try {
                    Thread.sleep(THREAD_LOOP_ITERATION_DELAY);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        
    }
    
}
