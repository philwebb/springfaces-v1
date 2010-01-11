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
import org.springframework.faces.mvc.support.MvcFacesStateHolderComponent;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// FIXME cache control

@FacesController
public class MainController {

    private BookingService bookingService;
    private ConversionService conversionService;

    @NavigationRules( { @NavigationCase(on = "search", to = "/search2?#{searchCriteria}") })
    @RequestMapping("/main2")
    public String main(@ModelAttribute("pageScope.searchCriteria") SearchCriteria searchCriteria) {
	searchCriteria.resetPage();
	return "enterSearchCriteria";
    }

    @RequestMapping("/search2")
    @NavigationRules( {
	    @NavigationCase(on = "select", to = "reviewHotel?id=#{hotels.selectedRow.id}"),
	    @NavigationCase(on = "changeSearch", to = "/main2?searchString=#{searchCriteria.searchString}&pageSize=#{searchCriteria.pageSize}", popup = true, fragments = "hotelSearchFragment") })
    public String search(@ModelAttribute("pageScope.searchCriteria") SearchCriteria searchCriteria, Model model) {
	model.addAttribute("pageScope.hotels", doSearch(searchCriteria));
	return "reviewHotels";
    }

    private DataModel doSearch(SearchCriteria searchCriteria) {
	List<Hotel> hotels = bookingService.findHotels(searchCriteria);
	return (DataModel) conversionService.executeConversion(hotels, DataModel.class);
    }

    // FIXME navigation reqest mapping? perhaps on navigation rules
    @NavigationCase(on = { "next", "previous" }, to = "/search2?#{searchCriteria}")
    public void navigate(FacesContext facesContext, NavigationRequestEvent event,
	    @ModelAttribute("pageScope.searchCriteria") SearchCriteria searchCriteria) {
	if ("next".equals(event.getOutcome())) {
	    searchCriteria.nextPage();
	}
	if ("previous".equals(event.getOutcome())) {
	    searchCriteria.previousPage();
	}
    }

    @NavigationCase(on = "sort", fragments = "hotels:searchResultsFragment")
    // to = "/search2?#{searchCriteria}
    public void sort(FacesContext facesContext, NavigationRequestEvent event,
	    @ModelAttribute("pageScope.searchCriteria") SearchCriteria searchCriteria,
	    @RequestParam("sortBy") String sortBy2) {
	System.out.println("sort");
	// FIXME should be command line param
	String sortBy = facesContext.getExternalContext().getRequestParameterMap().get("sortBy");
	searchCriteria.setSortBy(sortBy);
	MvcFacesStateHolderComponent pshc = MvcFacesStateHolderComponent.locate(facesContext, true);
	List<Hotel> hotels = bookingService.findHotels(searchCriteria);
	DataModel hotelsDataModel = (DataModel) conversionService.executeConversion(hotels, DataModel.class);
	pshc.getPageScope().put("hotels", hotelsDataModel);
    }

    @RequestMapping("/reviewHotel")
    @NavigationRules( { @NavigationCase(on = "cancel", to = "main2") })
    public String select(@RequestParam("id") Long id, Model model) {
	Hotel hotel = bookingService.findHotelById(id);
	model.addAttribute("hotel", hotel);
	return "reviewHotel";
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
