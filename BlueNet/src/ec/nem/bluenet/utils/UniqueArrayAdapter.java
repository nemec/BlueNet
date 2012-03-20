package ec.nem.bluenet.utils;

import android.content.Context;
import android.widget.ArrayAdapter;

public class UniqueArrayAdapter<T> extends ArrayAdapter<T> {
	
	public UniqueArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public void add(T obj){
		if(this.getPosition(obj) < 0){
			super.add(obj);
		}
	}
	
	@Override
	public void insert(T obj, int pos){
		if(this.getPosition(obj) < 0){
			super.insert(obj, pos);
		}
	}
}
