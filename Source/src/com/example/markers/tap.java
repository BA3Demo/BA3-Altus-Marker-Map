package com.example.markers;

import us.ba3.me.ConvertPointCallback;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.PointF;
import android.view.MotionEvent;

public class tap extends us.ba3.me.MapView{
    
    MainActivity mA = new MainActivity();
    
    AssetManager assetManager;

	public tap(Context context) {
		super(context);
	}

	@Override
	public void onLongPress(MotionEvent e){
		super.onLongPress(e);
		PointF cord = new PointF();
		cord.set(e.getX(), e.getY());
		
		super.getLocationForPoint(cord, new ConvertPointCallback(){
			@Override
			public void convertComplete(us.ba3.me.Location loc) {
				MainActivity.markPoint(loc);
			}
		});
	}
	
}


