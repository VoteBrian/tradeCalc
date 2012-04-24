package net.votebrian.tradecalc;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class TradeActivity extends FragmentActivity {

  private TradePagerAdapter mTabsAdapter;
  private ViewPager mViewPager;
  private TabHost mTabHost;

  private static Context mCtx;

  static DbAdapter mDbAdapter;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.layout.menu_activity, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case R.id.menu_share:
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "NFL Draft Proposal");
        i.putExtra(android.content.Intent.EXTRA_TEXT, buildTextBody());
        startActivity(i);
        return true;
      case R.id.menu_about:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Created by: Brian Flores\n\nDraft Pick value data taken from:\nhttp://sports.espn.go.com/nfl/draft06/news/story?id=2410670")
               .setCancelable(false)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                     return;
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private String buildTextBody() {
    String post = "";
    String teamA = "";
    String teamB = "";
    String selectionsA = "";
    String selectionsB = "";

    double sumA = 0;
    double sumB = 0;

    String[] names = getResources().getStringArray(R.array.team_names);

    Cursor crsA = mDbAdapter.pullASelections();
    for(int i = 0; i < crsA.getCount(); i++) {
      teamA = names[crsA.getInt(crsA.getColumnIndex(DbAdapter.KEY_TEAM))-1];
      sumA = sumA + crsA.getDouble(crsA.getColumnIndex(DbAdapter.KEY_VALUE));
      if(i != 0) {
        selectionsA = selectionsA + ", " + crsA.getInt(crsA.getColumnIndex(DbAdapter.KEY_PICK));
      } else {
        selectionsA = selectionsA + crsA.getInt(crsA.getColumnIndex(DbAdapter.KEY_PICK));
      }
      crsA.moveToNext();
    }

    Cursor crsB = mDbAdapter.pullBSelections();
    for(int i = 0; i < crsB.getCount(); i++) {
      teamB = names[crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_TEAM))-1];
      sumB = sumB + crsB.getDouble(crsB.getColumnIndex(DbAdapter.KEY_VALUE));
      if(i != 0) {
        selectionsB = selectionsB + ", " + crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_PICK));
      } else {
        selectionsB = selectionsB + crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_PICK));
      }
      crsB.moveToNext();
    }
    post = String.format("I proposed the #%s trade picks %s (%,.1f pts) to the #%s for picks %s (%,.1f pts) via @NFLTradeCalc",
        teamA,
        selectionsA,
        sumA,
        teamB,
        selectionsB,
        sumB);

    return post;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mCtx = this;

    mDbAdapter = new DbAdapter(mCtx);
    mDbAdapter.open();

    mTabHost = (TabHost) findViewById(android.R.id.tabhost);
    mTabHost.setup();

    mViewPager = (ViewPager) findViewById(R.id.trade_pager);

    mTabsAdapter = new TradePagerAdapter(this, mTabHost, mViewPager);

    mTabsAdapter.addTab(mTabHost.newTabSpec("teamA").setIndicator("Team A"),
        PageFragmentA.class, null);
    mTabsAdapter.addTab(mTabHost.newTabSpec("teamB").setIndicator("Team B"),
        PageFragmentB.class, null);

    if (savedInstanceState != null) {
      mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
    }
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    mDbAdapter.close();
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


  //-----PAGE FRAGMENT A-----//
  public static class PageFragmentA extends Fragment {
    private final int PAGE_IND = 1; // used to indicate ViewPager page.  I'm cheating.
    int mNum;
    Cursor cursor;
    int mTeam = 1;
    View mView;
    public int defaultColor;
    PicksAdapter picksAdapter;

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

    private void updateTeam(int team) {
      picksAdapter.updateTeam(PAGE_IND, team);
    }

    private void updateSum(double sum) {
      TextView tv = (TextView) mView.findViewById(R.id.footer_total);
      tv.setText(String.valueOf(sum));
      tv.invalidate();
      mView.invalidate();
      mView.buildDrawingCache();
      mView.destroyDrawingCache();
      picksAdapter.refresh(PAGE_IND);
    }
  }  // public static class PageFragmentA...


  //-----PAGE FRAGMENT B-----//
  public static class PageFragmentB extends Fragment {
    private final int PAGE_IND = 2; // used to indicate ViewPager page.  I'm cheating.
    int mNum;
    Cursor cursor;
    int mTeam = 1;
    View mView;
    public int defaultColor;
    PicksAdapter picksAdapter;

    static PageFragmentB newInstance(int num) {
      PageFragmentB f = new PageFragmentB();

      Bundle args = new Bundle();
      args.putInt("num", num);
      f.setArguments(args);

      return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
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

    private void updateTeam(int team) {
      picksAdapter.updateTeam(PAGE_IND, team);
    }

    private void updateSum(double sum) {
      TextView tv = (TextView) mView.findViewById(R.id.footer_total);
      tv.setText(String.valueOf(sum));
      tv.invalidate();
      mView.invalidate();
      mView.buildDrawingCache();
      mView.destroyDrawingCache();
      picksAdapter.refresh(PAGE_IND);
    }
  }  // public static class PageFragmentB...
}