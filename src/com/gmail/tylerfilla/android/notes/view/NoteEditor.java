package com.gmail.tylerfilla.android.notes.view;

import java.util.ArrayDeque;
import java.util.Deque;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.core.Note;

public class NoteEditor extends EditText {
    
    private Note note;
    
    private final Paint notepadLinePaint;
    private final Rect firstLineBounds;
    
    public NoteEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.notepadLinePaint = new Paint();
        this.firstLineBounds = new Rect();
        
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.NoteEditor);
        
        for (int i = 0; i < styledAttrs.getIndexCount(); i++) {
            int attr = styledAttrs.getIndex(i);
            switch (attr) {
            case R.styleable.NoteEditor_notepadLineColor:
                this.notepadLinePaint.setColor(styledAttrs.getColor(attr, 0x000000));
                break;
            case R.styleable.NoteEditor_notepadLineStrokeWidth:
                this.notepadLinePaint.setStrokeWidth(styledAttrs.getDimension(attr, 0.0f));
                break;
            }
        }
        
        styledAttrs.recycle();
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        this.getLineBounds(0, firstLineBounds);
        
        for (int i = 1; i < this.getLineCount() + this.getHeight() / this.getLineHeight(); i++) {
            int linePos = this.getLineHeight() * i + this.firstLineBounds.bottom
                    - this.getLineHeight();
            
            canvas.drawLine(0.0f, linePos, this.getWidth(), linePos, this.notepadLinePaint);
        }
        
        super.onDraw(canvas);
    }
    
    public Note getNote() {
        if (this.note != null) {
            this.note.setContent(Html.toHtml(this.getText()));
        }
        
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
        
        if (this.note != null && this.note.getContent() != null) {
            this.setText(Html.fromHtml(this.note.getContent(), new NoteContentHtmlImageGetter(),
                    new NoteContentHtmlTagHandler()));
        }
    }
    
    private class NoteContentHtmlImageGetter implements Html.ImageGetter {
        
        @Override
        public Drawable getDrawable(String source) {
            return null;
        }
        
    }
    
    private class NoteContentHtmlTagHandler implements Html.TagHandler {
        
        private static final String bulletLadder = "\u2022\u25E6\u25AA\u25AB";
        
        private final Deque<String> listStack = new ArrayDeque<String>();
        private final Deque<Integer> listCount = new ArrayDeque<Integer>();
        
        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tag.equalsIgnoreCase("s") || tag.equalsIgnoreCase("strike")) {
                if (opening) {
                    output.setSpan(new StrikethroughSpan(), output.length(), output.length(),
                            Spannable.SPAN_MARK_MARK);
                } else {
                    StrikethroughSpan[] strikeSpans = output.getSpans(0, output.length(),
                            StrikethroughSpan.class);
                    StrikethroughSpan strikeSpan = strikeSpans[strikeSpans.length - 1];
                    output.setSpan(strikeSpan, output.getSpanStart(strikeSpan), output.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (tag.equalsIgnoreCase("ul") || tag.equalsIgnoreCase("ol")) {
                if (opening) {
                    this.listStack.push(tag);
                    this.listCount.push(0);
                } else {
                    this.listStack.pop();
                    this.listCount.pop();
                }
            } else if (tag.equalsIgnoreCase("li")) {
                if (opening) {
                    if (this.listStack.size() > 0) {
                        String list = this.listStack.peek();
                        int level = this.listStack.size() - 1;
                        
                        String prefix = "\n";
                        
                        for (int i = 0; i < level; i++) {
                            prefix += '\t';
                        }
                        
                        if (list.equalsIgnoreCase("ul")) {
                            prefix += bulletLadder.charAt(Math.min(level, bulletLadder.length()));
                        } else if (list.equalsIgnoreCase("ol")) {
                            this.listCount.push(this.listCount.pop() + 1);
                            prefix += this.listCount.peek() + ".";
                        }
                        
                        prefix += ' ';
                        
                        output.append(prefix);
                    }
                }
            }
        }
        
    }
    
}
