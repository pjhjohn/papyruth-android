/*
 * Copyright 2014 Julian Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.papyruth.utils.support.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.papyruth.android.AppManager;
import com.squareup.picasso.Transformation;

/**
 * Created by julian on 13/6/21.
 */
public class CircleWithBorderTransformation implements Transformation {
    private int borderColor;
    private int drawableResourceId;
    public CircleWithBorderTransformation(int borderColor, int drawableResourceId) {
        this.borderColor = borderColor;
        this.drawableResourceId = drawableResourceId;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) source.recycle();
        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);

        Paint pClip = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        pClip.setShader(shader);
        pClip.setAntiAlias(true);
        float r = size/2f;
        canvas.drawCircle(r, r, r, pClip);

        Paint pBorder = new Paint();
        pBorder.setStyle(Paint.Style.STROKE);
        pBorder.setColor(borderColor);
        pBorder.setStrokeWidth(0.1f * r);
        pBorder.setAntiAlias(true);
        canvas.drawCircle(r, r, 0.95f * r, pBorder);

        pBorder.setStyle(Paint.Style.FILL);
        canvas.drawCircle(1.5f * r, 1.5f * r, 0.5f * r, pBorder);
        Bitmap icon = BitmapFactory.decodeResource(AppManager.getInstance().getContext().getResources(), drawableResourceId);
        canvas.drawBitmap(
            icon,
            new Rect(0, 0, icon.getWidth(), icon.getHeight()),
            new Rect((int)r, (int)r, 2*(int)r, 2*(int)r),
            null
        );

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return String.format("circle_color_%d_icon_%d", borderColor, drawableResourceId);
    }
}