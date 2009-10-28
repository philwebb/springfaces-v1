package org.springframework.faces.mvc.annotation.sample;

import org.springframework.faces.bind.annotation.FacesController;
import org.springframework.faces.bind.annotation.NavigationCase;
import org.springframework.faces.bind.annotation.NavigationRules;
import org.springframework.web.bind.annotation.RequestMapping;

@FacesController
@NavigationRules( { @NavigationCase(on = "con1", to = "cto1"), @NavigationCase(on = "mon1", to = "cmto1") })
@NavigationCase(on = "con2", to = "cto2")
public class SampleController {

	@RequestMapping("/withrules/*")
	@NavigationRules( { @NavigationCase(on = "mon1", to = "mto1") })
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
}
