package net.votebrian.tradecalc;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

//-----EFFICIENT ADAPTER -----//
public class PicksAdapter extends BaseAdapter {
  LayoutInflater mInflater;
  OnClickListener mListener;
  DbAdapter mDbAdapter;
  RowData mRowData;
  Context mCtx;
  ViewHolder mHolder;
  int mTeam = 1;
  double mSum = 0;
  int mPage;

  // handles to views that we will be updating
  class ViewHolder {
    TextView round;
    TextView sub_pick;
    TextView pick;
    TextView value;
  }

  public PicksAdapter(int page, Context context, DbAdapter adapter) {
    mPage = page;
    mInflater = LayoutInflater.from(context);
    mCtx = context;
    mDbAdapter = adapter;
    mHolder = new ViewHolder();

    mRowData = new RowData();
    
  }

  public int getCount() {
    return mRowData.length();
  }

  public Object getItem(int position) {
    return position;
  }

  public long getItemId(int position) {
    return position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    if(mPage == 1) {
      if(mRowData.BLOCKED[position] > 0) {
        convertView = mInflater.inflate(R.layout.trade_row_blocked, null);
      } else if(mRowData.SELA[position] > 0) {
        convertView = mInflater.inflate(R.layout.trade_row_selected, null);
      } else {
        convertView = mInflater.inflate(R.layout.trade_row, null);
      }
    } else {
      if(mRowData.BLOCKED[position] > 0) {
        convertView = mInflater.inflate(R.layout.trade_row_blocked, null);
      } else if(mRowData.SELB[position] > 0) {
        convertView = mInflater.inflate(R.layout.trade_row_selected, null);
      }
      else {
        convertView = mInflater.inflate(R.layout.trade_row, null);
      }
    }

    mHolder.round = (TextView) convertView.findViewById(R.id.row_round);
    mHolder.sub_pick = (TextView) convertView.findViewById(R.id.row_sub_pick);
    mHolder.pick = (TextView) convertView.findViewById(R.id.row_pick);
    mHolder.value = (TextView) convertView.findViewById(R.id.row_value);

    convertView.setTag(mHolder);

    mHolder.round.setText(String.valueOf(mRowData.ROUND[position]));
    mHolder.sub_pick.setText(String.valueOf(mRowData.SUBPICK[position]));
    mHolder.pick.setText(String.valueOf(mRowData.PICK[position]));
    mHolder.value.setText(String.valueOf(mRowData.VALUE[position]));

    return convertView;
  } // public View getView()

  public void updateTeam(int page, int team) {
    mTeam = team;

    Cursor cursor;
    cursor = mDbAdapter.fetchTeamPicks(mTeam);
    mRowData.initializeData(cursor.getCount());
    if(cursor.moveToFirst()) {
      for(int i = 0; i < cursor.getCount(); i++) {
        mRowData.ROUND[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_ROUND));
        mRowData.SUBPICK[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_SUB_PICK));
        mRowData.PICK[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_PICK));
        mRowData.BLOCKED[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_BLOCKED));
        mRowData.VALUE[i] = cursor.getDouble(cursor.getColumnIndex(DbAdapter.KEY_VALUE));
        mRowData.SELA[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_SEL_A));
        mRowData.SELB[i] = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_SEL_B));
        cursor.moveToNext();
      }
    }
    cursor.close();
  }

  private class RowData {
    int[] ROUND = {0};
    int[] SUBPICK = {0};
    int[] PICK = {0};
    int[] BLOCKED = {0};
    double[] VALUE = {0};
    int[] SELA = {0};
    int[] SELB = {0};

    public void initializeData(int rows) {
      ROUND = new int[rows];
      SUBPICK = new int[rows];
      PICK = new int[rows];
      BLOCKED = new int[rows];
      VALUE = new double[rows];
      SELA = new int[rows];
      SELB = new int[rows];
    }

    public int length() {
      return ROUND.length;
    }
  }

  public void itemClicked(int page, int position) {
    //Toast.makeText(mCtx, "Test" + position, Toast.LENGTH_SHORT).show();
    if(mDbAdapter.checkSelection(page, mRowData.PICK[position])) {
      removeSelection(page, position);
    } else {
      makeSelection(page, position);
    }
  }

  private void makeSelection(int page, int position) {
    if(mRowData.BLOCKED[position] > 0) {
      Toast.makeText(mCtx, "Compensatory picks cannot be traded", Toast.LENGTH_SHORT).show();
    } else {
      mDbAdapter.makeSelection(page, mRowData.PICK[position]);
    }
    refresh(page);
  }
  
  private void removeSelection(int page, int position) {
    mDbAdapter.removeSelection(page, mRowData.PICK[position]);
    refresh(page);
  }

  public void refresh(int page) {
    updateTeam(page, mTeam);
    updateScore(page);
  }

  private void updateScore(int page) {
    mSum = 0;
    if(page == 1) {
      for(int i = 0; i < mRowData.VALUE.length; i++) {
        if(mRowData.SELA[i] > 0) {
          mSum += mRowData.VALUE[i];
        }
      }
    } else {
      for(int i = 0; i < mRowData.VALUE.length; i++) {
        if(mRowData.SELB[i] > 0) {
          mSum += mRowData.VALUE[i];
        }
      }
    }
  }

  public double getSum(int page) {
    refresh(page);
    return (double) (Math.round(mSum*100))/100;
  }

  public void resetSelections(int page) {
    mDbAdapter.resetSelections(page);
  }

} // private static class EfficientAdapter extends BaseAdapter

