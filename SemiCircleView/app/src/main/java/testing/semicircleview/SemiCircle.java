package testing.semicircleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Saket on 28/7/17.
 */

public class SemiCircle extends View {


    public static int INVALID_VALUE = -1;
    public static final int MAX = 100;
    public static final int MIN = 0;

    /**
     * Offset = -90 indicates that the progress starts from 12 o'clock.
     */
    private static final int ANGLE_OFFSET = -90;

    /**
     * The current points value.
     */
    private int mPoints = MIN;

    /**
     * The min value of progress value.
     */
    private int mMin = MIN;

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private int mMax = MAX;

    /**
     * The increment/decrement value for each movement of progress.
     */
    private int mStep = 10;

    /**
     * The Drawable for the seek arc thumbnail
     */
    //private Drawable mIndicatorIcon;

    private BitmapDrawable mIndicatorIcon;

    private int mProgressWidth = 12;
    private int mArcWidth = 12;
    private boolean mClockwise = true;
    private boolean mEnabled = true;

    //
    // internal variables
    //
    /**
     * The counts of point update to determine whether to change previous progress.
     */
    private int mUpdateTimes = 0;
    private float mPreviousProgress = -1;
    private float mCurrentProgress = 0;

    /**
     * Determine whether reach max of point.
     */
    private boolean isMax = false;

    /**
     * Determine whether reach min of point.
     */
    private boolean isMin = false;

    private int mArcRadius = 0;
    private RectF mArcRect = new RectF();
    private Paint mArcPaint;

    private float mProgressSweep = 0;
    private Paint mProgressPaint;

    private float mTextSize = 72;
    private Paint mTextPaint;
    private Rect mTextRect = new Rect();

    private int mTranslateX;
    private int mTranslateY;

    // the (x, y) coordinator of indicator icon
    private int mIndicatorIconX;
    private int mIndicatorIconY;

    /**
     * The current touch angle of arc.
     */
    private double mTouchAngle;

    float center_x, center_y;
    final RectF oval = new RectF();

    float radius;
    private int width;
    private int height;
    private int textColor;
    private int progressColor;
    private Paint mTickPaint;

    private static final int TICK_COUNT = 48;
    private static final int RING_THICKNESS = 50;
    private static final int TICK_THICKNESS = 10;
    private static final int LONG_TICK = 30;
    private static final int SHORT_TICK = 10;
    final float rotationAnglePerTick = 360f / TICK_COUNT;
    private OnSwagPointsChangeListener mOnSwagPointsChangeListener;
    private Matrix matrix;
    private float[] tan;
    private float[] pos;
    private int distance;
    private int step;
    private PathMeasure pathMeasure;
    private float pathLength;
    private int bm_offsetX;
    private int bm_offsetY;
    float curX, curY;

    float curAngle;  //current angle
    float targetAngle; //target angle
    float stepAngle; //angle each step
    private Canvas canvass;
    private float downXValue;
    private float downYValue;
    private Path path;
    private boolean chkSwipBool = true;

    static int FORWARD = 1;
    static int BACKWARD = -1;
    static int STOP=0;
    int direction;


    public SemiCircle(Context context) {
        super(context);
    }

    public SemiCircle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        float density = getResources().getDisplayMetrics().density;

        // Defaults, may need to link this into theme settings
        int arcColor = ContextCompat.getColor(context, R.color.color_arc);
        int progressColor = ContextCompat.getColor(context, R.color.color_progress);
        int textColor = ContextCompat.getColor(context, R.color.color_text);
        mProgressWidth = (int) (mProgressWidth * density);
        mArcWidth = (int) (mArcWidth * density);
        mTextSize = (int) (mTextSize * density);

