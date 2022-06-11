# CalendarView
自定义日历  
1.单选  
2.时间段选择  
# 预览
![单选效果](./ic_preview_02.png)  
![时段选择效果](./ic_preview_01.png)  
# 资源
|名字|资源|
|-|-|
|AAR|[calendar_view.aar](https://github.com/RelinRan/CalendarView/blob/master/calendar_view.aar)|
|Gitee|[CalendarView](https://gitee.com/relin/CalendarView)|
|GitHub | [CalendarView](https://github.com/RelinRan/CalendarView)|
# Maven
1.build.grade | setting.grade
```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```
2./app/build.grade
```
dependencies {
	implementation 'com.github.RelinRan:CalendarView:2022.6.11.1'
}
```
# xml
```
<com.androidx.widget.CalendarView
    android:id="@+id/calendar"
    android:layout_width="match_parent"
    android:background="@android:color/white"
    android:layout_height="320dp"/>
```
# attr.xml
```
<attr name="initYear" format="integer" />
<attr name="initMonth" format="integer" />
<attr name="initDay" format="integer" />
<attr name="isInterval" format="boolean" />
<attr name="showToday" format="boolean" />
<attr name="weekTextSize" format="dimension|reference" />
<attr name="monthDayTextSize" format="dimension|reference" />
<attr name="circleRadius" format="dimension|reference" />
<attr name="weekTextColor" format="color|reference" />
<attr name="lastMonthDayTextColor" format="color|reference" />
<attr name="nowMonthDayTextColor" format="color|reference" />
<attr name="nextMonthDayTextColor" format="color|reference" />
<attr name="nowDayCircleColor" format="color|reference" />
<attr name="checkDayCircleColor" format="color|reference" />
<attr name="checkDayIntervalColor" format="color|reference" />
```
# 单选
```
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
CalendarView calendar = findViewById(R.id.calendar);
//设置当前时间
calendar.setDate(new Date());
//单选
calendar.setInterval(false);
calendar.setOnItemClickListener((calendarView, time) -> {
    String date = dateFormat.format(time);
});
```
# 时段选择
```
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
CalendarView calendar = findViewById(R.id.calendar);
//设置当前时间
calendar.setDate(new Date())
//时段选择
calendar.setInterval(true);
calendar.setOnIntervalSelectListener((view, start, end) -> {
    String startTime = dateFormat.format(start);
    String endTime = dateFormat.format(end);
});
```
