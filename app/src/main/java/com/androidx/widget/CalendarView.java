package com.androidx.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日历间隔
 */
public class CalendarView extends View {

    //年
    private int year = 2022;
    //月
    private int month = 6;
    //日
    private int day = 1;
    //是否区间选择
    private boolean interval = false;
    //是否显示今天
    private boolean showToday = true;
    //星期文字大小
    private float weekTextSize = dip(14);
    //天文字大小
    private float monthDayTextSize = dip(14);
    //半径
    private float circleRadius = dip(15);
    //星期文字颜色
    private int weekTextColor = Color.parseColor("#0076F6");
    //上个月天文字颜色
    private int lastMonthDayTextColor = Color.parseColor("#838383");
    //当前月天文字颜色
    private int nowMonthDayTextColor = Color.parseColor("#333333");
    //下个月天文字颜色
    private int nextMonthDayTextColor = Color.parseColor("#838383");
    //当天颜色
    private int nowDayCircleColor = Color.parseColor("#EA3B3B");
    //选中颜色
    private int checkDayCircleColor = Color.parseColor("#1982F3");
    //选中区间颜色
    private int checkDayIntervalColor = Color.parseColor("#E9E7E7");

    //单位宽度
    private float unitWidth;
    //单位高度
    private float unitHeight;
    //日历天
    private List<Day> days;
    //选中天
    private Day checkDay;
    //开始
    private Day startDay;
    //结束
    private Day endDay;

