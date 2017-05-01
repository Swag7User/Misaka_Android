package ch.uzh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import ch.uzh.helper.FriendsListEntry;

import java.util.List;

/**
 * Created by Jesus on 30.04.2017.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private Context mContext;
    private List<FriendsListEntry> friendlist;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView friendPicture;
        public TextView friendName;
        public ViewHolder(View view) {
            super(view);
            friendName = (TextView) view.findViewById(R.id.username);
            friendPicture = (ImageView) view.findViewById(R.id.userPicture);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendListAdapter(Context mContext, List<FriendsListEntry> friendlist) {
        this.mContext = mContext;
        this.friendlist = friendlist;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FriendListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_card_view, parent, false);

        return new FriendListAdapter.ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FriendsListEntry friend = friendlist.get(position);
        holder.friendName.setText(friend.getUserID());
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return friendlist.size();
    }
}