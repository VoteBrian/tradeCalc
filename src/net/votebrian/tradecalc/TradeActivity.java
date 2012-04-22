package net.votebrian.tradecalc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class TradeActivity extends Activity {

  private Context mCtx;
  private TradePagerAdapter tradeAdapter;
  private ViewPager tradePager;
  private int PAGER_COUNT = 2;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mCtx = this;

    tradeAdapter = new TradePagerAdapter();
    tradePager = (ViewPager) findViewById(R.id.trade_pager);
    tradePager.setAdapter(tradeAdapter);
    
  }


  //-----PAGER ADAPTER-----//
  public class TradePagerAdapter extends PagerAdapter {
    @Override
    public int getCount() {
      return PAGER_COUNT;
    }

    @Override
    public Object instantiateItem(View collection, int position) {
      String spinner_lbl = "";
      View layout = getLayoutInflater().inflate(R.layout.trades_page, null);

      switch(position) {
      case 0:
        spinner_lbl = "Team A";
        break;
      case 1:
        spinner_lbl = "Team B";
        break;
      }

      // set spinner label text
      TextView tv = (TextView) layout.findViewById(R.id.spinner_label);
      tv.setText(spinner_lbl);

      // set spinner drop-down list
      Spinner s1 = (Spinner) layout.findViewById(R.id.spinner);
      ArrayAdapter<CharSequence> s1Adapter = ArrayAdapter.createFromResource(mCtx, R.array.teams, android.R.layout.simple_spinner_item);
      s1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      s1.setAdapter(s1Adapter);

      ((ViewPager) collection).addView(layout);

      return layout;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
      return view == (LinearLayout)obj;
    }
  }
}