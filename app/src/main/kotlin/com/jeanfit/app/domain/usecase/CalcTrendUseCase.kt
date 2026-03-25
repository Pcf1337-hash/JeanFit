package com.jeanfit.app.domain.usecase

import javax.inject.Inject

class CalcTrendUseCase @Inject constructor() {
    fun calculate(newWeightKg: Float, previousTrend: Float?, lambda: Float = 0.1f): Float {
        return if (previousTrend == null) newWeightKg
        else lambda * newWeightKg + (1 - lambda) * previousTrend
    }
}
