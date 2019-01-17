package com.mdh.ivanmuniz.copilotapp.map;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mdh.ivanmuniz.copilotapp.R;
import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.list.IPathListItemListener;
import com.mdh.ivanmuniz.copilotapp.list.PathListItem;
import com.mdh.ivanmuniz.copilotapp.list.PathRecyclerViewAdapter;
import com.mdh.ivanmuniz.copilotapp.object.Path;
import com.mdh.ivanmuniz.copilotapp.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapPathRecyclerViewAdapter extends RecyclerView.Adapter<MapPathRecyclerViewAdapter.MapPathRecyclerViewHolder>  {

    public static final int CHECK_EVENT      = 64;
    public static final int UNCHECK_EVENT      = 128;

    private final IPathListItemListener mListener;
    private List<PathListItem> mList;
    private List<MapPathRecyclerViewAdapter.MapPathRecyclerViewHolder> viewHolders = new ArrayList<>();
    private Path excludedPath;

    public MapPathRecyclerViewAdapter( IPathListItemListener listener, LinearLayoutManager layoutManager, Context context, Path currentPath ) {

        mList = new ArrayList<>();

        excludedPath = currentPath;
        PathCollection pCollection = PathCollection.getInstance();

        List<String> fav = User.getInstance().getFavorites();
        List<Path> pList = pCollection.getPathList();

        for( Path path : pList ){
            // Create the wrapper and assign favorite/preview
            if( path.getId().equals( excludedPath.getId() ))
                continue;

            PathListItem nItem = new PathListItem( path );
            nItem .setFavorite( fav.contains( path.getId() ) );
            mList.add( nItem );
        }

        // Sort the list
        Collections.sort( mList, new PathRecyclerViewAdapter.CustomComparator() );

        // Set event listener
        mListener = listener;
    }

    public void filter(String text){
        List<String> fav = User.getInstance().getFavorites();
        ArrayList<PathListItem> filteredList = new ArrayList<>();
        String query = text.toLowerCase().trim();
        String name;
        String description;

        PathListItem item;
        for(Path path: PathCollection.getInstance().getPathList()) {

            if( path.getId().equals( excludedPath.getId() ))
                continue;

            name = path.getName().toLowerCase().trim();
            description = path.getDescription().toLowerCase().trim();

            if(name.contains(query) || description.contains(query)){
                item = new PathListItem(path);
                item.setFavorite( fav.contains( path.getId() ) );
                filteredList.add(item);
            }
        }

        // Sort the list
        Collections.sort( filteredList, new PathRecyclerViewAdapter.CustomComparator() );

        mList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public MapPathRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.path_list_item_checkable, parent, false);
        MapPathRecyclerViewHolder holder = new MapPathRecyclerViewHolder( view );
        viewHolders.add( holder );

        return holder;
    }

    @Override
    public void onBindViewHolder(final MapPathRecyclerViewHolder holder, int position) {
        holder.setData( mList.get( position ), mListener );
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class MapPathRecyclerViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        private IPathListItemListener mListener;
        public PathListItem mItem;
        private final View mView;
        private final TextView mIdView;
        private final TextView mContentView;
        private CheckBox mCheckView;

        public MapPathRecyclerViewHolder(View view) {
            super(view);

            mView = view;
            mIdView = view.findViewById(R.id.item_name);
            mContentView = view.findViewById(R.id.item_desc);
            mCheckView = view.findViewById(R.id.item_checked);
        }

        public void setData( PathListItem item, IPathListItemListener listener ) {
            mListener = listener;
            mItem = item;
            Path p = item.getPath();

            mIdView.setText( p.getName() );
            mContentView.setText( p.getDescription() );
            mCheckView.setChecked( item.getSelected() );
            mCheckView.setOnCheckedChangeListener( this );
        }

        public void onCheckedChanged( CompoundButton group, boolean checked ) {
            mItem.setSelected( checked );

            if(checked)
                mListener.onItemEvent( mItem, CHECK_EVENT );
            else
                mListener.onItemEvent( mItem, UNCHECK_EVENT );
        }
    }
}
