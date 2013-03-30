/**
 * 
 */
package com.photoshare.service.photos.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.photoshare.command.Command;
import com.photoshare.common.AbstractRequestListener;
import com.photoshare.exception.NetworkError;
import com.photoshare.exception.NetworkException;
import com.photoshare.fragments.BaseFragment;
import com.photoshare.service.LikeHelper;
import com.photoshare.service.likes.PhotoLikeRequestParam;
import com.photoshare.service.likes.PhotoLikeResponseBean;
import com.photoshare.service.photos.PhotoBean;
import com.photoshare.service.photos.RequestPhotoType;
import com.photoshare.service.photos.PhotosGetInfoRequestParam;
import com.photoshare.service.photos.PhotosGetInfoResponseBean;
import com.photoshare.service.users.UserInfo;
import com.photoshare.tabHost.R;
import com.photoshare.utils.Utils;
import com.photoshare.view.NotificationDisplayer;

/**
 * @author Aron
 * 
 */
public class FeedsFragment extends BaseFragment {

	private FeedsView feedsView;
	private UserInfo userInfo;
	private int currentPage = 0;
	private int demandPage = 10;
	private RequestPhotoType type;
	private ArrayList<PhotoBean> photos;
	private NotificationDisplayer mNotificationDisplayer;
	private String leftBtnText = "";
	private String rightBtnText = "";
	private String titlebarText = "";
	private int leftBtnVisibility = View.INVISIBLE;
	private int rightBtnVisibility = View.INVISIBLE;

	public static FeedsFragment newInstance(int fragmentViewId) {
		FeedsFragment feeds = new FeedsFragment();
		feeds.setFragmentViewId(fragmentViewId);
		return feeds;
	}

