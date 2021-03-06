const constants = {
    regex: {
        nameRegex: /^[a-zA-Zа-яА-ЯЇїІіЄєҐґ`´ʼ’'\-\s]+$/,
        phoneRegex: /^\+(?:[0-9] ?){6,14}[0-9]$/,
        emailRegex: /^[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+)*@(([0-9]{1,3})|([a-zA-Z]{2,11})|(aero|coop|info|museum|name))+(\\.(([0-9]{1,3})|([a-zA-Z]{2,3})|(aero|coop|info|museum|name)))*\.(([0-9]{1,3})|([a-zA-Z]{2,3})|(aero|coop|info|museum|name))*$/,
        timeRegex: /\d{2}:\d{2}/,
        addresssRegex: /^[a-zA-ZАа-щА-ЩЬьЮюЯяЇїІіЄєҐґ0-9\s]+$/
    },
    room: {
        warnings: {
            active: 'active_bookings',
            planning: 'planning_bookings'
        },
        properties: {
            id: 0,
            isActive: 6
        }
    },
    parameters: {
        titleMaxLenght: 255,
        descriptionMaxLenght: 250,
        nameMinLength: 2,
        nameMaxLength: 35,
        commentMaxLength: 250,
        hoursInDay: 24,
        timeZone: 2
    }
};

