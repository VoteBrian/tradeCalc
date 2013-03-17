package net.votebrian.tradecalc;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragment;

//-----PAGE FRAGMENT A-----//
public class PageFragmentA extends SherlockFragment {
  private final int PAGE_IND = 1; // used to indicate ViewPager page.  I'm cheating.
  Context mCtx;
  int mNum;
  Cursor cursor;
  int mTeam = 1;
  View mView;
  PicksAdapter picksAdapter;
  OnUpdateListener updateListener;
  DbAdapter mDbAdapter;

  static PageFragmentA newInstance(int num) {
    PageFragmentA f = new PageFragmentA();

    Bundle args = new Bundle();
    args.putInt("num", num);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mCtx = getActivity();
    mDbAdapter = new DbAdapter(mCtx);
    mDbAdapter.open();
  }

  @Override
  public void onDestroy() {
	  mDbAdapter.close();
    super.onDestroy();
  }

  public void setContext(Context context) {
    mCtx = context;
  }

  public void setDbHelper(DbAdapter adapter) {
    mDbAdapter = adapter;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    mView = inflater.inflate(R.layout.trades_page, container, false);

    Spinner s = (Spinner) mView.findViewById(R.id.spinner);
    ArrayAdapter<CharSequence> sAdapter = ArrayAdapter.createFromResource(mCtx, R.array.teams, android.R.layout.simple_spinner_item);
    sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    s.setAdapter(sAdapter);
    s.setOnItemSelectedListener(
        new OnItemSelectedListener() {
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mTeam = position;
            picksAdapter.resetSelections(PAGE_IND);
            updateTeam(mTeam);
            picksAdapter.notifyDataSetChanged();
            updateSum(picksAdapter.getSum(PAGE_IND));
          }

          public void onNothingSelected(AdapterView<?> parent) {
            // chill
          }
        });

    ListView lv = (ListView) mView.findViewById(R.id.picks_list);
    picksAdapter = new PicksAdapter(PAGE_IND, mCtx, mDbAdapter);
    lv.setAdapter(picksAdapter);

    lv.setOnItemClickListener(
        new android.widget.AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            picksAdapter.itemClicked(PAGE_IND, position);
            picksAdapter.notifyDataSetChanged();
            updateSum(picksAdapter.getSum(PAGE_IND));
          }
        });

    updateSum(picksAdapter.getSum(PAGE_IND));

    return mView;
  }  // View onCreateView(...)

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      updateListener = (OnUpdateListener) activity;
    } catch(ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnUpdateListener");
    }
  }

  private void updateTeam(int team) {
    picksAdapter.updateTeam(PAGE_IND, team);
    updateListener.onUpdatedTeamA(team);
  }

  private void updateSum(double sum) {
    mView.invalidate();
    mView.buildDrawingCache();
    mView.destroyDrawingCache();
    picksAdapter.refresh(PAGE_IND);
    updateListener.onUpdatedTotalA(sum);
  }

  public interface OnUpdateListener {
    public void onUpdatedTotalA(double sum);
    public void onUpdatedTeamA(int team);
  }
}
