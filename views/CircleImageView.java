package com.sky.redhome.views;

import android.annotation.SuppressLint;
import android.content.Context;  
import android.graphics.Bitmap;  
import android.graphics.Bitmap.Config;  
import android.graphics.Canvas;  
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;  
import android.graphics.PorterDuff.Mode;  
import android.graphics.PorterDuffXfermode;  
import android.graphics.Rect;  
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;  
import android.graphics.drawable.Drawable;  
import android.util.AttributeSet;  
import android.util.Log;
import android.view.View;
import android.widget.ImageView;  
  
/** 
 * 圆形的Imageview & 白色边缘
 * 
 * @author bingyang.djj & sky
 *  
 *  控件再用圆形美工格杀勿论！
 */  
public class CircleImageView extends ImageView {  
	
	final static int BORDER_VALUE = 10;
	
    private Paint paint = new Paint();  
  
    public CircleImageView(Context context) {  
        super(context);  
    }  
  
    public CircleImageView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  
  
    @Override  
    protected void onDraw(Canvas canvas) {  
  
        Drawable drawable = getDrawable();  
        if (null != drawable) {  
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();  
            Bitmap b = toRoundCorner(bitmap, 14); 
            
            int viewWidth  = this.getHeight();       // 获得控件View的高度
            
           
            paint.reset();  
            //width 需要用原图的长宽，因为新的切割后已经缩小了width
            int width = b.getWidth();
            int height = b.getHeight();
                        
            int newWidth = viewWidth - BORDER_VALUE;
            int newHeight = viewWidth - BORDER_VALUE;
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 得到新的图片
            Bitmap newbm = Bitmap.createBitmap(b, 0, 0, width, height, matrix,
            true);
            //填图背景
            float x = viewWidth/2;
            paint.setAntiAlias(true);  
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x , x , x  , paint);
            
            paint.reset();
            // 放在画布上
            canvas.drawBitmap(newbm, BORDER_VALUE/2, BORDER_VALUE/2, paint);
                                             
/*            final Rect rect = new Rect(0, 0, width, height);          
            canvas.drawBitmap(newbm, rect, rect, paint);*/
        } else {  
            super.onDraw(canvas);  
        }  
    }  
  
    private Bitmap toRoundCorner(Bitmap bitmap, int pixels) {  
    	int width = bitmap.getWidth();
    	int height = bitmap.getHeight();
        
        float min = (width < height?width:height);        
        float roundPx;
        float left,top,right,bottom;
        if (width <= height) {
                roundPx = width / 2;
                top = 0;
                bottom = width;
                left = 0;
                right = width;
                height = width;
        } else {
                roundPx = height / 2;
                float clip = (width - height) / 2;
                left = clip;
                right = width - clip;
                top = 0;
                bottom = height;
                width = height;
        }

        Log.i("toRounde", 0 + "  "+ 0+ " "+ min+" "+min);        
        Bitmap output = Bitmap.createBitmap(width,
                height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		 
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
		final Rect dst = new Rect((int)0, (int)0, (int)min, (int)min);
		final RectF rectF = new RectF(dst);
		
		paint.setAntiAlias(true);
		 
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx , roundPx, paint);
		
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
    }  
}  