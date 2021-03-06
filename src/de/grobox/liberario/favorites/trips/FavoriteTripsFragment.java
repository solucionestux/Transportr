package de.grobox.liberario.favorites.trips;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.favorites.trips.FavoriteTripManager.FavoriteTripsLoadedListener;
import de.grobox.liberario.fragments.TransportrFragment;
import de.grobox.liberario.ui.LceAnimator;
import de.schildbach.pte.dto.Location;

import static android.support.v7.util.SortedList.INVALID_POSITION;
import static de.grobox.liberario.utils.TransportrUtils.findDirections;

@ParametersAreNonnullByDefault
public class FavoriteTripsFragment extends TransportrFragment implements FavoriteTripListener, FavoriteTripsLoadedListener {

	public static final String TAG = FavoriteTripsFragment.class.getName();
	private static final String TOP_MARGIN = "topMargin";

	@Inject FavoriteTripManager favoriteTripManager;
	private ProgressBar progressBar;
	private RecyclerView list;
	private FavoriteTripAdapter adapter;

	public static FavoriteTripsFragment newInstance(boolean topMargin) {
		FavoriteTripsFragment f = new FavoriteTripsFragment();
		Bundle args = new Bundle();
		args.putBoolean(TOP_MARGIN, topMargin);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_favorites, container, false);
		getComponent().inject(this);

		progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

		list = (RecyclerView) v.findViewById(R.id.favorites);
		adapter = new FavoriteTripAdapter(this);
		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(getContext()));
		if (!getArguments().getBoolean(TOP_MARGIN)) {
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) list.getLayoutParams();
			params.topMargin = 0;
			list.setLayoutParams(params);
		}
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		List<FavoriteTripItem> trips = favoriteTripManager.getTrips();
		if (trips == null) {
			LceAnimator.showLoading(progressBar, list, null);
			favoriteTripManager.addOnFavoriteTripsLoadedListener(this);
		} else {
			onFavoriteTripsLoaded(trips);
		}
	}

	@Override
	public void onFavoriteTripsLoaded(List<FavoriteTripItem> trips) {
		LceAnimator.showContent(progressBar, list, null);
		adapter.clear();
		adapter.addAll(trips);
	}

	@Override
	public void onFavoriteClicked(FavoriteTripItem item) {
		if (item.getType() == FavoriteTripType.HOME) {
			if (item.getTo() == null) {
				changeHome();
			} else {
				findDirections(getContext(), item.getFrom(), item.getVia(), item.getTo());
			}
		} else if (item.getType() == FavoriteTripType.WORK) {
			if (item.getTo() == null) {
				changeWork();
			} else {
				findDirections(getContext(), item.getFrom(), item.getVia(), item.getTo());
			}
		} else if (item.getType() == FavoriteTripType.TRIP) {
			if (item.getFrom() == null || item.getTo() == null) throw new IllegalArgumentException();
			findDirections(getContext(), item.getFrom(), item.getVia(), item.getTo());
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void onFavoriteChanged(FavoriteTripItem item) {
		int position = adapter.findItemPosition(item);
		if (position != INVALID_POSITION) {
			adapter.updateItem(position, item);
		}
	}

	@Override
	public void changeHome() {
		HomePickerDialogFragment f = HomePickerDialogFragment.newInstance();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), HomePickerDialogFragment.TAG);
	}

	@Override
	public void changeWork() {
		WorkPickerDialogFragment f = WorkPickerDialogFragment.newInstance();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), WorkPickerDialogFragment.TAG);
	}

	@Override
	public void onHomeChanged(Location home) {
		onSpecialLocationChanged(adapter.getHome(), new FavoriteTripItem(FavoriteTripType.HOME, home));
	}

	@Override
	public void onWorkChanged(Location work) {
		onSpecialLocationChanged(adapter.getWork(), new FavoriteTripItem(FavoriteTripType.WORK, work));
	}

	private void onSpecialLocationChanged(@Nullable FavoriteTripItem oldItem, FavoriteTripItem newItem) {
		if (oldItem == null) return;
		int position = adapter.findItemPosition(oldItem);
		if (position == INVALID_POSITION) return;

		View view = list.findViewHolderForAdapterPosition(position).itemView;
		ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getWidth(), 0).start();

		adapter.updateItem(position, newItem);
	}

}
