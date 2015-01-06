package com.michaelnares.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by michael on 18/11/2014.
 */
public class CompassView extends View
{
    public CompassView(Context context) {
        super(context);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context, AttributeSet ats, int defaultStyle) {
        super(context, ats, defaultStyle);
        initCompassView();
    }

    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String eastString;
    private String southString;
    private String westString;
    private int textHeight;

    private int[] borderGradientColors, glassGradientColors;
    private float[] borderGradientPositions, glassGradientPositions;

    private int skyHorizonColorFrom, skyHorizonColorTo, groundHorizonColorFrom, groundHorizonColorTo;

    private float pitch;

    public void setPitch(float pitch) {
        this.pitch = pitch;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getPitch() {
        return pitch;
    }

    private float roll;

    public void setRoll(float roll) {
        this.roll = roll;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getRoll() {
        return roll;
    }

    protected void initCompassView() {
        setFocusable(true);

        Resources r = this.getResources();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(r.getColor(R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.STROKE);

        northString = r.getString(R.string.cardinal_north);
        eastString = r.getString(R.string.cardinal_east);
        southString = r.getString(R.string.cardinal_south);
        westString = r.getString(R.string.cardinal_west);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(r.getColor(R.color.text_color));
        textPaint.setFakeBoldText(true);
        textPaint.setSubpixelText(true);
        textPaint.setTextAlign(Paint.Align.LEFT);

        textHeight = (int) textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(r.getColor(R.color.marker_color));
        markerPaint.setAlpha(200);
        markerPaint.setStrokeWidth(1);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setShadowLayer(2, 1, 1, R.color.shadow_color);

        borderGradientColors = new int[4];
        borderGradientPositions = new float[4];

        borderGradientColors[3] = r.getColor(R.color.outer_border);
        borderGradientColors[2] = r.getColor(R.color.inner_border_one);
        borderGradientColors[1] = r.getColor(R.color.inner_border_two);
        borderGradientColors[0] = r.getColor(R.color.inner_border);
        borderGradientPositions[3] = 0.0f;
        borderGradientPositions[2] = 1 - 0.03f;
        borderGradientPositions[1] = 1 - 0.06f;
        borderGradientPositions[0] = 1.0f;

        glassGradientColors = new int[5];
        glassGradientPositions = new float[5];

        final int glassColor = 245;
        glassGradientColors[4] = Color.argb(65, glassColor, glassColor, glassColor);
        glassGradientColors[3] = Color.argb(100, glassColor, glassColor, glassColor);
        glassGradientColors[2] = Color.argb(50, glassColor, glassColor, glassColor);
        glassGradientColors[1] = Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientColors[0] = Color.argb(0, glassColor, glassColor, glassColor);

        glassGradientPositions[4] = 1 - 0.0f;
        glassGradientPositions[3] = 1 - 0.06f;
        glassGradientPositions[2] = 1 - 0.10f;
        glassGradientPositions[1] = 1 - 0.20f;
        glassGradientPositions[0] = 1 - 1.0f;

        skyHorizonColorFrom = r.getColor(R.color.horizon_sky_from);
        skyHorizonColorTo = r.getColor(R.color.horizon_sky_to);
    }

    private enum CompassDirection
    {
        N, NNE, NE, ENE,
        E, ESE, SE, SSE,
        S, SSW, SW, WSW,
        W, WNW, NW, NNW
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // fills as much space as possible
        int measureWidth = measure(widthMeasureSpec);
        int measureHeight = measure(heightMeasureSpec);

        int d = Math.min(measureWidth, measureHeight);

        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec)
    {
        int result;

        // decode the measurement specifications
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // return a default size if no bounds are specified
            result = 200;
        } else {
            result = specSize;
        }
        return result;
    } // ends measure method

    private float bearing;

    public void setBearing(float bearing) {
        this.bearing = bearing;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getBearing() {
        return bearing;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        if (isShown()) {
            String bearingStr = String.valueOf(bearing);
            if (bearingStr.length() > AccessibilityEvent.MAX_TEXT_LENGTH)
                bearingStr = bearingStr.substring(0, AccessibilityEvent.MAX_TEXT_LENGTH);

            event.getText().add(bearingStr);
            return true;
        } // ends if block
        else return false;
    }

    @Override
    public void onDraw(Canvas canvas) {
        float ringWidth = textHeight / 4;
        int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();

        int px = mMeasuredWidth / 2; // centre widthwise
        int py = mMeasuredHeight / 2; // centre heightwise
        Point center = new Point(px, py);

        int radius = Math.min(px, py);

        RectF boundingBox = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        RectF innerBoundingBox = new RectF(center.x - radius + ringWidth, center.y - radius + ringWidth,
                center.x + radius - ringWidth, center.y + radius - ringWidth);

        float innerRadius = innerBoundingBox.height() / 2;
        RadialGradient borderGradient = new RadialGradient(px, py, radius, borderGradientColors, borderGradientPositions, Shader.TileMode.CLAMP);

        Paint pgb = new Paint();
        pgb.setShader(borderGradient);

        Path outerRingPath = new Path();
        outerRingPath.addOval(boundingBox, Path.Direction.CW);

        canvas.drawPath(outerRingPath, pgb);

        LinearGradient skyShader = new LinearGradient(center.x, innerBoundingBox.top, center.x, innerBoundingBox.bottom, skyHorizonColorFrom,
                skyHorizonColorTo, Shader.TileMode.CLAMP);

        Paint skyPaint = new Paint();
        skyPaint.setShader(skyShader);

        LinearGradient groundShader = new LinearGradient(center.x, innerBoundingBox.top, center.x, innerBoundingBox.bottom, groundHorizonColorFrom,
                groundHorizonColorTo, Shader.TileMode.CLAMP);

        Paint groundPaint = new Paint();
        groundPaint.setShader(groundShader);

        float tiltDegree = pitch;

        //Normalise the pitch and roll values to keep them between 90 degrees and 180 degrees respectively.
        while (tiltDegree > 90 || tiltDegree < -90) {
            if (tiltDegree > 90) {
                tiltDegree = -90 + (tiltDegree - 90);
            }

            if (tiltDegree < -90) {
                tiltDegree = 90 - (tiltDegree + 90);
            }
        }// while loop ends here

        float rollDegree = roll;

        while (rollDegree > 180 || rollDegree < -180)
        {
            if (rollDegree > 180)
            {
                rollDegree = -180 + (rollDegree - 180);
            }

            if (rollDegree < -180)
            {
                rollDegree = 180 - (rollDegree + 180);
            }
        }// while loop ends here

        Path skyPath = new Path()
    }
}


