/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.faces.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockExternalContext12;
import org.apache.shale.test.mock.MockFacesContext12;
import org.apache.shale.test.mock.MockHttpServletRequest;
import org.apache.shale.test.mock.MockHttpServletResponse;
import org.apache.shale.test.mock.MockServletContext;
import org.easymock.EasyMock;

public class RequestMappedModelBindingExecutorTests extends TestCase {

	public void testBinderRequired() throws Exception {
		RequestMappedModelBindingExecutor executor = new RequestMappedModelBindingExecutor();
		try {
			executor.afterPropertiesSet();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The modelBinder attribute is required", e.getMessage());
		}
	}

	public void testBind() throws Exception {
		RequestMappedModelBindingExecutor executor = new RequestMappedModelBindingExecutor();
		ModelBinder modelBinder = (ModelBinder) EasyMock.createMock(ModelBinder.class);
		executor.setModelBinder(modelBinder);
		executor.afterPropertiesSet();
		MockFacesContext12 facesContext = new MockFacesContext12();
		ExternalContext externalContext = new MockExternalContext12(new MockServletContext(),
				new MockHttpServletRequest(), new MockHttpServletResponse());
		facesContext.setExternalContext(externalContext);
		Map model = new HashMap();
		model.put("test", "value");
		modelBinder.bindModel(model);
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { modelBinder });
		executor.storeModelToBind(facesContext, model);
		try {
			EasyMock.verify(new Object[] { modelBinder });
			fail("Model was bound too early");
		} catch (AssertionError e) {
		}
		executor.bindStoredModel(facesContext);
		EasyMock.verify(new Object[] { modelBinder });
	}
}
