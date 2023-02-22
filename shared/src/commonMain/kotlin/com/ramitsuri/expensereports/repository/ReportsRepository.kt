package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.data.ReportWithTotal
import com.ramitsuri.expensereports.data.ReportWithoutTotal
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.ReportApi
import com.ramitsuri.expensereports.utils.LogHelper

class ReportsRepository(private val api: ReportApi) {

    suspend fun getExpenseReport(year: Int): NetworkResponse<ReportWithTotal> {
        return when (val response = api.getExpenseDetailReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ReportWithTotal(response.data))
            }
        }
    }

    suspend fun getAssetsReport(year: Int): NetworkResponse<ReportWithoutTotal> {
        return when (val response = api.getAssetsDetailReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ReportWithoutTotal(response.data))
            }
        }
    }

    suspend fun getLiabilitiesReport(year: Int): NetworkResponse<ReportWithoutTotal> {
        return when (val response = api.getLiabilitiesDetailReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ReportWithoutTotal(response.data))
            }
        }
    }

    suspend fun getIncomeReport(year: Int): NetworkResponse<ReportWithTotal> {
        return when (val response = api.getIncomeDetailReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ReportWithTotal(response.data))
            }
        }
    }

    suspend fun getNetWorthReport(year: Int): NetworkResponse<ReportWithoutTotal> {
        return when (val response = api.getNetWorthDetailReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ReportWithoutTotal(response.data))
            }
        }
    }

    suspend fun getSavingsReport(year: Int): NetworkResponse<ReportWithTotal> {
        return when (val response = api.getSavingsDetailReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ReportWithTotal(response.data))
            }
        }
    }

    suspend fun getExpenseAfterDeductionReport(year: Int): NetworkResponse<ReportWithTotal> {
        return when (val response = api.getExpenseAfterDeductionReport(year)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                response
            }
            is NetworkResponse.Success -> {
                NetworkResponse.Success(ReportWithTotal(response.data))
            }
        }
    }

    companion object {
        private const val TAG = "ReportsRepo"
    }
}