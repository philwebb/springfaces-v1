package org.springframework.webflow.samples.booking;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.faces.mvc.converter.QuickConverter;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.annotation.NavigationRules;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@FacesController
@RequestMapping("/search")
@NavigationRules( {
	@NavigationCase(on = "select", to = "reviewHotel?id=#{hotels.selectedRow.id}"),
	@NavigationCase(on = "changeSearch", to = "/main?#{searchCriteria}", popup = true, fragments = "hotelSearchFragment") })
public class SearchController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private QuickConverter converter;

    @RequestMapping
    public ModelAndView search(@ModelAttribute("searchCriteria") SearchCriteria searchCriteria) {
	return new ModelAndView("reviewHotels").addObject("hotels", doSearch(searchCriteria));
    }

    @NavigationCase(on = { "next", "previous" }, to = "/search?#{searchCriteria}")
    public void navigate(FacesContext facesContext, String event,
	    @ModelAttribute("searchCriteria") SearchCriteria searchCriteria) {
	if ("next".equals(event)) {
	    searchCriteria.nextPage();
	}
	if ("previous".equals(event)) {
	    searchCriteria.previousPage();
	}
    }

    @NavigationCase(fragments = "hotels:searchResultsFragment")
    public void sort(@ModelAttribute("searchCriteria") SearchCriteria searchCriteria,
	    @RequestParam("sortBy") String sortBy) {
	searchCriteria.setSortBy(sortBy);
	MvcFacesRequestContextHolder.getRequestContext().getViewScope().put("hotels", doSearch(searchCriteria));
    }

    private DataModel doSearch(SearchCriteria searchCriteria) {
	List<Hotel> hotels = bookingService.findHotels(searchCriteria);
	return converter.toDataModel(hotels);
    }
}
