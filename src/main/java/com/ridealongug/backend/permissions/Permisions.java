package com.ridealongug.backend.permissions;

import com.ridealongug.backend.models.jpahelpers.enums.AppDomains;

public class Permisions {


    Permission ADMINISTRATOR = new Permission("ADMINISTRATOR", "Can administer the system", AppDomains.BACK_OFFICE, true);
    Permission CAN_VERIFY_LICENCE = new Permission("CAN_VERIFY_LICENCE", "Can verify customer driving licences", AppDomains.BACK_OFFICE, true);
    Permission CAN_VERIFY_VEHICLE = new Permission("CAN_VERIFY_VEHICLE", "Can inspect and approve vehicles for hire", AppDomains.BACK_OFFICE, true);
    Permission CAN_MANAGE_DEALERS = new Permission("CAN_MANAGE_DEALERS", "Can create and manage car bond dealers", AppDomains.BACK_OFFICE, true);
    Permission CAN_MANAGE_VEHICLES = new Permission("CAN_MANAGE_VEHICLES", "Can create, edit and remove vehicles", AppDomains.BACK_OFFICE, true);
    Permission CAN_MANAGE_DRIVERS = new Permission("CAN_MANAGE_DRIVERS", "Can manage platform driver profiles", AppDomains.BACK_OFFICE, true);
    Permission CAN_MANAGE_PRICING = new Permission("CAN_MANAGE_PRICING", "Can manage vehicle category pricing", AppDomains.BACK_OFFICE, true);
    Permission CAN_VIEW_ALL_BOOKINGS = new Permission("CAN_VIEW_ALL_BOOKINGS", "Can view every booking on the platform", AppDomains.BACK_OFFICE, true);
    Permission CAN_MANAGE_COUPONS = new Permission("CAN_MANAGE_COUPONS", "Can create and manage discount coupons", AppDomains.BACK_OFFICE, true);
    Permission CAN_VIEW_AUDIT_LOGS = new Permission("CAN_VIEW_AUDIT_LOGS", "Can view the super admin audit trail", AppDomains.BACK_OFFICE, true);
    Permission CAN_MANAGE_PAYOUTS = new Permission("CAN_MANAGE_PAYOUTS", "Can view and mark vehicle owner payouts as paid", AppDomains.BACK_OFFICE, true);
    Permission CAN_MANAGE_USERS = new Permission("CAN_MANAGE_USERS", "Can view, list, and delete any system user account", AppDomains.BACK_OFFICE, true);


    Permission CAN_SUBMIT_VEHICLE = new Permission("CAN_SUBMIT_VEHICLE", "Can submit own vehicle for listing on the platform", AppDomains.CLIENT_SIDE);
    Permission CAN_MANAGE_OWN_VEHICLES = new Permission("CAN_MANAGE_OWN_VEHICLES", "Can view and manage own submitted vehicles", AppDomains.CLIENT_SIDE);
    Permission CAN_VIEW_OWN_EARNINGS = new Permission("CAN_VIEW_OWN_EARNINGS", "Can view own vehicle rental earnings", AppDomains.CLIENT_SIDE);


    Permission CAN_BOOK_VEHICLE = new Permission("CAN_BOOK_VEHICLE", "Can book a vehicle", AppDomains.CLIENT_SIDE);
    Permission CAN_MANAGE_OWN_BOOKINGS = new Permission("CAN_MANAGE_OWN_BOOKINGS", "Can view and manage own bookings", AppDomains.CLIENT_SIDE);
    Permission CAN_SUBMIT_LICENCE = new Permission("CAN_SUBMIT_LICENCE", "Can submit a driving licence for verification", AppDomains.CLIENT_SIDE);
    Permission CAN_LEAVE_REVIEW = new Permission("CAN_LEAVE_REVIEW", "Can leave a review after a completed booking", AppDomains.CLIENT_SIDE);


    Permission CAN_ACCEPT_TRIPS = new Permission("CAN_ACCEPT_TRIPS", "Can accept and manage assigned trips", AppDomains.CLIENT_SIDE);
    Permission CAN_UPDATE_AVAILABILITY = new Permission("CAN_UPDATE_AVAILABILITY", "Can toggle own driver availability", AppDomains.CLIENT_SIDE);
}
