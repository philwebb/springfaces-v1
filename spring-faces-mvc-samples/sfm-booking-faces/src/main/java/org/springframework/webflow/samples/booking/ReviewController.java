package org.springframework.webflow.samples.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.annotation.NavigationRules;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@FacesController
@RequestMapping("/reviewHotel")
@NavigationRules( { @NavigationCase(on = "cancel", to = "main"),
	@NavigationCase(on = "book", to = "booking?hotelId=#{hotel.id}") })
public class ReviewController {
    // FIXME support flowRedirect:

    @Autowired
    private BookingService bookingService;

    @RequestMapping
    public ModelAndView reviewHotel(@RequestParam("id") Long id) {
	return new ModelAndView("reviewHotel").addObject("hotel", bookingService.findHotelById(id));
    }
}
