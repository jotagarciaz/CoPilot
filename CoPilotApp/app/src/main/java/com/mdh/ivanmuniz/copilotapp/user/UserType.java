package com.mdh.ivanmuniz.copilotapp.user;

public final class UserType {
    public static final int PLANNER = 0;
    public static final int OPERATOR = 1;

    public static Boolean userTypeExist( int userType ){
        if( userType < 0 || userType > 1 )
            return false;

        return true;
    }
}
