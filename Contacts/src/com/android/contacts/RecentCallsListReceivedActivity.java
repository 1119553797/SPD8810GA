package com.android.contacts;

public class RecentCallsListReceivedActivity extends MsmsRecentCallsListActivity{

    @Override
    protected int getCallType() {
        return RecentCallsListActivity.CALL_TYPE_SHOW_RECEIVED;
    }

}
