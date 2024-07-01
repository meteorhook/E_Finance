package com.example.e_finance.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

//四位数字验证码工具类
public class FourCodeCaptcha {

    //随机可选的字符数组
    private static final char[] CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static FourCodeCaptcha Captcha;

    //创建实例
    public static FourCodeCaptcha getInstance() {
        if (Captcha == null)
            Captcha = new FourCodeCaptcha();
        return Captcha;
    }

    private static final int DEFAULT_CODE_LENGTH = 4;
    private static final int DEFAULT_FONT_SIZE = 23;
    private static final int DEFAULT_LINE_NUMBER = 2;
    private static final int BASE_PADDING_LEFT = 10, RANGE_PADDING_LEFT = 6, BASE_PADDING_TOP = 28, RANGE_PADDING_TOP = 2;
    private static final int DEFAULT_WIDTH = 70, DEFAULT_HEIGHT = 40;

    //canvas 宽度和高度
    private int width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT;

    //数之间距离及头边距
    private int base_padding_left = BASE_PADDING_LEFT, range_padding_left = RANGE_PADDING_LEFT,
            base_padding_top = BASE_PADDING_TOP, range_padding_top = RANGE_PADDING_TOP;

    //随机数长度、干扰线数量、字体大小
    private int codeLength = DEFAULT_CODE_LENGTH, line_number = DEFAULT_LINE_NUMBER, font_size = DEFAULT_FONT_SIZE;

    //基础变量
    private String code;
    private int padding_left, padding_top;
    private Random random = new Random();

    //验证码图片
    public Bitmap createBitmap() {
        padding_left = 0;
        Bitmap bp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bp);
        code = createCode();
        c.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setTextSize(font_size);

        for (int i = 0; i < code.length(); i++) {
            randomTextStyle(paint);
            randomPadding();
            c.drawText(code.charAt(i) + "", padding_left, padding_top, paint);
        }

        //画线
        for (int i = 0; i < line_number; i++) {
            drawLine(c, paint);
        }

        c.save();//保存 Canvas.ALL_SAVE_FLAG
        c.restore();//
        return bp;
    }

    //获取验证码的值
    public String getCode() {
        return code;
    }

    //生成验证码
    private String createCode() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            buffer.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return buffer.toString();
    }

    private void drawLine(Canvas canvas, Paint paint) {
        int color = randomColor();
        int startX = random.nextInt(width);
        int startY = random.nextInt(height);
        int stopX = random.nextInt(width);
        int stopY = random.nextInt(height);
        paint.setStrokeWidth(1);
        paint.setColor(color);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    private int randomColor() {
        return randomColor(1);
    }

    private int randomColor(int rate) {
        int red = random.nextInt(256) / rate;
        int green = random.nextInt(256) / rate;
        int blue = random.nextInt(256) / rate;
        return Color.rgb(red, green, blue);
    }

    private void randomTextStyle(Paint paint) {
        int color = randomColor();
        paint.setColor(color);
        paint.setFakeBoldText(random.nextBoolean()); //true为粗体，false为非粗体
        float skewX = random.nextInt(11) / 10;
        skewX = random.nextBoolean() ? skewX : -skewX;
        paint.setTextSkewX(skewX); //float类型参数，负数表示右斜，整数左斜
        paint.setUnderlineText(true); //true为下划线，false为非下划线
        paint.setStrikeThruText(true); //true为删除线，false为非删除线
    }

    private void randomPadding() {
        padding_left += base_padding_left + random.nextInt(range_padding_left);
        padding_top = base_padding_top + random.nextInt(range_padding_top);
    }
}

