package net.votebrian.tradecalc;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PicksList extends ListActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setListAdapter(new EfficientAdapter(this));
  }


  //-----EFFICIENT ADAPTER -----//
  private static class EfficientAdapter extends BaseAdapter {
    LayoutInflater mInflater;

    static class ViewHolder {
      TextView round;
      TextView sub_pick;
      TextView pick;
      TextView value;
    }

    public EfficientAdapter(Context context) {
      mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
      return ROUND.length;
    }

    public Object getItem(int position) {
      return position;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;

      if(convertView == null) {
        convertView = mInflater.inflate(R.layout.trade_row, null);

        holder = new ViewHolder();
        holder.round = (TextView) convertView.findViewById(R.id.row_round);
        holder.sub_pick = (TextView) convertView.findViewById(R.id.row_sub_pick);
        holder.pick = (TextView) convertView.findViewById(R.id.row_pick);
        holder.value = (TextView) convertView.findViewById(R.id.row_value);

        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      holder.round.setText(ROUND[position]);
      holder.sub_pick.setText(SUBPICK[position]);
      holder.pick.setText(PICK[position]);
      holder.value.setText(VALUE[position]);

      return convertView;
    } // public View getView()

    public static final String[] ROUND = {"1", "1", "2", "3", "3", "4", "5", "6"};
    public static final String[] SUBPICK = {"1", "2", "1", "1", "2", "1", "1", "1"};
    public static final String[] PICK = {"1", "2", "3", "4", "5", "6", "7", "8"};
    public static final String[] VALUE = {"800", "700", "600", "500", "400", "300", "200", "100"};

  } // private static class EfficientAdapter extends BaseAdapter
} // public class PicksList extends ListActivity