    public CalendarView(Context context) {
        super(context);
        initAttributeSet(context, null);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributeSet(context, attrs);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributeSet(context, attrs);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
            Calendar calendar = Calendar.getInstance();
            year = array.getInt(R.styleable.CalendarView_initYear, calendar.get(Calendar.YEAR));
            month = array.getInt(R.styleable.CalendarView_initMonth, calendar.get(Calendar.MONTH) + 1);
            day = array.getInt(R.styleable.CalendarView_initDay, calendar.get(Calendar.DAY_OF_MONTH));
            interval = array.getBoolean(R.styleable.CalendarView_isInterval, interval);
            showToday = array.getBoolean(R.styleable.CalendarView_showToday, showToday);
            weekTextSize = array.getDimension(R.styleable.CalendarView_weekTextSize, weekTextSize);
            monthDayTextSize = array.getDimension(R.styleable.CalendarView_monthDayTextSize, monthDayTextSize);
            circleRadius = array.getDimension(R.styleable.CalendarView_circleRadius, circleRadius);
            weekTextColor = array.getColor(R.styleable.CalendarView_weekTextColor, weekTextColor);
            lastMonthDayTextColor = array.getColor(R.styleable.CalendarView_lastMonthDayTextColor, lastMonthDayTextColor);
            nowMonthDayTextColor = array.getColor(R.styleable.CalendarView_nowMonthDayTextColor, nowMonthDayTextColor);
            nextMonthDayTextColor = array.getColor(R.styleable.CalendarView_nextMonthDayTextColor, nextMonthDayTextColor);
            nowDayCircleColor = array.getColor(R.styleable.CalendarView_nowDayCircleColor, nowDayCircleColor);
            checkDayCircleColor = array.getColor(R.styleable.CalendarView_checkDayCircleColor, checkDayCircleColor);
            checkDayIntervalColor = array.getColor(R.styleable.CalendarView_checkDayIntervalColor, checkDayIntervalColor);
            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        unitWidth = getMeasuredWidth() / getCalendarTitle().length;
        unitHeight = getMeasuredHeight() / 7;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                checkDay = findTouchDay(event.getX(), event.getY());
                if (checkDay != null) {
                    //判读是否是区间选择
                    if (interval) {
                        if (endDay != null && startDay != null) {
                            startDay = checkDay;
                            endDay = null;
                        } else {
                            if (endDay == null && startDay != null) {
                                boolean isConversion = startDay.getTime() > checkDay.getTime();
                                if (isConversion) {
                                    endDay = startDay;
                                    startDay = checkDay;
                                } else {
                                    endDay = checkDay;
                                }
                            }
                            if (startDay == null) {
                                startDay = checkDay;
                            }
                        }
                        if (onIntervalSelectListener != null && startDay != null && endDay != null) {
                            onIntervalSelectListener.onCalendarIntervalSelected(this, toDate(startDay.getDay()), toDate(endDay.getDay()));
                        }
                    } else {
                        if (onItemSelectListener != null) {
                            onItemSelectListener.onCalendarItemSelected(this, toDate(checkDay.getDay()));
                        }
                    }
                    invalidate();
                }
                break;
        }
        return true;
    }

    /**
     * @param day 天
     * @return 日期
     */
    private Date toDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    /**
     * @param value
     * @return 转dp
     */
    private float dip(int value) {
        return getResources().getDisplayMetrics().density * value;
    }

    /**
     * 找到点击的day
     *
     * @param x 坐标X
     * @param y 坐标y
     * @return
     */
    private Day findTouchDay(float x, float y) {
        for (int i = 0; i < days.size(); i++) {
            float dayX = days.get(i).getX();
            float dayY = days.get(i).getY();
            if (x < dayX + unitHeight / 2 && x > dayX - unitHeight / 2 && y < dayY + unitHeight / 2 && y > dayY - unitHeight / 2) {
                return days.get(i);
            }
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawWeekText(canvas);
        drawDayOfMonth(canvas, year, month);
    }

    /**
     * 绘制星期字体
     *
     * @param canvas 画布
     */
    private void drawWeekText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(weekTextSize);
        paint.setColor(weekTextColor);
        String weekNames[] = getCalendarTitle();
        for (int i = 0; i < weekNames.length; i++) {
            String weekName = weekNames[i];
            Rect bounds = new Rect();
            paint.getTextBounds(weekName, 0, weekName.length(), bounds);
            canvas.drawText(weekName, unitWidth * i + (unitWidth / 2.0F - bounds.width() / 2.0F), unitHeight / 2.0F, paint);
        }
    }

    /**
     * 绘制对应月份的天数
     *
     * @param canvas 画布
     * @param year   年
     * @param month  月
     */
    private void drawDayOfMonth(Canvas canvas, int year, int month) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(monthDayTextSize);
        //绘制上个月的天数
        days = getCalendarDays(year, month);
        int nowDay = day == 0 ? getNowDay() : day;
        for (int i = 0; i < days.size(); i++) {
            int row = i / 7;
            int position = i % 7;
            int type = days.get(i).getType();
            int day = days.get(i).getDay();
            if (type == Day.LAST_MONTH) {
                paint.setColor(lastMonthDayTextColor);
                days.get(i).setDate(formatDate(year, month - 1, day));
            }
            if (type == Day.NOW_MONTH) {
                paint.setColor(nowMonthDayTextColor);
                days.get(i).setDate(formatDate(year, month, day));
            }
            if (type == Day.NEXT_MONTH) {
                paint.setColor(nextMonthDayTextColor);
                days.get(i).setDate(formatDate(year, month + 1, day));
            }
            days.get(i).setTime(parseDate(days.get(i).getDate()).getTime());
            String dayText = String.valueOf(days.get(i).getDay());
            Rect bounds = new Rect();
            paint.getTextBounds(dayText, 0, dayText.length(), bounds);
            float x = unitWidth * position + unitWidth / 2.0F - bounds.centerX();
            float y = unitHeight + unitHeight * row + unitHeight / 2.0F - bounds.centerY();
            days.get(i).setX(x);
            days.get(i).setY(y);
            days.get(i).setPosition(i);
            //圆坐标
            float cx = unitWidth * position + unitWidth / 2.0F;
            float cy = unitHeight + unitHeight * row + unitHeight / 2.0F;
            //当前天
            if (showToday && type == Day.NOW_MONTH && day == nowDay) {
                drawDayCircle(canvas, cx, cy, nowDayCircleColor);
                paint.setColor(Color.WHITE);
            }
            //选中天
            if (!interval && checkDay != null && type == checkDay.getType() && day == checkDay.getDay()) {
                drawDayCircle(canvas, cx, cy, checkDayCircleColor);
                paint.setColor(Color.WHITE);
            }
            //区间
            if (interval) {
                long dayTime = days.get(i).getTime();
                boolean isStart = startDay != null && dayTime == startDay.getTime();
                boolean isEnd = endDay != null && dayTime == endDay.getTime();
                if (isStart) {
                    drawDayCircle(canvas, cx, cy, checkDayCircleColor);
                    paint.setColor(Color.WHITE);
                }
                if (startDay != null && endDay != null) {
                    long statTime = startDay.getTime();
                    long endTime = endDay.getTime();
                    if (dayTime > statTime && dayTime < endTime) {
                        drawDayCircle(canvas, cx, cy, checkDayIntervalColor);
                        paint.setColor(Color.WHITE);
                    }
                }
                if (isEnd) {
                    drawDayCircle(canvas, cx, cy, checkDayCircleColor);
                    paint.setColor(Color.WHITE);
                }
            }
            canvas.drawText(dayText, x, y, paint);
        }
    }

    /**
     * 格式化日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return
     */
    public String formatDate(int year, int month, int day) {
        DecimalFormat format = new DecimalFormat("00");
        return year + "-" + format.format(month) + "-" + format.format(day);
    }

    /**
     * 转换时间
     *
     * @param date
     * @return
     */
    public Date parseDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    /**
     * 绘制天圆背景
     *
     * @param canvas 画布
     * @param cx     中心x
     * @param cy     中心y
     * @param color  颜色
     */
    private void drawDayCircle(Canvas canvas, float cx, float cy, int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawCircle(cx, cy, circleRadius, paint);
    }

    /**
     * 找到上个月需要显示的天数
     *
     * @param year  年
     * @param month 月
     * @return
     */
    public int[] getLastMonthEndDays(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        int nowMonth = month - 1;
        calendar.set(Calendar.MONTH, nowMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        //月第一天周几
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        //上个月需要显示的天数，如果日历需要第一天是日就-2;
        int lastMonthDayCount = week - 1;
        lastMonthDayCount = lastMonthDayCount == -1 ? 6 : lastMonthDayCount;
        int[] days = new int[lastMonthDayCount];
        //设置为上一个月
        calendar.set(Calendar.MONTH, nowMonth - 1);
        //获取上一个月天数
        int maximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = maximum; i > maximum - lastMonthDayCount; i--) {
            days[maximum - i] = i;
        }
        Arrays.sort(days);
        return days;
    }

    /**
     * 获取当前月天数
     *
     * @param year  年
     * @param month 月
     * @return
     */
    public int[] getNowMonthDays(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        //月第一天周几
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        //月天数
        int maximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int[] days = new int[maximum];
        for (int i = 0; i < maximum; i++) {
            days[i] = i + 1;
        }
        return days;
    }

    /**
     * 获取下个月显示的天数
     *
     * @param lastMonthDays 上个月天数
     * @param nowMonthDays  当前月天数
     * @return
     */
    public int[] geNextMonthStartDays(int lastMonthDays, int nowMonthDays) {
        //需求是6行7列
        int reqRows = 6, reqColumns = 7;
        int sumDays = lastMonthDays + nowMonthDays;
        int dayCount = reqRows * reqColumns - sumDays;
        int days[] = new int[dayCount];
        for (int i = 0; i < dayCount; i++) {
            days[i] = i + 1;
        }
        return days;
    }

    /**
     * 获取日历标题
     *
     * @return
     */
    public String[] getCalendarTitle() {
        return new String[]{"一", "二", "三", "四", "五", "六", "日"};
    }

    /**
     * 获取6行7列日历天数 "一", "二", "三", "四", "五", "六", "日"
     *
     * @param year  年
     * @param month 月
     * @return
     */
    public List<Day> getCalendarDays(int year, int month) {
        List<Day> days = new ArrayList<>();
        //上个月
        int lastMonthDays[] = getLastMonthEndDays(year, month);
        for (int i = 0; i < lastMonthDays.length; i++) {
            Day day = new Day();
            day.setType(Day.LAST_MONTH);
            day.setDay(lastMonthDays[i]);
            days.add(day);
        }
        //当前月
        int nowMonthDays[] = getNowMonthDays(year, month);
        for (int i = 0; i < nowMonthDays.length; i++) {
            Day day = new Day();
            day.setType(Day.NOW_MONTH);
            day.setDay(nowMonthDays[i]);
            days.add(day);
        }
        //下个月
        int nextMonthDays[] = geNextMonthStartDays(lastMonthDays.length, nowMonthDays.length);
        for (int i = 0; i < nextMonthDays.length; i++) {
            Day day = new Day();
            day.setType(Day.NEXT_MONTH);
            day.setDay(nextMonthDays[i]);
            days.add(day);
        }
        return days;
    }

    /**
     * 获取当前日期
     *
     * @return
     */
    public int getNowDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }


    /**
     * 日历时段选择监听
     */
    public interface OnIntervalSelectListener {

        /**
         * 日历时段选择监听
         *
         * @param view  日历对象
         * @param start 开始
         * @param end   结束
         */
        void onCalendarIntervalSelected(CalendarView view, Date start, Date end);

    }

    /**
     * 区域选择监听
     */
    private OnIntervalSelectListener onIntervalSelectListener;

    /**
     * 设置区域选择监听
     *
     * @param onIntervalSelectListener
     */
    public void setOnIntervalSelectListener(OnIntervalSelectListener onIntervalSelectListener) {
        this.onIntervalSelectListener = onIntervalSelectListener;
    }

    public interface OnItemSelectListener {

        /**
         * 单个item选择事件
         *
         * @param calendarView 日历view
         * @param time         单选天
         */
        void onCalendarItemSelected(CalendarView calendarView, Date time);

    }

    /**
     * Item选择事件
     */
    private OnItemSelectListener onItemSelectListener;

    /**
     * 设置Item选择事件
     *
     * @param onItemSelectListener
     */
    public void setOnItemClickListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    /**
     * 获取当前年
     *
     * @return
     */
    public int getYear() {
        return year;
    }

    /**
     * 获取当前月
     *
     * @return
     */
    public int getMonth() {
        return month;
    }

    /**
     * 重置选中
     */
    public void reset() {
        startDay = null;
        endDay = null;
        checkDay = null;
        invalidate();
    }

    /**
     * 是否显示今天
     *
     * @param showToday
     */
    public void setShowToday(boolean showToday) {
        this.showToday = showToday;
        invalidate();
    }

    /**
     * 设置选中
     *
     * @param checkDay
     */
    public void setCheckDay(Day checkDay) {
        this.checkDay = checkDay;
        invalidate();
    }

    /**
     * 获取选中天
     *
     * @return
     */
    public Day getCheckDay() {
        return checkDay;
    }

    /**
     * 设置区间开始
     *
     * @param startDay
     */
    public void setStartDay(Day startDay) {
        this.startDay = startDay;
        invalidate();
    }

    public Day getStartDay() {
        return startDay;
    }

    /**
     * 设置区间结束
     *
     * @param endDay
     */
    public void setEndDay(Day endDay) {
        this.endDay = endDay;
        invalidate();
    }

    public Day getEndDay() {
        return endDay;
    }

    /**
     * 设置是否区间选择
     *
     * @param interval
     */
    public void setInterval(boolean interval) {
        this.interval = interval;
        invalidate();
    }

    /**
     * 设置年份
     *
     * @param year
     */
    public void setYear(int year) {
        this.year = year;
        invalidate();
    }

    /**
     * 设置月份
     *
     * @param month
     */
    public void setMonth(int month) {
        this.month = month;
        invalidate();
    }

    /**
     * 设置天
     *
     * @param day
     */
    public void setDay(int day) {
        this.day = day;
        invalidate();
    }

    /**
     * 设置年月
     *
     * @param month
     */
    public void setYearMonth(int year, int month) {
        this.year = year;
        this.month = month;
        invalidate();
    }

    /**
     * 设置日期
     *
     * @param date 日期
     */
    public void setDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setYearMonthDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 设置年月日
     *
     * @param month
     */
    public void setYearMonthDay(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        invalidate();
    }

    /**
     * 设置天文字大小
     *
     * @param monthDayTextSize
     */
    public void setMonthDayTextSize(int monthDayTextSize) {
        this.monthDayTextSize = monthDayTextSize;
        invalidate();
    }

    /**
     * 设置上个月文字颜色
     *
     * @param lastMonthDayTextColor
     */
    public void setLastMonthDayTextColor(int lastMonthDayTextColor) {
        this.lastMonthDayTextColor = lastMonthDayTextColor;
        invalidate();
    }

    /**
     * 设置当前月文字颜色
     *
     * @param nowMonthDayTextColor
     */
    public void setNowMonthDayTextColor(int nowMonthDayTextColor) {
        this.nowMonthDayTextColor = nowMonthDayTextColor;
        invalidate();
    }

    /**
     * 设置下个月文字颜色
     *
     * @param nextMonthDayTextColor
     */
    public void setNextMonthDayTextColor(int nextMonthDayTextColor) {
        this.nextMonthDayTextColor = nextMonthDayTextColor;
        invalidate();
    }

    /**
     * 设置当天背景圆颜色
     *
     * @param nowDayCircleColor
     */
    public void setNowDayCircleColor(int nowDayCircleColor) {
        this.nowDayCircleColor = nowDayCircleColor;
        invalidate();
    }

    /**
     * 设置选择背景圆颜色
     *
     * @param checkDayCircleColor
     */
    public void setCheckDayCircleColor(int checkDayCircleColor) {
        this.checkDayCircleColor = checkDayCircleColor;
        invalidate();
    }

    /**
     * 设置区间背景圆颜色
     *
     * @param checkDayIntervalColor
     */
    public void setCheckDayIntervalColor(int checkDayIntervalColor) {
        this.checkDayIntervalColor = checkDayIntervalColor;
        invalidate();
    }

}


