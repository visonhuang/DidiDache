package com.example.kk.dididache.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by linzongzhan on 2017/8/14.
 */

public class WheelView extends View {

    public interface Listener {
        void listen(String info);
    }
    private Listener listener;
    public void setListener (Listener listener) {
        this.listener = listener;
    }

    private Canvas canvas;

    private static final int FRESH = 1;

    private Queue<Thread> threadQueue = new LinkedList<Thread>();

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FRESH :
                    invalidate();
                    break;
                default:
            }
            super.handleMessage(msg);
        }
    };

    private Context mContext;
    private AttributeSet attributeSet;

    private long downTime;
    private String text = "";
    private String text7;

    private static final int SELECTEDSIZE = 120;
    private int selectedSize = SELECTEDSIZE;
    private int linePadding = 20;

    public static final int FONT_COLOR = Color.parseColor("#4F4F4F");
    public static final int FONT_SIZE = 80;
    public static final int PADDING = 0;
    public static final int SHOW_COUNT = 3;
    public static final int SELECT = 0;

    private int width;
    private int height;
    private int itemHeight;

    private int showCount = SHOW_COUNT;
    private int select = SELECT;
    private int fontColor = FONT_COLOR;
    private int fontSize = FONT_SIZE;
    private int padding = PADDING;
    private int lineColor = Color.parseColor("#FFFFFF");

    private List<String> lists = new ArrayList<String>();
    private int listSize;
    private String selectTip;

    private List<WheelItem> wheelItems = new ArrayList<WheelItem>();
    private WheelSelect wheelSelect = null;

    private float mTouchY;
    private float mTouchY1;

    public WheelView(Context context) {
        super(context);
        mContext = context;
    }

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        attributeSet = attrs;

        //详细考虑一下
        for (int i = 0; i < 12; i++) {
            lists.add("");
        }


