package poi.michael.pointsofinterest.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by michael1026 on 10/19/2017.
 */

public class volleySingleton {
    private static volleySingleton mInstance;
    private RequestQueue mQueue;
    private static Context mContext;

    public static volleySingleton getInstance(Context context) {
        if(mInstance == null){
            mInstance = new volleySingleton(context);
        }
        // Return MySingleton new Instance
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        // If RequestQueue is null the initialize new RequestQueue
        if(mQueue == null){
            mQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        // Return RequestQueue
        return mQueue;
    }

    private volleySingleton(Context context) {
        mContext = context;
        mQueue = getRequestQueue();
    }
}
