package com.craining.blog.touchdel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DelHelp extends Activity{

	private ImageView img_ok;
	private TextView text_Advance;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.helpview);
		
		img_ok = (ImageView) findViewById(R.id.img_ok);
		text_Advance = (TextView) findViewById(R.id.text_advance);
		text_Advance.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//ÏÂ»®Ïß
		
		text_Advance.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent it = new Intent(Intent.ACTION_SEND); 
				it.putExtra(android.content.Intent.EXTRA_EMAIL, Opera.AUTHOR_EMAIL_ADDRESS); 
         	    it.putExtra(android.content.Intent.EXTRA_SUBJECT, Opera.AUTHOR_EMAIL_SUB);
				it.setType("text/plain"); 
				startActivity(Intent.createChooser(it, getString(R.string.str_chose_email_app))); 
				
				finish();
			}
		});
		img_ok.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
	}

}
