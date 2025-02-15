package com.gncbrown.GetMeBack.Utilities;

public class KalmanFilter {
    private double estimateLat, estimateLng, velocityLat, velocityLng;
    private double estimateVariance;
    private final double measurementVariance;
    private long lastTimestamp;

    public KalmanFilter(double initialEstimateLat, double initialEstimateLng, double initialEstimateVariance, double measurementVariance) {
        this.estimateLat = initialEstimateLat;
        this.estimateLng = initialEstimateLng;
        this.velocityLat = 0;
        this.velocityLng = 0;
        this.estimateVariance = initialEstimateVariance;
        this.measurementVariance = measurementVariance;
        this.lastTimestamp = System.currentTimeMillis();
    }

    public double[] update(double measuredLat, double measuredLng) {
        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - lastTimestamp) / 1000.0; // Convert ms to seconds
        lastTimestamp = currentTime;

        // Predict next state
        estimateLat += velocityLat * deltaTime;
        estimateLng += velocityLng * deltaTime;
        estimateVariance += measurementVariance;

        // Compute Kalman gain
        double kalmanGain = estimateVariance / (estimateVariance + measurementVariance);

        // Update estimate with measurement
        estimateLat += kalmanGain * (measuredLat - estimateLat);
        estimateLng += kalmanGain * (measuredLng - estimateLng);

        // Update velocity estimate
        velocityLat = (estimateLat - measuredLat) / deltaTime;
        velocityLng = (estimateLng - measuredLng) / deltaTime;

        // Update variance
        estimateVariance = (1 - kalmanGain) * estimateVariance;

        return new double[]{estimateLat, estimateLng};
    }
}