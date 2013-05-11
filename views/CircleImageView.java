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
            int width = bitmap.getWidth();
            int height = b.getHeight();
                        
            int newWidth = viewWidth;
            int newHeight = viewWidth;
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
            canvas.drawBitmap(newbm, 0, 0, paint);
                                 
/*            final Rect rect = new Rect(0, 0, width, height);          
            canvas.drawBitmap(newbm, rect, rect, paint);*/
        } else {  
            super.onDraw(canvas);  
        }  
    }  
  
    private Bitmap toRoundCorner(Bitmap bitmap, int pixels) {  
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),  
                bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
          
        final int color = 0xff424242;  
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        float x = bitmap.getWidth() /2;  
        canvas.drawCircle(x , x , x - 8 , paint);  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
        
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        

        return output;  
    }  
}  