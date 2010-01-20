package org.springframework.webflow.samples.booking;

import java.security.Principal;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.faces.mvc.converter.QuickConverter;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.annotation.NavigationRules;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.core.collection.MutableAttributeMap;

// FIXME cache control

@FacesController
public class MainController {

    private BookingService bookingService;
    private QuickConverter converter;

    private DataModel doSearch(SearchCriteria searchCriteria) {
	List<Hotel> hotels = bookingService.findHotels(searchCriteria);
	return converter.toDataModel(hotels);
    }

    private DataModel getBookings() {
	ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
	Principal user = externalContext.getUserPrincipal();
	List<Booking> bookings = bookingService.findBookings(user == null ? "" : user.getName());
	return converter.toDataModel(bookings);
    }

    @NavigationRules( { @NavigationCase(on = "search", to = "/search?#{searchCriteria}") })
    @RequestMapping("/main")
    public ModelAndView main(@ModelAttribute("viewScope.searchCriteria") SearchCriteria searchCriteria) {
	searchCriteria.resetPage();
	ModelAndView modelAndView = new ModelAndView("enterSearchCriteria");
	modelAndView.addObject("viewScope.bookings", getBookings());
	return modelAndView;
    }

    @NavigationCase(fragments = "bookingsFragment")
    public void cancelBooking(@ModelAttribute("bookings.selectedRow") Booking booking,
	    @ModelAttribute("viewScope") MutableAttributeMap viewScope) {
	bookingService.cancelBooking(booking);
	// FIXME would be nice to have onRender to do this
	viewScope.put("bookings", getBookings());
    }

    @RequestMapping("/search")
    @NavigationRules( {
	    @NavigationCase(on = "select", to = "reviewHotel?id=#{hotels.selectedRow.id}"),
	    @NavigationCase(on = "changeSearch", to = "/main?searchString=#{searchCriteria.searchString}&pageSize=#{searchCriteria.pageSize}", popup = true, fragments = "hotelSearchFragment") })
    public String search(@ModelAttribute("viewScope.searchCriteria") SearchCriteria searchCriteria, Model model) {
	model.addAttribute("viewScope.hotels", doSearch(searchCriteria));
	return "reviewHotels";
    }

    @RequestMapping("/reviewHotel")
    @NavigationRules( { @NavigationCase(on = "cancel", to = "main"),
	    @NavigationCase(on = "book", to = "booking?hotelId=#{hotel.id}") })
    public String reviewHotel(@RequestParam("id") Long id, Model model) {
	Hotel hotel = bookingService.findHotelById(id);
	model.addAttribute("viewScope.hotel", hotel);
	return "reviewHotel";
    }

    // FIXME support flow redirect
    // FIXME change the default scope to viewScope

    @NavigationCase(on = { "next", "previous" }, to = "/search?#{searchCriteria}")
    public void navigate(FacesContext facesContext, NavigationRequestEvent event,
	    @ModelAttribute("viewScope.searchCriteria") SearchCriteria searchCriteria) {
	if ("next".equals(event.getOutcome())) {
	    searchCriteria.nextPage();
	}
	if ("previous".equals(event.getOutcome())) {
	    searchCriteria.previousPage();
	}
    }

    @NavigationCase(fragments = "hotels:searchResultsFragment")
    public void sort(@ModelAttribute("viewScope") MutableAttributeMap viewScope,
	    @ModelAttribute("searchCriteria") SearchCriteria searchCriteria, @RequestParam("sortBy") String sortBy) {
	searchCriteria.setSortBy(sortBy);
	viewScope.put("hotels", doSearch(searchCriteria));
    }

    @Autowired
    public void setBookingService(BookingService bookingService) {
	this.bookingService = bookingService;
    }

    @Autowired
    public void setConverter(QuickConverter converter) {
	this.converter = converter;
    }
}
