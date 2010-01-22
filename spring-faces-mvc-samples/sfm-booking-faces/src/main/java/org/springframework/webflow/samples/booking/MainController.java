package org.springframework.webflow.samples.booking;

import java.security.Principal;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.faces.mvc.converter.QuickConverter;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.annotation.NavigationRules;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@FacesController
@NavigationRules( { @NavigationCase(on = "search", to = "/search?#{searchCriteria}") })
@RequestMapping("/main")
public class MainController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private QuickConverter converter;

    @RequestMapping
    public ModelAndView main(@ModelAttribute("searchCriteria") SearchCriteria searchCriteria) {
	searchCriteria.resetPage();
	return new ModelAndView("enterSearchCriteria").addObject("bookings", getBookings());
    }

    @NavigationCase(fragments = "bookingsFragment")
    public void cancelBooking(@ModelAttribute("bookings.selectedRow") Booking booking) {
	bookingService.cancelBooking(booking);
	RequestContextHolder.getRequestContext().getViewScope().put("bookings", getBookings());
    }

    private DataModel getBookings() {
	ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
	Principal user = externalContext.getUserPrincipal();
	List<Booking> bookings = bookingService.findBookings(user == null ? "" : user.getName());
	return converter.toDataModel(bookings);
    }
}
