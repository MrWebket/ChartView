package com.demo.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

@SuppressLint("DrawAllocation")
public class ChartView extends View {

	private static final String TAG = ChartView.class.getSimpleName();

	private boolean isDebug = true;

	private static final int DEFAULT_RADIUS_SIZE = 3;

	private static final int DEFAULT_PADDING_SIZE = 5;
	private static final int DEFAULT_EQUALLY_COUNT = 4;

	private static final int DEFAULT_TEXT_SIZE = 15;

	private int height;
	private int width;

	private int mDefaultColor = Color.BLUE;

	private int mTextColor = Color.WHITE;

	private float[] param;

	private float radiusSize;
	private float paddingSize;

	private float mMaxValue;
	private float mMinValue;

	private float mEquallySize;
	private int mEquallyCount = DEFAULT_EQUALLY_COUNT;

	private Paint mTextPaint;

	private float mTextSize = DEFAULT_TEXT_SIZE;

	private float mMaxTextWidth;

	private Paint mLinePaint;

	private float mTextHeight;

	private Paint mCirclePaint;
	
	private String mStartTime = "2015-01-01";
	private String mEndTime  = "2015-12-31";;

	public ChartView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ChartView(Context context, AttributeSet attr) {
		super(context, attr);

		final TypedArray array = getContext().obtainStyledAttributes(attr, R.styleable.ChartView);

		mDefaultColor = array.getColor(R.styleable.ChartView_defaultColor, Color.BLUE);
		mTextColor = array.getColor(R.styleable.ChartView_defaultTextColor, Color.WHITE);

		radiusSize = array.getFloat(R.styleable.ChartView_radiusSize, 0);
		paddingSize = array.getFloat(R.styleable.ChartView_paddingSize, 0);

		mTextSize = array.getFloat(R.styleable.ChartView_defaultTextSize, 0);

		if (radiusSize == 0) {
			radiusSize = dip2px(getContext(), DEFAULT_RADIUS_SIZE);
		}
		if (paddingSize == 0) {
			paddingSize = dip2px(getContext(), DEFAULT_PADDING_SIZE);
		}

		array.recycle();

		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true); // 消除锯齿
		mTextPaint.setStyle(Style.FILL);
		mTextPaint.setColor(mTextColor);
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mLinePaint = new Paint();
		mLinePaint.setAntiAlias(true); // 消除锯齿
		mLinePaint.setStyle(Style.FILL);
		mLinePaint.setColor(mTextColor);
		mLinePaint.setStrokeWidth(3);

		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true); // 消除锯齿
		mCirclePaint.setStyle(Style.FILL);
		mCirclePaint.setColor(mTextColor);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		if (width == 0) {
			width = getWidth();
		}
		if (height == 0) {
			height = getHeight();
		}
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawColor(mDefaultColor);

		int left = getLeft();
		int top = getTop();
		if (isDebug) {
			Log.d(TAG, "left = " + left);
			Log.d(TAG, "top = " + top);
			Log.d(TAG, "height = " + height);
		}
		
		Rect bounds = new Rect();
		mTextPaint.getTextBounds(mStartTime, 0, mStartTime.length(), bounds);

		mTextHeight = bounds.height();
		
		float validStartY = height - paddingSize / 2 - mTextHeight - paddingSize;
		float validWidth = width - left - paddingSize;

		float validHeight = height - paddingSize * 2 - mTextHeight;
		float totalY = validHeight / mEquallyCount;
		
		for (int i = 0; i < mEquallyCount; i++) {
			String text = String.valueOf(getFormatValue((mMinValue + i * mEquallySize) >= mMaxValue ? mMaxValue : mMinValue + i * mEquallySize));
			float textWidth = mTextPaint.measureText(text);

			try {
				float y = ((mEquallyCount - i) * totalY) + mTextHeight;

				if (isDebug) {
					Log.d(TAG, "text = " + text);
					Log.d(TAG, "mMinValue = " + mMinValue);
				}
				if (i == 0) {
					textWidth = mTextPaint.measureText(String.valueOf(mMinValue));
					canvas.drawText(String.valueOf(mMinValue), textWidth / 2 + left + paddingSize, validStartY, mTextPaint);
					continue;
				}
				canvas.drawText(text, textWidth / 2 + left + paddingSize, y, mTextPaint);
				if (i == mEquallyCount - 1) {
					textWidth = mTextPaint.measureText(String.valueOf(mMaxValue));
					canvas.drawText(String.valueOf(mMaxValue), textWidth / 2 + left + paddingSize, paddingSize + mTextHeight, mTextPaint);
				}
			} finally {
				if (textWidth > mMaxTextWidth) {
					mMaxTextWidth = textWidth;
				}
			}
		}
		
		float mConsumeWidth = mMaxTextWidth + paddingSize * 2 + left; // 已经使用的宽度
		
		for (int i = 1; i < mEquallyCount ; i++) {
			float y = ((mEquallyCount - i) * totalY) + mTextHeight;
			
			canvas.drawLine(mConsumeWidth, y - mTextHeight / 2, validWidth , y - mTextHeight / 2, mLinePaint);
			
		}
		//
		
		float textWidth = mTextPaint.measureText(mStartTime);
		
		canvas.drawText(mStartTime, mConsumeWidth, height - paddingSize / 2, mTextPaint);
		
		
		//Y轴
		canvas.drawLine(mConsumeWidth , validStartY, mConsumeWidth, paddingSize , mLinePaint);
		
		float stopX = validWidth;
		
		if (param != null && param.length > 0) {

			float splitWidth = (width - mConsumeWidth - paddingSize) / (param.length - 1);

			for (int i = 0; i < param.length; i++) {

				float value = param[i];

				float startX = mConsumeWidth + i * splitWidth;

				float startY = validHeight - ((value - mMinValue) / (mMaxValue  - mMinValue) * validHeight);

				if (startY == 0 || startY < paddingSize + mTextHeight / 2) {
					startY = paddingSize + mTextHeight / 2;
				}

				canvas.drawCircle(startX, startY == 0 ? paddingSize + mTextHeight / 2 : startY, radiusSize, mCirclePaint);

				if (isDebug) {
					Log.d(TAG, "mConsumeWidth = " + mConsumeWidth);
					Log.d(TAG, "startX = " + startX);
					Log.d(TAG, "startY = " + startY);
					Log.d(TAG, "value = " + value);
				}

				if (i == param.length - 1) {
					//X轴
					canvas.drawLine(mConsumeWidth , validStartY , stopX, validStartY, mLinePaint);
					
					textWidth = mTextPaint.measureText(mEndTime);
					canvas.drawText(mEndTime, stopX - textWidth / 2, height - paddingSize / 2, mTextPaint);
					return;
				}
				stopX = startX + splitWidth;

				float stopY = validHeight - ((param[i + 1] - mMinValue) / (mMaxValue - mMinValue ) * validHeight);

				if (stopY == 0 || stopY < paddingSize + mTextHeight / 2) {
					stopY = paddingSize + mTextHeight / 2;
				}

				if (isDebug) {
					Log.d(TAG, "stopX = " + stopX);
					Log.d(TAG, "stopY = " + stopY);
				}

				canvas.drawLine(startX, startY, stopX, stopY, mLinePaint);
			}
		}
	}

	public void setDataSource(float[] param) {
		this.param = param;
		mMaxValue = getFormatValue(getCalculateValue(this.param)[0]);
		mMinValue = getFormatValue(getCalculateValue(this.param)[1]);

		mEquallySize = Float.valueOf(getFormatValue((mMaxValue - mMinValue) / (float) mEquallyCount));

		if (isDebug) {
			Log.d(TAG, "mMaxValue = " + mMaxValue);
			Log.d(TAG, "mMinValue = " + mMinValue);
			Log.d(TAG, "mEquallySize = " + mEquallySize);
		}
		invalidate();
	}

	private float[] getCalculateValue(float[] param) {
		int size = param.length;

		float max = 0;
		float min = Integer.MAX_VALUE;
		for (int i = 0; i < size; i++) {
			if (param[i] > max) {
				max = param[i];
			}
			if (param[i] < min) {
				min = param[i];
			}
		}
		return new float[] { max, min };
	}

	@SuppressLint("DefaultLocale")
	private float getFormatValue(double d) {
		return Float.parseFloat(String.format("%.2f", d));
	}
	
	
	public void setDomain(String startTime, String endTime) {
		this.mStartTime = startTime;
		this.mEndTime = endTime;
		
		invalidate();
	}

	/**
	 * 将dip或dp值转换为px值
	 * 
	 * @param dipValue
	 * @return
	 */
	private int dip2px(Context context, float dipValue) {
		if (context == null) {
			return 0;
		}
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
}
