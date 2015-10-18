package io.microdev.note.util;

import android.content.Context;
import android.util.TypedValue;

public class DimenUtil {
    
    public static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
    
    public static int dpToPxInt(Context context, int dp) {
        return (int) dpToPx(context, (float) dp);
    }
    
}