        mIndicatorIcon = (BitmapDrawable) ContextCompat.getDrawable(context, R.drawable.pointe);


        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.semiArc, 0, 0);

            Drawable indicatorIcon = a.getDrawable(R.styleable.semiArc_indicatorIcon);
            if (indicatorIcon != null)
                mIndicatorIcon = (BitmapDrawable) indicatorIcon;

            int indicatorIconHalfWidth = mIndicatorIcon.getIntrinsicWidth() / 2;
            int indicatorIconHalfHeight = mIndicatorIcon.getIntrinsicHeight() / 2;

            bm_offsetX = mIndicatorIcon.getBitmap().getWidth() / 2;
            bm_offsetY = mIndicatorIcon.getBitmap().getHeight() - 121;

          /*  mIndicatorIcon.setBounds(-indicatorIconHalfWidth, -indicatorIconHalfHeight, indicatorIconHalfWidth,
                    indicatorIconHalfHeight);*/

            mPoints = a.getInteger(R.styleable.semiArc_points, mPoints);
            mMin = a.getInteger(R.styleable.semiArc_min, mMin);
            mMax = a.getInteger(R.styleable.semiArc_max, mMax);
            mStep = a.getInteger(R.styleable.semiArc_step, mStep);

            mProgressWidth = (int) a.getDimension(R.styleable.semiArc_progressWidth, mProgressWidth);
            progressColor = a.getColor(R.styleable.semiArc_progressColor, progressColor);

            mArcWidth = (int) a.getDimension(R.styleable.semiArc_arcWidth, mArcWidth);
            arcColor = a.getColor(R.styleable.semiArc_arcColor, arcColor);

            mTextSize = (int) a.getDimension(R.styleable.semiArc_textSize, mTextSize);
            textColor = a.getColor(R.styleable.semiArc_textColor, textColor);

            mClockwise = a.getBoolean(R.styleable.semiArc_clockwise,
                    mClockwise);
            mEnabled = a.getBoolean(R.styleable.semiArc_enabled, mEnabled);
            a.recycle();
        }

        // range check
        mPoints = (mPoints > mMax) ? mMax : mPoints;
        mPoints = (mPoints < mMin) ? mMin : mPoints;

        mProgressSweep = (float) mPoints / valuePerDegree();

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);

        mTickPaint = new Paint();
        mTickPaint.setColor(Color.WHITE);
        mTickPaint.setStrokeWidth(TICK_THICKNESS);
        mTickPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(mTextSize);

        step = 12;
        distance = 0;
        pos = new float[2];
        tan = new float[2];

        curX = 0;
        curY = 0;

        stepAngle = 10;
        curAngle = 0;
        targetAngle = 0;
        direction = STOP;



        matrix = new Matrix();
    }

    private float valuePerDegree() {
        return (float) (mMax) / 360.0f;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int min = Math.min(width, height);


     /*   if (width > height) {
            radius = height / 4;
        } else {
            radius = width / 4;
        }
*/
        mTranslateX = (int) (width / 2);
        mTranslateY = (int) (height);

        int arcDiameter = min - getPaddingLeft();
        radius = arcDiameter / 3;

        center_x = width / 2;
        center_y = height;

        oval.set(center_x - radius,
                center_y - radius,
                center_x + radius,
                center_y + radius);

        //updateIndicatorIconPosition();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvass = canvas;
        if (!mClockwise) {
            canvas.scale(-1, 1, oval.centerX(), oval.centerY());
        }


        String textPoint = String.valueOf(mPoints);
        mTextPaint.getTextBounds(textPoint, 0, textPoint.length(), mTextRect);
        // center the text
        int xPos = canvas.getWidth() / 2 - mTextRect.width();
        int yPos = (int) ((oval.centerY()) - ((mTextPaint.descent() + mTextPaint.ascent())));
//		Log.d("onDraw", String.valueOf(mPoints));
        canvas.drawText(String.valueOf(mPoints), xPos, 150, mTextPaint);

        path = new Path();

        path.addArc(oval, 180, 180);

        pathMeasure = new PathMeasure(path, false);
        pathLength = pathMeasure.getLength();


        // canvas.drawArc(oval, 180, 180, false, mArcPaint);

        ScNotches scNotches = new ScNotches(path);
        scNotches.getPainter().setStrokeWidth(9);
        scNotches.setColors(Color.GRAY);
        scNotches.setCount(50);
        scNotches.setLength(180);

        scNotches.draw(canvas);

       /* ScNotches scNotchess = new ScNotches(path);
        scNotchess.getPainter().setStrokeWidth(9);
        scNotchess.setColors( Color.RED);
        scNotchess.setCount(1);
        scNotchess.setLength(180);*/


        //   canvas.drawArc(oval, 180,mProgressSweep, false, mProgressPaint);



/*
        if (mEnabled) {
            // draw the indicator icon
            canvas.translate(mTranslateX - mIndicatorIconX, mTranslateY - mIndicatorIconY);



            mIndicatorIcon.draw(canvas);

        }*/


        matrix.reset();




        if((targetAngle-curAngle)>stepAngle){
            curAngle += stepAngle;
            matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);
            matrix.postTranslate(curX, curY);
            canvas.drawBitmap(mIndicatorIcon.getBitmap(), matrix, null);

            invalidate();
        }else if((curAngle-targetAngle)>stepAngle){

            Log.e("Rounding Image------------==============>","Rotate");

            if (chkSwipBool){

                curAngle = Math.round(-89.0);

            }else {
                curAngle -= stepAngle;
            }
            matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);
            matrix.postTranslate(curX, curY);
            canvas.drawBitmap(mIndicatorIcon.getBitmap(), matrix, null);

            invalidate();
        }else{
            curAngle=targetAngle;

            if((direction==FORWARD && distance < pathLength)
                    ||(direction==BACKWARD && distance > 0)||direction==STOP && distance ==0){

                Log.e("-direction==FORWARD -----------==============>","Rotate");

                pathMeasure.getPosTan(distance, pos, tan);

                targetAngle = (float)(Math.atan2(tan[1], tan[0])*180.0/Math.PI);
                matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);

                curX = pos[0]-bm_offsetX;
                curY = pos[1]-bm_offsetY;
                matrix.postTranslate(curX, curY);

                canvas.drawBitmap(mIndicatorIcon.getBitmap(), matrix, null);

                distance += step * direction;

                invalidate();
            }else{
                matrix.postRotate(curAngle, bm_offsetX, bm_offsetY);
                matrix.postTranslate(curX, curY);
                canvas.drawBitmap(mIndicatorIcon.getBitmap(), matrix, null);
            }
        }




    }




 /*   @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();


      *//*  if (mIndicatorIcon != null && mIndicatorIcon.isStateful()) {
            int[] state = getDrawableState();
            //mIndicatorIcon.setState(state);

            Log.e("drawableStateChanged---Moving---","============>>>>>>>>");
            //moveNiddleTempIncrease();
        }*//*
        //invalidate();
    }*/

    private void updateIndicatorIconPosition() {
        int thumbAngle = (int) (mProgressSweep + 327);
        mIndicatorIconX = (int) (radius * Math.cos(Math.toRadians(thumbAngle)));
        mIndicatorIconY = (int) (radius * Math.sin(Math.toRadians(thumbAngle)));
    }

    public void setPoints(int points) {
        points = points > mMax ? mMax : points;
        points = points < mMin ? mMin : points;
        updateProgress(points, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnabled) {
            // 阻止父View去攔截onTouchEvent()事件，確保touch事件可以正確傳遞到此層View。
            this.getParent().requestDisallowInterceptTouchEvent(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    downXValue = event.getX();
                    downYValue = event.getY();

                    step = 12;
                    if (mOnSwagPointsChangeListener != null)
                        mOnSwagPointsChangeListener.onStartTrackingTouch(this);
//					updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //updateOnTouch(event);

                    float currentX = event.getX();
                    float currentY = event.getY();
                    // check if horizontal or vertical movement was bigger

                    if (Math.abs(downXValue - currentX) > Math.abs(downYValue
                            - currentY)) {
                        Log.v("", "x");
                        // going backwards: pushing stuff to the right
                        if (downXValue < currentX) {
                            Log.v("", "right");


                            direction=FORWARD;

                            invalidate();
                        }

                        // going forwards: pushing stuff to the left
                        if (downXValue > currentX) {
                            Log.v("", "left");

                            chkSwipBool = false;

                            direction=BACKWARD;

                            /*pathMeasure = new PathMeasure(path, false);
                            pathLength = pathMeasure.getLength();*/


                          //  moveNiddleTempDecrease();
                            //moveNiddleTempIncrease();
                            invalidate();


                        }

                    }

                    Log.e("Indicator---Moving---", "============>>>>>>>>");


                    // invalidate();
                    break;
                case MotionEvent.ACTION_UP:

                    step = 0;
                    // moveNiddleTempIncrease();

                    //invalidate();

                    if (mOnSwagPointsChangeListener != null)
                        mOnSwagPointsChangeListener.onStopTrackingTouch(this);
                  /*  setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);*/
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (mOnSwagPointsChangeListener != null)
                        mOnSwagPointsChangeListener.onStopTrackingTouch(this);
                  /*  setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);*/
                    break;
            }
            return true;
        }
        return false;
    }

    /**
     * Update all the UI components on touch events.
     *
     * @param event MotionEvent
     */
    private void updateOnTouch(MotionEvent event) {
        setPressed(true);
        mTouchAngle = convertTouchEventPointToAngle(event.getX(), event.getY());
        int progress = convertAngleToProgress(mTouchAngle);
        updateProgress(progress, true);
    }


    private double convertTouchEventPointToAngle(float xPos, float yPos) {
        // transform touch coordinate into component coordinate
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        x = (mClockwise) ? x : -x;
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2));
        angle = (angle < 0) ? (angle + 360) : angle;
