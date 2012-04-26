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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class TradeActivity extends FragmentActivity
    implements PageFragmentA.OnUpdateListener, PageFragmentB.OnUpdateListener {

  private TradePagerAdapter mTabsAdapter;
  private ViewPager mViewPager;
  private TabHost mTabHost;
  TextView tvTotalA;
  TextView tvTotalB;

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
        builder.setMessage("Created by: Brian Flores and Tom Vanderslice\n\nDraft Pick value data taken from:\nhttp://sports.espn.go.com/nfl/draft06/news/story?id=2410670")
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
        selectionsA = selectionsA + ", " +
                      crsA.getInt(crsA.getColumnIndex(DbAdapter.KEY_ROUND)) +
                      "." + crsA.getInt(crsA.getColumnIndex(DbAdapter.KEY_SUB_PICK));
      } else {
        selectionsA = selectionsA +
                      crsA.getInt(crsA.getColumnIndex(DbAdapter.KEY_ROUND)) +
                      "." + crsA.getInt(crsA.getColumnIndex(DbAdapter.KEY_SUB_PICK));
      }
      crsA.moveToNext();
    }

    Cursor crsB = mDbAdapter.pullBSelections();
    for(int i = 0; i < crsB.getCount(); i++) {
      teamB = names[crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_TEAM))-1];
      sumB = sumB + crsB.getDouble(crsB.getColumnIndex(DbAdapter.KEY_VALUE));
      if(i != 0) {
          selectionsB = selectionsB + ", " +
                        crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_ROUND)) +
                        "." + crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_SUB_PICK));
        } else {
          selectionsB = selectionsB +
                        crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_ROUND)) +
                        "." + crsB.getInt(crsB.getColumnIndex(DbAdapter.KEY_SUB_PICK));
        }
      crsB.moveToNext();
    }
    post = String.format("I propose the #%s trade picks %s to the #%s for picks %s via @NFLTradeCalc",
        teamA,
        selectionsA,
        teamB,
        selectionsB);

    return post;
  }

  //-----ON CREATE-----//
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

    tvTotalA = (TextView) findViewById(R.id.footer_total_a);
    tvTotalB = (TextView) findViewById(R.id.footer_total_b);
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    mDbAdapter.close();
  }

  @Override
  public void onUpdatedA(double total) {
    tvTotalA.setText(String.valueOf(total));
  }

  @Override
  public void onUpdatedB(double total) {
    tvTotalB.setText(String.valueOf(total));
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
      Fragment fragment;
      TabInfo info = mTabs.get(position);
      fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
      return fragment;
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
}