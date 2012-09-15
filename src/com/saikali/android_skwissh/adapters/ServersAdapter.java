package com.saikali.android_skwissh.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.saikali.android_skwissh.R;
import com.saikali.android_skwissh.ServerDetailActivity;
import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
import com.saikali.android_skwissh.objects.SkwisshServerGroupContent;
import com.saikali.android_skwissh.objects.SkwisshServerGroupContent.SkwisshServerGroupItem;

public class ServersAdapter extends BaseExpandableListAdapter {

	public Context context;
	private List<SkwisshServerGroupItem> serverGroupItems = new ArrayList<SkwisshServerGroupItem>();
	private LayoutInflater inflater;
	private Typeface tf;

	public ServersAdapter(Context context) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.tf = Typeface.createFromAsset(context.getAssets(), "fonts/Oxygen.otf");
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public Object getChild(int gPosition, int cPosition) {
		return serverGroupItems.get(gPosition).getServers().get(cPosition);
	}

	@Override
	public long getChildId(int gPosition, int cPosition) {
		return cPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final SkwisshServerItem server = (SkwisshServerItem) getChild(groupPosition, childPosition);
		ChildViewHolder childViewHolder;

		if (convertView == null) {
			childViewHolder = new ChildViewHolder();
			convertView = inflater.inflate(R.layout.activity_servers_list_server_item, null);
			childViewHolder.serverName = (TextView) convertView.findViewById(R.id.serverName);
			childViewHolder.serverStatus = (ImageView) convertView.findViewById(R.id.imageViewServerStatus);
			convertView.setTag(childViewHolder);
		} else {
			childViewHolder = (ChildViewHolder) convertView.getTag();
		}
		Resources res = context.getResources();
		if (server.isAvailable()) {
			childViewHolder.serverStatus.setImageDrawable(res.getDrawable(R.drawable.server_up));
		} else {
			childViewHolder.serverStatus.setImageDrawable(res.getDrawable(R.drawable.server_down));
		}

		childViewHolder.serverName.setText(server.getHostname());
		childViewHolder.serverName.setTypeface(tf);
		childViewHolder.serverName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serverDetailIntent = new Intent(v.getContext(), ServerDetailActivity.class);
				serverDetailIntent.putExtra("server_id", server.getId());
				serverDetailIntent.putExtra("group_id", server.getServerGroup().getId());
				v.getContext().startActivity(serverDetailIntent);
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int gPosition) {
		return this.serverGroupItems.get(gPosition).getServers().size();
	}

	@Override
	public Object getGroup(int gPosition) {
		return this.serverGroupItems.get(gPosition);
	}

	@Override
	public int getGroupCount() {
		return this.serverGroupItems.size();
	}

	@Override
	public long getGroupId(int gPosition) {
		return gPosition;
	}

	public void updateEntries() {
		this.serverGroupItems = SkwisshServerGroupContent.ITEMS;
		this.notifyDataSetChanged();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		final SkwisshServerGroupItem serverGroup = (SkwisshServerGroupItem) getGroup(groupPosition);
		GroupViewHolder gholder;

		if (convertView == null) {
			gholder = new GroupViewHolder();
			convertView = inflater.inflate(R.layout.activity_servers_list_servergroup_item, null);

			gholder.serverGroupName = (TextView) convertView.findViewById(R.id.serverGroupName);
			gholder.serversCount = (TextView) convertView.findViewById(R.id.serversCount);
			convertView.setTag(gholder);

			ExpandableListView elv = (ExpandableListView) parent;
			for (int i = 0; i < this.getGroupCount(); i++)
				elv.expandGroup(i);
		} else {
			gholder = (GroupViewHolder) convertView.getTag();
		}
		gholder.serverGroupName.setText(serverGroup.getName());
		gholder.serverGroupName.setTypeface(tf);
		String label = " servers";
		if (serverGroup.getServers().size() == 1)
			label = " server";
		gholder.serversCount.setText(Integer.toString(serverGroup.getServers().size()) + label);
		gholder.serversCount.setTypeface(tf);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	class GroupViewHolder {
		public TextView serverGroupName;
		public TextView serversCount;
	}

	class ChildViewHolder {
		public ImageView serverStatus;
		public TextView serverName;
	}

}
