package br.com.teste;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;


import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.Accounts;
import com.google.api.services.analytics.model.GaData;
import com.google.api.services.analytics.model.Profiles;
import com.google.api.services.analytics.model.Webproperties;

import java.io.File;
import java.io.IOException;


public class HelloAnalytics1 {

	private static final String APPLICATION_NAME = "Hello Analytics";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String KEY_FILE_LOCATION = "/home/foo/bar/../client_secrets.p12";
	private static final String SERVICE_ACCOUNT_EMAIL = "xxxxx";

	public static void main(String[] args) {
		try {
			Analytics analytics = initializeAnalytics();
			String profile = getFirstProfileId(analytics);
			
			System.out.println("First Profile Id: " + profile);
			printResults(getResults(analytics, profile));
		} catch (GoogleJsonResponseException e) { 
			      System.err.println("There was a service error: " + e.getDetails().getCode() + " : " 
			          + e.getDetails().getMessage()); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Analytics initializeAnalytics() throws Exception {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
				.setJsonFactory(JSON_FACTORY).setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
				.setServiceAccountPrivateKeyFromP12File(new File(KEY_FILE_LOCATION))
				.setServiceAccountScopes(AnalyticsScopes.all()).build();

		return new Analytics.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	private static String getFirstProfileId(Analytics analytics) throws IOException {
		String profileId = null;

		Accounts accounts = analytics.management().accounts().list().execute();

		if (accounts.getItems().isEmpty()) {
			System.err.println("No accounts found");
		} else {
			String firstAccountId = accounts.getItems().get(0).getId();

			Webproperties properties = analytics.management().webproperties().list(firstAccountId).execute();

			if (properties.getItems().isEmpty()) {
				System.err.println("No Webproperties found");
			} else {
				String firstWebpropertyId = properties.getItems().get(0).getId();

				Profiles profiles = analytics.management().profiles().list(firstAccountId, firstWebpropertyId)
						.execute();

				if (profiles.getItems().isEmpty()) {
					System.err.println("No views (profiles) found");
				} else {
					profileId = profiles.getItems().get(0).getId();
				}
			}
		}
		return profileId;
	}

	private static GaData getResults(Analytics analytics, String profileId) throws IOException {
		return analytics.data().ga().get("ga:" + profileId, "7daysAgo", "today", "ga:sessions").execute();
	}

	private static void printResults(GaData results) {
		if (results != null && !results.getRows().isEmpty()) {
			System.out.println("View (Profile) Name: " + results.getProfileInfo().getProfileName());
			System.out.println("Total Sessions: " + results.getRows().get(0).get(0));
		} else {
			System.out.println("No results found");
		}
	}

}
