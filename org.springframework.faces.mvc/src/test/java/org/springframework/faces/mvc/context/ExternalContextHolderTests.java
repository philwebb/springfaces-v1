package org.springframework.faces.mvc.context;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;

public class ExternalContextHolderTests extends TestCase {

	public void testNone() throws Exception {
		RequestContextHolder.setRequestContext(null);
		assertNull(ExternalContextHolder.getExternalContext());
	}

	public void testAvailable() throws Exception {
		RequestControlContext requestContext = (RequestControlContext) EasyMock.createMock(RequestControlContext.class);
		ExternalContext externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
		EasyMock.expect(requestContext.getExternalContext()).andReturn(externalContext);
		EasyMock.replay(new Object[] { requestContext, externalContext });
		RequestContextHolder.setRequestContext(requestContext);
		try {
			assertSame(externalContext, ExternalContextHolder.getExternalContext());
		} finally {
			RequestContextHolder.setRequestContext(null);
		}
	}

}