//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.WheelView,0,0);
//        try {
//            linePadding = a.getInteger(R.styleable.WheelView_padding,0);
//            fontSize = a.getInteger(R.styleable.WheelView_fontSize,40);
//            fontColor = a.getInteger(R.styleable.WheelView_fontColor,Color.BLACK);
//            lineColor = a.getInteger(R.styleable.WheelView_lineColor,Color.BLACK);
//        } finally {
//            a.recycle();
//        }
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //输入内容
    public synchronized void setLists (List<String> lists) {
        this.lists = lists;
        listSize = lists.size();
        invalidate();
        //return this;
    }

    private synchronized void initWheelItems (int width,int itemHeight,List<String> lists) {
        wheelItems.clear();
        for (int i = 0; i < showCount + 2; i++) {
            int startY = itemHeight * (i - 1);
            int stringIndex = select - showCount / 2 - 1 + i;
            if (stringIndex < 0) {

                stringIndex = lists.size() + stringIndex;
            }
            if (stringIndex < lists.size()) {
                if (i == 2) {
                    text = lists.get(stringIndex);
                }
                wheelItems.add(new WheelItem(startY,width,itemHeight,fontColor,fontSize,lists.get(stringIndex)));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        Paint mPaint = new Paint();
        mPaint.setTextSize(selectedSize / 2);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        itemHeight = (int) (fontMetrics.bottom - fontMetrics.top) + 2 * padding;
        initWheelItems(width,itemHeight,lists);
        wheelSelect = new WheelSelect(showCount / 2 * itemHeight,width,itemHeight,selectTip,fontColor,fontSize,padding,lineColor);
        height = itemHeight * showCount;
        super.onMeasure(widthMeasureSpec,MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
    }

    private synchronized void handleMove (float dy) {
        for (WheelItem item : wheelItems) {
            item.adjust(dy);
        }
        freshView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :

                if (threadQueue.peek() != null) {
                    threadQueue.poll().interrupt();
                }

                mTouchY = event.getY();
                mTouchY1 = event.getY();
                downTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_MOVE :
                float dy = event.getY() - mTouchY;
                mTouchY = event.getY();

                if (threadQueue.peek() != null) {
                    threadQueue.poll().interrupt();
                }
                handleMove(dy);

                break;
            case MotionEvent.ACTION_UP :
                if (System.currentTimeMillis() - downTime < 200 && Math.abs(event.getY() - mTouchY1) > 100) {
                    slowMove(event.getY() - mTouchY1);
                } else {
                    handleUp();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private synchronized void adjust () {
        if (wheelItems.get(0).getStartY() >= (-itemHeight / 2)) {
            WheelItem item = wheelItems.remove(wheelItems.size() - 1);
            item.setStartY(wheelItems.get(0).getStartY() - itemHeight);
            int index = lists.indexOf(wheelItems.get(0).getText());
            if (index == -1) {
                return ;
            }
            index -= 1;

            if (index < 0) {
                index = lists.size() + index;
            }
            item.setText(lists.get(index));
            wheelItems.add(0,item);
            freshView();
            return ;
        }
        if (wheelItems.get(0).getStartY() <= (-itemHeight / 2 - itemHeight)) {
            WheelItem item = wheelItems.remove(0);
            item.setStartY(wheelItems.get(wheelItems.size() - 1).getStartY() + itemHeight);
            int index = lists.indexOf(wheelItems.get(wheelItems.size() - 1).getText());
            if (index == -1) {
                return;
            }
            index += 1;
            if (index >= lists.size()) {
                index = 0;
            }
            item.setText(lists.get(index));
            wheelItems.add(item);
            freshView();
            return;
        }
    }

    private synchronized void handleUp () {
        int index = -1;
        for (int i = 0; i < wheelItems.size(); i++) {
            WheelItem item =wheelItems.get(i);
            if (item.getStartY() >= wheelSelect.getStartY() && item.getStartY() < (wheelSelect.getStartY() + itemHeight / 2)) {
                index = i;
                break;
            }
            if (item.getStartY() >= (wheelSelect.getStartY() + itemHeight / 2) && item.getStartY() < (wheelSelect.getStartY() + itemHeight)) {
                index = i - 1;
                break;
            }

        }

        if (index == -1) {
            return ;
        }

        float dy = wheelSelect.getStartY() - wheelItems.get(index).getStartY();
        for (WheelItem item :  wheelItems) {
            item.adjust(dy);
        }

        freshView();
        adjust();
        String textCopy = wheelItems.get(index).getText();

       // Log.d(TAG, textCopy);

        if (text == null) {
            text = textCopy;
        } else {
            if (!text.equals(textCopy)) {
                text = textCopy;
                if (listener != null) {
                    listener.listen(text);
                }
            }
        }


    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        this.canvas = canvas;
        canvas.drawColor(Color.parseColor("#bcd2fa"));
        for (WheelItem item : wheelItems) {

            float startY = item.getStartY();
            if (startY <= itemHeight) {
                float distance = Math.abs(startY - itemHeight);
                float fontTimes = distance / itemHeight + 2;
                float alphaTimes = distance / itemHeight +1;
                item.setFontSize((int)(selectedSize / fontTimes));
                item.setAlpha((int)((255 / alphaTimes)));
            } else {
                float distance = Math.abs(startY - itemHeight);
                float fontTimes = distance / itemHeight + 2;
                float alphaTimes = distance /itemHeight + 1;
                item.setFontSize((int)(selectedSize / fontTimes));
                item.setAlpha((int)((255 / alphaTimes)));
            }

            item.onDraw(canvas);
        }

        //选中线
        wheelSelect = new WheelSelect((int) itemHeight,width,itemHeight,null,fontColor,fontSize,linePadding,lineColor);
        wheelSelect.onDraw(canvas);
        adjust();

    }

    private synchronized void slowMove (final float dy) {
        if (threadQueue.peek() != null) {
            threadQueue.poll().interrupt();
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int height = Math.abs((int) dy);
                int distance = 0;
                int distanceCopy = 27;
                while (distance < height * 2) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handleMove(dy > 0 ? distanceCopy : distanceCopy * (-1));
                    distance += distanceCopy;

                    distanceCopy--;
                    if (distanceCopy <= 0) {
                        break;
                    }
                }
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handleUp();

            }
        });
        thread.start();

        threadQueue.add(thread);

    }

    private void freshView () {
        Message message = new Message();
        message.what = FRESH;
        handler.sendMessage(message);
    }

    //字体颜色
    public void setFontColor (int fontColor) {
        this.fontColor = fontColor;
    }

    //选中字体大小
    public void setSelectedSize (int selectedSize) {
        this.selectedSize = selectedSize * 2;
    }

    //线的颜色
    public void setLineColor (int lineColor) {
        this.lineColor = lineColor;
    }

    //获得选中文本
    public String getIndexText () {
        return text;
    }

    //设置索引
    public void setIndex (String text) {
        int index = lists.indexOf(text);
        select = index;
    //    freshView();
    }

    public void setIndexAfterNew (String text) {
        int index = lists.indexOf(text);
        select = index;

        initWheelItems(width,itemHeight,lists);
    }

    //设置选中线颜色
    public void setLinePadding (int linePadding) {
        this.linePadding = linePadding;
    }

    private synchronized void slowMoveCopy (final float dy) {
        if (threadQueue.peek() != null) {
            threadQueue.poll().interrupt();
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int height = Math.abs((int) dy);
                int distance = 0;

//                int speed = height / itemHeight;
//                int distanceCopy = 7 * speed;

                int distanceCopy = 80;

                int onlyOne = 1;
                int onlyOne1 = 1;
                int onlyOne2 = 2;
                while (distance < height) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handleMove(dy > 0 ? distanceCopy : distanceCopy * (-1));
                    distance += distanceCopy;

                    if (distance >= height - itemHeight * 10 && distance < height - itemHeight * 5) {
                        if (onlyOne1 == 1) {
                            onlyOne1 = 0;
                            distanceCopy = 55;
                        }
                    }

                    if (distance >= height - itemHeight * 30 && distance < height - itemHeight * 10) {
                        if (onlyOne2 == 1) {
                            onlyOne2 = 0;
                            distance = 65;
                        }
                    }

                    if (distance >= height - itemHeight * 5) {
                        if (onlyOne == 1) {
                            onlyOne = 0;
                            distanceCopy = 27;
                        }
                        distanceCopy--;
                        if (distanceCopy <= 0) {
                            break;
                        }
                    }
                }
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handleUp();

            }
        });
        thread.start();

        threadQueue.add(thread);

    }

    private void moveToGoal (int distance) {
        int dy = distance * itemHeight;
        slowMoveCopy((float) dy);
    }

    public void smoothTo (String text) {
        int after = lists.indexOf(text);
        int before = lists.indexOf(this.text);

        this.text = text;
//        int distance1 = after - before;
//        int distance2;
//        if (after >= before) {
//            distance2 = listSize  - after + before;
//        } else {
//            distance2 = - (after + listSize - before);
//        }
//        if (Math.abs(distance1) > Math.abs(distance2)) {
//            moveToGoal(distance2);
//        } else {
//            moveToGoal(distance1);
//        }
        moveToGoal(before - after);
    }

}
