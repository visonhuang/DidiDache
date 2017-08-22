package com.example.kk.dididache.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.kk.dididache.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by linzongzhan on 2017/8/16.
 */

public class SelectTime extends LinearLayout{

    private WheelView year;
    private WheelView month;
    private WheelView day;
    private WheelView hour;
    private WheelView minute;
    private List<String> listYear = new ArrayList<>();
    private List<String> listMonth = new ArrayList<>();
    private List<String> listDay = new ArrayList<>();
    private List<String> listHour = new ArrayList<>();
    private List<String> listMinute = new ArrayList<>();
    private String indexYear = "2017";
    private String indexMonth = "8";
    private String indexDay = "17";
    private String indexHour = "9";
    private String indexMinute = "2";
    private Calendar calendar = Calendar.getInstance();
    public interface OnDateChangeListener{
        void onChange(Calendar calendar);
    }
    private OnDateChangeListener onDateChangeListener = null;

    public OnDateChangeListener getOnDateChangeListener() {
        return onDateChangeListener;
    }

    public void setOnDateChangeListener(OnDateChangeListener onDateChangeListener) {
        this.onDateChangeListener = onDateChangeListener;
    }

    public SelectTime(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        for (int i = 1970;i <= 2100;i++) {
            listYear.add(i + "");
        }
        for (int i = 1;i <= 12;i++) {
            listMonth.add(i + "");
        }
        for (int i = 1;i <= 31;i++) {
            listDay.add(i + "");
        }
        for (int i = 0;i < 24;i++) {
            listHour.add(i + "");
        }
        for (int i = 0;i < 60;i++) {
            listMinute.add(i + "");
        }

        LayoutInflater.from(context).inflate(R.layout.layout_select_time,this);

        year = (WheelView) findViewById(R.id.wheelview_year);
        month = (WheelView) findViewById(R.id.wheelview_month);
        day = (WheelView) findViewById(R.id.wheelview_day);
        hour = (WheelView) findViewById(R.id.wheelview_hour);
        minute = (WheelView) findViewById(R.id.wheelview_minute);


        year.setLists(listYear);
        month.setLists(listMonth);
        day.setLists(listDay);
        hour.setLists(listHour);
        minute.setLists(listMinute);

        year.setIndex(indexYear);
        month.setIndex(indexMonth);
        day.setIndex(indexDay);
        hour.setIndex(indexHour);
        minute.setIndex(indexMinute);


       // getSelectedTime();

        year.setListener(new WheelView.Listener() {
            @Override
            public void listen(String info) {

                changeDate();

                calendar.set(Calendar.YEAR,(Integer.valueOf(info)));
                if (onDateChangeListener != null) {
                    onDateChangeListener.onChange(calendar);
                }
            }
        });

        month.setListener(new WheelView.Listener() {
            @Override
            public void listen(String info) {

                changeDate();

                calendar.set(Calendar.MONTH,(Integer.valueOf(info)));
                if (onDateChangeListener != null) {
                    onDateChangeListener.onChange(calendar);
                }
            }
        });

        day.setListener(new WheelView.Listener() {
            @Override
            public void listen(String info) {
                calendar.set(Calendar.DATE,Integer.valueOf(info));
                if (onDateChangeListener != null) {
                    onDateChangeListener.onChange(calendar);
                }
            }
        });

        hour.setListener(new WheelView.Listener() {
            @Override
            public void listen(String info) {
                calendar.set(Calendar.HOUR_OF_DAY,Integer.valueOf(info));
                if (onDateChangeListener != null) {
                    onDateChangeListener.onChange(calendar);
                }
            }
        });

        minute.setListener(new WheelView.Listener() {
            @Override
            public void listen(String info) {
                calendar.set(Calendar.MINUTE,Integer.valueOf(info));
                if (onDateChangeListener != null) {
                    onDateChangeListener.onChange(calendar);
                }
            }
        });
    }

    public void setIndex (String Indexyear, String Indexmonth, String Indexday, String Indexhour, String Indexminute) {
        year.setIndex(Indexyear);
        month.setIndex(Indexmonth);
        day.setIndex(Indexday);
        hour.setIndex(Indexhour);
        minute.setIndex(Indexminute);
    }

    public void setIndex (Calendar calendar1) {
        Calendar calendar = (Calendar) calendar1.clone();
        year.setIndex(calendar.get(Calendar.YEAR) + "");
        month.setIndex((calendar.get(Calendar.MONTH) + 1) + "");
        day.setIndex(calendar.get(Calendar.DATE) + "");
        hour.setIndex(calendar.get(Calendar.HOUR_OF_DAY) + "");
        minute.setIndex(calendar.get(Calendar.MINUTE) + "");
    }

    public void setIndexAfterNew (Calendar calendar1) {
        Calendar calendar = (Calendar) calendar1.clone();
        year.setIndexAfterNew(calendar.get(Calendar.YEAR) + "");
        month.setIndexAfterNew((calendar.get(Calendar.MONTH) + 1) + "");
        day.setIndexAfterNew(calendar.get(Calendar.DATE) + "");
        hour.setIndexAfterNew(calendar.get(Calendar.HOUR_OF_DAY) + "");
        minute.setIndexAfterNew(calendar.get(Calendar.MINUTE) + "");
    }

//    public List<String> getSelectedTime () {
//        List<String> time = new ArrayList<>();
//        time.add(year.getIndexText());
//        time.add(month.getIndexText());
//        time.add(day.getIndexText());
//        time.add(hour.getIndexText());
//        time.add(minute.getIndexText());
//        return time;
//    }

