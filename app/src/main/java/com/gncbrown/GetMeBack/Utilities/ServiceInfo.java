package com.gncbrown.GetMeBack.Utilities;

import androidx.annotation.NonNull;

import java.util.Comparator;

public class ServiceInfo {
	private static final String TAG = ServiceInfo.class.getSimpleName();

	private String serviceName = "";
	private String servicePackage = "";
	private Boolean running = false;

	public ServiceInfo(String serviceName) {
		super();
		this.serviceName = serviceName.replaceFirst(".*[.]", "");
		this.servicePackage = serviceName;
		this.running = false;
	}

	public ServiceInfo(String serviceName, boolean running) {
		super();
		this.serviceName = serviceName.replaceFirst(".*[.]", "");
		this.servicePackage = serviceName;
		this.running = running;
	}

	@NonNull
    public String toString() {
		return serviceName + "[" + servicePackage + "] running " + running;
	}

	public String getServiceName() {
		return serviceName;
	}
	public String getServicePackage() {
		return servicePackage;
	}

	public static class CustomComparator implements Comparator<ServiceInfo> {
		public int compare(ServiceInfo o1, ServiceInfo o2) {
			String s1 = o1.getServiceName();
			String s2 = o2.getServiceName();
			return o1.getServiceName().compareToIgnoreCase(o2.getServiceName());
		}
	}

	public static class NameComparator implements Comparator<ServiceInfo> {
		public int compare(ServiceInfo o1, ServiceInfo o2) {
			String s1 = o1.getServiceName();
			String s2 = o2.getServiceName();
			return o1.getServiceName().compareToIgnoreCase(o2.getServiceName());
		}
	}

	public void setRunning(Boolean running) {
		this.running = running;
	}

	public Boolean isRunning() {
		return this.running;
	}
}