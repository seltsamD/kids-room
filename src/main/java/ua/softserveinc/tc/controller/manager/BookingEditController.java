package ua.softserveinc.tc.controller.manager;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;
import ua.softserveinc.tc.constants.BookingConstants;
import ua.softserveinc.tc.dao.BookingDao;
import ua.softserveinc.tc.dto.BookingDto;
import ua.softserveinc.tc.dto.ChildDto;
import ua.softserveinc.tc.entity.User;
import ua.softserveinc.tc.entity.Child;
import ua.softserveinc.tc.entity.Room;
import ua.softserveinc.tc.entity.Booking;
import ua.softserveinc.tc.entity.BookingState;
import ua.softserveinc.tc.entity.Role;
import ua.softserveinc.tc.service.BookingService;
import ua.softserveinc.tc.service.ChildService;
import ua.softserveinc.tc.service.RoomService;
import ua.softserveinc.tc.service.UserService;
import ua.softserveinc.tc.validator.TimeValidatorImpl;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static ua.softserveinc.tc.util.DateUtil.toDateAndTime;

@Controller
public class BookingEditController {

  @Autowired
  private UserService userService;

  @Autowired
  private RoomService roomService;

  @Autowired
  private BookingService bookingService;

  @Autowired
  private ChildService childService;

  @Autowired
  private BookingDao bookingDao;

  @Autowired
  private TimeValidatorImpl timeValidator;

  /**
   * Counting number of children in the selected room for appropriate date.
   *
   * @param date current date for counting active children
   * @param id selected room id
   * @return number of active children
   */

  @GetMapping(value = "getAmountOfChildren/{date}/{id}")
  @ResponseBody
  public Long getAmountOfChildrenForCurrentDay(@PathVariable String date,
      @PathVariable Long id) {
    Room room = roomService.findByIdTransactional(id);
    Date dateLo = toDateAndTime(date + " " + room.getWorkingHoursStart());
    Date dateHi = toDateAndTime(date + " " + room.getWorkingHoursEnd());
    List<Booking> bookings = bookingService
        .getBookings(new Date[]{dateLo, dateHi}, room, BookingState.ACTIVE);

    return (bookings.size() != 0) ? bookings.size() : 0L;

  }

  @GetMapping(value = "get-kids/{id}", produces = "text/plain;charset=UTF-8")
  @ResponseBody
  public String listKids(@PathVariable Long id) {
    List<Child> kids = userService.getEnabledChildren(userService.findByIdTransactional(id));
    Gson gson = new Gson();
    return gson.toJson(kids.stream()
        .map(ChildDto::new)
        .collect(Collectors.toList()));
  }
}
