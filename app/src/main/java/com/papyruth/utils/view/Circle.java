package com.papyruth.utils.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import timber.log.Timber;

public class Circle extends View {
    private static final int START_ANGLE_POINT = -90;
    private final Paint paint;
    private final RectF rect;
    private final int width;
    private final int height;
    private final int stroke;

    private float angle;

    public Circle(Context context, AttributeSet attrs) {
        super(context, attrs);
        /* Get Attribute Metrics */
        int[] attrsArray = new int[] {
            android.R.attr.layout_width, // 0
            android.R.attr.layout_height // 1
        };
        TypedArray typedArray = context.obtainStyledAttributes(attrs, attrsArray);
        this.width = typedArray.getDimensionPixelSize(0, ViewGroup.LayoutParams.MATCH_PARENT);
        this.height= typedArray.getDimensionPixelSize(1, ViewGroup.LayoutParams.MATCH_PARENT);
        this.stroke= width > height ? width / 2 : height / 2;
        this.angle = 0.0f;
        typedArray.recycle();

        Timber.d("[%d x %d][rad:%d, angle:%f]", width, height, stroke, angle);

        /* Paint */
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(Color.WHITE);

        /* Drawing Bound */
        rect = new RectF(stroke/2, stroke/2, this.width - stroke/2, this.height - stroke/2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(rect, START_ANGLE_POINT, angle, false, paint);
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}