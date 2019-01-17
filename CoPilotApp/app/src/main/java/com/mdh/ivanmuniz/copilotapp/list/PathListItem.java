package com.mdh.ivanmuniz.copilotapp.list;

import com.mdh.ivanmuniz.copilotapp.object.Path;

public class PathListItem {
    private Path pathItem;

    private Boolean isSelected;
    private Boolean isFavorite;

    public PathListItem( Path item ){
        pathItem = item;
        isSelected = false;
        isFavorite = false;
    }

    public void setSelected( Boolean isSelected ){ this.isSelected = isSelected; }
    public void setFavorite( Boolean isFavorite ){ this.isFavorite = isFavorite; }

    public Boolean getSelected() { return isSelected; }
    public Boolean getFavorite() { return isFavorite; }
    public Path getPath() { return pathItem; }
}
