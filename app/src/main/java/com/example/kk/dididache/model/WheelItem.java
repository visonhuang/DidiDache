package com.example.kk.dididache.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by linzongzhan on 2017/8/13.
 */

public class WheelItem {

    private float startY;
    private int width;
    private int height;
    private RectF rect = new RectF();
    private int fontColor;
    private int fontSize;
    private String text;
    private int alpha = 255;
    private Paint mpaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WheelItem (float startY, int width, int height, int fontColor, int fontSize, String text) {
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.text = text;
        adjust(0);
    }

    public void adjust (float dy) {
        startY += dy;
        rect.left = 0;
        rect.top = startY;
        rect.right = width;
        rect.bottom = startY + height;
    }

    public float getStartY () {
        return startY;
    }

    public void setStartY (float startY) {
        this.startY = startY;
        rect.left = 0;
        rect.top = startY;
        rect.right = width;
        rect.bottom = startY + height;
    }

    public void setText (String text) {
        this.text = text;
    }

    public String getText () {
        return text;
    }

    public void onDraw (Canvas canvas) {
        mpaint.setTextSize(fontSize);
        mpaint.setColor(fontColor);
        mpaint.setAlpha(alpha);

        int textWidth = (int) mpaint.measureText(text);

        Paint.FontMetrics fontMetrics = mpaint.getFontMetrics();
        int baseLine = (int) (rect.centerY() + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom);//存在问题

        canvas.drawText(text,rect.centerX() - textWidth / 2,baseLine,mpaint);
    }


    public void setFontSize (int fontSize) {
        this.fontSize = fontSize;
    }

    public void setAlpha (int alpha) {
        this.alpha = alpha;
    }
}
