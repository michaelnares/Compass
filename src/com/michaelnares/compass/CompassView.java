package com.michaelnares.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashMap;

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

    protected void initCompassView()
    {
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

        int glassColor = 245;
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
    public void onDraw(Canvas canvas)
    {
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

        while (rollDegree > 180 || rollDegree < -180) {
            if (rollDegree > 180) {
                rollDegree = -180 + (rollDegree - 180);
            }

            if (rollDegree < -180) {
                rollDegree = 180 - (rollDegree + 180);
            }
        }// while loop ends here

        Path skyPath = new Path();
        skyPath.addArc(innerBoundingBox, -tiltDegree, (180 + 2 * tiltDegree));
        canvas.save();
        canvas.rotate(-rollDegree, px, py);
        canvas.drawOval(innerBoundingBox, groundPaint);
        canvas.drawPath(skyPath, skyPaint);
        canvas.drawPath(skyPath, markerPaint);

        int markWidth = radius / 3; // face marking next
        int startX = center.x - markWidth;
        int endX = center.x + markWidth;

        //Below calculation is to make sure the pitch scale always starts at the current value.
        double h = innerRadius * Math.cos(Math.toRadians(90-tiltDegree));
        double justTiltY = center.y - h;
        float pxPerDegree = (innerBoundingBox.height()/2)/45f;

        for (int i = 90; i >= -90; i -= 10)
        {
            double yPos = justTiltY + i * pxPerDegree;

            //Only display the scale within the inner face.
            if (yPos < (innerBoundingBox.top + textHeight) || yPos > (innerBoundingBox.bottom - textHeight));
            //Draw a line and the title angle for each scale increment
            canvas.drawLine(startX, (float)yPos, endX, (float)yPos, markerPaint);
            int displayPos = (int)tiltDegree - i;
            String displayString = String.valueOf(displayPos);
            float stringSizeWidth = textPaint.measureText(displayString);
            canvas.drawText(displayString, (int)(center.x - (stringSizeWidth/2)), (int)yPos + 1, textPaint);
        } // ends for loop
        markerPaint.setStrokeWidth(2);
        canvas.drawLine(center.x - radius/2, (float)justTiltY, center.x + radius/2, (float)justTiltY, markerPaint);
        markerPaint.setStrokeWidth(1);

        Path rollArrow = new Path();
        rollArrow.moveTo(center.x - 3, (int)innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, (int)innerBoundingBox.top + 10);
        rollArrow.moveTo(center.x + 3, (int)innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, (int)innerBoundingBox.top + 10);
        canvas.drawPath(rollArrow, markerPaint);

        String rollText = String.valueOf(rollDegree);
        double rollTextWidth = textPaint.measureText(rollText);
        canvas.drawText(rollText, (float)(center.x - (rollTextWidth/2)), innerBoundingBox.top + textHeight + 2, textPaint);
        canvas.restore();
        canvas.save();
        canvas.rotate(180, center.x, center.y);

        for (int i = -180; i < 180; i +=10)
        {
            //Show a numeric value every 30 degrees
          if (i % 30 == 0)
            {
                String rollString = String.valueOf(i* -1); // display a numeric value that is the opposite to the current rotation
                float rollStringWidth = textPaint.measureText(rollString);
                PointF rollStringCenter = new PointF(center.x-(rollStringWidth/2), (innerBoundingBox.top) + 1 + textHeight);
                canvas.drawText(rollString, rollStringCenter.x, rollStringCenter.y, textPaint);
            } // ends if block
           else
          {
                canvas.drawLine(center.x, (int)innerBoundingBox.top, center.x, ((int)innerBoundingBox.top) + 5, markerPaint);
            }
            canvas.rotate(10, center.x, center.y);
        } // ends for loop
        canvas.restore();
        canvas.save();
       canvas.rotate((-1 * (bearing)), px, py);
        double increment = 22.5;

        HashMap<Integer, String> compassDirections = new HashMap<Integer, String>();
        compassDirections.put(0, "N");
        compassDirections.put(1, "NNE");
        compassDirections.put(2, "NE");
        compassDirections.put(3, "ENE");
        compassDirections.put(4, "E");
        compassDirections.put(5, "ESE");
        compassDirections.put(6, "SE");
        compassDirections.put(7, "SSE");
        compassDirections.put(8, "S");
        compassDirections.put(9, "SSW");
        compassDirections.put(10, "SW");
        compassDirections.put(11, "WSW");
        compassDirections.put(12, "W");
        compassDirections.put(13, "WNW");
        compassDirections.put(14, "NW");
        compassDirections.put(15, "NNW");
        
        for (double i = 0; i < 360; i += increment)
        {
            double doubleIndex = i/22.5;
            int intIndex = (int)doubleIndex;
            String headString = compassDirections.get(intIndex);
            if (headString != null)
            {
            float headStringWidth = textPaint.measureText(headString);
            PointF headStringCenter = new PointF(center.x - (headStringWidth/2), boundingBox.top + 1 + textHeight);
            if (i % increment == 0)
                {
                canvas.drawText(headString, headStringCenter.x, headStringCenter.y, textPaint);
                }
                else
                {
                canvas.drawLine(center.x, (int)boundingBox.top, center.x, (int)boundingBox.top + 3, markerPaint);
                canvas.rotate((int)increment, center.x, center.y);
                }
            } // ends if block for if headString != null
        }// ends for loop
        canvas.restore();
        RadialGradient glassShader = new RadialGradient(px, py, (int)innerRadius,
                glassGradientColors, glassGradientPositions, Shader.TileMode.CLAMP);
        Paint glassPaint = new Paint();
        glassPaint.setShader(glassShader);
        canvas.drawOval(innerBoundingBox, glassPaint);
        canvas.drawOval(boundingBox, circlePaint); // outer ring
        canvas.drawOval(innerBoundingBox, circlePaint);
        }
}


