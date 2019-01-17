package com.mdh.ivanmuniz.copilotapp.object;

public final class PointType {
    // Types
    public static final int START = 0;
    public static final int LINE = 1;
    public static final int CURVE = 2;

    // States
    public static final int LOAD = 3;
    public static final int UNLOAD = 4;
    public static final int WAIT = 5;
    public static final int INTEREST = 6;
    public static final int FINISH = 7;

    private int pointType;

    public PointType() {}

    public PointType(int pointType) { this.pointType = pointType; }

    public void setPointType(int pointType) {
        if(pointType < 0 | pointType > 7)
            throw new IllegalArgumentException("Not a valid value");
        else
            this.pointType = pointType;
    }

    public int getPointType() {
        return pointType;
    }

    public static String toString(int pointType) {
        switch(pointType){
            case LINE: return "Line";
            case CURVE: return "Curve";
            case LOAD: return "Load";
            case UNLOAD: return "Unload";
            case WAIT: return "Wait";
            case START: return "Start";
            case INTEREST: return "Interest";
            case FINISH: return "Finish";
            default: return "";
        }
    }

    @Override
    public String toString() {
        return toString( pointType );
    }
}