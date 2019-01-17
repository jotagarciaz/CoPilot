package com.mdh.ivanmuniz.copilotapp.object;

import com.google.android.gms.maps.model.LatLng;

/*
* The Point class represents points in a coordinate system,
* The class contains three attributes lat, lng and type
* lat and lng are the x and y coordinate respectively
* type is the activity at the Point such as line, curve, unload, wait etc...
*/
public class Point {
    private Double lat;
    private Double lng;
    private PointType type; // START, LINE OR CURVE
    private Double curveMagnitude   = 0.0;
    private int curveDirection      = 1;

    // states
    private boolean load = false;
    private boolean unload = false;
    private boolean wait = false;
    private boolean interest = false;
    private boolean finish = false;

    public Point(){
        lat = 0.0;
        lng = 0.0;
        type = new PointType(PointType.LINE);
    }

    public Point(Double lat, Double lng, int type){
        this.lat = lat;
        this.lng = lng;
        this.type = new PointType(type);
    }

    public Point(Double lat, Double lng, PointType type){
        this.lat = lat;
        this.lng = lng;
        this.type = type;
    }

    public Double getCurveMagnitude() {
        return curveMagnitude;
    }

    public void setCurveMagnitude(Double curveMagnitude) {
        this.curveMagnitude = curveMagnitude;
    }

    public int getCurveDirection() {
        return curveDirection;
    }

    public void setCurveDirection(int curveDirection) {
        this.curveDirection = curveDirection;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setType(int type) {
        this.type.setPointType(type);
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public int getType() {
        return type.getPointType();
    }

    public boolean isLine() {
        return getType() == PointType.LINE;
    }

    public boolean isCurve() {
        return getType() == PointType.CURVE;
    }

    public boolean isLoad() {
        return load;
    }

    public void setLoad(boolean load) {
        this.load = load;
    }

    public boolean isUnload() {
        return unload;
    }

    public void setUnload(boolean unload) {
        this.unload = unload;
    }

    public boolean isWait() {
        return wait;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public boolean isInterest() {
        return interest;
    }

    public void setInterest(boolean interest) { this.interest = interest; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder( type.toString() );
        int numFlags = (load ? 1 : 0) + (unload ? 1 : 0) + (wait ? 1 : 0) + (finish ? 1 : 0) + (interest ? 1 : 0);

        if(numFlags > 0){
            builder.append( " [" );

            if(load) builder.append( " load," );
            if(unload) builder.append( " unload," );
            if(wait) builder.append( " wait," );
            if(interest) builder.append( " interest," );
            if(finish) builder.append( " finish," );

            builder.replace( builder.length() - 1, builder.length(), " ]");
        }

        return builder.toString();
    }
}