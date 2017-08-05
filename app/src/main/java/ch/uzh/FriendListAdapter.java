package ch.uzh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import ch.uzh.helper.FriendsListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Jesus on 30.04.2017.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    private static final Logger log = LoggerFactory.getLogger(FriendListAdapter.class);


    private Context mContext;
    private List<FriendsListEntry> friendlist;
    MainWindow mainWindow;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public ImageView friendPicture;
        public TextView friendName;
        public Button onlineStatus;
        public TextView newMsgAlert;
        Context context = itemView.getContext();

        public ViewHolder(View v) {
            super(v);
            friendName = (TextView) v.findViewById(R.id.username);
            friendPicture = (ImageView) v.findViewById(R.id.userPicture);
            onlineStatus = (Button) v.findViewById(R.id.onlineStatus);
            newMsgAlert = (TextView) v.findViewById(R.id.newMsgAlert);
            view = v;
            view.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    // item clicked
                    int pos = getAdapterPosition();
                    log.info(String.valueOf(pos));
                    ((MainActivity) context).onClickCalled(friendlist.get(pos).getUserID());
                }
            });

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendListAdapter(Context mContext, List<FriendsListEntry> friendlist) {
        this.mContext = mContext;
        this.friendlist = friendlist;
        GlobalState state = ((GlobalState) mContext.getApplicationContext());
        mainWindow = state.getMainWindow();
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
        FriendsListEntry e = mainWindow.getFriendsListEntry(friend.getUserID());
        if(e.isOnline()){
            holder.onlineStatus.setBackground(mContext.getResources().getDrawable(R.drawable.circlegreen));
        }
        else{
            holder.onlineStatus.setBackground(mContext.getResources().getDrawable(R.drawable.circlered));
        }
        if(mainWindow.hasNewMsg(friend.getUserID())){
            holder.newMsgAlert.setVisibility(View.VISIBLE);
        }
        else{
            holder.newMsgAlert.setVisibility(View.INVISIBLE);
        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return friendlist.size();
    }
}