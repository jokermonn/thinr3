package com.joker.thinr3plugin;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class CustomView extends View {
  public CustomView(Context context) {
    super(context);
  }

  public CustomView(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomView);
    boolean normal = a.getBoolean(R.styleable.CustomView_cv_normal, false);
    int color = a.getColor(R.styleable.CustomView_cv_color, 0xffffffff);
    a.recycle();
  }

  public void update() {
  }
}
