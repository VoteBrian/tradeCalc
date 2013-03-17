package net.votebrian.tradecalc;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TradeActivity extends SherlockFragmentActivity
    implements PageFragmentA.OnUpdateListener, PageFragmentB.OnUpdateListener {

  private ActionBar mBar;
  private TradePagerAdapter mTabsAdapter;
  private ViewPager mViewPager;
  private TabHost mTabHost;
  TextView tvTotalA;
  TextView tvTeamA;
  TextView tvTotalB;
  TextView tvTeamB;
  String team_names[];

  private static Context mCtx;

  public static DbAdapter mDbAdapter;

  //-----ON CREATE-----//
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mCtx = this;

    // populate team_names array for use in footer
    team_names = getResources().getStringArray(R.array.team_names);

    mDbAdapter = new DbAdapter(mCtx);
    mDbAdapter.open();

    //LinearLayout testLayout = getContentById("testLayout");
    mTabHost = (TabHost) findViewById(android.R.id.tabhost);
    mTabHost.setup();

    mBar = getSupportActionBar();
    mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    //mBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

    mViewPager = (ViewPager) findViewById(R.id.trade_pager);

    mTabsAdapter = new TradePagerAdapter(this, mViewPager);

    mTabsAdapter.addTab(mBar.newTab().setText("Team A"),
        PageFragmentA.class, null);
    mTabsAdapter.addTab(mBar.newTab().setText("Team B"),
        PageFragmentB.class, null);

    if (savedInstanceState != null) {
      mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
    }

    tvTotalA = (TextView) findViewById(R.id.footer_total_a);
    tvTeamA = (TextView) findViewById(R.id.footer_team_a);
    tvTotalB = (TextView) findViewById(R.id.footer_total_b);
    tvTeamB = (TextView) findViewById(R.id.footer_team_b);
  }

  public DbAdapter getDbAdapter() {
    return mDbAdapter;
  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    mDbAdapter.close();
  }

  @Override
  public void onUpdatedTotalA(double total) {
    tvTotalA.setText(String.valueOf(total));
  }

  @Override
  public void onUpdatedTeamA(int team) {
	  tvTeamA.setText(team_names[team]);
  }

  @Override
  public void onUpdatedTotalB(double total) {
    tvTotalB.setText(String.valueOf(total));
  }

  @Override
  public void onUpdatedTeamB(int team) {
	  tvTeamB.setText(team_names[team]);
  }

  //-----ON CREATE OPTIONS MENU-----//
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getSupportMenuInflater();
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
      case R.id.menu_list:
        Intent intent = new Intent(this, DraftList.class);
        startActivity(intent);
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


  //-----PAGER ADAPTER-----//
  public static class TradePagerAdapter extends FragmentPagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private final Context mContext;
    private final ViewPager mViewPager;
    private final ActionBar mActionBar;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    public TradePagerAdapter(SherlockFragmentActivity activity, ViewPager pager) {
      super(activity.getSupportFragmentManager());
      mContext = activity;
      mActionBar = activity.getSupportActionBar();
      mViewPager = pager;
      mViewPager.setAdapter(this);
      mViewPager.setOnPageChangeListener(this);
    }

    static final class TabInfo {
      private final Class<?> clss;
      private final Bundle args;

      TabInfo(Class<?> _clss, Bundle _args) {
        clss = _clss;
        args = _args;
      }
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
      TabInfo info = new TabInfo(clss, args);
      tab.setTag(info);
      tab.setTabListener(this);
      mTabs.add(info);
      mActionBar.addTab(tab);
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mTabs.size();
    }

    @Override
    public SherlockFragment getItem(int position) {
      SherlockFragment fragment;
      TabInfo info = mTabs.get(position);
      fragment = (SherlockFragment) Fragment.instantiate(mContext, info.clss.getName(), info.args);
      return fragment;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
      Object tag = tab.getTag();
      for(int i=0; i < mTabs.size(); i++) {
        if(mTabs.get(i) == tag) {
          mViewPager.setCurrentItem(i);
        }
      }
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
      // nothing
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
      // nothing
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      // nothing
    }

    @Override
    public void onPageSelected(int position) {
      mActionBar.setSelectedNavigationItem(position);
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