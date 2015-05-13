package com.currencycloud.client;

import co.freeside.betamax.Betamax;
import co.freeside.betamax.MatchRule;
import com.currencycloud.client.model.CurrencyCloudException;
import com.currencycloud.client.model.ErrorMessage;
import org.junit.Test;
import org.testng.Assert;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;

@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "UnnecessaryBoxing"})
public class ErrorTest extends BetamaxTestSupport {

    private String loginId = "rjnienaber@gmail.com";
    private String apiKey = "ef0fd50fca1fb14c1fab3a8436b9ecb65f02f129fd87eafa45ded8ae257528f0";

    @Test
    @Betamax(tape = "contains_full_details_for_api_error", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testContainsFullDetailsForApiError() throws Exception {
        loginId = "non-existent-login-id";
        apiKey = "ef0fd50fca1fb14c1fab3a8436b9ecb57528f0";
        CurrencyCloudException error = testFailedLogin("auth_invalid_user_login_details", 400);
        Assert.assertEquals(error.getErrorMessages().get("api_key").get(0).getCode(), "api_key_length_is_invalid");
        Assert.assertEquals(error.getErrorMessages().get("api_key").get(0).getMessage(), "api_key should be 64 character(s) long");
        Assert.assertEquals(error.getErrorMessages().get("api_key").get(0).getParams().get("length"), new Integer(64));
    }

    @Test
    @Betamax(tape = "is_raised_on_a_bad_request", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnABadRequest() throws Exception {
        loginId = "non-existent-login-id";
        apiKey = "ef0fd50fca1fb14c1fab3a8436b9ecb57528f0";
        CurrencyCloudException error = testFailedLogin("auth_invalid_user_login_details", 400);
        ErrorMessage errorMessage = error.getErrorMessages().get("api_key").get(0);
        Assert.assertEquals(errorMessage.getCode(), "api_key_length_is_invalid");
        Assert.assertEquals(errorMessage.getMessage(), "api_key should be 64 character(s) long");
        Assert.assertEquals(errorMessage.getParams().get("length"), new Integer(64));
    }

    @Test
    @Betamax(tape = "is_raised_on_a_forbidden_request", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnAForbiddenRequest() throws Exception {
        CurrencyCloudException error = testFailedLogin("auth_failed", 403);
        assertThat(error.getErrorCode(), equalTo("auth_failed"));
        assertThat(error.getHttpStatusCode(), equalTo(403));


        ErrorMessage errorMessage = error.getErrorMessages().get("username").get(0);
        assertThat(errorMessage.getCode(), equalTo("invalid_supplied_credentials"));
        assertThat(errorMessage.getMessage(), equalTo("Authentication failed with the supplied credentials"));
        assertThat(errorMessage.getParams(), is(anEmptyMap()));
    }

    @Test
    @Betamax(tape = "is_raised_on_an_internal_server_error", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnAnInternalServerError() throws Exception {
        CurrencyCloudException error = testFailedLogin("internal_application_error", 500);
        ErrorMessage errorMessage = error.getErrorMessages().get("base").get(0);
        assertThat(errorMessage.getCode(), equalTo("internal_application_error"));
        assertThat(errorMessage.getMessage(), equalTo("A general application error occurred"));
        assertThat(errorMessage.getParams(), hasEntry("request_id", (Object)2771875643610572878L));
    }

    @Test
    @Betamax(tape = "is_raised_on_incorrect_authentication_details", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedOnIncorrectAuthenticationDetails() throws Exception {
        loginId = "non-existent-login-id";
        apiKey = "efb5ae2af84978b7a37f18dd61c8bbe139b403009faea83484405a3dcb64c4d8";
        CurrencyCloudException e = testFailedLogin("auth_failed", 401);
        Assert.assertEquals(e.getErrorMessages().get("username").size(), 1);
        Assert.assertEquals(e.getErrorMessages().get("username").get(0).getCode(), "invalid_supplied_credentials");
        Assert.assertEquals(e.getErrorMessages().get("username").get(0).getMessage(), "Authentication failed with the supplied credentials");
        Assert.assertTrue(e.getErrorMessages().get("username").get(0).getParams().isEmpty());
    }

    @Test
    @Betamax(tape = "is_raised_when_too_many_requests_have_been_issued", match = {MatchRule.method, MatchRule.uri, MatchRule.body})
    public void testIsRaisedWhenTooManyRequestsHaveBeenIssued() throws Exception {
        loginId = "rjnienaber@gmail.com2";
        CurrencyCloudException error = testFailedLogin("too_many_requests", 429);
        ErrorMessage errorMessage = error.getErrorMessages().get("base").get(0);
        assertThat(errorMessage.getCode(), equalTo("too_many_requests"));
        assertThat(errorMessage.getMessage(), equalTo("Too many requests have been made to the api. Please refer to the Developer Center for more information"));
        assertThat(errorMessage.getParams(), is(anEmptyMap()));
    }

    // todo: handling of timout errors

    // todo: handling of "resource not found"

    private CurrencyCloudException testFailedLogin(String errorCode, int httpStatusCode) {
        try {
            client.authenticate(loginId, apiKey);
            throw new AssertionError("Should have failed");
        } catch (CurrencyCloudException error) {
            assertThat(error.getHttpStatusCode(), equalTo(httpStatusCode));
            assertThat(error.getErrorCode(), equalTo(errorCode));
            assertThat(error.getErrorMessages().size(), equalTo(1));
            return error;
        }
    }
}