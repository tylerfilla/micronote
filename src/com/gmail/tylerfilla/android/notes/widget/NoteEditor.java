package com.gmail.tylerfilla.android.notes.widget;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.gmail.tylerfilla.android.notes.R;
import com.gmail.tylerfilla.android.notes.core.Note;
import com.gmail.tylerfilla.android.notes.util.CheckboxSpan;
import com.gmail.tylerfilla.android.notes.util.CheckboxSpanClickRef;

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
            case R.styleable.NoteEditor_lineColor:
                this.notepadLinePaint.setColor(styledAttrs.getColor(attr, 0x000000));
                break;
            case R.styleable.NoteEditor_lineSize:
                this.notepadLinePaint.setStrokeWidth(styledAttrs.getDimension(attr, 0.0f));
                break;
            }
        }
        
        styledAttrs.recycle();
        
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        this.getLineBounds(0, firstLineBounds);
        
        for (int i = 1; i < this.getLineCount() + this.getHeight() / this.getLineHeight(); i++) {
            int linePos = this.getLineHeight() * i + this.firstLineBounds.bottom
                    - this.getLineHeight();
            
            canvas.drawLine(0.0f, linePos, this.getWidth(), linePos, this.notepadLinePaint);
        }
        
        for (DynamicDrawableSpan span : this.getText().getSpans(0, this.getText().length(),
                DynamicDrawableSpan.class)) {
            int start = this.getText().getSpanStart(span);
            int end = this.getText().getSpanEnd(span);
            
            this.getText().removeSpan(span);
            this.getText().setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        super.onDraw(canvas);
    }
    
    public Note getNote() {
        /*
         * if (this.note != null) { String newContent = CustomSpannedToHtml.toHtml(this.getText());
         * if (!newContent.equals(note.getContent())) { this.note.setContent(newContent); } }
         */
        
        return this.note;
    }
    
    public void setNote(Note note) {
        this.note = note;
        
        /*
         * if (note != null && note.getContent() != null) { note.clearChanged();
         * this.setText(Html.fromHtml(note.getContent(), new NoteContentHtmlImageGetter(), new
         * NoteContentHtmlTagHandler())); }
         */
        /*
         * this.setText(Html.fromHtml("<input type='checkbox' value='1' />", new
         * NoteContentHtmlImageGetter(), new NoteContentHtmlTagHandler()));
         */
        this.setMovementMethod(new LinkMovementMethod());
        
        CheckboxSpan c = new CheckboxSpan(this);
        CheckboxSpanClickRef cr = new CheckboxSpanClickRef(c);
        
        SpannableStringBuilder ss = new SpannableStringBuilder();
        ss.append(" * ");
        ss.setSpan(cr, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(c, 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        this.setText(ss, BufferType.SPANNABLE);
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
        private final Deque<Integer> listCountStack = new ArrayDeque<Integer>();
        
        private final HashMap<String, String> customAttributes = new HashMap<String, String>();
        
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
                    this.listCountStack.push(0);
                } else {
                    this.listStack.pop();
                    this.listCountStack.pop();
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
                            this.listCountStack.push(this.listCountStack.pop() + 1);
                            prefix += this.listCountStack.peek() + ".";
                        }
                        
                        prefix += ' ';
                        
                        output.append(prefix);
                    }
                }
            } else if (tag.equalsIgnoreCase("input")) {
                try {
                    this.loadCustomAttributes(xmlReader);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                
                if (!this.customAttributes.isEmpty()) {
                    if ("checkbox".equalsIgnoreCase(this.customAttributes.get("type"))) {
                        
                    }
                }
            }
        }
        
        private void loadCustomAttributes(XMLReader xmlReader) throws IllegalAccessException,
                IllegalArgumentException, NoSuchFieldException {
            this.customAttributes.clear();
            
            // A little 'dark magic' as suggested by @narkis at http://stackoverflow.com/q/20788393
            // Based on a solution by @rekire at http://stackoverflow.com/a/15196299
            
            Field fieldElement = xmlReader.getClass().getDeclaredField("theNewElement");
            fieldElement.setAccessible(true);
            Object element = fieldElement.get(xmlReader);
            
            if (element == null) {
                return;
            }
            
            Field fieldAttributes = element.getClass().getDeclaredField("theAtts");
            fieldAttributes.setAccessible(true);
            Object attributes = fieldAttributes.get(element);
            
            Field fieldAttributeData = attributes.getClass().getDeclaredField("data");
            fieldAttributeData.setAccessible(true);
            String[] attributeData = (String[]) fieldAttributeData.get(attributes);
            
            Field fieldLength = attributes.getClass().getDeclaredField("length");
            fieldLength.setAccessible(true);
            int length = (Integer) fieldLength.get(attributes);
            
            for (int i = 0; i < length; i++) {
                this.customAttributes.put(attributeData[i * 5 + 1], attributeData[i * 5 + 4]);
            }
        }
        
    }
    
}
