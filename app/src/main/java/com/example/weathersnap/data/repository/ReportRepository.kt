package com.example.weathersnap.data.repository

import com.example.weathersnap.data.local.WeatherReportDao
import com.example.weathersnap.data.local.WeatherReportEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val dao: WeatherReportDao
) {

    suspend fun saveReport(report: WeatherReportEntity) = dao.insertReport(report)

    fun getAllReports(): Flow<List<WeatherReportEntity>> = dao.getAllReports()
}
