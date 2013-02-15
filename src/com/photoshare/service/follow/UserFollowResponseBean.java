/**
 * 
 */
package com.photoshare.service.follow;

import org.json.JSONException;
import org.json.JSONObject;

import com.photoshare.common.ResponseBean;
import com.photoshare.utils.Utils;

/**
 * @author czj_yy
 * 
 */
public class UserFollowResponseBean extends ResponseBean {

	private boolean isFollowing;

	private long UserId;

	private long FollowId;

	public static final String KEY_USER_ID = "userId";
	public static final String KEY_FOLLOW_ID = "followId";
	public static final String KEY_FOLLOWING = "isFollowing";

	/**
	 * @param response
	 */
	public UserFollowResponseBean(String response) {
		super(response);
		// TODO Auto-generated constructor stub

		if (response == null)
			return;

		try {
			JSONObject obj = new JSONObject(response);
			if (obj != null) {
				isFollowing = obj.optBoolean(KEY_FOLLOWING);
				UserId = obj.optLong(KEY_USER_ID);
				FollowId = obj.optLong(KEY_FOLLOW_ID);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Utils.logger("exception in parsing json data:" + e.getMessage());
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();

		sb.append(KEY_USER_ID).append(" = ").append(UserId).append("\r\n");
		sb.append(KEY_FOLLOW_ID).append(" = ").append(FollowId).append("\r\n");
		sb.append(KEY_FOLLOWING).append(" = ").append(isFollowing)
				.append("\r\n");

		return sb.toString();
	}

	public boolean isFollowing() {
		return isFollowing;
	}

	public long getUserId() {
		return UserId;
	}

	public long getFollowId() {
		return FollowId;
	}

}
