package org.springframework.faces.mvc.el;

import javax.el.CompositeELResolver;
import javax.faces.context.FacesContext;

import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;

/**
 * Resolves "implicit" or well-known variables from Faces MVC; for example "viewScope" in an expression like
 * #{viewScope.foo}. The list of implicit flow variables consists of:
 * 
 * <pre>
 * requestScope
 * flashScope
 * viewScope
 * currentUser
 * </pre>
 * 
 * @author Phillip Webb
 */
public class ImplicitMvcFacesElResolver extends CompositeELResolver {
	// FIXME test
	public ImplicitMvcFacesElResolver() {
		add(new RequestContext());
		add(new ExternalContext());
	}

	private static class RequestContext extends BeanBackedElResolver {
		public RequestContext() {
			map("requestScope");
			map("flashScope");
			map("viewScope");
		}

		protected Object getBean() {
			return MvcFacesRequestContextHolder.getRequestContext();
		}
	}

	private static class ExternalContext extends BeanBackedElResolver {
		public ExternalContext() {
			map("currentUser", "userPrincipal");
		}

		protected Object getBean() {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			return (facesContext == null ? null : facesContext.getExternalContext());
		}
	}
}
