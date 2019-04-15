package com.example.springsocial.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * @author Yazeed Hammad - yazeed.hammad@erabia.com
 *
 *         Jan 6, 2019
 */

@RestController
@EnableWebMvc
@RequestMapping("google")
public class TestController {

	private static final Collection<String> SCOPES = Arrays.asList("email", "profile");
	private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";
	private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	private GoogleAuthorizationCodeFlow flow;

	@GetMapping("/login")
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		// Ensure that this is no request forgery going on, and that the user
		// sending us this connect request is the user that was supposed to.
		if (req.getSession().getAttribute("state") == null
				|| !req.getParameter("state").equals((String) req.getSession().getAttribute("state"))) {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			resp.sendRedirect("/books");
			return;
		}

		req.getSession().removeAttribute("state"); // Remove one-time use state.

		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				req.getServletContext().getInitParameter("166177836426-dp2sgpdemfdc0h37ud3ikjhemm93c0hv.apps.googleusercontent.com"),
				req.getServletContext().getInitParameter("bBF2AXHH5BeG2rBA2tRjIeO1"), SCOPES).build();

		final TokenResponse tokenResponse = flow.newTokenRequest(req.getParameter("code"))
				.setRedirectUri(req.getServletContext().getInitParameter("bookshelf.callback")).execute();

		req.getSession().setAttribute("token", tokenResponse.toString()); // Keep track of the token.
		final Credential credential = flow.createAndStoreCredential(tokenResponse, null);
		final HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);

		final GenericUrl url = new GenericUrl(USERINFO_ENDPOINT); // Make an authenticated request.
		final com.google.api.client.http.HttpRequest request = requestFactory.buildGetRequest(url);
		request.getHeaders().setContentType("application/json");

		final String jsonIdentity = request.execute().parseAsString();
		@SuppressWarnings("unchecked")
		HashMap<String, String> userIdResult = new ObjectMapper().readValue(jsonIdentity, HashMap.class);
		// From this map, extract the relevant profile info and store it in the session.
		req.getSession().setAttribute("userEmail", userIdResult.get("email"));
		req.getSession().setAttribute("userId", userIdResult.get("sub"));
		req.getSession().setAttribute("userImageUrl", userIdResult.get("picture"));
		resp.sendRedirect((String) req.getSession().getAttribute("loginDestination"));
	}
}
