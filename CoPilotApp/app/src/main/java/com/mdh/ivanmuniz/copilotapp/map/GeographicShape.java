package com.mdh.ivanmuniz.copilotapp.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicLine;
import net.sf.geographiclib.GeodesicMask;

import java.util.ArrayList;
import java.util.List;

final class GeographicShape {

    private GeographicShape () {}

    static double DistanceBetween(LatLng startPoint, LatLng endPoint){
        float[] results = new float[1];
        Location.distanceBetween(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude, results);
        return results[0];
    }

    static LatLng getMidPoint(LatLng startPoint, LatLng endPoint){
        return LatLngBounds.builder().include(startPoint).include(endPoint).build().getCenter();
    }

    static List<LatLng> ClockwiseArc(LatLng startPoint, LatLng centerPoint, LatLng endPoint) {
        List<LatLng> points = new ArrayList<>();
        points.add(startPoint);
        int step = 1; // Azimuth value, leave untouched for now

        GeodesicData f = Geodesic.WGS84.Inverse(centerPoint.latitude, centerPoint.longitude, startPoint.latitude, startPoint.longitude);
        GeodesicData t = Geodesic.WGS84.Inverse(centerPoint.latitude, centerPoint.longitude, endPoint.latitude, endPoint.longitude);

        double ffaz = f.azi1;
        double tfaz = (((ffaz < 0) && (t.azi1 < 0)) || (t.azi1 > 0)) ? t.azi1 : 360 + t.azi1;

        while (Math.abs((int) ffaz - (int) tfaz) >= step) {
            GeodesicData llb = Geodesic.WGS84.Direct(centerPoint.latitude, centerPoint.longitude, ffaz, f.s12);
            points.add(new LatLng(llb.lat2, llb.lon2));
            ffaz += step;
            if (ffaz > 360)
                ffaz -= 360;
        }

        points.add(endPoint);

        return points;
    }


    public static List<LatLng> CounterclockwiseArc(LatLng startPoint, LatLng centerPoint, LatLng endPoint) {
        List<LatLng> points = new ArrayList<>();
        points.add(startPoint);
        int step = 1; // Azimuth value, leave untouched for now

        GeodesicData f = Geodesic.WGS84.Inverse(centerPoint.latitude, centerPoint.longitude, startPoint.latitude, startPoint.longitude);
        GeodesicData t = Geodesic.WGS84.Inverse(centerPoint.latitude, centerPoint.longitude, endPoint.latitude, endPoint.longitude);

        double ffaz = (f.azi1 < 0) ? 360 + f.azi1 : f.azi1;
        double tfaz = (t.azi1 < 0) ? 360 + t.azi1 : t.azi1;

        while (Math.abs(Math.abs((int) ffaz) - Math.abs((int) tfaz)) >= step) {
            GeodesicData llb = Geodesic.WGS84.Direct(centerPoint.latitude, centerPoint.longitude, ffaz, f.s12);
            points.add(new LatLng(llb.lat2, llb.lon2));
            ffaz -= step;
            if (ffaz < 0)
                ffaz = 360;
        }

        points.add(endPoint);
        return points;
    }

    // For testing purposes
    public static List<LatLng> test123(LatLng startPoint, LatLng endPoint) {
        List<LatLng> points = new ArrayList<>();
        points.add(startPoint);
        Geodesic geod = Geodesic.WGS84;
        GeodesicLine line = geod.InverseLine(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude,
                GeodesicMask.DISTANCE_IN | GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);

        double ds0 = DistanceBetween(startPoint, endPoint);
        int num = (int) (Math.ceil(line.Distance() / ds0)); // The number of intervals

        // Use intervals of equal length
        double ds = line.Distance() / num;
        for (int i = 0; i <= num; ++i) {
            GeodesicData g = line.Position(i * ds, GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
            System.out.println(i + " " + g.lat2 + " " + g.lon2);
            points.add(new LatLng(g.lat2, g.lon2));
        }

        points.add(endPoint);
        return points;
    }
}