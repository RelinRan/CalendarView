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
    private boolean interval = true;
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
    private int lastMonthDayTextColor = Color.parseColor("#A8A8A8");
    //当前月天文字颜色
    private int nowMonthDayTextColor = Color.parseColor("#333333");
    //下个月天文字颜色
    private int nextMonthDayTextColor = Color.parseColor("#A8A8A8");
    //当天颜色
    private int nowDayCircleColor = Color.parseColor("#EA3B3B");
    //不可选文字颜色
    private int disableTextColor = Color.parseColor("#EEEEEE");
    //选中颜色
    private int checkDayCircleColor = Color.parseColor("#1982F3");
    //选中区间颜色
    private int checkDayIntervalColor = Color.parseColor("#E9E7E7");
    //区间选中显示类型
    private int intervalShape = 1;
    //移动距离促发条件
    private float absMove = dip(15);

    /**
     * 图形-圆
     */
    public static int SHAPE_CIRCLE = 1;
    /**
     * 图形 - 矩形
     */
    public static int SHAPE_RECT = 2;
    /**
     * 月份滑动方式-水平方向
     */
    public static int HORIZONTAL = 1;
    /**
     * 月份滑动方式 - 垂直方向
     */
    public static int VERTICAL = 2;
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
    //开始时间
    private long intervalStart = -1;
    //结束时间
    private long intervalEnd = -1;
    //选中时间
    private long checkTime = -1;
    //最大时间
    private long maxTime = -1;
    //最小时间
    private long minTime = -1;
    //月份滑动方式
    private int monthMode = HORIZONTAL;

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
            disableTextColor = array.getColor(R.styleable.CalendarView_disableTextColor, disableTextColor);
            intervalShape = array.getInt(R.styleable.CalendarView_intervalShape, intervalShape);
            absMove = array.getDimension(R.styleable.CalendarView_absMove, absMove);
            monthMode = array.getInt(R.styleable.CalendarView_monthMode, HORIZONTAL);
            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        unitWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / getCalendarTitle().length;
        unitHeight = (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 7;
    }

    /**
     * 点击坐标
     */
    private float dx, dy;
    /**
     * 移动坐标
     */
    private float oldScrollX, oldScrollY;
    /**
     * X - 移动方向
     */
    private int directionX;
    /**
     * Y - 移动方向
     */
    private int directionY;
    /**
     * X - 是否移动
     */
    private boolean isMoveX;
    /**
     * Y - 是否移动
     */
    private boolean isMoveY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dx = event.getX();
                dy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float scrollX = event.getX() - dx;
                float scrollY = event.getY() - dy;
                float absMx = Math.abs(scrollX);
                float absMy = Math.abs(scrollY);
                isMoveX = absMx > absMy && absMx > absMove && absMy > absMove;
                isMoveY = absMx < absMy && absMx > absMove && absMy > absMove;
                directionX = scrollX > 0 ? 1 : -1;
                directionY = scrollY > 0 ? -1 : 1;
                if (onCalendarScrollChangeListener != null) {
                    onCalendarScrollChangeListener.onCalendarScrollChange(this, scrollX, scrollY, oldScrollX, oldScrollY);
                }
                oldScrollX = scrollX;
                oldScrollY = scrollY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                if (monthMode == 1 && isMoveX && !isMoveY) {
                    if (directionX == -1) {
                        calendar.add(Calendar.MONTH, 1);
                    }
                    if (directionX == 1) {
                        calendar.add(Calendar.MONTH, -1);
                    }
                    setMonth(calendar.get(Calendar.MONTH) + 1);
                    if (onCalendarChangeListener != null) {
                        onCalendarChangeListener.onCalendarChange(this, calendar.getTime());
                    }
                }
                if (monthMode == 2 && isMoveY && !isMoveX) {
                    if (directionY == -1) {
                        calendar.add(Calendar.MONTH, -1);
                    }
                    if (directionY == 1) {
                        calendar.add(Calendar.MONTH, 1);
                    }
                    setMonth(calendar.get(Calendar.MONTH) + 1);
                    if (onCalendarChangeListener != null) {
                        onCalendarChangeListener.onCalendarChange(this, calendar.getTime());
                    }
                }
                if (!isMoveX && !isMoveY) {
                    onTouchItemEvent(event);
                }
                break;
        }
        return true;
    }

    public interface OnCalendarScrollChangeListener {

        /**
         * 滑动监听事件
         *
         * @param v          View
         * @param scrollX    x-滑动距离
         * @param scrollY    y-滑动距离
         * @param oldScrollX x-上次滑动距离
         * @param oldScrollY y-上次滑动距离
         */
        void onCalendarScrollChange(View v, float scrollX, float scrollY, float oldScrollX, float oldScrollY);
    }

    private OnCalendarScrollChangeListener onCalendarScrollChangeListener;

    /**
     * 设置滑动监听事件
     *
     * @param onCalendarScrollChangeListener
     */
    public void setOnCalendarScrollChangeListener(OnCalendarScrollChangeListener onCalendarScrollChangeListener) {
        this.onCalendarScrollChangeListener = onCalendarScrollChangeListener;
    }

    private OnCalendarChangeListener onCalendarChangeListener;

    /**
     * 设置日期改变监听
     *
     * @param onCalendarChangeListener
     */
    public void setOnCalendarChangeListener(OnCalendarChangeListener onCalendarChangeListener) {
        this.onCalendarChangeListener = onCalendarChangeListener;
    }

    public interface OnCalendarChangeListener {

        /**
         * 日历改变监听
         *
         * @param v    日历View
         * @param date 日期时间
         */
        void onCalendarChange(CalendarView v, Date date);

    }

    /**
     * Item点击事件
     *
     * @param event 触摸事件
     */
    private void onTouchItemEvent(MotionEvent event) {
        checkDay = findTouchDay(event.getX(), event.getY());
        if (checkDay != null && isEnableSelect(checkDay.getTime())) {
            //判读是否是区间选择
            if (interval) {
                if (endDay != null && startDay != null) {
                    startDay = checkDay;
                    intervalStart = startDay.getTime();
                    endDay = null;
                    intervalEnd = -1;
                } else {
                    if (startDay == null) {
                        startDay = checkDay;
                        intervalStart = startDay.getTime();
                    }
                    if (endDay == null && startDay != null) {
                        if (startDay.getTime() > checkDay.getTime()) {
                            startDay = checkDay;
                            endDay = startDay;
                            intervalStart = startDay.getTime();
                            intervalEnd = endDay.getTime();
                        }
                        if (startDay.getTime() < checkDay.getTime()) {
                            endDay = checkDay;
                            intervalEnd = endDay.getTime();
                        }
                        if (startDay.getTime() == checkDay.getTime()){
                            endDay = null;
                            intervalEnd = -1;
                        }
                    }
                    if (endDay != null && startDay == null && endDay.getTime() != checkDay.getTime()) {
                        if (endDay.getTime() > checkDay.getTime()) {
                            startDay = checkDay;
                            endDay = startDay;
                            intervalStart = startDay.getTime();
                            intervalEnd = endDay.getTime();
                        }
                        if (endDay.getTime() < checkDay.getTime()) {
                            startDay = endDay;
                            endDay = checkDay;
                        }
                        intervalStart = startDay.getTime();
                        intervalEnd = endDay.getTime();
                        if (endDay.getTime() == checkDay.getTime()){
                            startDay = null;
                            intervalStart = -1;
                        }
                    }
                }
                if (onIntervalSelectListener != null && startDay != null && endDay != null) {
                    onIntervalSelectListener.onCalendarIntervalSelected(this, toDate(startDay.getDay()), toDate(endDay.getDay()));
                }
            } else {
                checkTime = checkDay.getTime();
                if (onItemSelectListener != null) {
                    onItemSelectListener.onCalendarItemSelected(this, toDate(checkDay.getDay()));
                }
            }
            invalidate();
        }
    }

    /**
     * @param time
     * @return 是否可选时间
     */
    private boolean isEnableSelect(long time) {
        if (minTime == -1 && maxTime == -1) {
            return true;
        }
        if (minTime != -1 && maxTime == -1) {
            return time >= minTime;
        }
        if (minTime == -1 && maxTime != -1) {
            return time <= maxTime;
        }
        return time >= minTime && time <= maxTime;
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
            float x = unitWidth * i + (unitWidth / 2.0F - bounds.width() / 2.0F) + getPaddingLeft();
            float y = unitHeight / 2.0F + getPaddingTop();
            canvas.drawText(weekName, x, y, paint);
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
        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH) + 1;
        int nowDay = day == 0 ? calendar.get(Calendar.DAY_OF_MONTH) : day;
        for (int i = 0; i < days.size(); i++) {
            int row = i / 7;
            int position = i % 7;
            int type = days.get(i).getType();
            int day = days.get(i).getDay();
            String dayText = String.valueOf(days.get(i).getDay());
            if (type == Day.LAST_MONTH) {
                days.get(i).setTextColor(lastMonthDayTextColor);
                days.get(i).setDate(formatDate(year, month - 1, day));
            }
            if (type == Day.NOW_MONTH) {
                days.get(i).setTextColor(nowMonthDayTextColor);
                days.get(i).setDate(formatDate(year, month, day));
            }
            if (type == Day.NEXT_MONTH) {
                days.get(i).setTextColor(nextMonthDayTextColor);
                days.get(i).setDate(formatDate(year, month + 1, day));
            }
            long time = parseDate(days.get(i).getDate()).getTime();
            if (!isEnableSelect(time)) {
                days.get(i).setTextColor(disableTextColor);
            }
            days.get(i).setTime(time);
            Rect bounds = new Rect();
            paint.getTextBounds(dayText, 0, dayText.length(), bounds);
            float x = unitWidth * position + unitWidth / 2.0F - bounds.centerX() + getPaddingLeft();
            float y = unitHeight + unitHeight * row + unitHeight / 2.0F - bounds.centerY() + getPaddingTop();
            days.get(i).setX(x);
            days.get(i).setY(y);
            days.get(i).setPosition(i);
            //圆坐标
            float cx = unitWidth * position + unitWidth / 2.0F + getPaddingLeft();
            float cy = unitHeight + unitHeight * row + unitHeight / 2.0F + getPaddingTop();
            days.get(i).setCx(cx);
            days.get(i).setCy(cy);
            if (isSameDay(time, checkTime)) {
                checkDay = days.get(i);
            }
            if (isSameDay(time, intervalStart)) {
                startDay = days.get(i);
            }
            if (isSameDay(time, intervalEnd)) {
                endDay = days.get(i);
            }
        }
        for (int i = 0; i < days.size(); i++) {
            int type = days.get(i).getType();
            int day = days.get(i).getDay();
            float cx = days.get(i).getCx();
            float cy = days.get(i).getCy();
            float x = days.get(i).getX();
            float y = days.get(i).getY();
            paint.setColor(days.get(i).getTextColor());
            String dayText = String.valueOf(days.get(i).getDay());
            //当前天
            if (showToday && type == Day.NOW_MONTH && year == nowYear && nowMonth == month && day == nowDay) {
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
                boolean hasInterval = startDay != null && endDay != null;
                boolean isStart = startDay != null && dayTime == startDay.getTime();
                boolean isEnd = endDay != null && dayTime == endDay.getTime();
                if (startDay != null && endDay != null) {
                    long statTime = startDay.getTime();
                    long endTime = endDay.getTime();
                    if (dayTime > statTime && dayTime < endTime) {
                        if (intervalShape == SHAPE_RECT) {
                            drawDayRect(canvas, 0, cx, cy, checkDayIntervalColor);
                        } else {
                            drawDayCircle(canvas, cx, cy, checkDayIntervalColor);
                        }
                        paint.setColor(Color.WHITE);
                    }
                }
                if (isStart) {
                    if (intervalShape == SHAPE_RECT && hasInterval) {
                        drawDayRect(canvas, -1, cx, cy, checkDayIntervalColor);
                    }
                    drawDayCircle(canvas, cx, cy, checkDayCircleColor);
                    paint.setColor(Color.WHITE);
                }
                if (isEnd) {
                    if (intervalShape == SHAPE_RECT && hasInterval) {
                        drawDayRect(canvas, 1, cx, cy, checkDayIntervalColor);
                    }
                    drawDayCircle(canvas, cx, cy, checkDayCircleColor);
                    paint.setColor(Color.WHITE);
                }
            }
            canvas.drawText(dayText, x, y, paint);
        }
    }

    /**
     * @param time   时间
     * @param millis 毫秒时间
     * @return 是否为同一天
     */
    private boolean isSameDay(long time, long millis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date(time)).equals(dateFormat.format(new Date(millis)));
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
     * 绘制天矩形背景
     *
     * @param canvas 画布
     * @param type   类型，-1：开始,0：中间 ,1:结尾
     * @param cx     中心x
     * @param cy     中心y
     * @param color  颜色
     */
    private void drawDayRect(Canvas canvas, int type, float cx, float cy, int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (type == -1) {
            canvas.drawRect(cx, cy - circleRadius, cx + unitWidth / 2, cy + circleRadius, paint);
        }
        if (type == 0) {
            canvas.drawRect(cx - unitWidth / 2, cy - circleRadius, cx + unitWidth / 2, cy + circleRadius, paint);
        }
        if (type == 1) {
            canvas.drawRect(cx - unitWidth / 2, cy - circleRadius, cx, cy + circleRadius, paint);
        }
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
     * 获取选中天
     *
     * @return
     */
    public Day getCheckDay() {
        return checkDay;
    }


    public Day getStartDay() {
        return startDay;
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

    /**
     * 设置开始时间
     *
     * @param millis
     */
    public void setCheckTime(long millis) {
        if (isEnableSelect(millis)) {
            this.checkTime = millis;
            invalidate();
        }
    }

    /**
     * 设置开始时间
     *
     * @param text 时间文字,格式 yyyy-MM-dd
     */
    public void setCheckTime(String text) {
        setCheckTime(text, "yyyy-MM-dd");
    }

    /**
     * 设置开始时间
     *
     * @param text    时间文字
     * @param pattern 格式 yyyy-MM-dd
     */
    public void setCheckTime(String text, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            setCheckTime(dateFormat.parse(text).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 选中时间
     */
    public long getCheckTime() {
        return checkTime;
    }

    /**
     * 设置开始时间
     *
     * @param millis
     */
    public void setIntervalStart(long millis) {
        if (isEnableSelect(millis)) {
            this.intervalStart = millis;
            invalidate();
        }
    }

    /**
     * 设置开始时间
     *
     * @param text 时间文字,格式 yyyy-MM-dd
     */
    public void setIntervalStart(String text) {
        setIntervalStart(text, "yyyy-MM-dd");
    }

    /**
     * 设置开始时间
     *
     * @param text    时间文字,格式 yyyy-MM-dd
     * @param pattern 时间文字,格式 yyyy-MM-dd
     */
    public void setIntervalStart(String text, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            setIntervalStart(dateFormat.parse(text).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 开始时间
     */
    public long getIntervalStart() {
        return intervalStart;
    }

    /**
     * 设置结束时间
     *
     * @param millis 结束时间
     */
    public void setIntervalEnd(long millis) {
        if (isEnableSelect(millis)) {
            this.intervalEnd = millis;
            invalidate();
        }
    }

    /**
     * 设置开始时间
     *
     * @param text 时间文字,格式：yyyy-MM-dd
     */
    public void setIntervalEnd(String text) {
        setIntervalEnd(text, "yyyy-MM-dd");
    }

    /**
     * 设置开始时间
     *
     * @param text    时间文字
     * @param pattern 格式：yyyy-MM-dd
     */
    public void setIntervalEnd(String text, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            setIntervalEnd(dateFormat.parse(text).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 结束时间
     */
    public long getIntervalEnd() {
        return intervalEnd;
    }

    /**
     * 设置最大时间
     *
     * @param maxTime
     */
    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
        invalidate();
    }

    /**
     * 设置最大时间
     *
     * @param text 时间文字,格式：yyyy-MM-dd
     */
    public void setMaxTime(String text) {
        setMaxTime(text, "yyyy-MM-dd");
    }

    /**
     * 设置最大时间
     *
     * @param text    时间文字
     * @param pattern 格式：yyyy-MM-dd
     */
    public void setMaxTime(String text, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            setMaxTime(dateFormat.parse(text).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置最小时间
     *
     * @param minTime
     */
    public void setMinTime(long minTime) {
        this.minTime = minTime;
        invalidate();
    }

    /**
     * 设置最小时间
     *
     * @param text 时间文字,格式：yyyy-MM-dd
     */
    public void setMinTime(String text) {
        setMinTime(text, "yyyy-MM-dd");
    }

    /**
     * 设置最小时间
     *
     * @param text    时间文字
     * @param pattern 格式：yyyy-MM-dd
     */
    public void setMinTime(String text, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            setMinTime(dateFormat.parse(text).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置禁用颜色
     *
     * @param disableTextColor
     */
    public void setDisableTextColor(int disableTextColor) {
        this.disableTextColor = disableTextColor;
        invalidate();
    }

    /**
     * 设置区间图形样式
     *
     * @param intervalShape {@link CalendarView#SHAPE_CIRCLE}
     */
    public void setIntervalShape(int intervalShape) {
        this.intervalShape = intervalShape;
        invalidate();
    }

    /**
     * 设置月份滑动模式
     *
     * @param monthMode 滑动模式{@link #HORIZONTAL}
     */
    public void setMonthMode(int monthMode) {
        this.monthMode = monthMode;
        invalidate();
    }

    /**
     * @return 月份滑动模式
     */
    public int getMonthMode() {
        return monthMode;
    }

    /**
     * 设置移动距离促发条件
     * @param absMove
     */
    public void setAbsMove(float absMove) {
        this.absMove = absMove;
        invalidate();
    }

    /**
     * @return 设置移动距离促发条件
     */
    public float getAbsMove() {
        return absMove;
    }
}


