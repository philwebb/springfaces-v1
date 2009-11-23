package org.springframework.faces.mvc.support;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext12;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.MvcFacesTestUtils;

public class MvcHandlerVariableResolverTest extends TestCase {
	public void testResolve() throws Exception {
		FacesContext facesContext = new MockFacesContext12();
		VariableResolver nextResolver = (VariableResolver) MvcFacesTestUtils.nullImplementation(VariableResolver.class);
		MvcHandlerVariableResolver resolver = new MvcHandlerVariableResolver(nextResolver);
		MvcFacesContext mvcFacesContext = EasyMock.createMock(MvcFacesContext.class);
		FacesHandler facesHandler = EasyMock.createMock(FacesHandler.class);
		EasyMock.expect(facesHandler.resolveVariable("test")).andReturn(null);
		EasyMock.replay(new Object[] { facesHandler });
		MvcFacesRequestContext requestContext = new MvcFacesRequestContext(mvcFacesContext, facesHandler);
		resolver.resolveVariable(facesContext, "test");
		EasyMock.verify(new Object[] { facesHandler });
	}
}
