package de.testo.demo.t300interface.util;

import android.content.Context;
import android.widget.TextView;

public class TextViewHelper {

    public static TextView GenerateDefaultTextView(Context ctx) {
        return GenerateDefaultTextView(ctx, "");
    }

    public static TextView GenerateDefaultTextView(Context ctx, String text) {
        TextView textView = new TextView(ctx);
        textView.setText(text);
        textView.setPadding(15,20,15,20 );
        return textView;
    }
}
