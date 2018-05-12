package poi.michael.pointsofinterest.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by micha on 5/7/2018.
 */

public class MapTools {
    LocationHelper mLocationHelper;

    public interface LocationListener {
        void onLocationRetrieved(Location location);
        void onNullLocation();
        void onError(Exception e);
    }

    public class LocationHelper {
        private LocationListener mLocationListener;
        private FusedLocationProviderClient mFusedLocationProviderClient;
        private Context mContext;

        public LocationHelper(Context context, LocationListener listener) {
            mLocationListener = listener;
            mContext = context;
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        }

        public void getCurrentLocation() {
            try {
                final Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLocationListener.onLocationRetrieved(task.getResult());
                        } else if (task.getResult() == null) {
                            mLocationListener.onNullLocation();
                        } else {
                            mLocationListener.onError(task.getException());
                            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            } catch (SecurityException e)  {
                Log.e("Exception: %s", e.getMessage());
            }
        }
    }

    public LocationHelper getLocationHelper(Context context, LocationListener listener) {
        if(mLocationHelper == null) {
            mLocationHelper = new LocationHelper(context, listener);
        }
        return mLocationHelper;
    }
}
