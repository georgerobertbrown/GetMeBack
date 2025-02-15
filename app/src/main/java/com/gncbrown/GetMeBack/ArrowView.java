package com.gncbrown.GetMeBack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class ArrowView extends View {

    private Paint paint;
    private Path path;
    private float bearing = 0;

    public ArrowView(Context context) {
        super(context);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear the path
        path.reset();

        // Define the arrow shape
        int width = getWidth();
        int height = getHeight();
        int arrowWidth = width / 2;
        int arrowHeight = height / 2;

        path.moveTo(width / 2, 0); // Top point
        path.lineTo(width, arrowHeight); // Right point
        path.lineTo(width / 2 + arrowWidth / 2, arrowHeight); // Right point
        path.lineTo(width / 2 + arrowWidth / 2, height); // Bottom right point
        path.lineTo(width / 2 - arrowWidth / 2, height); // Bottom left point
        path.lineTo(width / 2 - arrowWidth / 2, arrowHeight); // Left point
        path.lineTo(0, arrowHeight); // Left point
        path.close();

        // Rotate the canvas
        canvas.rotate(bearing, width / 2, height / 2);

        // Draw the arrow
        canvas.drawPath(path, paint);
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
        invalidate(); // Force a redraw
    }
}