//		System.out.printf("(%f, %f) %f\n", x, y, angle);
        return angle;
    }

    private int convertAngleToProgress(double angle) {
        return (int) Math.round(valuePerDegree() * angle);
    }


    private void updateProgress(int progress, boolean fromUser) {

        // detect points change closed to max or min
        final int maxDetectValue = (int) ((double) mMax * 0.95);
        final int minDetectValue = (int) ((double) mMax * 0.05) + mMin;
//		System.out.printf("(%d, %d) / (%d, %d)\n", mMax, mMin, maxDetectValue, minDetectValue);

        mUpdateTimes++;
        if (progress == INVALID_VALUE) {
            return;
        }

        // avoid accidentally touch to become max from original point
        // 避免在靠近原點點到直接變成最大值
        if (progress > maxDetectValue && mPreviousProgress == INVALID_VALUE) {
//			System.out.printf("Skip (%d) %.0f -> %.0f %s\n",
//					progress, mPreviousProgress, mCurrentProgress, isMax ? "Max" : "");
            return;
        }


        // record previous and current progress change
        // 紀錄目前和前一個進度變化
        if (mUpdateTimes == 1) {
            mCurrentProgress = progress;
        } else {
            mPreviousProgress = mCurrentProgress;
            mCurrentProgress = progress;
        }

//		if (mPreviousProgress != mCurrentProgress)
//			System.out.printf("Progress (%d)(%f) %.0f -> %.0f (%s, %s)\n",
//					progress, mTouchAngle,
//					mPreviousProgress, mCurrentProgress,
//					isMax ? "Max" : "",
//					isMin ? "Min" : "");

        // 不能直接拿progress來做step
        mPoints = progress - (progress % mStep);

        /**
         * Determine whether reach max or min to lock point update event.
         *
         * When reaching max, the progress will drop from max (or maxDetectPoints ~ max
         * to min (or min ~ minDetectPoints) and vice versa.
         *
         * If reach max or min, stop increasing / decreasing to avoid exceeding the max / min.
         */
        // 判斷超過最大值或最小值，最大最小值不重複判斷
        // 用數值範圍判斷預防轉太快直接略過最大最小值。
        // progress變化可能從98 -> 0/1 or 0/1 -> 98/97，而不會過0或100
        if (mUpdateTimes > 1 && !isMin && !isMax) {
            if (mPreviousProgress >= maxDetectValue && mCurrentProgress <= minDetectValue &&
                    mPreviousProgress > mCurrentProgress) {
                isMax = true;
                progress = mMax;
                mPoints = mMax;
//				System.out.println("Reach Max " + progress);
                if (mOnSwagPointsChangeListener != null) {
                    mOnSwagPointsChangeListener
                            .onPointsChanged(this, progress, fromUser);
                    return;
                }
            } else if ((mCurrentProgress >= maxDetectValue
                    && mPreviousProgress <= minDetectValue
                    && mCurrentProgress > mPreviousProgress) || mCurrentProgress <= mMin) {
                isMin = true;
                progress = mMin;
                mPoints = mMin;
//				Log.d("Reach", "Reach Min " + progress);
                if (mOnSwagPointsChangeListener != null) {
                    mOnSwagPointsChangeListener
                            .onPointsChanged(this, progress, fromUser);
                    return;
                }
            }
            invalidate();
        } else {

            // Detect whether decreasing from max or increasing from min, to unlock the update event.
            // Make sure to check in detect range only.
            if (isMax & (mCurrentProgress < mPreviousProgress) && mCurrentProgress >= maxDetectValue) {
//				System.out.println("Unlock max");
                isMax = false;
            }
            if (isMin
                    && (mPreviousProgress < mCurrentProgress)
                    && mPreviousProgress <= minDetectValue && mCurrentProgress <= minDetectValue
                    && mPoints >= mMin) {
//				Log.d("Unlock", String.format("Unlock min %.0f, %.0f\n", mPreviousProgress, mCurrentProgress));
                isMin = false;
            }
        }

        if (!isMax && !isMin) {
            progress = (progress > mMax) ? mMax : progress;
            progress = (progress < mMin) ? mMin : progress;

            if (mOnSwagPointsChangeListener != null) {
                progress = progress - (progress % mStep);

                mOnSwagPointsChangeListener
                        .onPointsChanged(this, progress, fromUser);
            }

            mProgressSweep = (float) progress / valuePerDegree();
//			if (mPreviousProgress != mCurrentProgress)
//				System.out.printf("-- %d, %d, %f\n", progress, mPoints, mProgressSweep);
            updateIndicatorIconPosition();
            invalidate();
        }
    }


    public int getPoints() {
        return mPoints;
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public void setClockwise(boolean isClockwise) {
        mClockwise = isClockwise;
    }

    public boolean isClockwise() {
        return mClockwise;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public int getProgressColor() {
        return mProgressPaint.getColor();
    }

    public void setProgressColor(int color) {
        mProgressPaint.setColor(color);
        invalidate();
    }

    public int getArcColor() {
        return mArcPaint.getColor();
    }

    public void setArcColor(int color) {
        mArcPaint.setColor(color);
        invalidate();
    }

    public void setTextColor(int textColor) {
        mTextPaint.setColor(textColor);
        invalidate();
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int mMax) {
        if (mMax <= mMin)
            throw new IllegalArgumentException("Max should not be less than min.");
        this.mMax = mMax;
    }

    public int getMin() {
        return mMin;
    }

    public void setMin(int min) {
        if (mMax <= mMin)
            throw new IllegalArgumentException("Min should not be greater than max.");
        mMin = min;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        mStep = step;
    }

    public interface OnSwagPointsChangeListener {

        /**
         * Notification that the point value has changed.
         *
         * @param swagPoints The SwagPoints view whose value has changed
         * @param points     The current point value.
         * @param fromUser   True if the point change was triggered by the user.
         */
        void onPointsChanged(SemiCircle swagPoints, int points, boolean fromUser);

        void onStartTrackingTouch(SemiCircle swagPoints);

        void onStopTrackingTouch(SemiCircle swagPoints);
    }

    public void setOnSwagPointsChangeListener(OnSwagPointsChangeListener onSwagPointsChangeListener) {
        mOnSwagPointsChangeListener = onSwagPointsChangeListener;
    }




}
