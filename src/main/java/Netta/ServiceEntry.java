package Netta;

public class ServiceEntry {
    private String serviceName, serviceInfo;

    public ServiceEntry(String serviceName, String serviceInfo) {
        this.serviceInfo = serviceInfo;
        this.serviceName = serviceName;
    }

    /**
     * Get the registered name of the service
     *
     * @return String registered name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the registered info of the service. Typically http://IPADDRESS:PORT/
     *
     * @return String registered info
     */
    public String getServiceInfo() {
        return serviceInfo;
    }

    public String print() {
        return "ServiceInfo: " + serviceInfo + " ServiceName: " + serviceName;
    }
}
