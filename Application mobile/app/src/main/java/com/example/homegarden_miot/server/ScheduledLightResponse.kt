package com.example.homegarden_miot.server

data class ScheduledLightResponse(val scheduledLight: Map<DayOfWeek, String>)