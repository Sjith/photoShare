/**
 * 
 */
package com.photoshare.service.photos;

import org.json.JSONException;
import org.json.JSONObject;

import com.photoshare.common.ResponseBean;
import com.photoshare.exception.NetworkException;

/**
 * @author czj_yy
 * 
 */
public class PhotoGetInfoResponseBean extends ResponseBean {

	private PhotoBean photo;

	/**
	 * @param response
	 */
	public PhotoGetInfoResponseBean(String response) {
		super(response);

		if (response == null) {
			return;
		}

		try {
			JSONObject obj = new JSONObject(response);
			photo = new PhotoBean().parse(obj);
		} catch (JSONException e) {

		} catch (NetworkException e) {

		}
	}

	public PhotoBean getPhoto() {
		return photo;
	}

}
