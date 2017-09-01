/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Aug 29, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Joe Kiniry <kiniry@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.auth;

import static us.freeandfair.corla.auth.AuthenticationStage.*;
import static us.freeandfair.corla.model.Administrator.AdministratorType.COUNTY;

import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import spark.Request;
import spark.Response;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.json.SubmittedCredentials;
import us.freeandfair.corla.model.Administrator;
import us.freeandfair.corla.model.Administrator.AdministratorType;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.query.AdministratorQueries;

/**
 * An abstract base class that enforces the two-stage state machine for two-factor
 * authentication.
 * 
 * @author Joseph R. Kiniry
 * @author Daniel M. Zimmerman
 */
@SuppressWarnings({"PMD.AtLeastOneConstructor", "PMD.CyclomaticComplexity",
    "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity",
    "PMD.EmptyMethodInAbstractClassShouldBeAbstract", "PMD.GodClass"})
public abstract class AbstractAuthentication implements AuthenticationInterface {  
  /**
   * Authenticate the administrator `the_username` with credentials
   * `the_password` (for traditional authentication) or `the_second_factor`
   * (for two-factor authentication).  This method should be called twice in
   * succession, first for traditional authentication and second for two-factor
   * authentication.
   * 
   * @trace authentication.authenticate_county_administrator
   * @trace authentication.authenticate_state_administrator
   * @return true iff authentication succeeds.
   * @param the_request The request.
   * @param the_username the username of the person to attempt to authenticate.
   * @param the_password the password for `username`.
   * @param the_second_factor the second factor for `username`.
   */
  @Override
  @SuppressWarnings("PMD.NPathComplexity")
  public boolean authenticateAdministrator(final Request the_request,
                                           final Response the_response,
                                           final String the_username, 
                                           final String the_password,
                                           final String the_second_factor) {
    boolean result = true;
    AuthenticationStage auth_stage = null;
    final Object auth_stage_attribute = the_request.session().attribute(AUTH_STAGE);
    if (auth_stage_attribute instanceof AuthenticationStage) {
      auth_stage = (AuthenticationStage) auth_stage_attribute;
    }
    if (auth_stage == null) {
      auth_stage = NO_AUTHENTICATION;
    } else if (auth_stage != NO_AUTHENTICATION) {
      // if the existing authenticated admin is not this user, deauthenticate
      // the session
      final Object admin_attribute = the_request.session().attribute(ADMIN);
      if (admin_attribute instanceof Administrator &&
          !((Administrator) admin_attribute).username().equals(the_username)) {
        deauthenticate(the_request);
        auth_stage = NO_AUTHENTICATION;
      }
    }
    try {
      // If we didn't get a well-formed request in the first place, fail.
      if (the_username == null || the_username.isEmpty()) {
        result = false;
      } else {
        switch (auth_stage) {
          case NO_AUTHENTICATION:
            if (traditionalAuthenticate(the_request, the_response,
                                        the_username, the_password)) {
              // We have traditionally authenticated.
              final Administrator admin = 
                  AdministratorQueries.byUsername(the_username);
              admin.updateLastLoginTime();
              Persistence.saveOrUpdate(admin);
              the_request.session().attribute(AUTH_STAGE, TRADITIONALLY_AUTHENTICATED);
              the_request.session().attribute(ADMIN, admin);
              Main.LOGGER.info("Traditional authentication succeeded for administrator " + 
                               the_username);
            } else {
              Main.LOGGER.info("Traditional authentication failed for administrator " + 
                               the_username);
              result = false;
            }
            break;

          case TRADITIONALLY_AUTHENTICATED:
            if (secondFactorAuthenticate(the_request, the_username, the_second_factor)) {
              // We have both traditionally and second-factor authenticated.
              final Administrator admin = 
                  AdministratorQueries.byUsername(the_username);
              admin.updateLastLoginTime();
              Persistence.saveOrUpdate(admin);
              the_request.session().attribute(AUTH_STAGE, SECOND_FACTOR_AUTHENTICATED); 
              the_request.session().attribute(ADMIN, admin);
              Main.LOGGER.info("Second factor authentication succeeded for administrator " + 
                               the_username);
            } else {
              // Send the authentication state machine back to its initial state.
              the_request.session().attribute(AUTH_STAGE, NO_AUTHENTICATION);
              Main.LOGGER.info("Second factor authentication failed for administrator" + 
                               the_username);
              result = false;
            }
            break;

          case SECOND_FACTOR_AUTHENTICATED:
            // we are already second-factor authenticated as this user
            break;

          default:
            // this should never happen
            deauthenticate(the_request);
            break;
        }
      }
    } catch (final PersistenceException e) {
      // there's nothing we can really do here other than saying that the
      // authentication failed; it's also possible we failed to update the last
      // login time, but that's not critical
      deauthenticate(the_request);
    }

    if (!result) {
      // a failed authentication attempt removes any existing session authentication 
      deauthenticate(the_request);
      Main.LOGGER.info("Authentication failed for user " + the_username);
    }

    return result;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public County authenticatedCounty(final Request the_request) {
    County result = null;
<<<<<<< HEAD
    if (isAuthenticatedAs(the_request, AdministratorType.COUNTY,
                    the_request.queryParams(AuthenticationInterface.USERNAME))) {
      final Administrator admin = 
          (Administrator) the_request.session().attribute(ADMIN);
      if (admin != null) {
        result = admin.county();
=======
    final Object auth_stage_attribute =
        the_request.session().attribute(AuthenticationInterface.AUTH_STAGE);
    if (auth_stage_attribute instanceof AuthenticationStage &&
        ((AuthenticationStage) auth_stage_attribute) == SECOND_FACTOR_AUTHENTICATED) {
      final Object admin_attribute =
          the_request.session().attribute(ADMIN);
      if (admin_attribute instanceof Administrator) {
        final Administrator admin = (Administrator) admin_attribute;
        final String username = admin.username();
        if (isAuthenticatedAs(the_request, COUNTY, username)) {
          result = CountyQueries.forAdministrator(admin);
        }
>>>>>>> Snapshot of ongoing debug work for Dan.
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Administrator authenticatedAdministrator(final Request the_request) {
    Administrator result = null;
    final Object admin_attribute = the_request.session().attribute(ADMIN);
    if (admin_attribute instanceof Administrator) {
      result = (Administrator) admin_attribute;
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public AuthenticationStatus authenticationStatus(final Request the_request) {
    final Object admin_attribute = the_request.session().attribute(ADMIN);
    final Object stage_attribute = the_request.session().attribute(AUTH_STAGE);
    final AdministratorType type;
    if (admin_attribute instanceof Administrator) {
      type = ((Administrator) admin_attribute).type();
    } else {
      type = null;
    }
    final AuthenticationStage stage;
    if (stage_attribute instanceof AuthenticationStage) {
      stage = (AuthenticationStage) stage_attribute;
    } else {
      stage = null;
    }
    return new AuthenticationStatus(type, stage);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void traditionalDeauthenticate(final Request the_request,
                                        final String the_username) {
    Main.LOGGER.info("session is now traditionally deauthenticated");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void twoFactorDeauthenticate(final Request the_request,
                                      final String the_username) {
    Main.LOGGER.info("session is now second factor deauthenticated");
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean traditionalAuthenticated(final Request the_request) {
    final AuthenticationStage auth_stage = 
        (AuthenticationStage) (the_request.session().attribute(AUTH_STAGE));
    return auth_stage != null &&
        (auth_stage == SECOND_FACTOR_AUTHENTICATED ||
         auth_stage == TRADITIONALLY_AUTHENTICATED);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean secondFactorAuthenticated(final Request the_request) {
    final Object auth_stage_attribute = 
        the_request.session().attribute(AuthenticationInterface.AUTH_STAGE);
    AuthenticationStage auth_stage = null;
    if (auth_stage_attribute instanceof AuthenticationStage) {
      auth_stage = (AuthenticationStage) auth_stage_attribute;
    }
    return auth_stage != null && 
        auth_stage == SECOND_FACTOR_AUTHENTICATED;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAuthenticated(final Request the_request) {
    final Object auth_stage_attribute = 
        the_request.session().attribute(AUTH_STAGE);
    if (auth_stage_attribute instanceof AuthenticationStage) {
      return ((AuthenticationStage) 
               the_request.session().attribute(AUTH_STAGE)) == 
                   SECOND_FACTOR_AUTHENTICATED;
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAuthenticatedAs(final Request the_request,
                                   final AdministratorType the_type,
                                   final String the_username) {
    boolean result = false;
    final Object auth_stage_attribute =
        the_request.session().attribute(AuthenticationInterface.AUTH_STAGE);
    final Object admin_attribute = the_request.session().attribute(ADMIN);
    
    if (auth_stage_attribute instanceof AuthenticationStage &&
        admin_attribute instanceof Administrator) {
      final AuthenticationStage stage = (AuthenticationStage) auth_stage_attribute;
      final Administrator admin = (Administrator) admin_attribute;
      result = stage != NO_AUTHENTICATION &&
               admin.type() == the_type &&
               the_username.equals(admin.username());
    } else if (auth_stage_attribute != null || admin_attribute != null) {
      // this should never happen since we control what's in the session object,
      // but if it does, we'll clear out that attribute and thereby force another
      // authentication
      Main.LOGGER.error("Invalid admin or auth stage type detected in session.");
      deauthenticate(the_request);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean secondFactorAuthenticatedAs(final Request the_request,
                                             final AdministratorType the_type,
                                             final String the_username) {
    boolean result = false;
    final Object auth_stage_attribute =
        the_request.session().attribute(AuthenticationInterface.AUTH_STAGE);
    final Object admin_attribute = the_request.session().attribute(ADMIN);
    
    if (auth_stage_attribute instanceof AuthenticationStage &&
        admin_attribute instanceof Administrator) {
      final AuthenticationStage stage = (AuthenticationStage) auth_stage_attribute;
      final Administrator admin = (Administrator) admin_attribute;
      result = stage == SECOND_FACTOR_AUTHENTICATED &&
               admin.type() == the_type &&
               the_username.equals(admin.username());
    } else if (auth_stage_attribute != null || admin_attribute != null) {
      // this should never happen since we control what's in the session object,
      // but if it does, we'll clear out that attribute and thereby force another
      // authentication
      Main.LOGGER.error("Invalid admin or auth stage type detected in session.");
      deauthenticate(the_request);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void deauthenticate(final Request the_request) {
    // If we are authenticated in any fashion
    final Object admin_attribute = 
        the_request.session().attribute(ADMIN);
    final Administrator admin;
    
    if (admin_attribute instanceof Administrator) {
      admin = (Administrator) admin_attribute;
    } else {
      admin = null;
    }
    
    if (admin == null) {
      Main.LOGGER.warn("Deauthenticated an unauthenticated session.");
    } else {
      // update the last logout time for the logged in administrator
      admin.updateLastLogoutTime();
      Persistence.saveOrUpdate(admin);
      Main.LOGGER.info("Deauthenticated user '" + admin.username() + "'");
      // Take care of any specific back-end deauthentication logic.
      traditionalDeauthenticate(the_request, admin.username());
      twoFactorDeauthenticate(the_request, admin.username());
    }
    
    the_request.session().removeAttribute(ADMIN);
    the_request.session().removeAttribute(AUTH_STAGE);
  }
 
  /**
   * {@inheritDoc}
   */
  @Override
  public void setLogger(final Logger the_logger) {
    // skip, as we have access to Main.LOGGER
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setGSON(final Gson the_gson) {
    // skip, as we have access to Main.GSON
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAuthenticationServerName(final String the_name) {
    // skip, as there is no server necessary for the built-in test service
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public final SubmittedCredentials authenticationCredentials(final Request the_request) {
    SubmittedCredentials result = null;
    // Check for JSON credentials in the request.
    try {
      result = Main.GSON.fromJson(the_request.body(), SubmittedCredentials.class);
    } catch (final JsonParseException jse) {
      // There wasn't JSON there!
    }
    // If there wasn't a JSON request, is there an HTTP params one?
    if (result == null && the_request.queryParams(USERNAME) != null) {
      result = 
          new SubmittedCredentials(
              the_request.queryParams(USERNAME), 
              the_request.queryParams(PASSWORD), 
              the_request.queryParams(SECOND_FACTOR));
    }
    // If there wasn't an HTTP params one, is the session already authenticated? 
    if (result == null && isAuthenticated(the_request)) {
      final Administrator admin = (Administrator) the_request.session().attribute(ADMIN);
      result = new SubmittedCredentials(admin.username(), null, null);
    }
    return result;
  }
}
