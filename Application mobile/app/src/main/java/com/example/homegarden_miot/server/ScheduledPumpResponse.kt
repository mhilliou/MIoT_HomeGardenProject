package com.example.homegarden_miot.server

data class ScheduledPumpResponse(val scheduledPump: Map<DayOfWeek, String>)