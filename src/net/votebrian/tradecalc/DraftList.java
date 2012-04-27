package net.votebrian.tradecalc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class DraftList extends Activity {
  private DbAdapter mDbAdapter;
  View mView;
  FullListAdapter mFullList;
  ListView lv;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.full_list);

    mDbAdapter = new DbAdapter(this);
    mDbAdapter.open();

    lv = (ListView) findViewById(R.id.full_list_view);
    mFullList = new FullListAdapter(this, mDbAdapter);

    lv.setAdapter(mFullList);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }
}
