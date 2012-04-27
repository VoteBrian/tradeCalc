package net.votebrian.tradecalc;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FullListAdapter extends BaseAdapter {
  DbAdapter mDbAdapter;
  LayoutInflater mInflater;
  RowData mRowData;
  Cursor cursor;
  ViewHolder mHolder;
  String team_names[];

  public FullListAdapter(Context context, DbAdapter adapter) {
    mRowData = new RowData();
    mDbAdapter = adapter;
    mInflater = LayoutInflater.from(context);

    cursor = mDbAdapter.fetchAllPicks();
    mRowData.initializeData(cursor.getCount());
    mRowData.populateData();

    mHolder = new ViewHolder();

    team_names = context.getResources().getStringArray(R.array.teams);
  }

  class ViewHolder {
    TextView round;
    TextView sub_pick;
    TextView pick;
    TextView team;
    TextView value;
  }

  private class RowData {
    int[] ROUND = {0};
    int[] SUBPICK = {0};
    int[] PICK = {0};
    double[] VALUE = {0};
    int[] TEAM = {0};

    public void initializeData(int rows) {
      ROUND = new int[rows];
      SUBPICK = new int[rows];
      PICK = new int[rows];
      VALUE = new double[rows];
      TEAM = new int[rows];
    }

    public void populateData() {
      int i;

      if(cursor.moveToFirst()) {
        for(i = 0; i < cursor.getCount(); i++) {
          ROUND[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_ROUND));
          SUBPICK[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_SUB_PICK));
          PICK[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_PICK));
          VALUE[i] = cursor.getDouble(cursor.getColumnIndex(DbAdapter.KEY_VALUE));
          TEAM[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_TEAM));
          cursor.moveToNext();
        }
      }
    }

    public int length() {
      return ROUND.length;
    }
  }

  @Override
  public int getCount() {
    return mRowData.length();
  }

  @Override
  public Object getItem(int position) {
    return position;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    if(position == 0) {
      convertView = mInflater.inflate(R.layout.full_list_row, null);
    } else {
      convertView = mInflater.inflate(R.layout.full_list_row, null);
    }

    mHolder.round = (TextView) convertView.findViewById(R.id.full_row_round);
    mHolder.sub_pick = (TextView) convertView.findViewById(R.id.full_row_sub_pick);
    mHolder.pick = (TextView) convertView.findViewById(R.id.full_row_pick);
    mHolder.value = (TextView) convertView.findViewById(R.id.full_row_value);
    mHolder.team = (TextView) convertView.findViewById(R.id.full_row_team);

    convertView.setTag(mHolder);

    mHolder.round.setText(String.valueOf(mRowData.ROUND[position]));
    mHolder.sub_pick.setText(String.valueOf(mRowData.SUBPICK[position]));
    mHolder.pick.setText(String.valueOf(mRowData.PICK[position]));
    mHolder.value.setText(String.valueOf(mRowData.VALUE[position]));
    mHolder.team.setText( team_names[mRowData.TEAM[position]-1] );
    
    return convertView;
  }

}
