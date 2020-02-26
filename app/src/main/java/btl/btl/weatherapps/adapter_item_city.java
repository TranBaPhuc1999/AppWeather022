package btl.btl.weatherapps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class adapter_item_city extends BaseAdapter {
    private List<item_city> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public adapter_item_city(Context context, List<item_city> listData) {
        this.listData = listData;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_city, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.text_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        item_city detail = this.listData.get(position);
        holder.name.setText(detail.getName_city());

        return convertView;
    }

}
