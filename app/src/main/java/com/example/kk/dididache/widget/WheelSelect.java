package com.example.kk.dididache.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

/**
 * Created by linzongzhan on 2017/8/13.
 */

public class WheelSelect {

    public static final int COLOR_BACKGROUND = Color.parseColor("#77777777");
    private int startY;
    private int width;
    private int height;
    private Rect rect = new Rect();
    private String selectText;
    private int fontColor;
    private int fontSize;
    private int padding = 0;
    private int lineColor = COLOR_BACKGROUND;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WheelSelect (int startY,int width,int height,String selectText,int fontColor,int fontSize,int padding,int lineColor) {
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.selectText = selectText;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.padding = padding;
        this.lineColor = lineColor;
    }

    public int getStartY () {
        return startY;
    }

    public void setStartY (int startY) {
        this.startY = startY;
    }

    public void onDraw (Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(lineColor);

        canvas.drawLine(0 + padding,startY,width - padding,startY,mPaint);
        canvas.drawLine(0 + padding,startY + height,width - padding,startY + height,mPaint);

        shadow.setStyle(Paint.Style.FILL);
        shadow.setColor(Color.WHITE);
        shadow.setAlpha(255);

        LinearGradient top = new LinearGradient(0,0,0,startY / 2,0xffbcd2fa,0x00bcd2fa,Shader.TileMode.REPEAT);
        shadow.setShader(top);
        canvas.drawRect(0,0,width - padding,startY / 2,shadow);

        LinearGradient bottom = new LinearGradient(0,0,0,startY / 2,0x00bcd2fa,0xffbcd2fa,Shader.TileMode.REPEAT);
        shadow.setShader(bottom);
        canvas.drawRect(0,startY + height * 3 / 2,width - padding,startY + height * 2,shadow);

        if (selectText != null) {
            mPaint.setTextSize(fontSize);
            mPaint.setColor(fontColor);

            int textWidth = (int) mPaint.measureText(selectText);

            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            int baseLine = (int) (rect.centerY() + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom);

            canvas.drawText(selectText,rect.right - padding - textWidth,baseLine,mPaint);
        }
    }
}
