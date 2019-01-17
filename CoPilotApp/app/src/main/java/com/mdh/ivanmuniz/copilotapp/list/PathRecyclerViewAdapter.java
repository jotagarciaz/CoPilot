package com.mdh.ivanmuniz.copilotapp.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mdh.ivanmuniz.copilotapp.R;
import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.object.Path;
import com.mdh.ivanmuniz.copilotapp.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class PathRecyclerViewAdapter extends RecyclerView.Adapter<PathRecyclerViewAdapter.PathRecyclerViewHolder> implements IPathListItemListener, PathCollection.IPathCollectionEventHandler {

    public static final int CLICK_EVENT         = 0;
    public static final int EDIT_NAME_EVENT     = 1;
    public static final int EDIT_PATH_EVENT     = 4;
    public static final int EDIT_GCODE_EVENT    = 8;
    public static final int REMOVE_EVENT        = 16;
    public static final int FAVORITE_EVENT      = 32;

    private List<PathListItem> mList;
    private final IPathListItemListener mListener;
    private List<PathRecyclerViewHolder> viewHolders = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private static String lastSelectedPath = "";
    private Boolean firstRun = true;


    public PathRecyclerViewAdapter(IPathListItemListener listener, LinearLayoutManager layoutManager, Context context ) {
        this.layoutManager = layoutManager;
        mList = new ArrayList<>();

        PathCollection pCollection = PathCollection.getInstance();

        // Listen for path insertion and removal
        pCollection.addListener( this );

        List<String> fav = User.getInstance().getFavorites();
        List<Path> pList = pCollection.getPathList();

        for( Path path : pList ){
            // Create the wrapper and assign favorite/preview
            PathListItem nItem = new PathListItem( path );
            nItem.setFavorite( fav.contains( path.getId() ) );
            mList.add( nItem );
        }

        // Sort the list
        Collections.sort( mList, new CustomComparator() );

        // In case we switched view after selecting an item, switch
        // to the last selected item
        selectActivePath();

        // Set event listener
        mListener = listener;
    }

    public void Destroy(){
        PathCollection.getInstance().removeListener( this );
        mList = null;
        viewHolders = null;
        layoutManager = null;
    }

    public void filter(String text){
        List<String> fav = User.getInstance().getFavorites();
        ArrayList<PathListItem> filteredList = new ArrayList<>();
        String query = text.toLowerCase().trim();
        String name;
        String description;

        PathListItem item;
        for(Path path: PathCollection.getInstance().getPathList()){
            name = path.getName().toLowerCase().trim();
            description = path.getDescription().toLowerCase().trim();

            if(name.contains(query) || description.contains(query)){
                item = new PathListItem(path);
                item.setFavorite( fav.contains( path.getId() ) );
                filteredList.add(item);
            }
        }

        // Sort the list
        Collections.sort( filteredList, new CustomComparator() );

        filterList(filteredList);
    }

    private void filterList(ArrayList<PathListItem> filteredList){
        mList = filteredList;
        notifyDataSetChanged();
    }

    public static class CustomComparator implements Comparator<PathListItem> {
        @Override
        public int compare(PathListItem o1, PathListItem o2) {

            Path p1 = o1.getPath();
            Path p2 = o2.getPath();

            if( o1.getFavorite() && o2.getFavorite() )
                return p2.getEditDate().compareTo( p1.getEditDate() );

            if( o1.getFavorite() )
                return -1;

            if( o2.getFavorite() )
                return 1;

            return p2.getEditDate().compareTo( p1.getEditDate() );
        }
    }

    private void selectActivePath(){
        if( !lastSelectedPath.equals("") ) {
            for( int i = 0; i < mList.size(); i++ ){
                if( !mList.get( i ).getPath().getId().equals( lastSelectedPath ) )
                    continue;

                mList.get( i ).setSelected( true );
                break;
            }
        }
        // Set first item to be selected
        else if( mList.size() > 0 )
            mList.get( 0 ).setSelected( true );
    }

    /*
     * Handles insertion and removal events from the path collection,
     * should remove and insert into the list automatically
     */
    public void onDataEvent( Path path, int type ){
        // If item has been inserted, create a new wrapper for this item and add it
        if( type == PathCollection.INSERT_EVENT ){
            // Get local setting list, what paths are favorites?
            List<String> fav = User.getInstance().getFavorites();

            // Create the wrapper and assign favorite/preview
            PathListItem nItem = new PathListItem( path );
            nItem.setFavorite( fav.contains( path.getId() ) );
            mList.add( nItem );

            // Sort the list
            Collections.sort( mList, new CustomComparator() );

            // Notify the adapter that an item has been added
            int index = mList.indexOf( nItem );
            if( index > -1 )
                notifyItemInserted( index );

        }
        else if( type == PathCollection.REMOVE_EVENT ){
            for( int i = 0; i < mList.size(); i++ ){
                PathListItem item = mList.get( i );

                if( item.getPath() != path )
                    continue;

                notifyItemRemoved( i );
                mList.remove( item );
                break;
            }
        }
        else if ( type == PathCollection.UPDATE_EVENT || type == PathCollection.PREVIEW_EVENT ){
            for( int i = 0; i < mList.size(); i++ ){
                PathListItem item = mList.get( i );

                if( item.getPath() != path )
                    continue;

                notifyItemChanged( i );
                break;
            }

            if( firstRun ){
                selectActivePath();
                firstRun = false;
            }
        }
    }

    @Override
    public PathRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_item, parent, false);
        PathRecyclerViewHolder holder = new PathRecyclerViewHolder( view );

        viewHolders.add( holder );

        return holder;
    }

    public void onItemEvent(PathListItem item, final int type ){
        switch( type )
        {
            case CLICK_EVENT:
                // Deselect all items in list besides this one
                for( PathListItem value : mList ) {
                    if( value == item )
                        continue;

                    value.setSelected( false );
                }

                // Collapse all views not currently selected
                for( PathRecyclerViewHolder holder : viewHolders ) {
                    if( holder.mItem == item )
                        continue;

                    holder.setExpanded( false );
                }

                // Scroll item to top
                int index = mList.indexOf( item );
                // Remember the last selected id in case we switch view
                lastSelectedPath = item.getPath().getId();

                if( index > -1 )
                    layoutManager.scrollToPositionWithOffset( index, 0 );
                break;
        }

        // Notify parent listener
        mListener.onItemEvent( item, type );
    }

    @Override
    public void onBindViewHolder(final PathRecyclerViewHolder holder, int position) {
        holder.setData( mList.get( position ), this );
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class PathRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {
        public PathListItem mItem;
        private final View mView;
        private final TextView mIdView;
        private final TextView mContentView;
        private boolean isExpanded;
        private TextView mDateView;
        private ToggleButton mFavoriteView;
        private IPathListItemListener mListener;
        private ConstraintLayout mExpanded;
        private Button mEditPathView;
        private Button mEditGCodeView;
        private ImageView mImageView;
        private ImageButton mDeleteView;

        private Drawable favSelected;
        private Drawable favDefault;

        public PathRecyclerViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_name);
            mContentView = view.findViewById(R.id.item_desc);
            mFavoriteView = view.findViewById(R.id.item_favorite);
            mDateView = view.findViewById(R.id.item_date);
            mExpanded = view.findViewById(R.id.item_expanded);
            mEditPathView = view.findViewById(R.id.button_path);
            mEditGCodeView = view.findViewById(R.id.button_gcode);
            mImageView = view.findViewById(R.id.item_preview);
            mDeleteView = view.findViewById(R.id.button_delete);

            mDeleteView.setVisibility( (User.getInstance().getAdmin())? View.VISIBLE : View.GONE );

            favDefault = ContextCompat.getDrawable( view.getContext(), R.drawable.ic_star_border_black_24dp );
            favSelected = ContextCompat.getDrawable( view.getContext(), R.drawable.ic_star_black_24dp );

            mFavoriteView.setBackgroundDrawable( favDefault );

            isExpanded = false;
        }

        public void setData( PathListItem item, IPathListItemListener listener ) {
            mItem = item;
            Path p = item.getPath();

            mIdView.setText( p.getName() );
            mContentView.setText( p.getDescription() );
            mFavoriteView.setChecked( item.getFavorite() );
            mImageView.setImageBitmap( item.getPath().getPreview() );
            setExpanded( item.getSelected() );

            // Time difference from now and item
            CharSequence dateCharString = DateUtils.getRelativeTimeSpanString( p.getEditDate().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            mDateView.setText( dateCharString.toString() ); // For testing purposes

            mListener = listener;
            setFavoriteIcon();

            // If data is set (bound), also register listeners for click events
            mView.setOnClickListener( this );
            mIdView.setOnTouchListener( this );
            mEditPathView.setOnClickListener( this );
            mEditGCodeView.setOnClickListener( this );
            mDeleteView.setOnClickListener( this );
            mFavoriteView.setOnClickListener( this );
        }

        public boolean onTouch(View view, MotionEvent event){
            if( view != mIdView )
                return false;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Drawable[] compoundDrawables = mIdView.getCompoundDrawables();

                if( compoundDrawables.length < 3 || compoundDrawables[2] == null )
                    return false;

                int leftEdgeOfRightDrawable = mIdView.getRight() - compoundDrawables[2].getBounds().width();
                // when EditBox has padding, adjust leftEdge like
                // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                if (event.getRawX() >= leftEdgeOfRightDrawable) {
                    // Pencil click
                    mListener.onItemEvent( mItem, EDIT_NAME_EVENT );
                    return true;
                }
            }

            return false;
        }

        public void onClick( View view ) {
            // Figured out specific click events
            if( view == mFavoriteView) {
                mItem.setFavorite( mFavoriteView.isChecked() );
                setFavoriteIcon();
                mListener.onItemEvent( mItem, FAVORITE_EVENT );
                return;
            }
            else if( view == mEditPathView ) {
                mListener.onItemEvent( mItem, EDIT_PATH_EVENT );
                return;
            }
            else if( view == mEditGCodeView ) {
                mListener.onItemEvent( mItem, EDIT_GCODE_EVENT );
                return;
            }
            else if( view == mDeleteView ){
                mListener.onItemEvent( mItem, REMOVE_EVENT );
                return;
            }
            else if( view == mIdView ) {
                // Special case, we only want a click event when the pencil (drawableRight) is clicked.
                // Is handled as a touchEvent
                return;
            }

            // If general click
            // Expand if not already expanded
            if( !isExpanded ) {
                setExpanded( true );
            }

            // alert general click listener
            mListener.onItemEvent( mItem, CLICK_EVENT );
        }

        public void setExpanded( boolean expanded ) {
            this.isExpanded = expanded;
            mItem.setSelected( expanded );

            if( expanded ){
                mExpanded.setVisibility( View.VISIBLE );
                mIdView.setCompoundDrawablesWithIntrinsicBounds( null, null, ContextCompat.getDrawable( mIdView.getContext(), R.drawable.ic_edit_black_18dp ), null );
                return;
            }

            mIdView.setCompoundDrawables( null, null, null, null );
            mExpanded.setVisibility( View.GONE );
        }

        private void setFavoriteIcon(){
            mFavoriteView.setBackgroundDrawable( (mFavoriteView.isChecked())? favSelected : favDefault );
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
