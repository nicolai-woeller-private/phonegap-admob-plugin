package com.google.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.ads.AdSize;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import android.view.View;

/**
 * This class represents the native implementation for the AdMob Cordova plugin.
 * This plugin can be used to request AdMob ads natively via the Google AdMob
 * SDK. The Google AdMob SDK is a dependency for this plugin.
 */
@SuppressWarnings("deprecation")
public class AdMobPlugin extends CordovaPlugin {

	/** The adView to display to the user. */
	private AdView adView;
	private InterstitialAd intertitial;
	
	/** Cordova Actions. */
	public static final String ACTION_CREATE_BANNER_VIEW = "createBannerView";
	public static final String ACTION_CREATE_INTERSTITIAL_VIEW = "createInterstitialView";
	public static final String ACTION_REQUEST_AD = "requestAd";
	public static final String KILL_AD = "killAd";

	/**
	 * This is the main method for the AdMob plugin. All API calls go through
	 * here. This method determines the action, and executes the appropriate
	 * call.
	 *
	 * @param action
	 *            The action that the plugin should execute.
	 * @param inputs
	 *            The input parameters for the action.
	 * @param callbackId
	 *            The callback ID. This is currently unused.
	 * @return A PluginResult representing the result of the provided action. A
	 *         status of INVALID_ACTION is returned if the action is not
	 *         recognized.
	 */
	@Override
	public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
		if (ACTION_CREATE_BANNER_VIEW.equals(action)) {
			executeCreateBannerView(inputs, callbackContext);
			return true;
		} else if (ACTION_CREATE_INTERSTITIAL_VIEW.equals(action)) {
			executeCreateInterstitialView(inputs, callbackContext);
			return true;
		} else if (ACTION_REQUEST_AD.equals(action)) {
			executeRequestAd(inputs, callbackContext);
			return true;
		} else if (KILL_AD.equals(action)) {
			executeKillAd(callbackContext);
			return true;
		} else {
			callbackContext.error("Invalid Action");
		}
		return false;
	}

	/**
	 * Parses the create banner view input parameters and runs the create banner
	 * view action on the UI thread. If this request is successful, the
	 * developer should make the requestAd call to request an ad for the banner.
	 *
	 * @param inputs
	 *            The JSONArray representing input parameters. This function
	 *            expects the first object in the array to be a JSONObject with
	 *            the input parameters.
	 * @return A PluginResult representing whether or not the banner was created
	 *         successfully.
	 */
	private void executeCreateBannerView(JSONArray inputs, CallbackContext callbackContext) {
		String publisherId = "";
		String size = "";

		// Get the input data.
		try {
			JSONObject data = inputs.getJSONObject(0);
			publisherId = data.getString("publisherId");
			size = data.getString("adSize");
		} catch (JSONException exception) {
			callbackContext.error(exception.getMessage());
		}
		AdSize adSize = adSizeFromSize(size);
		createBannerView(publisherId, adSize, callbackContext);
	}

	private synchronized void createBannerView(final String publisherId, final AdSize adSize,
			final CallbackContext callbackContext) {
		final CordovaInterface cordova = this.cordova;

		// Create the AdView on the UI thread.
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					android.util.Log.v("MobileAds", MobileAds.class.toString());
					MobileAds.initialize(cordova.getActivity(), publisherId);
					adView = (AdView) new View(cordova.getActivity());
					
					AdRequest adRequest = new AdRequest.Builder().build();
					adView.loadAd(adRequest);
				} catch (Exception e) {
					android.util.Log.v("createBannerView", e.getMessage());
				}
				callbackContext.success();
			}
		};
		this.cordova.getActivity().runOnUiThread(runnable);
	}

	/**
	 * Parses the create banner view input parameters and runs the create banner
	 * view action on the UI thread. If this request is successful, the
	 * developer should make the requestAd call to request an ad for the banner.
	 *
	 * @param inputs
	 *            The JSONArray representing input parameters. This function
	 *            expects the first object in the array to be a JSONObject with
	 *            the input parameters.
	 * @return A PluginResult representing whether or not the banner was created
	 *         successfully.
	 */
	private void executeCreateInterstitialView(JSONArray inputs, CallbackContext callbackContext) {
		String publisherId = "";

		// Get the input data.
		try {
			JSONObject data = inputs.getJSONObject(0);
			publisherId = data.getString("publisherId");
		} catch (JSONException exception) {
			callbackContext.error(exception.getMessage());
		}
		createInterstitialView(publisherId, callbackContext);
	}

	private synchronized void createInterstitialView(final String publisherId, final CallbackContext callbackContext) {
		// Create the AdView on the UI thread.
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					MobileAds.initialize(cordova.getActivity(), publisherId);

					// Create the InterstitialAd and set the adUnitId.
					intertitial = new InterstitialAd(cordova.getActivity());
					// Defined in res/values/strings.xml
					intertitial.setAdUnitId(publisherId);

					intertitial.show();
				} catch (Exception e) {
					android.util.Log.v("createInterstitialView", e.getMessage());
				}
			}
		};
		this.cordova.getActivity().runOnUiThread(runnable);
	}

	/**
	 * Parses the request ad input parameters and runs the request ad action on
	 * the UI thread.
	 *
	 * @param inputs
	 *            The JSONArray representing input parameters. This function
	 *            expects the first object in the array to be a JSONObject with
	 *            the input parameters.
	 * @return A PluginResult representing whether or not an ad was requested
	 *         succcessfully. Listen for onReceiveAd() and onFailedToReceiveAd()
	 *         callbacks to see if an ad was successfully retrieved.
	 */
	private void executeRequestAd(JSONArray inputs, CallbackContext callbackContext) {
		boolean isTesting = false;
		JSONObject inputExtras = null;

		// Get the input data.
		try {
			JSONObject data = inputs.getJSONObject(0);
			isTesting = data.getBoolean("isTesting");
			inputExtras = data.getJSONObject("extras");
			// callbackContext.success();
			// return true;
		} catch (JSONException e) {
			android.util.Log.v("executeRequestAd", e.getMessage());
			callbackContext.error(e.getMessage());
		}

		// Request an ad on the UI thread.
		if (adView != null) {
			requestAd(isTesting, inputExtras, callbackContext);
		} else if (intertitial != null) {
			requestIntertitial(isTesting, inputExtras, callbackContext);
		} else {
			callbackContext.error("adView && intertitial are null. Did you call createBannerView?");
			return;
		}
	}

	private synchronized void requestIntertitial(final boolean isTesting, final JSONObject inputExtras,
			final CallbackContext callbackContext) {
		// Create the AdView on the UI thread.
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (intertitial == null) {
					callbackContext.error("intertitial is null. Did you call createBannerView?");
					return;
				} else {
					AdRequest adRequest = new AdRequest.Builder().build();
					intertitial.loadAd(adRequest);
					callbackContext.success();
				}
			}
		};
		cordova.getActivity().runOnUiThread(runnable);
	}

	private synchronized void requestAd(final boolean isTesting, final JSONObject inputExtras,
			final CallbackContext callbackContext) {
		// Create the AdView on the UI thread.
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (adView == null) {
					callbackContext.error("AdView is null.  Did you call createBannerView?");
					return;
				} else {
					AdRequest adRequest = new AdRequest.Builder().build();
					adView.loadAd(adRequest);
					callbackContext.success();
				}
			}
		};
		cordova.getActivity().runOnUiThread(runnable);
	}

	private void executeKillAd(final CallbackContext callbackContext) {
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (adView == null) {
					callbackContext.error("AdView is null.  Did you call createBannerView or already destroy it?");
				} else {
					adView.removeAllViews();
					adView.destroy();
					adView = null;
					callbackContext.success();
				}
			}
		};

		cordova.getActivity().runOnUiThread(runnable);
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
			adView = null;
		}
		super.onDestroy();
	}

	/**
	 * Gets an AdSize object from the string size passed in from JavaScript.
	 * Returns null if an improper string is provided.
	 *
	 * @param size
	 *            The string size representing an ad format constant.
	 * @return An AdSize object used to create a banner.
	 */
	public static AdSize adSizeFromSize(String size) {
		if ("BANNER".equals(size)) {
			return AdSize.BANNER;
		} else if ("IAB_MRECT".equals(size)) {
			return AdSize.IAB_MRECT;
		} else if ("IAB_BANNER".equals(size)) {
			return AdSize.IAB_BANNER;
		} else if ("IAB_LEADERBOARD".equals(size)) {
			return AdSize.IAB_LEADERBOARD;
		} else if ("SMART_BANNER".equals(size)) {
			return AdSize.SMART_BANNER;
		} else {
			return null;
		}
	}
}
