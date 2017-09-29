package me.blog.korn123.easydiary.fragments;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import me.blog.korn123.easydiary.adapters.CaldroidItemAdapter;

public class CaldroidCustomFragment extends CaldroidFragment {

	@Override
	public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
		// TODO Auto-generated method stub
		return new CaldroidItemAdapter(getActivity(), month, year,
				getCaldroidData(), extraData);
	}

}
