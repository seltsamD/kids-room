package ua.softserveinc.tc.controller.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ua.softserveinc.tc.constants.ReportConstants;
import ua.softserveinc.tc.entity.Booking;
import ua.softserveinc.tc.entity.BookingState;
import ua.softserveinc.tc.entity.Room;
import ua.softserveinc.tc.entity.User;
import ua.softserveinc.tc.service.BookingService;
import ua.softserveinc.tc.service.RoomService;
import ua.softserveinc.tc.service.UserService;
import ua.softserveinc.tc.util.BookingsCharacteristics;
import ua.softserveinc.tc.util.DateUtil;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;



@Controller
public class ReportParentController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/manager-report-parent")
    public ModelAndView parentBookings(@RequestParam(value = ReportConstants.START_DATE) String startDate,
                                       @RequestParam(value = ReportConstants.END_DATE) String endDate,
                                       @RequestParam(value = ReportConstants.ROOM_ID) Long roomId,
                                       @RequestParam(value = ReportConstants.EMAIL) String email,
                                       Principal principal) {

        Room room = roomService.findByIdTransactional(roomId);
        User manager = userService.getUserByEmail(principal.getName());

        if (!room.getManagers().contains(manager)) {
            throw new AccessDeniedException("You don't have access to this page");
        }

        User parent = userService.getUserByEmail(email);

        BookingsCharacteristics characteristic = new BookingsCharacteristics.Builder()
                .setDates(new Date[] {DateUtil.toBeginOfDayDate(startDate),
                        DateUtil.toEndOfDayDate(endDate)})
                .setUsers(Collections.singletonList(parent))
                .setRooms(Collections.singletonList(room))
                .setBookingsStates(Collections.singletonList(BookingState.COMPLETED))
                .build();

        List<Booking> bookings = bookingService.getBookings(characteristic);

        Long sumTotal =  bookingService.getSumTotal(bookings);


        ModelAndView modelAndView = new ModelAndView(ReportConstants.PARENT_VIEW);
        ModelMap modelMap = modelAndView.getModelMap();

        modelMap.addAttribute(ReportConstants.ROOM, room);
        modelMap.addAttribute(ReportConstants.PARENT, parent);
        modelMap.addAttribute(ReportConstants.END_DATE, endDate);
        modelMap.addAttribute(ReportConstants.BOOKINGS, bookings);
        modelMap.addAttribute(ReportConstants.SUM_TOTAL, sumTotal);
        modelMap.addAttribute(ReportConstants.START_DATE, startDate);
        modelMap.addAttribute("pageChecker","reportParent"); //value for checking the page in header.jsp

        return modelAndView;
    }
}
