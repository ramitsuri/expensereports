package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.data.ExpenseReport
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.ReportApi
import com.ramitsuri.expensereports.utils.LogHelper

class ReportsRepository(private val api: ReportApi) {

    suspend fun getExpenseReport(year: Int): NetworkResponse<ExpenseReport> {
        return when (val response = api.getExpenseReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ExpenseReport(response.data))
            }
        }
    }

    companion object {
        private const val TAG = "ReportsRepo"
    }
}