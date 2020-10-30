package com.magentastudio.quotesapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;


public class IconTextInput extends ConstraintLayout
{

    private static final String TAG = "IconTextInput";
    private final @ColorInt int COLOR_BLACK = 0xff000000;
    private final @ColorInt int COLOR_TRANSPARENT = 0x00000000;

    /*Child Views*/
    private ImageView imgIcon;
    private EditText editText;
    private View bottomBorder;

    /*Attributes values*/
    private String hint;
    private Drawable icon;
    private @ColorInt int iconTint;
    private float textSize;
    private int typeOfInput;
    private int customFont;

    public IconTextInput(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IconTextInput, 0, 0);

        try
        {
            hint = a.getString(R.styleable.IconTextInput_hint);
            icon = a.getDrawable(R.styleable.IconTextInput_icon);
            iconTint = a.getColor(R.styleable.IconTextInput_iconTint, getResources().getColor(R.color.colorPrimary));
            textSize = a.getDimension(R.styleable.IconTextInput_textSize, convertDpToPixel(14));
            typeOfInput = a.getInt(R.styleable.IconTextInput_android_inputType, EditorInfo.TYPE_CLASS_TEXT);
            customFont = a.getResourceId(R.styleable.IconTextInput_customFont, -1);
            init();
        } finally
        {
            a.recycle();
        }
    }

    @SuppressLint("ResourceType")
    private void init()
    {
        /*imgIcon*/
        imgIcon = new ImageView(getContext());
        if (icon != null) imgIcon.setImageDrawable(icon);
        imgIcon.setColorFilter(iconTint);
//        imgIcon.setLayoutParams(new ViewGroup.LayoutParams(convertDpToPixel(24), convertDpToPixel(24)));


        /*editText*/
        editText = new EditText(getContext());
        editText.setHint(hint);
        editText.setBackgroundColor(COLOR_TRANSPARENT);
        editText.setLayoutParams(new ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        editText.setHintTextColor(0xFF9f9f9f);
        editText.setInputType(typeOfInput);
        if (customFont != -1)
            editText.setTypeface(ResourcesCompat.getFont(getContext(), customFont));
//        else
//            editText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.san_francisco_display_regular));


        /*bottomBorder*/
        bottomBorder = new View(getContext());
        bottomBorder.setBackgroundColor(COLOR_BLACK);
        bottomBorder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertDpToPixel(2)));


        /*add child views to parent*/
        addView(imgIcon);
        addView(editText);
        addView(bottomBorder);

        /*set IDs of views*/
        if (getId() == NO_ID)
            setId(1);
        imgIcon.setId(2);
        editText.setId(3);
        bottomBorder.setId(4);


        /*Set constraints*/
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);

        /*set imgIcon constraints*/
        constraintSet.connect(imgIcon.getId(), ConstraintSet.LEFT, getId(), ConstraintSet.LEFT, convertDpToPixel(16));
        constraintSet.connect(imgIcon.getId(), ConstraintSet.TOP, getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(imgIcon.getId(), ConstraintSet.BOTTOM, bottomBorder.getId(), ConstraintSet.TOP, 0);

        /*set editText constraints*/
        constraintSet.connect(editText.getId(), ConstraintSet.TOP, getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(editText.getId(), ConstraintSet.LEFT, imgIcon.getId(), ConstraintSet.RIGHT, convertDpToPixel(16));
        constraintSet.connect(editText.getId(), ConstraintSet.RIGHT, getId(), ConstraintSet.RIGHT, 0);
        constraintSet.connect(editText.getId(), ConstraintSet.BOTTOM, bottomBorder.getId(), ConstraintSet.TOP, 0);

        /*set bottomBorder constraints*/
        constraintSet.connect(bottomBorder.getId(), ConstraintSet.BOTTOM, getId(), ConstraintSet.BOTTOM, 0);

        /*set imgIcon dimensions*/
        constraintSet.constrainWidth(imgIcon.getId(), ConstraintSet.WRAP_CONTENT);
        constraintSet.constrainHeight(imgIcon.getId(), ConstraintSet.WRAP_CONTENT);


        /*apply all the constraints*/
        constraintSet.applyTo(this);

        /*invalidate the layout so it draws everything*/
        invalidate();
    }


    public Editable getText()
    {
        return editText.getText();
    }

    private int convertDpToPixel(float dp)
    {
        return (int) (dp * ((float) getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void setText(String text)
    {
        editText.setText(text);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l)
    {
        editText.setOnClickListener(l);
        imgIcon.setOnClickListener(l);
        bottomBorder.setOnClickListener(l);

        editText.setClickable(true);
        editText.setFocusable(true);

    }

    public void setInutType(int type)
    {
        editText.setInputType(type);
    }
}
