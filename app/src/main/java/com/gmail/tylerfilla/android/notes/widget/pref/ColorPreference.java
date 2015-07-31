package com.gmail.tylerfilla.android.notes.widget.pref;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.gmail.tylerfilla.android.notes.util.DimenUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorPreference extends DialogPreference {
    
    private View customDialogColorFieldH;
    private View customDialogColorFieldSV;
    private View customDialogColorSwatch;
    private EditText customDialogEditTextHex;
    
    private ViewGroup customDialogContainer;
    
    private boolean customDialogContainerScrollable;
    
    private int currentColor;
    private float[] currentHSV;
    
    private int persistentColor;
    
    private volatile boolean customDialogHexModifiedInternally;
    
    private List<String> colorPresetNames;
    private List<Integer> colorPresetValues;
    
    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        this.customDialogContainerScrollable = true;
        
        this.currentColor = 0;
        this.currentHSV = new float[3];
        
        this.persistentColor = 0;
        
        this.customDialogHexModifiedInternally = false;
        
        this.colorPresetNames = new ArrayList<>();
        this.colorPresetValues = new ArrayList<>();
        
        // Get entries and entryValues array resources
        TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.entries, android.R.attr.entryValues });
        int resIdEntries = typedArray.getResourceId(0, -1);
        int resIdEntryValues = typedArray.getResourceId(1, -1);
        typedArray.recycle();
        
        // Add entry data to preset lists
        if (resIdEntries > -1) {
            this.colorPresetNames.addAll(Arrays.asList(context.getResources().getStringArray(resIdEntries)));
        }
        if (resIdEntryValues > -1) {
            // Arrays.asList() does not handle boxing
            for (int colorValue : context.getResources().getIntArray(resIdEntryValues)) {
                this.colorPresetValues.add(colorValue);
            }
        }
    }
    
    @Override
    protected void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        
        // Configure buttons
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(null, null);
        
        // Preset list initial selection
        final int presetListInitialSelection = this.colorPresetValues.contains(this.currentColor) ? this.colorPresetValues.indexOf(this.currentColor) : this.colorPresetValues.size() - 1;
        
        // Preset list adapter
        BaseAdapter presetListAdapter = new BaseAdapter() {
            
            @Override
            public int getCount() {
                return ColorPreference.this.colorPresetNames.size();
            }
            
            @Override
            public Object getItem(int position) {
                return ColorPreference.this.colorPresetValues.get(position);
            }
            
            @Override
            public long getItemId(int position) {
                return position;
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Reuse convertView
                View view = convertView;
                
                // If view needs to be created
                if (view == null) {
                    view = ((LayoutInflater) builder.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.simple_list_item_single_choice, parent, false);
                }
                
                // Get reference to main text view
                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                
                // Set text
                checkedTextView.setText(ColorPreference.this.colorPresetNames.get(position));
                
                // Get color value for this preset
                int colorValue = ColorPreference.this.colorPresetValues.get(position);
                
                // If the value isn't custom
                if ((colorValue & 0xff000000) == 0xff000000) {
                    // Set text color
                    checkedTextView.setTextColor(colorValue);
                } else {
                    // If initial selection is the custom preset
                    if (presetListInitialSelection == position) {
                        // Set text color to custom color
                        checkedTextView.setTextColor(ColorPreference.this.currentColor);
                    }
                }
                
                return view;
            }
            
        };
        
        // Preset list on click listener
        DialogInterface.OnClickListener presetListOnClickListener = new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get color value for this preset
                int colorValue = ColorPreference.this.colorPresetValues.get(which);
                
                // If the value isn't custom
                if ((colorValue & 0xff000000) == 0xff000000) {
                    // Store preset color value
                    ColorPreference.this.currentColor = colorValue;
                    Color.colorToHSV(ColorPreference.this.currentColor, ColorPreference.this.currentHSV);
                } else {
                    // Open custom dialog
                    ColorPreference.this.openCustomDialog();
                }
                
                // Simulate a positive button click
                ColorPreference.super.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                
                // Dismiss preset dialog
                dialog.dismiss();
            }
            
        };
        
        // Configure preset list
        builder.setSingleChoiceItems(presetListAdapter, presetListInitialSelection, presetListOnClickListener);
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            this.persistentColor = this.currentColor;
        } else {
            this.currentColor = this.persistentColor;
            Color.colorToHSV(this.currentColor, this.currentHSV);
        }
        
        this.persistInt(this.persistentColor);
        
        this.callChangeListener(this.persistentColor);
        this.notifyChanged();
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            this.persistentColor = this.getPersistedInt(this.persistentColor);
        } else {
            this.persistentColor = (int) defaultValue;
        }
        
        this.persistInt(this.persistentColor);
        
        this.currentColor = this.persistentColor;
        Color.colorToHSV(this.currentColor, this.currentHSV);
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getColor(index, this.persistentColor);
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof ColorPreference.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        
        ColorPreference.SavedState savedState = (ColorPreference.SavedState) state;
        this.persistentColor = savedState.getSavedColor();
        super.onRestoreInstanceState(savedState.getSuperState());
    }
    
    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.setSavedColor(this.currentColor);
        
        return savedState;
    }
    
    @Override
    public CharSequence getSummary() {
        // Name of color
        String name;
        
        // If this color is a preset
        if (this.colorPresetValues.contains(this.currentColor)) {
            // Use the preset name
            name = this.colorPresetNames.get(this.colorPresetValues.indexOf(this.currentColor));
        } else {
            // Use HTML hex notation
            name = ((this.currentColor & 0x00ffffff) == 0 ? "" : Integer.toHexString(this.currentColor & 0x00ffffff)).toUpperCase();
            
            // Pad beginning with zeroes
            while (name.length() < 6) {
                name = "0" + name;
            }
            
            // Add the hex identifier thingy
            name = "#" + name;
        }
        
        // Format name into summary text
        return String.format(super.getSummary().toString(), name);
    }
    
    private void openCustomDialog() {
        // Builder for custom dialog
        AlertDialog.Builder customDialogBuilder = new AlertDialog.Builder(this.getContext());
        
        // Configure custom dialog buttons
        customDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ColorPreference.this.onDialogClosed(false);
            }
            
        });
        customDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ColorPreference.this.onDialogClosed(true);
            }
            
        });
        
        // Create and set custom dialog view
        customDialogBuilder.setView(this.createCustomDialogView());
        
        // Show custom dialog
        customDialogBuilder.show();
        
        // Initially update custom dialog
        this.updateCustomDialog();
    }
    
    private View createCustomDialogView() {
        this.customDialogContainer = new ScrollView(this.getContext()) {
            
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ColorPreference.this.customDialogContainerScrollable) {
                    return super.onInterceptTouchEvent(ev);
                }
                
                return false;
            }
            
        };
        
        RelativeLayout customDialogLayout = new RelativeLayout(this.getContext());
        
        this.customDialogColorFieldH = new View(this.getContext()) {
            
            @Override
            public void draw(Canvas canvas) {
                float height = (float) this.getHeight();
                float width = (float) this.getWidth();
                
                float lensY = (ColorPreference.this.currentHSV[0]/360.0f)*height;
                
                Paint paintHue = new Paint();
                Paint paintLens = new Paint();
                
                paintHue.setStyle(Paint.Style.FILL);
                paintLens.setStyle(Paint.Style.STROKE);
                
                paintHue.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, height, new int[] { 0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff, 0xff0000ff, 0xffff00ff, 0xffff0000, }, null, Shader.TileMode.MIRROR));
                
                paintLens.setColor(Color.BLACK);
                paintLens.setStrokeWidth(6.0f);
                
                canvas.drawRect(0.1f*width, 0.0f, width - 0.1f*width, height, paintHue);
                canvas.drawRect(0.0f, lensY - 0.02f*height, width, lensY + 0.02f*height, paintLens);
            }
            
        };
        
        this.customDialogColorFieldSV = new View(this.getContext()) {
            
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
                
                paintSaturation.setShader(new LinearGradient(0.0f, 0.0f, width, 0.0f, new int[] { 0xffffffff, 0x00000000, }, null, Shader.TileMode.MIRROR));
                paintValue.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, height, new int[] { 0x00000000, 0xff000000, }, null, Shader.TileMode.MIRROR));
                
                canvas.drawRect(0.0f, 0.0f, width, height, paintHue);
                canvas.drawRect(0.0f, 0.0f, width, height, paintSaturation);
                canvas.drawRect(0.0f, 0.0f, width, height, paintValue);
                
                canvas.drawCircle(lensX, lensY, 16.0f, paintLens);
            }
            
        };
        
        this.customDialogColorSwatch = new View(this.getContext());
        this.customDialogEditTextHex = new EditText(this.getContext());
        
        this.customDialogEditTextHex.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(6),
                new InputFilter() {
                    
                    private boolean isHexChar(char c) {
                        return "0123456789abcdef".contains(String.valueOf(c).toLowerCase());
                    }
                    
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        boolean keepOriginal = true;
                        
                        StringBuilder stringBuilder = new StringBuilder(end - start);
                        
                        for (int ci = start; ci < end; ci++) {
                            char c = source.charAt(ci);
                            
                            if (this.isHexChar(c)) {
                                stringBuilder.append(c);
                            } else {
                                keepOriginal = false;
                            }
                        }
                        
                        if (keepOriginal) {
                            return null;
                        } else {
                            if (source instanceof Spanned) {
                                SpannableString spannableString = new SpannableString(stringBuilder);
                                TextUtils.copySpansFrom((Spanned) source, start, stringBuilder.length(), null, spannableString, 0);
                                
                                return spannableString;
                            } else {
                                return stringBuilder;
                            }
                        }
                    }
                    
                },
        });
        this.customDialogEditTextHex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        
        this.customDialogColorFieldH.setOnTouchListener(new View.OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ColorPreference.this.customDialogContainerScrollable = false;
                    break;
                case MotionEvent.ACTION_UP:
                    ColorPreference.this.customDialogContainerScrollable = true;
                    break;
                }
                
                ColorPreference.this.currentHSV[0] = 360.0f*Math.max(0.0f, Math.min(359.0f/360.0f, (event.getY() - v.getTop())/v.getHeight()));
                ColorPreference.this.currentColor = Color.HSVToColor(ColorPreference.this.currentHSV);
                
                ColorPreference.this.updateCustomDialog();
                
                return true;
            }
            
        });
        
        this.customDialogColorFieldSV.setOnTouchListener(new View.OnTouchListener() {
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ColorPreference.this.customDialogContainerScrollable = false;
                    break;
                case MotionEvent.ACTION_UP:
                    ColorPreference.this.customDialogContainerScrollable = true;
                    break;
                }
                
                ColorPreference.this.currentHSV[1] = Math.max(0.0f, Math.min(1.0f, (event.getX() - v.getLeft())/v.getWidth()));
                ColorPreference.this.currentHSV[2] = Math.max(0.0f, Math.min(1.0f, 1.0f - (event.getY() - v.getTop())/v.getHeight()));
                ColorPreference.this.currentColor = Color.HSVToColor(ColorPreference.this.currentHSV);
                
                ColorPreference.this.updateCustomDialog();
                
                return true;
            }
            
        });
        
        this.customDialogEditTextHex.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!ColorPreference.this.customDialogHexModifiedInternally) {
                    int newColor = ColorPreference.this.currentColor;
                    try {
                        newColor = (int) Long.parseLong("ff" + s.toString(), 16);
                    } catch (NumberFormatException e) {
                    }
                    
                    ColorPreference.this.currentColor = newColor;
                    Color.colorToHSV(newColor, ColorPreference.this.currentHSV);
                    
                    ColorPreference.this.updateCustomDialog();
                }
                
                ColorPreference.this.customDialogHexModifiedInternally = false;
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
            
        });
        
        this.customDialogColorFieldH.setId(1);
        this.customDialogColorFieldSV.setId(2);
        this.customDialogColorSwatch.setId(3);
        this.customDialogEditTextHex.setId(4);
        
        RelativeLayout.LayoutParams layoutParamsViewDialogColorFieldH = new RelativeLayout.LayoutParams((int) DimenUtil.dpToPx(this.getContext(), 48), (int) DimenUtil.dpToPx(this.getContext(), 220));
        RelativeLayout.LayoutParams layoutParamsViewDialogColorFieldSV = new RelativeLayout.LayoutParams((int) DimenUtil.dpToPx(this.getContext(), 220), (int) DimenUtil.dpToPx(this.getContext(), 220));
        RelativeLayout.LayoutParams layoutParamsViewDialogColorSwatch = new RelativeLayout.LayoutParams((int) DimenUtil.dpToPx(this.getContext(), 48), (int) DimenUtil.dpToPx(this.getContext(), 48));
        RelativeLayout.LayoutParams layoutParamsEditTextDialogInputHex = new RelativeLayout.LayoutParams((int) DimenUtil.dpToPx(this.getContext(), 120), (int) DimenUtil.dpToPx(this.getContext(), 48));
        
        layoutParamsViewDialogColorFieldH.setMargins((int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12));
        layoutParamsViewDialogColorFieldH.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        
        layoutParamsViewDialogColorFieldSV.setMargins((int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12));
        layoutParamsViewDialogColorFieldSV.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        
        layoutParamsViewDialogColorSwatch.setMargins((int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12));
        layoutParamsViewDialogColorSwatch.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParamsViewDialogColorSwatch.addRule(RelativeLayout.BELOW, this.customDialogColorFieldSV.getId());
        layoutParamsViewDialogColorSwatch.addRule(RelativeLayout.RIGHT_OF, this.customDialogEditTextHex.getId());
        
        layoutParamsEditTextDialogInputHex.setMargins((int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12), (int) DimenUtil.dpToPx(this.getContext(), 12));
        layoutParamsEditTextDialogInputHex.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParamsEditTextDialogInputHex.addRule(RelativeLayout.BELOW, this.customDialogColorFieldSV.getId());
        
        this.customDialogColorFieldH.setLayoutParams(layoutParamsViewDialogColorFieldH);
        this.customDialogColorFieldSV.setLayoutParams(layoutParamsViewDialogColorFieldSV);
        this.customDialogColorSwatch.setLayoutParams(layoutParamsViewDialogColorSwatch);
        this.customDialogEditTextHex.setLayoutParams(layoutParamsEditTextDialogInputHex);
        
        customDialogLayout.addView(this.customDialogColorFieldH);
        customDialogLayout.addView(this.customDialogColorFieldSV);
        customDialogLayout.addView(this.customDialogColorSwatch);
        customDialogLayout.addView(this.customDialogEditTextHex);
        
        this.customDialogContainer.addView(customDialogLayout);
        
        return this.customDialogContainer;
    }
    
    private void updateCustomDialog() {
        if (this.customDialogContainer != null) {
            // Set hex internally modified flag
            this.customDialogHexModifiedInternally = true;
            
            // Update color swatch
            this.customDialogColorSwatch.setBackgroundColor(this.currentColor);
            
            // Force redraw of color fields
            this.customDialogColorFieldH.postInvalidate();
            this.customDialogColorFieldSV.postInvalidate();
            
            // Handle dumb selection reset and focus hogging
            int sel = this.customDialogEditTextHex.getSelectionStart();
            this.customDialogEditTextHex.setText(Integer.toHexString(ColorPreference.this.currentColor).substring(2).toUpperCase());
            this.customDialogEditTextHex.setSelection(Math.min(sel, this.customDialogEditTextHex.length()));
            this.customDialogEditTextHex.clearFocus();
        }
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
