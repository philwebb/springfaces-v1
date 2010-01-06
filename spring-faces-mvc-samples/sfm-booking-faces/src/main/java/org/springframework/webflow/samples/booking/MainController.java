package org.springframework.webflow.samples.booking;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.convert.ConversionService;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.bind.annotation.NavigationRules;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FacesController
public class MainController {

    // FIXME cache control
    private BookingService bookingService;
    private ConversionService conversionService;

    @NavigationRules( { @NavigationCase(on = "search", to = "/search2?#{searchCriteria}") })
    @RequestMapping("/main2")
    public String main(@ModelAttribute("pageScope.searchCriteria") SearchCriteria searchCriteria) {
	return "enterSearchCriteria";
    }

    // FIXME popup=true?
    @RequestMapping("/search2")
    @NavigationRules( {
	    @NavigationCase(on = "select", to = "reviewHotel?id=#{hotels.selectedRow.id}"),
	    @NavigationCase(on = "changeSearch", to = "/main2?searchString=#{searchCriteria.searchString}&pageSize=#{searchCriteria.pageSize}", popup = true) })
    public String search(@ModelAttribute("pageScope.searchCriteria") SearchCriteria searchCriteria, Model model) {
	List<Hotel> hotels = bookingService.findHotels(searchCriteria);
	DataModel hotelsDataModel = (DataModel) conversionService.executeConversion(hotels, DataModel.class);
	model.addAttribute("pageScope.hotels", hotelsDataModel);
	return "reviewHotels";
    }

    @RequestMapping("/reviewHotel")
    @NavigationRules( { @NavigationCase(on = "cancel", to = "main2") })
    public String select(@RequestParam("id") Long id, Model model) {
	Hotel hotel = bookingService.findHotelById(id);
	model.addAttribute("hotel", hotel);
	return "reviewHotel";
    }

    // FIXME navigation reqest mapping? perhaps on navigation rules
    @NavigationCase(on = { "next", "previous" }, to = "/search2?#{searchCriteria}")
    public void navigate(FacesContext facesContext, NavigationRequestEvent event,
	    @ModelAttribute("pageScope.searchCriteria") SearchCriteria searchCriteria) {
	// FIXME should be got
	searchCriteria = (SearchCriteria) facesContext.getELContext().getELResolver().getValue(
		facesContext.getELContext(), null, "searchCriteria");
	if ("next".equals(event.getOutcome())) {
	    searchCriteria.nextPage();
	}
	if ("previous".equals(event.getOutcome())) {
	    searchCriteria.previousPage();
	}
    }

    @Autowired
    public void setBookingService(BookingService bookingService) {
	this.bookingService = bookingService;
    }

    @Autowired
    public void setConversionService(ConversionService conversionService) {
	this.conversionService = conversionService;
    }
}
