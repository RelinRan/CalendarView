package com.androidx.widget;

/**
 * 天实体
 */
public class Day {

    /**
     * 上个月
     */
    public static final int LAST_MONTH = -1;
    /**
     * 当前月
     */
    public static final int NOW_MONTH = 0;
    /**
     * 下个月
     */
    public static final int NEXT_MONTH = 1;
    /**
     * 位置
     */
    private int position;
    /**
     * 坐标
     */
    float x, y;
    private float cx, cy;
    /**
     * 天
     */
    private int day;
    /**
     * 类型{@link #LAST_MONTH}
     */
    private int type;

    /**
     * 日期字符串
     */
    private String date;
    /**
     * 日期
     */
    private long time;

    private int textColor;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getCx() {
        return cx;
    }

    public void setCx(float cx) {
        this.cx = cx;
    }

    public float getCy() {
        return cy;
    }

    public void setCy(float cy) {
        this.cy = cy;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}