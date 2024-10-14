//
//	The MIT License (MIT)
//
//	Copyright © 2018-2024 Jacopo Filié
//
//	Permission is hereby granted, free of charge, to any person obtaining a copy
//	of this software and associated documentation files (the "Software"), to deal
//	in the Software without restriction, including without limitation the rights
//	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//	copies of the Software, and to permit persons to whom the Software is
//	furnished to do so, subject to the following conditions:
//
//	The above copyright notice and this permission notice shall be included in all
//	copies or substantial portions of the Software.
//
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//	SOFTWARE.
//

package com.jackfelle.jfkit.layout;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jackfelle.jfkit.R;
import com.jackfelle.jfkit.data.Strings;
import com.jackfelle.jfkit.utilities.Utilities;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.cardview.widget.CardView;

@UiThread
public class ProgressView extends RelativeLayout
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private @Nullable CardView alertView;
	private @Nullable View contentView;
	private @Nullable ProgressBar spinnerView;
	private boolean spinnerViewHidden;
	private @Nullable String text;
	private @Nullable @ColorInt Integer textColor;
	private @Nullable Typeface textFont;
	private @Nullable TextView textLabel;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	public @Nullable Drawable getContentBackground() {
		View contentView = this.contentView;
		return (contentView == null) ? null : contentView.getBackground();
	}
	
	public void setContentBackground(@ColorInt int color) {
		View contentView = this.contentView;
		if(contentView != null) {
			contentView.setBackgroundColor(color);
		}
	}
	
	public void setContentBackground(@Nullable Drawable drawable) {
		View contentView = this.contentView;
		if(contentView != null) {
			contentView.setBackground(drawable);
		}
	}
	
	public boolean isSpinnerViewHidden() {
		return this.spinnerViewHidden;
	}
	
	public void setSpinnerViewHidden(boolean isHidden) {
		if(this.spinnerViewHidden == isHidden) {
			return;
		}
		
		this.spinnerViewHidden = isHidden;
		
		ProgressBar spinnerView = this.spinnerView;
		if(spinnerView != null) {
			spinnerView.setVisibility(isHidden ? GONE : VISIBLE);
		}
	}
	
	public @Nullable String getText() {
		return this.text;
	}
	
	public void setText(@StringRes int text) {
		this.setText(this.getContext().getString(text));
	}
	
	public void setText(@Nullable String text) {
		if(Utilities.areObjectsEqual(this.text, text)) {
			return;
		}
		
		this.text = text;
		
		this.updateLayout();
	}
	
	protected @Nullable @ColorInt Integer getTextColor() {
		return this.textColor;
	}
	
	protected void setTextColor(@Nullable @ColorInt Integer color) {
		if(Utilities.areObjectsEqual(this.textColor, color)) {
			return;
		}
		
		this.textColor = color;
		
		this.updateLayout();
	}
	
	public @Nullable Typeface getTextFont() {
		return this.textFont;
	}
	
	public void setTextFont(@Nullable Typeface font) {
		if(Utilities.areObjectsEqual(this.textFont, font)) {
			return;
		}
		
		this.textFont = font;
		
		this.updateLayout();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifetime
	
	@SuppressLint("ClickableViewAccessibility") private static void initialize(@NonNull Context context, @NonNull ProgressView object) {
		View layout = View.inflate(context, R.layout.progress_view, object);
		
		object.alertView = layout.findViewById(R.id.alert_view);
		object.contentView = layout.findViewById(R.id.content_view);
		object.spinnerView = layout.findViewById(R.id.spinner_view);
		object.textLabel = layout.findViewById(R.id.text_label);
		
		layout.setOnTouchListener((v, event) -> {
			// This will block any touch that will try to pass through the view.
			return true;
		});
		
		object.updateLayout();
	}
	
	@SuppressWarnings("ThisEscapedInObjectConstruction") public ProgressView(@NonNull Context context) {
		super(context);
		ProgressView.initialize(context, this);
	}
	
	@SuppressWarnings("ThisEscapedInObjectConstruction") public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		ProgressView.initialize(context, this);
	}
	
	@SuppressWarnings("ThisEscapedInObjectConstruction") public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		ProgressView.initialize(context, this);
	}
	
	@SuppressWarnings("ThisEscapedInObjectConstruction") @TargetApi(21) public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		ProgressView.initialize(context, this);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - User interface management
	
	protected void updateLayout() {
		TextView textLabel = this.textLabel;
		if(textLabel == null) {
			return;
		}
		
		String text = this.getText();
		textLabel.setText(text);
		textLabel.setTextColor(Utilities.replaceIfNull(this.getTextColor(), Color.WHITE));
		textLabel.setTypeface(Utilities.replaceIfNull(this.getTextFont(), Typeface.DEFAULT));
		textLabel.setVisibility(Strings.isNullOrEmptyString(text) ? GONE : VISIBLE);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
