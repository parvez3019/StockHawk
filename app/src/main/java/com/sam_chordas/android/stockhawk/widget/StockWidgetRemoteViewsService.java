package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by parvez on 14/6/16.
 */
public class StockWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor cursor = null;
            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null) {
                    cursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                Log.d("Remoteview","datasetchanged");

                // Query Generation
                cursor = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        new String[] {
                                QuoteColumns._ID,
                                QuoteColumns.SYMBOL,
                                QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE,
                                QuoteColumns.CHANGE,
                                QuoteColumns.ISUP
                        },
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }

                // Bind Layout with views
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                // Bind data to the views
                views.setTextViewText(R.id.stock_symbol, cursor.getString(cursor.getColumnIndex
                        (getResources().getString(R.string.string_symbol))));

                if (cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP)) == 1) {
                    views.setInt(R.id.change, getResources().getString(R.string.string_background_resource), R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, getResources().getString(R.string.string_background_resource), R.drawable.percent_change_pill_red);
                }

                Log.d("Remoteview", cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));

                if (Utils.showPercent) {
                    views.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                } else {
                    views.setTextViewText(R.id.change, cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE)));
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getResources().getString(R.string.string_symbol), cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null; // use the default loading view

            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (cursor != null && cursor.moveToPosition(position)) {
                    final int QUOTES_ID_COL = 0;
                    return cursor.getLong(QUOTES_ID_COL);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}