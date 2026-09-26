package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Typeface;

/**
 * The bundled CJK typeface. The camera's firmware font is missing several Han glyphs used by the
 * Chinese translation (e.g. U+5F95 徕 in 徕卡), which render as hollow boxes. Every TextView and
 * every Canvas text Paint uses this typeface instead.
 *
 * The font at assets/fonts/cn.ttf is a subset of Noto Sans CJK SC (SIL Open Font License 1.1),
 * reduced to the characters this app uses plus the GB 2312 character set.
 */
final class Fonts {
    private static final String PATH = "fonts/cn.ttf";
    private static Typeface instance;

    private Fonts() {}

    /** the shared bundled typeface, loaded once */
    static synchronized Typeface get(Context context) {
        if (instance == null) {
            instance = Typeface.createFromAsset(context.getApplicationContext().getAssets(), PATH);
        }
        return instance;
    }
}
