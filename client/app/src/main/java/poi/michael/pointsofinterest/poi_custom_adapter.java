package poi.michael.pointsofinterest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/**
 * Created by micha on 2/18/2018.
 */

class poi_custom_adapter extends ArrayAdapter<String> {
    public poi_custom_adapter(@NonNull Context context, String[] names) {
        super(context, R.layout.poi_list_fragment, names);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater poi_inflater = LayoutInflater.from(getContext());
        View customView = poi_inflater.inflate(R.layout.poi_list_fragment, parent, false);

        return customView;
    }


}
