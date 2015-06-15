package com.gmail.tylerfilla.android.notes.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class ColorPreference extends DialogPreference {
    
    private static final int DEFAULT_DEFAULT_COLOR = 0;
    
    private int currentColor;
    private float[] currentHSV;
    
    private boolean hexModified;
    
    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.currentColor = 0xFF000000;
        this.currentHSV = new float[3];
        
        this.hexModified = false;
        
        this.setPositiveButtonText("Okay");
        this.setNegativeButtonText("Cancel");
    }
    
    @Override
    protected View onCreateDialogView() {
        RelativeLayout viewDialog = new RelativeLayout(this.getContext());
        
        final View viewColorFieldSV = new View(this.getContext()) {
            
            @Override
            public void draw(Canvas canvas) {
                float height = (float) this.getHeight();
                float width = (float) this.getWidth();
                
                float lensX = ColorPreference.this.currentHSV[1]*width;
                float lensY = (1.0f - ColorPreference.this.currentHSV[2])*height;
                
                Paint paintHue = new Paint();
                Paint paintSaturation = new Paint();
                Paint paintValue = new Paint();
                
                Paint paintLens = new Paint();
                
                paintHue.setStyle(Paint.Style.FILL);
                paintSaturation.setStyle(Paint.Style.FILL);
                paintValue.setStyle(Paint.Style.FILL);
                
                paintLens.setStyle(Paint.Style.STROKE);
                
                paintHue.setColor(Color.HSVToColor(new float[] { ColorPreference.this.currentHSV[0], 1.0f, 1.0f, }));
                
                paintLens.setColor(Color.HSVToColor(new float[] { 0.0f, 0.0f, 1.0f - ColorPreference.this.currentHSV[2], }));
                paintLens.setStrokeWidth(4.0f);
                
                paintSaturation.setShader(new LinearGradient(0.0f, 0.0f, width, 0.0f, new int[] { 0xFFFFFFFF, 0x00000000, }, null, Shader.TileMode.MIRROR));
                paintValue.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, height, new int[] { 0x00000000, 0xFF000000, }, null, Shader.TileMode.MIRROR));
                
                canvas.drawRect(0.0f, 0.0f, width, height, paintHue);
                canvas.drawRect(0.0f, 0.0f, width, height, paintSaturation);
                canvas.drawRect(0.0f, 0.0f, width, height, paintValue);
                
                canvas.drawCircle(lensX, lensY, 16.0f, paintLens);
            }
            
        };
        
        final View viewColorFieldH = new View(this.getContext()) {
            
            @Override
            public void draw(Canvas canvas) {
                float height = (float) this.getHeight();
                float width = (float) this.getWidth();
                
                float lensY = (ColorPreference.this.currentHSV[0]/360.0f)*height;
                
                Paint paintHue = new Paint();
                Paint paintLens = new Paint();
                
                paintHue.setStyle(Paint.Style.FILL);
                paintLens.setStyle(Paint.Style.STROKE);
                
                paintHue.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, height, new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000, }, null, Shader.TileMode.MIRROR));
                
                paintLens.setColor(Color.BLACK);
                paintLens.setStrokeWidth(6.0f);
                
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
                
                canvas.drawRect(0.1f*width, 0.0f, width - 0.1f*width, height, paintHue);
                canvas.drawRect(0.0f, lensY - 0.02f*height, width, lensY + 0.02f*height, paintLens);
            }
            
        };
        
        final EditText editTextInputHex = new EditText(this.getContext());
        final View viewSwatch = new View(this.getContext());
        
        viewColorFieldSV.setOnTouchListener(new View.OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ColorPreference.this.currentHSV[1] = Math.max(0.0f, Math.min(1.0f, (event.getX() - v.getLeft())/v.getWidth()));
                ColorPreference.this.currentHSV[2] = Math.max(0.0f, Math.min(1.0f, 1.0f - (event.getY() - v.getTop())/v.getHeight()));
                ColorPreference.this.currentColor = Color.HSVToColor(ColorPreference.this.currentHSV);
                
                viewColorFieldSV.invalidate();
                editTextInputHex.setText(Integer.toHexString(ColorPreference.this.currentColor).substring(2));
                viewSwatch.setBackgroundColor(ColorPreference.this.currentColor);
                
                ColorPreference.this.hexModified = true;
                
                return true;
            }
            
        });
        
        viewColorFieldH.setOnTouchListener(new View.OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ColorPreference.this.currentHSV[0] = 360.0f*Math.max(0.0f, Math.min(359.0f/360.0f, (event.getY() - v.getY())/v.getHeight()));
                ColorPreference.this.currentColor = Color.HSVToColor(ColorPreference.this.currentHSV);
                
                viewColorFieldSV.invalidate();
                viewColorFieldH.invalidate();
                editTextInputHex.setText(Integer.toHexString(ColorPreference.this.currentColor).substring(2));
                viewSwatch.setBackgroundColor(ColorPreference.this.currentColor);
                
                ColorPreference.this.hexModified = true;
                
                return true;
            }
            
        });
        
        editTextInputHex.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!ColorPreference.this.hexModified) {
                    int newColor = ColorPreference.this.currentColor;
                    try {
                        newColor = (int) Long.parseLong("FF" + s.toString(), 16);
                    } catch (NumberFormatException e) {
                    }
                    
                    ColorPreference.this.currentColor = newColor;
                    Color.colorToHSV(newColor, ColorPreference.this.currentHSV);
                    
                    viewColorFieldSV.invalidate();
                    viewColorFieldH.invalidate();
                    viewSwatch.setBackgroundColor(ColorPreference.this.currentColor);
                }
                
                ColorPreference.this.hexModified = false;
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
            
        });
        
        editTextInputHex.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        editTextInputHex.setText(Integer.toHexString(ColorPreference.this.currentColor).substring(2));
        
        viewSwatch.setBackgroundColor(ColorPreference.this.currentColor);
        
        viewColorFieldSV.setId(1);
        viewColorFieldH.setId(2);
        editTextInputHex.setId(3);
        viewSwatch.setId(4);
        
        RelativeLayout.LayoutParams layoutParamsViewColorFieldSV = new RelativeLayout.LayoutParams(this.dpToPx(220), this.dpToPx(220));
        RelativeLayout.LayoutParams layoutParamsViewColorFieldH = new RelativeLayout.LayoutParams(this.dpToPx(48), this.dpToPx(220));
        RelativeLayout.LayoutParams layoutParamsEditTextInputHex = new RelativeLayout.LayoutParams(this.dpToPx(120), this.dpToPx(48));
        RelativeLayout.LayoutParams layoutParamsViewSwatch = new RelativeLayout.LayoutParams(this.dpToPx(48), this.dpToPx(48));
        
        layoutParamsViewColorFieldSV.setMargins(this.dpToPx(12), this.dpToPx(12), this.dpToPx(12), this.dpToPx(12));
        layoutParamsViewColorFieldSV.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        
        layoutParamsViewColorFieldH.setMargins(this.dpToPx(12), this.dpToPx(12), this.dpToPx(12), this.dpToPx(12));
        layoutParamsViewColorFieldH.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        
        layoutParamsEditTextInputHex.setMargins(this.dpToPx(12), this.dpToPx(12), this.dpToPx(12), this.dpToPx(12));
        layoutParamsEditTextInputHex.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParamsEditTextInputHex.addRule(RelativeLayout.BELOW, viewColorFieldSV.getId());
        
        layoutParamsViewSwatch.setMargins(this.dpToPx(12), this.dpToPx(12), this.dpToPx(12), this.dpToPx(12));
        layoutParamsViewSwatch.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParamsViewSwatch.addRule(RelativeLayout.BELOW, viewColorFieldSV.getId());
        layoutParamsViewSwatch.addRule(RelativeLayout.RIGHT_OF, editTextInputHex.getId());
        
        viewColorFieldSV.setLayoutParams(layoutParamsViewColorFieldSV);
        viewColorFieldH.setLayoutParams(layoutParamsViewColorFieldH);
        editTextInputHex.setLayoutParams(layoutParamsEditTextInputHex);
        viewSwatch.setLayoutParams(layoutParamsViewSwatch);
        
        viewDialog.addView(viewColorFieldSV);
        viewDialog.addView(viewColorFieldH);
        viewDialog.addView(editTextInputHex);
        viewDialog.addView(viewSwatch);
        
        return viewDialog;
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            this.callChangeListener(this.currentColor);
            this.persistInt(this.currentColor);
        }
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, ColorPreference.DEFAULT_DEFAULT_COLOR);
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            this.currentColor = this.getPersistedInt(ColorPreference.DEFAULT_DEFAULT_COLOR);
        } else {
            this.currentColor = (int) defaultValue;
            this.persistInt(this.currentColor);
        }
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Parcelable upwardState = null;
        
        if (state == null || !(state instanceof ColorPreference.SavedState)) {
            upwardState = state;
            
            super.onRestoreInstanceState(upwardState);
            return;
        }
        
        ColorPreference.SavedState savedState = (ColorPreference.SavedState) state;
        upwardState = savedState.getSuperState();
        
        super.onRestoreInstanceState(upwardState);
        
        this.currentColor = savedState.getSavedColor();
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        
        if (this.isPersistent()) {
            return superState;
        }
        
        SavedState savedState = new SavedState(superState);
        savedState.setSavedColor(this.currentColor);
        
        return savedState;
    }
    
    private int dpToPx(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) px, this.getContext().getResources().getDisplayMetrics());
    }
    
    private static class SavedState extends BaseSavedState {
        
        public static final Parcelable.Creator<ColorPreference.SavedState> CREATOR = new Parcelable.Creator<ColorPreference.SavedState>() {
            
            public ColorPreference.SavedState createFromParcel(Parcel in) {
                return new ColorPreference.SavedState(in);
            }
            
            public ColorPreference.SavedState[] newArray(int size) {
                return new ColorPreference.SavedState[size];
            }
            
        };
        
        private int savedColor;
        
        public SavedState(Parcelable superState) {
            super(superState);
            
            this.savedColor = 0;
        }
        
        public SavedState(Parcel source) {
            super(source);
            
            this.savedColor = source.readInt();
        }
        
        public int getSavedColor() {
            return this.savedColor;
        }
        
        public void setSavedColor(int savedColor) {
            this.savedColor = savedColor;
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            
            dest.writeInt(this.savedColor);
        }
        
    }
    
}
