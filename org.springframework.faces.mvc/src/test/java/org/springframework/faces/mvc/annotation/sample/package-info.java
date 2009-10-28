@NavigationRules( { @NavigationCase(on = "pon1", to = "pto1"), @NavigationCase(on = "con1", to = "cpto1"),
		@NavigationCase(on = "mon1", to = "mpto1") })
@NavigationCase(on = "pon2", to = "pto2")
package org.springframework.faces.mvc.annotation.sample;

import org.springframework.faces.bind.annotation.NavigationCase;
import org.springframework.faces.bind.annotation.NavigationRules;

