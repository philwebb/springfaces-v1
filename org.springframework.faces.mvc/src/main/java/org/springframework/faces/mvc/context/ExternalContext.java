package org.springframework.faces.mvc.context;

import java.io.Writer;
import java.security.Principal;
import java.util.Locale;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;

/**
 * A facade that provides normalized access to an external system that has called into the Faces MVC system.
 * <p>
 * This context object provides a normalized interface for internal MVC artifacts to use to reason on and manipulate the
 * state of an external actor calling into MVC. It represents the context about a single, <i>external</i> client request
 * to manipulate a flow execution.
 * <p>
 * The design of this interface was inspired by WebFlow and JSF's own ExternalContext abstraction and shares the same
 * name for consistency. If a particular external client type does not support all methods defined by this interface,
 * they can just be implemented as returning an empty map or <code>null</code>.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Jeremy Grelle
 * @author Scott Andrews
 */
public interface ExternalContext {

	/**
	 * Returns the logical path to the application hosting this external context.
	 * @return the context path
	 */
	public String getContextPath();

	/**
	 * Provides access to the parameters associated with the user request that led to SWF being called. This map is
	 * expected to be immutable and cannot be changed.
	 * @return the immutable request parameter map
	 */
	public ParameterMap getRequestParameterMap();

	/**
	 * Provides access to the external request attribute map, providing a storage for data local to the current user
	 * request and accessible to both internal and external SWF artifacts.
	 * @return the mutable request attribute map
	 */
	public MutableAttributeMap getRequestMap();

	/**
	 * Provides access to the external session map, providing a storage for data local to the current user session and
	 * accessible to both internal and external SWF artifacts.
	 * @return the mutable session attribute map
	 */
	public SharedAttributeMap getSessionMap();

	/**
	 * Provides access to the <i>global</i> external session map, providing a storage for data globally accross the user
	 * session and accessible to both internal and external SWF artifacts.
	 * <p>
	 * Note: most external context implementations do not distinguish between the concept of a "local" user session
	 * scope and a "global" session scope. The Portlet world does, but not the Servlet for example. In those cases
	 * calling this method returns the same map as calling {@link #getSessionMap()}.
	 * @return the mutable global session attribute map
	 */
	public SharedAttributeMap getGlobalSessionMap();

	/**
	 * Provides access to the external application map, providing a storage for data local to the current user
	 * application and accessible to both internal and external SWF artifacts.
	 * @return the mutable application attribute map
	 */
	public SharedAttributeMap getApplicationMap();

	/**
	 * Returns true if the current request is an asynchronous Ajax request.
	 * @return true if the current request is an Ajax request
	 */
	public boolean isAjaxRequest();

	/**
	 * Provides access to the user's principal security object.
	 * @return the user principal
	 */
	public Principal getCurrentUser();

	/**
	 * Returns the client locale.
	 * @return the locale
	 */
	public Locale getLocale();

	/**
	 * Provides access to the context object for the current environment.
	 * @return the environment specific context object
	 */
	public Object getNativeContext();

	/**
	 * Provides access to the request object for the current environment.
	 * @return the environment specific request object.
	 */
	public Object getNativeRequest();

	/**
	 * Provides access to the response object for the current environment.
	 * @return the environment specific response object.
	 */
	public Object getNativeResponse();

	/**
	 * Get a writer for writing out a response.
	 * @return the writer
	 * @throws IllegalStateException if the response has completed or is not allowed
	 */
	public Writer getResponseWriter() throws IllegalStateException;

	// FIXME what to expose
	// /**
	// * Is a <i>render</i> response allowed to be written for this request? Always return false after a response has
	// been
	// * completed. May return false before that to indicate a response is not allowed to be completed. For example, in
	// a
	// * Portlet environment, render responses are only allowed in render requests.
	// * @return true if yes, false otherwise
	// */
	// public boolean isResponseAllowed();
	//
	// /**
	// * Request that a flow execution redirect be performed by the calling environment. Typically called from within a
	// * flow execution to request a refresh operation, usually to support "refresh after event processing" behavior.
	// * Calling this method also sets responseComplete status to true.
	// * @see #isResponseComplete()
	// * @throws IllegalStateException if the response has completed
	// */
	// public void requestFlowExecutionRedirect() throws IllegalStateException;
	//
	// /**
	// * Request that a flow definition redirect be performed by the calling environment. Typically called from within a
	// * flow execution end state to request starting a new, independent execution of a flow in a chain-like manner.
	// * Calling this method also sets responseComplete status to true.
	// * @see #isResponseComplete()
	// * @param flowId the id of the flow definition to redirect to
	// * @param input input to pass the flow; this input is generally encoded the url to launch the flow
	// * @throws IllegalStateException if the response has completed
	// */
	// public void requestFlowDefinitionRedirect(String flowId, MutableAttributeMap input) throws IllegalStateException;
	//
	// /**
	// * Request a redirect to an arbitrary resource location. May not be supported in some environments. Calling this
	// * method also sets responseComplete status to true.
	// * @see #isResponseComplete()
	// * @param location the location of the resource to redirect to
	// * @throws IllegalStateException if the response has completed
	// */
	// public void requestExternalRedirect(String location) throws IllegalStateException;
	//
	// /**
	// * Request that the current redirect requested be sent to the client in a manner that causes the client to issue
	// the
	// * redirect from a popup dialog. Only call this method after a redirect has been requested.
	// * @see #requestFlowExecutionRedirect()
	// * @see #requestFlowDefinitionRedirect(String, MutableAttributeMap)
	// * @see #requestExternalRedirect(String)
	// * @throws IllegalStateException if a redirect has not been requested
	// */
	// public void requestRedirectInPopup() throws IllegalStateException;
	//
	// /**
	// * Called by flow artifacts such as View states and end states to indicate they handled the response, typically by
	// * writing out content to the response stream. Setting this flag allows this external context to know the response
	// * was handled, and that it not need to take additional response handling action itself.
	// */
	// public void recordResponseComplete();
	//
	// /**
	// * Has the response been completed? Response complete status can be achieved by:
	// * <ul>
	// * <li>Writing out the response and calling {@link #recordResponseComplete()}, or
	// * <li>Calling one of the redirect request methods
	// * </ul>
	// * @see #getResponseWriter()
	// * @see #recordResponseComplete()
	// * @see #requestFlowExecutionRedirect()
	// * @see #requestFlowDefinitionRedirect(String, MutableAttributeMap)
	// * @see #requestExternalRedirect(String)
	// * @return true if yes, false otherwise
	// */
	// public boolean isResponseComplete();
	//
	// /**
	// * Returns true if the response has been completed with flow execution redirect request.
	// * @return true if a redirect response has been completed
	// * @see #isResponseComplete()
	// * @see #requestFlowExecutionRedirect()
	// */
	// public boolean isResponseCompleteFlowExecutionRedirect();
}