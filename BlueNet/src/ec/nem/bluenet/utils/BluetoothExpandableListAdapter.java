package ec.nem.bluenet.utils;

import java.util.ArrayList;
import java.util.List;

import ec.nem.bluenet.NodeListener;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class BluetoothExpandableListAdapter extends BaseExpandableListAdapter implements NodeListener {

	Context context;
	List<Child> connectedNodes;
	TextView title;
	
	public static class Child {
		String name;
		TextView title;
		
		public Child(Context c, String name, String text){
			this.name = name;
			title = new TextView(c);
			title.setText(text);
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT, 64);
			title.setLayoutParams(lp);
			title.setPadding(36, 0, 0, 0);
		}
		
		public String getName(){
			return name;
		}
		
		public View getView(){
			return title;
		}
	}
	
	public BluetoothExpandableListAdapter(Context c){
		context = c;
		connectedNodes = new ArrayList<Child>();
		title = new TextView(context);
		title.setText("No devices connected.");

		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
		ViewGroup.LayoutParams.FILL_PARENT, 64);
		title.setLayoutParams(lp);
		title.setPadding(36, 0, 0, 0);
		title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

		onNodeEnter(null);
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return connectedNodes.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
		//return getChild(groupPosition, childPosition).getId();
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		return connectedNodes.get(childPosition).getView();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return connectedNodes.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return connectedNodes;
	}

	@Override
	public int getGroupCount() {
		return 1;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 1;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
	        return title;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public void onNodeEnter(String node) {
		if(node !=  null){
			connectedNodes.add(new Child(context, node, node));
			/*title.setText("Connected to network: " +
					connectedNodes.get(0).getName() +
					"\nClick to view network.");*/
		}
	}

	@Override
	public void onNodeExit(String node) {
		for(int x = 0; x < connectedNodes.size(); x++){
			if(connectedNodes.get(x).getName() == node){
				connectedNodes.remove(x);
				x--;
			}
		}
		if(connectedNodes.size() == 0){
			//title.setText("No connected devices.");
		}
	}

}
