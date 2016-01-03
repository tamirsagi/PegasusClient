package client.pegasusclient.app.UI.Fragments.settings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import client.pegasusclient.app.UI.Activities.R;

/**
 * @author Tamir Sagi
 *         This class manages the rcycler list within setting Fragment
 */

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.RecordHolder> {

    private Context mContext;
    private String[] mSettings;
    private int[] mSettingsIconsIds;
    private RecyclerViewClickListener observer;

    public SettingsRecyclerAdapter(Context context, RecyclerViewClickListener observer, String[] settingsTitles, int[] settingsIconsIDs) {
        mSettings = settingsTitles;
        mSettingsIconsIds = settingsIconsIDs;
        mContext = context;
        this.observer = observer;

    }


    @Override
    public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.settings_list_item, parent, false);
        return new RecordHolder(itemView);          //return custom item
    }

    @Override
    public void onBindViewHolder(RecordHolder holder, int position) {
        holder.tv_Title.setText(mSettings[position]);
        holder.iv_Icon.setImageResource(mSettingsIconsIds[position]);
    }


    @Override
    public int getItemCount() {
        return mSettings.length;
    }


    /**
     * this class represents an item within the recycler list.
     */
    class RecordHolder extends RecyclerView.ViewHolder implements RecyclerViewClickListener {
        TextView tv_Title;
        ImageView iv_Icon;

        public RecordHolder(View itemView) {
            super(itemView);
            tv_Title = (TextView) itemView.findViewById(R.id.setting_title);
            iv_Icon = (ImageView) itemView.findViewById(R.id.setting_title_icon);
        }

        @Override
        public void getClickedItemPosition(int position) {
            observer.getClickedItemPosition(this.getAdapterPosition());
        }
    }


}