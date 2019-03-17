package de.haw_landshut.lackmann.haw_mensa;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_ITEMS = 10;

    SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    private Fragment[] fragments = new Fragment[getCount()];

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        for (Fragment fragment : fragments) {
            if (fragment != null) {
                ((RefreshableFragment) fragment).refresh();
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(MainActivity.TAG, String.format("New Fragment requested %d", position));
        if (fragments[position] == null) {
            DayFragment fragment = new DayFragment();
            fragments[position] = fragment;
        }

        return fragments[position];
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Context context = MainActivity.getAppContext();
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);

        switch (position) {
            case 0:
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY:
                        return context.getString(R.string.section_today).toUpperCase();
                    case Calendar.TUESDAY:
                        return context.getString(R.string.section_yesterday).toUpperCase();
                    default:
                        return context.getString(R.string.section_monday).toUpperCase();
                }
            case 1:
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY:
                        return context.getString(R.string.section_tomorrow).toUpperCase();
                    case Calendar.TUESDAY:
                        return context.getString(R.string.section_today).toUpperCase();
                    case Calendar.WEDNESDAY:
                        return context.getString(R.string.section_yesterday).toUpperCase();
                    default:
                        return context.getString(R.string.section_tuesday).toUpperCase();
                }
            case 2:
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.TUESDAY:
                        return context.getString(R.string.section_tomorrow).toUpperCase();
                    case Calendar.WEDNESDAY:
                        return context.getString(R.string.section_today).toUpperCase();
                    case Calendar.THURSDAY:
                        return context.getString(R.string.section_yesterday).toUpperCase();
                    default:
                        return context.getString(R.string.section_wednesday).toUpperCase();
                }
            case 3:
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.WEDNESDAY:
                        return context.getString(R.string.section_tomorrow).toUpperCase();
                    case Calendar.THURSDAY:
                        return context.getString(R.string.section_today).toUpperCase();
                    case Calendar.FRIDAY:
                        return context.getString(R.string.section_yesterday).toUpperCase();
                    default:
                        return context.getString(R.string.section_thursday).toUpperCase();
                }
            case 4:
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.THURSDAY:
                        return context.getString(R.string.section_tomorrow).toUpperCase();
                    case Calendar.FRIDAY:
                        return context.getString(R.string.section_today).toUpperCase();
                    default:
                        return context.getString(R.string.section_friday).toUpperCase();
                }
            case 5:
                return context.getString(R.string.section_next_monday).toUpperCase();
            case 6:
                return context.getString(R.string.section_next_tuesday).toUpperCase();
            case 7:
                return context.getString(R.string.section_next_wednesday).toUpperCase();
            case 8:
                return context.getString(R.string.section_next_thursday).toUpperCase();
            case 9:
                return context.getString(R.string.section_next_friday).toUpperCase();
            default:
                return null;
        }
    }

    public void setToFetching(boolean on, boolean animated) {
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                ((DayFragment) fragment).setToFetching(on, animated);
            }
        }
    }

    public ArrayList<Integer> getDaySections() {
        ArrayList<Integer> sections = new ArrayList<Integer>();
        for (int i = 0; i < getCount(); i++) {
            sections.add(i);
        }
        return sections;
    }
}