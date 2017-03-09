
package com.testcamerasuper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

public class BitmapUtil {

    /**
     * 给图片添加水印和时间戳
     * 
     * @param originBitmap 原始图片
     * @param degree 旋转角度
     * @param watermark 水印文字
     * @param paint 绘制水印的画笔对象
     * @return 最终处理的结果
     */
    public static Bitmap rotateBitmapAndWaterMark(Bitmap originBitmap, int degree, String watermark, Paint paint) {
        int width = originBitmap.getWidth();
        int height = originBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.save();
        canvas.rotate(degree, width / 2, height / 2);
        canvas.drawBitmap(originBitmap, 0, 0, null);
        canvas.restore();
        int textWidht = (int) paint.measureText(watermark);
        FontMetrics fontMetrics = paint.getFontMetrics();
        int textHeight = (int) (fontMetrics.ascent - fontMetrics.descent);
        int x = (width - textWidht) / 2;
        int y = (height - textHeight) / 2;
        y = (int) (y - fontMetrics.descent);
        canvas.drawText(watermark, x, y, paint);
        canvas.drawText(String.valueOf(System.currentTimeMillis()), x, y + textHeight, paint);
        // 立即回收无用内存
        originBitmap.recycle();
        originBitmap = null;
        return resultBitmap;
    }
}
