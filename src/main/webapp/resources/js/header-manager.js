var currentDate;

$(function () {
    currentDate = $('#date-booking').val();
    var listRoom = $('#selectRoom li a');
    var inactiveRoom = true;
    for (var i = 0; i < listRoom.length; i++) {
        if ((localStorage['roomId']) == listRoom[i].id) {
            inactiveRoom = false;
            break;
        }
    }
    if (inactiveRoom) {
        localStorage['room'] = $('#selectRoom li a').first().text();
        localStorage['roomId'] = $('#selectRoom li a').first().attr('id');
    }
    $('#selectRoom li a').each(function () {
        $(this).click(function () {
            var room = $(this).text();
            var roomId = $(this).attr('id');

            localStorage['room'] = room;
            localStorage['roomId'] = roomId;

            getAmountOfChildrenByCurrentDate();
            $('#room').text(room);
            selectRoomForManager(roomId);
        });
    });

    getAmountOfChildrenByCurrentDate();
    $('#room').text(localStorage['room']);
    selectRoomForManager(localStorage['roomId']);
});

function getAmountOfChildrenByCurrentDate() {
    $.ajax({
        url: 'dailyBookings/' + currentDate + '/' + localStorage['roomId'],
        success: function (result) {
            $('#amountOfChildren').text(result);
        }
    });
}