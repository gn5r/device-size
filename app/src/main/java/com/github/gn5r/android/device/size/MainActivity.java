package com.github.gn5r.android.device.size;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements TextWatcher {

    private final String PX = "px";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Size hardware = this.getHardwareSize();
        Size display = this.getDisplaySize();
        this.setHardwareSize(hardware);
        this.setDisplaySize(display);
        this.setNavbarHeight(hardware.getHeight(), display.getHeight());

        EditText e = (EditText) findViewById(R.id.display_ratio);
        e.addTextChangedListener(this);

        this.setLinkText();
    }

    private Size getHardwareSize() {
        Display d = getWindowManager().getDefaultDisplay();
        Point p = new Point(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealSize(p);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            try {
                Method getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                int width = (Integer) getRawWidth.invoke(d);
                int height = (Integer) getRawHeight.invoke(d);
                p.set(width, height);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getLocalizedMessage());
            }
        }
        return new Size(p.x, p.y);
    }

    private void setHardwareSize(Size size) {
        TextView h = (TextView) findViewById(R.id.hardware_height);
        TextView w = (TextView) findViewById(R.id.hardware_width);
        h.setText(size.getHeight() + PX);
        w.setText(size.getWidth() + PX);
    }

    private Size getDisplaySize() {
        Display d = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);
        return new Size(p.x, p.y);
    }

    private void setDisplaySize(Size size) {
        TextView h = (TextView) findViewById(R.id.display_height);
        TextView w = (TextView) findViewById(R.id.display_width);
        h.setText(size.getHeight() + PX);
        w.setText(size.getWidth() + PX);
    }

    private void setNavbarHeight(int hardware, int display) {
        TextView navbar = (TextView) findViewById(R.id.navbar_height);
        navbar.setText((hardware - display) + PX);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int contentsHeight = this.getContentsHeight();
        Size display = this.getDisplaySize();
        this.setStatusBarHeight(display.getHeight(), contentsHeight);
    }

    private int getContentsHeight() {
        LinearLayout l = (LinearLayout) findViewById(R.id.content_root);
        Log.d(getClass().getSimpleName(), ToStringBuilder.reflectionToString(l, ToStringStyle.SHORT_PREFIX_STYLE));
        TextView h = (TextView) findViewById(R.id.contents_height);
        TextView w = (TextView) findViewById(R.id.contents_width);
        h.setText(l.getHeight() + PX);
        w.setText(l.getWidth() + PX);
        return l.getHeight();
    }

    private void setStatusBarHeight(int display, int contents) {
        TextView h = (TextView) findViewById(R.id.statusbar_height);
        h.setText((display - contents) + PX);
    }

    private void setCssPixelSize(Size size) {
        TextView h = (TextView) findViewById(R.id.css_height);
        TextView w = (TextView) findViewById(R.id.css_width);
        h.setText(size.getHeight() + PX);
        w.setText(size.getWidth() + PX);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        LinearLayout l = (LinearLayout) findViewById(R.id.content_root);
        imm.hideSoftInputFromWindow(l.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        l.requestFocus();
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String val = editable.toString();
        Log.d(getClass().getSimpleName(), "Input value:" + val);
        if (val.matches("\\d")) {
            Log.d(getClass().getSimpleName(), "Input value is a number");
            int ratio = Integer.valueOf(StringUtils.defaultString(editable.toString(), "1"));
            Size display = this.getDisplaySize();
            int contentsHeight = this.getContentsHeight();
            this.setCssPixelSize(new Size(display.getWidth() / ratio, contentsHeight / ratio));
        }
    }

    private void setLinkText() {
        final String message = "※ディスプレイピクセル比はここから取得";
        Map<String, String> map = new HashMap<String, String>() {
            {
                put("ここ", "https://www.tagindex.com/tool/device.html");
            }
        };

        SpannableString ss = this.createSpannableString(message, map);
        TextView link = (TextView) findViewById(R.id.link_text);
        link.setText(ss);
        link.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString createSpannableString(String message, Map<String, String> map) {
        SpannableString ss = new SpannableString(message);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            int start = 0;
            int end = 0;

            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                break;
            }
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Uri uri = Uri.parse(entry.getValue());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return ss;
    }

    public class Size {
        private int width;
        private int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}