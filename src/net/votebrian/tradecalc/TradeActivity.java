package net.votebrian.tradecalc;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class TradeActivity extends FragmentActivity {

  private TradePagerAdapter mTabsAdapter;
  private ViewPager mViewPager;
  private TabHost mTabHost;

  private static DbAdapter mDbAdapter;

  private static Context mCtx;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mCtx = this;

    mDbAdapter = new DbAdapter(this);
    mDbAdapter.open();

    mTabHost = (TabHost) findViewById(android.R.id.tabhost);
    mTabHost.setup();

    mViewPager = (ViewPager) findViewById(R.id.trade_pager);

    mTabsAdapter = new TradePagerAdapter(this, mTabHost, mViewPager);

    mTabsAdapter.addTab(mTabHost.newTabSpec("teamA").setIndicator("Team A"),
        CountingFragmentA.class, null);
    mTabsAdapter.addTab(mTabHost.newTabSpec("teamB").setIndicator("Team B"),
        CountingFragmentB.class, null);
    mTabsAdapter.addTab(mTabHost.newTabSpec("results").setIndicator("Results"),
        CountingFragmentA.class, null);

    if (savedInstanceState != null) {
      mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
    }
  }


  //-----PAGER ADAPTER-----//
  public static class TradePagerAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private final Context mContext;
    private final TabHost mTabHost;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    public TradePagerAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
      super(activity.getSupportFragmentManager());
      mContext = activity;
      mTabHost = tabHost;
      mViewPager = pager;
      mTabHost.setOnTabChangedListener(this);
      mViewPager.setAdapter(this);
      mViewPager.setOnPageChangeListener(this);
    }

    static final class TabInfo {
      private final String tag;
      private final Class<?> clss;
      private final Bundle args;

      TabInfo(String _tag, Class<?> _clss, Bundle _args) {
        tag = _tag;
        clss = _clss;
        args = _args;
      }
    }

    public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
      tabSpec.setContent(new DummyTabFactory(mContext));
      String tag = tabSpec.getTag();

      TabInfo info = new TabInfo(tag, clss, args);
      mTabs.add(info);
      mTabHost.addTab(tabSpec);
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
      TabInfo info = mTabs.get(position);
      return Fragment.instantiate(mContext, info.clss.getName(), info.args);
    }

    @Override
    public void onTabChanged(String tabId) {
      int position = mTabHost.getCurrentTab();
      mViewPager.setCurrentItem(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      // nothing
    }

    // This seems to just correct for a change in focus
    @Override
    public void onPageSelected(int position) {
      TabWidget widget = mTabHost.getTabWidget();
      int oldFocusability = widget.getDescendantFocusability();
      widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
      mTabHost.setCurrentTab(position);
      widget.setDescendantFocusability(oldFocusability);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
      // nothing
    }

    //-----DUMMY FACTORY-----//
    static class DummyTabFactory implements TabHost.TabContentFactory {
      private final Context mContext;

      public DummyTabFactory(Context context) {
        mContext = context;
      }

      @Override
      public View createTabContent(String tag) {
        View v = new View(mContext);
        v.setMinimumWidth(0);
        v.setMinimumHeight(0);
        return v;
      }
    }
  }


  //-----COUNTER FRAGMENT A-----//
  public static class CountingFragmentA extends Fragment {
    private final int TEAM_IND = 1; // used to indicate ViewPager page.  I'm cheating.
    int mNum;
    Cursor cursor;
    int mTeam = 1;
    View mView;
    public int defaultColor;

    static CountingFragmentA newInstance(int num) {
      CountingFragmentA f = new CountingFragmentA();

      Bundle args = new Bundle();
      args.putInt("num", num);
      f.setArguments(args);

      return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mNum = getArguments() != null ? getArguments().getInt("num") : 1;
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
              mTeam = position + 1;
              populateList(getActivity());
            }

            public void onNothingSelected(AdapterView<?> parent) {
              // chill
            }
          });

      populateList(getActivity());
      
      return mView;
    }

    public void populateList(FragmentActivity activity) {
      ListView lv = (ListView) mView.findViewById(R.id.picks_list);
      cursor = mDbAdapter.fetchTeamPicks(mTeam);
      activity.startManagingCursor(cursor);

      String[] from = new String[] {
          DbAdapter.KEY_ROUND,
          DbAdapter.KEY_SUB_PICK,
          DbAdapter.KEY_PICK,
          DbAdapter.KEY_VALUE};
      int[] to = new int[] {R.id.row_round, R.id.row_sub_pick, R.id.row_pick, R.id.row_value};

      SimpleCursorAdapter picks = new SimpleCursorAdapter(
          getActivity().getApplicationContext(),
          R.layout.trade_row,
          cursor,
          from,
          to);

      lv.setAdapter(picks);
      lv.setOnItemClickListener(
          new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              if(checkSelection(id)) {
                  //Toast.makeText(getActivity(), "TRUE " + id, Toast.LENGTH_SHORT).show();
                  removeSelection(id, view);
              } else {
                  //Toast.makeText(getActivity(), "FALSE " + id, Toast.LENGTH_SHORT).show();
                  makeSelection(id, view);
              }
            }
          });
    }

    private boolean checkSelection(long id) {
      return mDbAdapter.checkSelection(1, id);
    }

    private void makeSelection(long id, View view) {
      mDbAdapter.makeSelection(1, id);
      view.setBackgroundColor(0xFF33B5E5);
      TextView v1 = (TextView) view.findViewById(R.id.static_round);
      v1.setTextColor(0xFFF2F2F2);
    }

    private void removeSelection(long id, View view) {
      mDbAdapter.removeSelection(1, id);
      view.setBackgroundColor(0xFFF2F2F2);
      TextView v1 = (TextView) view.findViewById(R.id.static_round);
      v1.setTextColor(0xFF33B5E5);
    }
  }
  

  //-----COUNTER FRAGMENT B-----//
  public static class CountingFragmentB extends Fragment {
    int mNum;
    Cursor cursor;
    int mTeam = 1;
    View mView;

    static CountingFragmentB newInstance(int num) {
      CountingFragmentB f = new CountingFragmentB();

      Bundle args = new Bundle();
      args.putInt("num", num);
      f.setArguments(args);

      return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mNum = getArguments() != null ? getArguments().getInt("num") : 1;
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
              mTeam = position + 1;
              populateList(getActivity(), mNum);
            }

            public void onNothingSelected(AdapterView<?> parent) {
              // chill
            }
          });

      populateList(getActivity(), mNum);
      
      return mView;
    }

    public void populateList(FragmentActivity activity, int num) {
      final int mNum = num;
      ListView lv = (ListView) mView.findViewById(R.id.picks_list);
      cursor = mDbAdapter.fetchTeamPicks(mTeam);
      activity.startManagingCursor(cursor);

      String[] from = new String[] {
          DbAdapter.KEY_ROUND,
          DbAdapter.KEY_SUB_PICK,
          DbAdapter.KEY_PICK,
          DbAdapter.KEY_VALUE};
      int[] to = new int[] {R.id.row_round, R.id.row_sub_pick, R.id.row_pick, R.id.row_value};

      SimpleCursorAdapter picks = new SimpleCursorAdapter(
          getActivity().getApplicationContext(),
          R.layout.trade_row,
          cursor,
          from,
          to);

      lv.setAdapter(picks);
      lv.setOnItemClickListener(
          new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              CharSequence text = "Fragment " + 2;
              Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
              //parent.setSelected(true);
              view.setBackgroundColor(0xFF33B5E5);
            }
          });
    }
  }

}