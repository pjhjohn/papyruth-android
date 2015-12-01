package com.papyruth.support.opensource.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

public class SkewContrastColorFilterTransformation implements Transformation{
    private int mColor;
    private int mOffset;
    private int mOffsetBase;
    public SkewContrastColorFilterTransformation() {
        mColor = Color.WHITE;
        mOffset = 0x20;
        this.setOffsetBase(mColor, mOffset);
    }
    public SkewContrastColorFilterTransformation(int color){
        mColor = color;
        mOffset = 0x20;
        this.setOffsetBase(mColor, mOffset);
    }
    public SkewContrastColorFilterTransformation(int color, int offset){
        mColor = color;
        mOffset = offset; // >= 0, <= 255
        this.setOffsetBase(mColor, mOffset);
    }

    private void setOffsetBase(int color, int offset) {
        int colorA = (color >> 24) & 0xff;
        int colorR = (color >> 16) & 0xff;
        int colorG = (color >> 8) & 0xff;
        int colorB = color & 0xff;
        int[] channels = {colorR, colorG, colorB};
        int maxOverflow = 0;
        for(int channel : channels) {
            final int overflow = channel + offset - 0xff;
            if(overflow > 0 && maxOverflow < overflow) maxOverflow = overflow;
        } mOffsetBase = -maxOverflow;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final int width = source.getWidth();
        final int height = source.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Path path = new Path();
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new BitmapShader(source, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        paint.setAntiAlias(true);

        /* Top-Left : Lightended color */
        paint.setColorFilter(new PorterDuffColorFilter(offsetColor(mColor, mOffsetBase + mOffset), PorterDuff.Mode.SRC_ATOP));
        path.reset();
        path.moveTo(0, 0);
        path.lineTo(width, 0);
        path.lineTo(0, height);
        path.close();
        canvas.drawPath(path, paint);

        /* Bottom-Right : Darkened color */
        paint.setColorFilter(new PorterDuffColorFilter(offsetColor(mColor, mOffsetBase - mOffset), PorterDuff.Mode.SRC_ATOP));
        path.reset();
        path.moveTo(width, height);
        path.lineTo(width, 0);
        path.lineTo(0, height);
        path.close();
        canvas.drawPath(path, paint);

        source.recycle();
        return bitmap;
    }

    private int offsetColor(int color, int offset) {
        int colorA = (color >> 24) & 0xff;
        int colorR = (color >> 16) & 0xff;
        int colorG = (color >> 8) & 0xff;
        int colorB = color & 0xff;
        return (colorA << 24)|(offsetChannel(colorR, offset) << 16)|(offsetChannel(colorG, offset) << 8)|(offsetChannel(colorB, offset));
    }

    private int offsetChannel(int channel, int offset) {
        return channel + offset < 0 ? 0 : (channel + offset > 0xff ? 0xff : channel + offset);
    }

    @Override
    public String key() {
        return "SkewedContrastColorFilterTransformation("+mColor+", "+mOffset+", "+mOffsetBase+")";
    }
}
