package com.example.lich;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;

public class AnalogClockView extends View {
    private Paint paint;
    private int width, height;
    private float radius, centerX, centerY;
    private RectF rectF;
    private TimeUpdateListener timeUpdateListener;

    public interface TimeUpdateListener {
        void onTimeUpdate(String formattedTime);
    }

    public void setTimeUpdateListener(TimeUpdateListener listener) {
        this.timeUpdateListener = listener;
    }

    public AnalogClockView(Context context) {
        super(context);
        init();
    }

    public AnalogClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        rectF = new RectF();
    }
    //mặt đồng hồ và kim giờ, phút, giây
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        centerX = width / 2f;
        centerY = height / 2f;
        radius = Math.min(width, height) * 0.4f;
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //mặt đồng hồ
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
        canvas.drawCircle(centerX, centerY, radius, paint);

        //kim giờ và phút
        paint.setTextSize(40f);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 1; i <= 12; i++) {
            String num = String.valueOf(i);
            float angle = (float) Math.toRadians(i * 30 - 90);
            float x = centerX + (radius - 50) * (float) Math.cos(angle);
            float y = centerY + (radius - 50) * (float) Math.sin(angle);
            canvas.drawText(num, x - paint.measureText(num)/2, y + 15, paint);
        }

        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        // Thông báo thời gian qua interface
        if (timeUpdateListener != null) {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            timeUpdateListener.onTimeUpdate(formattedTime);
        }

        //kim giờ
        drawHand(canvas, (hours * 30) + (minutes * 0.5f), radius * 0.5f, Color.BLACK, 8f);

        //kim phút
        drawHand(canvas, minutes * 6 + (seconds * 0.1f), radius * 0.7f, Color.BLACK, 6f);

        //kim giây
        drawHand(canvas, seconds * 6, radius * 0.8f, Color.RED, 3f);


        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawCircle(centerX, centerY, 10, paint);

        postInvalidateDelayed(1000);
    }

    private void drawHand(Canvas canvas, float angle, float length, int color, float strokeWidth) {
        float radian = (float) Math.toRadians(angle - 90);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawLine(centerX, centerY,
                centerX + length * (float) Math.cos(radian),
                centerY + length * (float) Math.sin(radian),
                paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }
}