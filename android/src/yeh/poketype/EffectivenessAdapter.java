package yeh.poketype;

import java.util.ArrayList;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class EffectivenessAdapter extends BaseExpandableListAdapter {
	private ArrayList<EffectivenessGroup> mGroups;
	private LayoutInflater mInflater;
	private SparseArray<String> mEffectivenessMap = new SparseArray<String>();

	public EffectivenessAdapter(Activity activity,
	        ArrayList<EffectivenessGroup> groups) {
		mGroups = groups;
		mInflater = activity.getLayoutInflater();
		
		// Populate effectiveness HashMap
		mEffectivenessMap.append(400, "4");
		mEffectivenessMap.append(200, "2");
		mEffectivenessMap.append(100, "1");
		mEffectivenessMap.append(50, "1/2");
		mEffectivenessMap.append(25, "1/4");
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mGroups.get(groupPosition).mTypes.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
	        boolean isLastChild, View convertView, ViewGroup parent) {
		EffectivenessItem child = (EffectivenessItem) getChild(
		        groupPosition, childPosition);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.type_effectiveness_item,
			        null);
		}
		TextView type = (TextView) convertView.findViewById(R.id.type);
		type.setText(child.toString());
		type.setCompoundDrawablesWithIntrinsicBounds(child.mIcon, 0, 0, 0);

		TextView effectiveness = (TextView) convertView
		        .findViewById(R.id.effectiveness);
		effectiveness.setText("" + child.mEffectiveness + "%");

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroups.get(groupPosition).mTypes.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
	        View convertView, ViewGroup parent) {
		EffectivenessGroup group = (EffectivenessGroup) getGroup(groupPosition);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.type_effectiveness_group,
			        null);
		}
		TextView text = (TextView) convertView
		        .findViewById(R.id.effectiveness_group);
		text.setText(group.mName);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}