    public Calendar getSelectedTime () {
        calendar.set(Calendar.YEAR,Integer.valueOf(year.getIndexText()));
        calendar.set(Calendar.MONTH,Integer.valueOf(month.getIndexText()) - 1);
        calendar.set(Calendar.DATE,Integer.valueOf(day.getIndexText()));
        calendar.set(Calendar.HOUR_OF_DAY,Integer.valueOf(hour.getIndexText()));
        calendar.set(Calendar.MINUTE,Integer.valueOf(minute.getIndexText()));
        return (Calendar)calendar.clone();
    }


    public void setHeight (int height) {

        year.setSelectedSize(height);
        month.setSelectedSize(height);
        day.setSelectedSize(height);
        hour.setSelectedSize(height);
        minute.setSelectedSize(height);
    }

    public void setLinePadding (int linePadding) {
        year.setLinePadding(linePadding);
        month.setLinePadding(linePadding);
        day.setLinePadding(linePadding);
        hour.setLinePadding(linePadding);
        minute.setLinePadding(linePadding);
    }

    public void smoothToGoal1 (Calendar calendar1) {
        Calendar calendar = (Calendar) calendar1.clone();
        String yearT = calendar.get(Calendar.YEAR) + "";
        String monthT = (calendar.get(Calendar.MONTH) + 1) + "";
        String dayT = calendar.get(Calendar.DATE) + "";
        String hourT = calendar.get(Calendar.HOUR_OF_DAY) + "";
        String minuteT = calendar.get(Calendar.MINUTE) + "";

      //  Log.d("月份",monthT);
        Log.d("年",yearT);
        Log.d("月",monthT);
        Log.d("日",dayT);
        Log.d("时",hourT);
        Log.d("分",minuteT);

        int day2 = 28;
        String dayNow = dayT;
        int yearNum = Integer.parseInt(yearT);
        if ((yearNum % 4 == 0 && yearNum % 100 != 0) || yearNum % 400 == 0) {
            day2 = 29;
        }
        int monthNum = Integer.parseInt(monthT);
        if (monthNum == 1 || monthNum == 3 || monthNum == 5 || monthNum == 7 || monthNum == 8 || monthNum == 10 || monthNum == 12) {
            List<String> list = new ArrayList<>();
            for (int i = 1;i <= 31;i++) {
                list.add(i + "");
            }
            day.setLists(list);
        }
        if (monthNum == 4 || monthNum == 6 || monthNum == 9 || monthNum == 11) {
            List<String> list = new ArrayList<>();
            for (int i = 1;i <= 30;i++) {
                list.add(i + "");
            }
            day.setLists(list);
        }
        if (monthNum == 2) {
            List<String> list = new ArrayList<>();
            for (int i = 1;i <= day2;i++) {
                list.add(i + "");
            }
            day.setLists(list);

        }


        year.smoothTo(yearT);
        month.smoothTo(monthT);
        day.smoothTo(dayT);
        hour.smoothTo(hourT);
        minute.smoothTo(minuteT);


    }

    public void smoothToGoal (Calendar calendar1) {
        smoothToGoal1(calendar1);
        smoothToGoal1(calendar1);
    }

    private void changeDate () {
        int day2 = 28;
        String dayNow = day.getIndexText();
        int yearNum = Integer.parseInt(year.getIndexText());
        if ((yearNum % 4 == 0 && yearNum % 100 != 0) || yearNum % 400 == 0) {
            day2 = 29;
        }
        int monthNum = Integer.parseInt(month.getIndexText());
        if (monthNum == 1 || monthNum == 3 || monthNum == 5 || monthNum == 7 || monthNum == 8 || monthNum == 10 || monthNum == 12) {
            List<String> list = new ArrayList<>();
            for (int i = 1;i <= 31;i++) {
                list.add(i + "");
            }
            day.setLists(list);
            day.setIndexAfterNew(dayNow);
        }
        if (monthNum == 4 || monthNum == 6 || monthNum == 9 || monthNum == 11) {
            List<String> list = new ArrayList<>();
            for (int i = 1;i <= 30;i++) {
                list.add(i + "");
            }
            day.setLists(list);
            if (dayNow.equals("31")) {
                day.setIndexAfterNew("30");
            } else {
                day.setIndexAfterNew(dayNow);
            }
        }
        if (monthNum == 2) {
            List<String> list = new ArrayList<>();
            for (int i = 1;i <= day2;i++) {
                list.add(i + "");
            }
            day.setLists(list);
            if (day2 == 29) {
                if (dayNow.equals("31") || dayNow.equals("30")) {
                    day.setIndexAfterNew("29");
                } else {
                    day.setIndexAfterNew(dayNow);
                }
            }
            if (day2 == 28) {
                if (dayNow.equals("31") || dayNow.equals("30") || dayNow.equals("29")) {
                    day.setIndexAfterNew("28");
                } else {
                    day.setIndexAfterNew(dayNow);
                }
            }
        }
    }
}
