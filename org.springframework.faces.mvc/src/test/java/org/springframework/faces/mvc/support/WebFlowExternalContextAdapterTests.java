package org.springframework.faces.mvc.support;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.webflow.context.ExternalContext;

public class WebFlowExternalContextAdapterTests extends TestCase {

	public void testAdapter() throws Exception {

		ExternalContext externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
		EasyMock.expect(externalContext.getContextPath()).andReturn(null);
		EasyMock.expect(externalContext.getRequestParameterMap()).andReturn(null);
		EasyMock.expect(externalContext.getRequestMap()).andReturn(null);
		EasyMock.expect(externalContext.getSessionMap()).andReturn(null);
		EasyMock.expect(externalContext.getGlobalSessionMap()).andReturn(null);
		EasyMock.expect(externalContext.getApplicationMap()).andReturn(null);
		externalContext.isAjaxRequest();
		EasyMock.expectLastCall().andReturn(Boolean.FALSE);
		EasyMock.expect(externalContext.getCurrentUser()).andReturn(null);
		EasyMock.expect(externalContext.getLocale()).andReturn(null);
		EasyMock.expect(externalContext.getNativeContext()).andReturn(null);
		EasyMock.expect(externalContext.getNativeRequest()).andReturn(null);
		EasyMock.expect(externalContext.getNativeResponse()).andReturn(null);

		EasyMock.replay(new Object[] { externalContext });
		WebFlowExternalContextAdapter adapter = new WebFlowExternalContextAdapter(externalContext);

		adapter.getContextPath();
		adapter.getRequestParameterMap();
		adapter.getRequestMap();
		adapter.getSessionMap();
		adapter.getGlobalSessionMap();
		adapter.getApplicationMap();
		adapter.isAjaxRequest();
		adapter.getCurrentUser();
		adapter.getLocale();
		adapter.getNativeContext();
		adapter.getNativeRequest();
		adapter.getNativeResponse();

		EasyMock.verify(new Object[] { externalContext });
	}
}
