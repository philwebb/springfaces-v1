package org.springframework.faces.mvc.annotation.sample;

import javax.faces.context.FacesContext;

import junit.framework.Assert;

import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.bind.annotation.NavigationRules;
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

	@NavigationCase(on = "methodcallwithrequestmapping")
	@RequestMapping
	public String methodCallWithRequestMapping() {
		return "someview";
	}

	@NavigationCase(on = "methodcallwithto", to = "test")
	public String methodCallWithTo() {
		Assert.fail("Method called");
		return null;
	}

	public boolean isMethodCalled() {
		return methodCalled;
	}
}
