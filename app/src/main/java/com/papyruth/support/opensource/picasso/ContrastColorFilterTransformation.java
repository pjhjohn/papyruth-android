package com.papyruth.support.opensource.picasso;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

import com.squareup.picasso.Transformation;

public class ContrastColorFilterTransformation implements Transformation{
    private int mColor;
    private int mOffset;
    private int mOffsetBase;
    public ContrastColorFilterTransformation() {
        mColor = Color.WHITE;
        mOffset = 0x20;
        this.setOffsetBase(mColor, mOffset);
    }
    public ContrastColorFilterTransformation(int color){
        mColor = color;
        mOffset = 0x20;
        this.setOffsetBase(mColor, mOffset);
    }
    public ContrastColorFilterTransformation(int color, int offset){
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
        }

        mOffsetBase = -maxOverflow;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Rect rLeft = new Rect(0, 0, width/2, height);
        Rect rRight = new Rect(width/2, 0, width, height);

        Canvas canvas = new Canvas(bitmap);
        /* Left : Lightended color */
        Paint pLight = new Paint();
        pLight.setAntiAlias(true);
        pLight.setColorFilter(new PorterDuffColorFilter(offsetColor(mColor, mOffsetBase + mOffset), PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(source, rLeft, rLeft, pLight);
        /* Right : Darkened color */
        Paint pDark = new Paint();
        pDark.setAntiAlias(true);
        pDark.setColorFilter(new PorterDuffColorFilter(offsetColor(mColor, mOffsetBase - mOffset), PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(source, rRight, rRight, pDark);
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
        return "ContrastColorFilterTransformation("+mColor+", "+mOffset+", "+mOffsetBase+")";
    }
}