	public RequestPhotoType getType() {
		return type;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			if (bundle.containsKey(UserInfo.KEY_USER_INFO)) {
				userInfo = bundle.getParcelable(UserInfo.KEY_USER_INFO);
			}
			if (bundle.containsKey(PhotoBean.KEY_PHOTO_TYPE)) {
				type = RequestPhotoType.SWITCH(bundle
						.getString(PhotoBean.KEY_PHOTO_TYPE));
			}
			if (bundle.containsKey(PhotoBean.KEY_PHOTOS)) {
				photos = bundle.getParcelableArrayList(PhotoBean.KEY_PHOTOS);
			}
		}
		Tag = getFeedsFragment();
		if (type != null) {
			switch (type) {
			case MyFeeds:
				titlebarText = getFeedsText();
				break;
			case MyLikedPhotos:
				titlebarText = getMyLikedPhotoText();
				break;
			case MyPhotos:
				titlebarText = getMyPhotoText();
				break;
			case PopularPhotos:
				titlebarText = getPopularPhoto();
				break;
			default:
				break;
			}
		}
		if (!processArguments()) {
			initTitleBar(leftBtnText, rightBtnText, titlebarText,
					leftBtnVisibility, rightBtnVisibility);
		} else {
			leftBtnText = getBackText();
			leftBtnVisibility = View.VISIBLE;
			initTitleBar(leftBtnText, rightBtnText, titlebarText,
					leftBtnVisibility, rightBtnVisibility);
		}
		if (photos != null && photos.size() != 0) {
			initFeeds();
		} else {
			try {
				AsyncGetFeeds();
			} catch (NetworkException e) {
				AsyncSignIn();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (!processArguments()) {
			container.addView(super.onCreateView(inflater, container,
					savedInstanceState));
		}
		return inflater.inflate(R.layout.feeds_layout, container, false);
	}

	private String getLikeTag() {
		return getString(R.string.nLikeTag);
	}

	private String getLikeTicker() {
		return getString(R.string.nLikeTicker);
	}

	private String getFeedsText() {
		return getString(R.string.feeds);
	}

	private String getMyPhotoText() {
		return getString(R.string.myphotos);
	}

	private String getMyLikedPhotoText() {
		return getString(R.string.mylikedphotos);
	}

	private String getBackText() {
		return getString(R.string.back);
	}

	private String getPopularPhoto() {
		return getString(R.string.popular);
	}

	private String getFeedsFragment() {
		return getString(R.string.ffeedsFragment);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (outState != null) {
			outState.putParcelableArrayList(PhotoBean.KEY_PHOTOS, photos);
		}
		super.onSaveInstanceState(outState);
	}

	private void AsyncLikePhoto(final PhotoBean photo) throws NetworkException {
		PhotoLikeRequestParam param = new PhotoLikeRequestParam.LikeBuilder()
				.UserId(user.getUserInfo().getUid()).PhotoId(photo.getPid())
				.isLike(photo.isLike()).build();
		// mNotificationDisplayer.displayNotification();

		LikeHelper.ICallback mCallback = new LikeHelper.ICallback() {

			public void OnNetworkError(NetworkError error) {

				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {

						public void run() {

						}

					});
				}
			}

			public void OnFault(Throwable fault) {
				mExceptionHandler.obtainMessage(NetworkError.ERROR_NETWORK)
						.sendToTarget();
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {

						public void run() {

						}

					});
				}
			}

			public void OnComplete(final PhotoLikeResponseBean bean) {
				mNotificationDisplayer.setTag(getSuccessTag());
				mNotificationDisplayer.setTicker(getSuccessTicker());
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {

						public void run() {
							if (bean != null && bean.getUserId() != 0) {
								photo.setLike(!bean.isLike());
							}
							mNotificationDisplayer.displayNotification();
							mNotificationDisplayer.cancleNotification();
						}

					});
				}
			}
		};
		async.publishLikePhoto(param, mCallback);
		mNotificationDisplayer.cancleNotification();
	}

	private void AsyncGetFeeds() throws NetworkException {

		PhotosGetInfoRequestParam param = new PhotosGetInfoRequestParam.PhotoRequestBuilder()
				.CurrentPage(currentPage).DemandPage(demandPage)
				.UserId(userInfo.getUid()).Method(type)
				.Field(PhotosGetInfoRequestParam.FIELDS_ALL).build();
		AbstractRequestListener<PhotosGetInfoResponseBean> listener = new AbstractRequestListener<PhotosGetInfoResponseBean>() {

			@Override
			public void onNetworkError(NetworkError networkError) {
				mExceptionHandler
						.obtainMessage(NetworkError.ERROR_REFRESH_DATA)
						.sendToTarget();
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {

						public void run() {
							if (feedsView != null)
								feedsView.onRefreshComplete();
						}

					});
				}
			}

			@Override
			public void onFault(Throwable fault) {
				mExceptionHandler.obtainMessage(NetworkError.ERROR_NETWORK)
						.sendToTarget();
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {

						public void run() {
							if (feedsView != null)
								feedsView.onRefreshComplete();
						}

					});
				}
			}

			@Override
			public void onComplete(final PhotosGetInfoResponseBean bean) {
				if (bean != null) {
					photos = bean.getPhotos();

					currentPage += demandPage;
				}
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {

						public void run() {
							if (bean.getPhotos() != null
									&& !bean.getPhotos().isEmpty()) {
								initFeeds();
								if (feedsView != null) {
									feedsView.onRefreshComplete();
								}
							}
						}

					});
				}
			}
		};
		async.getPhotosInfo(param, listener);
	}

	private void initFeeds() {
		feedsView = new FeedsView(getActivity(), (View) getActivity()
				.findViewById(R.id.feedsLayoutId), photos, async);
		feedsView.registerCallback(mFeedsActionListener);
		feedsView.applyView();
		mNotificationDisplayer = new NotificationDisplayer.NotificationBuilder()
				.Context(getActivity()).Tag(getLikeTag())
				.Ticker(getLikeTicker()).build();
	}

	private void showLike(PhotoBean photo) {

		// String likeText = ctx.getString(R.string.like);
		// String unlikeText = ctx.getString(R.string.unlike);
		boolean isLike = photo.isLike();
		Toast toast = new Toast(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_like, null);
		ImageView image = (ImageView) layout.findViewById(R.id.toastLikeView);
		if (isLike) {
			image.setImageResource(R.drawable.unlike);
		} else {
			image.setImageResource(R.drawable.like);
		}
		toast.setGravity(Gravity.CENTER, 0, 0);
		// ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
		// PhotoType.MIDDLE.getWidth(), PhotoType.MIDDLE.getHeight());
		// layout.setLayoutParams(params);
		// image.setLayoutParams(params);
		layout.setAlpha(0.2f);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	private FeedsView.OnFeedsActionListener mFeedsActionListener = new FeedsView.OnFeedsActionListener() {

		public void OnRefresh() {
			try {
				AsyncGetFeeds();
			} catch (NetworkException e) {
				AsyncSignIn();
			}
		}

		public void OnNameClick(UserInfo info) {
			Bundle args = new Bundle();
			args.putParcelableArrayList(PhotoBean.KEY_PHOTOS, photos);
			args.putParcelable(UserInfo.KEY_USER_INFO, info);
			Command.UserHome(getActivity(), args);
		}

		public void OnLikeClick(PhotoBean photo) {
			try {
				Utils.logger("OnLikeClicked");
				showLike(photo);
				AsyncLikePhoto(photo);
			} catch (NetworkException e) {
				AsyncSignIn();
			}
		}

		public void OnCommentListClick(PhotoBean photo) {
			Bundle args = new Bundle();
			args.putParcelableArrayList(PhotoBean.KEY_PHOTOS, photos);
			args.putParcelable(PhotoBean.KEY_PHOTO, photo);
			forward(getCommentsFragment(), args);
		}

		public void OnLikeListClick(PhotoBean photo) {
			Bundle args = new Bundle();
			args.putParcelableArrayList(PhotoBean.KEY_PHOTOS, photos);
			args.putParcelable(PhotoBean.KEY_PHOTO, photo);
			forward(getLikeFragment(), args);
		}

		public void OnUserHeadLoaded(final ImageView image,
				final Drawable drawable, String url) {
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						image.setImageDrawable(drawable);
					}
				});
			}
		}

		public void OnFeedPhotoLoaded(final ImageView image,
				final Drawable drawable, String url) {
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						image.setImageDrawable(drawable);
					}
				});
			}
		}

		public void OnUserHeadDefault(final ImageView image) {
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						image.setImageResource(R.drawable.icon);
					}
				});
			}
		}

		public void OnFeedPhotoDefault(final ImageView image) {
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {

					public void run() {
						image.setImageResource(R.drawable.icon);
					}
				});
			}

		}
	};

	public void setPhotos(ArrayList<PhotoBean> photos) {
		this.photos = photos;
	}

	public void addPhotoBean(PhotoBean photo) {
		feedsView.addPhotoBean(photo);
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public void setType(RequestPhotoType type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.photoshare.fragments.BaseFragment#OnRightBtnClicked()
	 */
	@Override
	protected void onRightBtnClicked() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.photoshare.fragments.BaseFragment#OnLeftBtnClicked()
	 */
	@Override
	protected void onLeftBtnClicked() {
		backward(null);
	}

	private String getUserHomeFragment() {
		return getString(R.string.fuserHomeFragment);
	}

	private String getCommentsFragment() {
		return getString(R.string.fcommentsFragment);
	}

	private String getLikeFragment() {
		return getString(R.string.flikeFragment);
	}

	@Override
	protected void onLoginSuccess() {
		// TODO Auto-generated method stub

	}

}
