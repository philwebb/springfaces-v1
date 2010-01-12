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
package org.springframework.faces.mvc.annotation.sample;

import javax.faces.context.FacesContext;

import junit.framework.Assert;

import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.annotation.NavigationRules;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.web.bind.annotation.RequestMapping;

@FacesController
@NavigationRules( { @NavigationCase(on = "con1", to = "cto1"), @NavigationCase(on = "mon1", to = "cmto1"),
		@NavigationCase(on = "caon1", fromAction = "#{bean.action1}", to = "cato1"),
		@NavigationCase(on = "caon1", fromAction = "#{bean.action2}", to = "cato2"),
		@NavigationCase(on = "ceon1", onException = IllegalStateException.class, to = "ceto1") })
@NavigationCase(on = "con2", to = "cto2")
public class SampleController {

	private boolean methodCalled = false;
	private String outcome = null;

	@RequestMapping("/withrules/*")
	@NavigationRules( { @NavigationCase(on = "mon1", to = "mto1"),
			@NavigationCase(on = "maon1", fromAction = "#{bean.action1}", to = "mato1"),
			@NavigationCase(on = "maon1", fromAction = "#{bean.action2}", to = "mato2") })
	@NavigationCase(on = "mon2", to = "mto2")
	public String withRules() {
		return "someview";
	}

	@RequestMapping("/defaulton/*")
	@NavigationRules( { @NavigationCase(to = "dto1") })
	public String defaultOn() {
		return "someview";
	}

	@RequestMapping("/norules/*")
	@NavigationCase(on = "mon2", to = "mto2")
	public String noRules() {
		return "someview";
	}

	@NavigationCase(on = "methodcall")
	public String methodCall(String navigation, NavigationCase navigationCase, NavigationRequestEvent event,
			FacesContext facesContext) {
		this.methodCalled = true;
		Assert.assertEquals("methodcall", navigation);
		Assert.assertEquals("methodcall", navigationCase.on()[0]);
		Assert.assertEquals("#{action.test}", event.getFromAction());
		Assert.assertEquals("methodcall", event.getOutcome());
		Assert.assertNotNull(facesContext);
		return "someview";
	}

	@NavigationCase(on = "methodcallwithrequestmappingandnoto")
	@RequestMapping
	public String methodCallWithRequestMappingAndNoTo() {
		methodCalled = true;
		return "someview";
	}

	@NavigationCase(on = "methodcallwithrequestmapping", to = "test")
	@RequestMapping
	public String methodCallWithRequestMapping() {
		methodCalled = true;
		return "someview";
	}

	@NavigationCase(on = "methodcallwithto", to = "test")
	public void methodCallWithTo() {
		this.methodCalled = true;
	}

	@NavigationCase(on = "methodcallwithtoandreturn", to = "test")
	public String methodCallWithToAndReturn() {
		this.methodCalled = true;
		return outcome;
	}

	public boolean isMethodCalled() {
		return methodCalled;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
}
