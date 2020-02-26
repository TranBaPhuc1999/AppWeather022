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

public class adapter_item_day extends BaseAdapter {

    private List<item_day> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public adapter_item_day(Context context, List<item_day> listData) {
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
        ImageView image;
        TextView min_tem, max_tem, day;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        adapter_item_day.ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_day, null);
            holder = new adapter_item_day.ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image_day);
            holder.min_tem = (TextView) convertView.findViewById(R.id.min_tem_day);
            holder.max_tem = (TextView) convertView.findViewById(R.id.max_tem_day);
            holder.day = (TextView) convertView.findViewById(R.id.text_day);
            convertView.setTag(holder);
        } else {
            holder = (adapter_item_day.ViewHolder) convertView.getTag();
        }

        item_day detail = this.listData.get(position);
        holder.min_tem.setText(detail.getMin_tem()+" °C");
        holder.max_tem.setText(detail.getMax_tem()+" °C");

        SimpleDateFormat fomat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fomat.parse(detail.getDay());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dayOfWeek = new SimpleDateFormat("EEEE").format(date);
        String dayOfMonth = new SimpleDateFormat("dd").format(date);
        String month = new SimpleDateFormat("MM").format(date);
        holder.day.setText(dayOfMonth+"/"+month+"    "+dayOfWeek);

        holder.image.setImageResource(detail.getType());


        return convertView;
    }

}
